package net.gini.android.vision.camera.api;

import static net.gini.android.vision.camera.api.Util.calculateTapAreaForCameraAPI;
import static net.gini.android.vision.camera.api.Util.convertCameraSizes;
import static net.gini.android.vision.camera.api.Util.getLargestFourThreeRatioSize;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import net.gini.android.vision.camera.photo.Photo;
import net.gini.android.vision.camera.photo.Size;
import net.gini.android.vision.util.promise.SimpleDeferred;
import net.gini.android.vision.util.promise.SimplePromise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @exclude
 */
public class CameraController implements CameraInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CameraController.class);

    private Camera mCamera;

    private boolean mPreviewRunning = false;
    private AtomicBoolean mFocusing = new AtomicBoolean();
    private AtomicBoolean mTakingPicture = new AtomicBoolean();

    private Size mPreviewSize = new Size(0,0);
    private Size mPictureSize = new Size(0,0);

    private final Activity mActivity;
    private final Handler mResetFocusHandler;
    private final UIExecutor mUIExecutor;

    private Runnable mResetFocusMode = new Runnable() {
        @Override
        public void run() {
            if (mCamera == null) {
                return;
            }
            Camera.Parameters parameters = mCamera.getParameters();
            if (!parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCamera.setParameters(parameters);
        }
    };

    public CameraController(@NonNull Activity activity) {
        mActivity = activity;
        mResetFocusHandler = new Handler();
        mUIExecutor = new UIExecutor();
    }

    @NonNull
    @Override
    public SimplePromise open() {
        LOG.info("Open camera");
        if (mCamera != null) {
            LOG.debug("Camera already open");
            LOG.info("Camera opened");
            return SimpleDeferred.resolvedPromise(null);
        }
        try {
            mCamera = Camera.open();
            if (mCamera != null) {
                configureCamera(mActivity);
                LOG.info("Camera opened");
                return SimpleDeferred.resolvedPromise(null);
            } else {
                LOG.error("No back-facing camera");
                return SimpleDeferred.rejectedPromise("No back-facing camera");
            }
        } catch (RuntimeException e) {
            LOG.error("Cannot start camera", e);
            return SimpleDeferred.rejectedPromise(e);
        }
    }

    @Override
    public void close() {
        LOG.info("Closing camera");
        if (mCamera == null) {
            LOG.debug("Camera already closed");
            LOG.info("Camera closed");
            return;
        }
        mCamera.release();
        mCamera = null;
        LOG.info("Camera closed");
    }

    @NonNull
    @Override
    public SimplePromise startPreview(@NonNull SurfaceTexture surfaceTexture) {
        LOG.info("Start preview");
        if (mCamera == null) {
            LOG.error("Cannot start preview: camera not open");
            return SimpleDeferred.rejectedPromise("Cannot start preview: camera not open");
        }
        if (mPreviewRunning) {
            LOG.info("Preview already running");
            return SimpleDeferred.resolvedPromise(null);
        }
        try {
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
            mPreviewRunning = true;
            LOG.info("Preview started");
        } catch (IOException e) {
            LOG.error("Cannot start preview", e);
            return SimpleDeferred.rejectedPromise(e);
        }
        return SimpleDeferred.resolvedPromise(null);
    }

    @Override
    public void stopPreview() {
        LOG.info("Stop preview");
        if (mCamera == null) {
            LOG.info("Preview not running: camera is stopped");
            return;
        }
        mCamera.stopPreview();
        mPreviewRunning = false;
        LOG.info("Preview stopped");
    }

    @Override
    public void enableTapToFocus(@NonNull View tapView, @Nullable final TapToFocusListener listener) {
        LOG.info("Tap to focus enabled");
        tapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    final float x = event.getX();
                    final float y = event.getY();
                    LOG.debug("Handling tap to focus touch at point ({}, {})", x, y);
                    if (mCamera == null) {
                        LOG.error("Cannot focus on tap: camera not open");
                        return false;
                    }
                    if (mFocusing.get()) {
                        LOG.debug("Already focusing");
                        return false;
                    }

                    mCamera.cancelAutoFocus();
                    Rect focusRect = calculateTapAreaForCameraAPI(x, y, getBackFacingCameraOrientation(), view.getWidth(), view.getHeight());
                    LOG.debug("Focus rect calculated (l:{}, t:{}, r:{}, b:{})", focusRect.left, focusRect.top, focusRect.right, focusRect.bottom);

                    Camera.Parameters parameters = mCamera.getParameters();
                    if (!parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    }
                    if (parameters.getMaxNumFocusAreas() > 0) {
                        List<Camera.Area> mylist = new ArrayList<Camera.Area>();
                        mylist.add(new Camera.Area(focusRect, 1000));
                        parameters.setFocusAreas(mylist);
                        LOG.debug("Focus area set");
                    } else {
                        LOG.warn("Focus areas not supported");
                    }

                    try {
                        mFocusing.set(true);
                        if (listener != null) {
                            listener.onFocusing(new Point(Math.round(x), Math.round(y)));
                        }
                        mCamera.setParameters(parameters);
                        LOG.info("Focusing started");
                        mCamera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(final boolean success, Camera camera) {
                                LOG.info("Focusing finished with result: {}", success);
                                mFocusing.set(false);
                                if (listener != null) {
                                    listener.onFocused(success);
                                }
                                mResetFocusHandler.removeCallbacks(mResetFocusMode);
                                mResetFocusHandler.postDelayed(mResetFocusMode, 5000);
                            }
                        });
                    } catch (Exception e) {
                        mFocusing.set(false);
                        LOG.error("Could not focus", e);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void disableTapToFocus(@NonNull View tapView) {
        LOG.info("Tap to focus disabled");
        tapView.setOnTouchListener(null);
    }

    @NonNull
    @Override
    public SimplePromise focus() {
        LOG.info("Start focusing");
        final SimpleDeferred deferred = new SimpleDeferred();

        if (mCamera == null) {
            LOG.error("Cannot focus: camera not open");
            deferred.resolve(false);
            return deferred.promise();
        }
        if (mFocusing.get()) {
            LOG.info("Already focusing");
            return deferred.promise();
        }

        mFocusing.set(true);
        mCamera.cancelAutoFocus();
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(final boolean success, Camera camera) {
                LOG.info("Focusing finished with result: {}", success);
                mFocusing.set(false);
                mUIExecutor.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deferred.resolve(success);
                    }
                });

            }
        });

        return deferred.promise();
    }

    @NonNull
    @Override
    public SimplePromise takePicture() {
        LOG.info("Take picture");
        final SimpleDeferred deferred = new SimpleDeferred();

        if (mCamera == null) {
            LOG.error("Cannot take picture: camera not open");
            deferred.reject("Cannot take picture: camera not open");
            return deferred.promise();
        }
        if (mTakingPicture.get()) {
            LOG.info("Already taking a picture");
            return deferred.promise();
        }

        mTakingPicture.set(true);
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] bytes, Camera camera) {
                mTakingPicture.set(false);

                LOG.info("Restarting preview");
                if (mCamera == null) {
                    LOG.error("Cannot start preview: camera not open");
                    return;
                }
                mCamera.startPreview();
                LOG.info("Preview started");

                final Photo photo = Photo.fromJpeg(bytes, getBackFacingCameraOrientation());

                mUIExecutor.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("Picture taken");
                        deferred.resolve(photo);
                    }
                });
            }
        });

        return deferred.promise();
    }

    @NonNull
    @Override
    public Size getPreviewSize() {
        return mPreviewSize;
    }

    @NonNull
    @Override
    public Size getPictureSize() {
        return mPictureSize;
    }

    private void configureCamera(Activity activity) {
        LOG.debug("Configuring camera");
        if (mCamera == null) {
            LOG.error("Cannot configure camera: camera not open");
            return;
        }

        Camera.Parameters params = mCamera.getParameters();

        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        Size previewSize = getLargestFourThreeRatioSize(convertCameraSizes(previewSizes));
        if (previewSize != null) {
            mPreviewSize = previewSize;
            params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            LOG.debug("Preview size ({}, {})", mPreviewSize.width, mPreviewSize.height);
        } else {
            LOG.warn("No 4:3 preview size found");
        }

        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        Size pictureSize = getLargestFourThreeRatioSize(convertCameraSizes(pictureSizes));
        if (pictureSize != null) {
            mPictureSize = pictureSize;
            params.setPictureSize(mPictureSize.width, mPictureSize.height);
            LOG.debug("Picture size ({}, {})", mPictureSize.width, mPictureSize.height);
        } else {
            LOG.warn("No 4:3 picture size found");
        }

        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            LOG.debug("Focus mode continuous picture");
        } else {
            LOG.warn("Focus mode continuous picture not supported");
        }

        List<String> supportedFlashModes = params.getSupportedFlashModes();
        if (supportedFlashModes != null && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            LOG.debug("Flash on");
        } else {
            LOG.warn("Flash not supported");
        }

        mCamera.setParameters(params);

        setCameraDisplayOrientation(activity, mCamera);
    }

    private void setCameraDisplayOrientation(Activity activity, android.hardware.Camera camera) {
        LOG.debug("Setting camera display orientation");
        Camera.CameraInfo info = getBackFacingCameraInfo();
        if (info == null) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        LOG.debug("Default display rotation {}", degrees);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        LOG.debug("Camera display orientation set to {}", result);
    }

    @Nullable
    private Camera.CameraInfo getBackFacingCameraInfo() {
        LOG.debug("Getting back facing camera info");
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                LOG.debug("Camera info found");
                return cameraInfo;
            }
        }
        LOG.debug("No camera info found");
        return null;
    }

    private int getBackFacingCameraOrientation() {
        LOG.debug("Getting back facing camera orientation");
        Camera.CameraInfo cameraInfo = getBackFacingCameraInfo();
        if (cameraInfo != null) {
            LOG.debug("Camera orientation: {}", cameraInfo.orientation);
            return cameraInfo.orientation;
        }
        LOG.debug("No camera info, using default camera orientation: 0");
        return 0;
    }
}

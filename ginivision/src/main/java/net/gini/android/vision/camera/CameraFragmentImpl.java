package net.gini.android.vision.camera;

import static net.gini.android.vision.camera.api.Util.cameraExceptionToGiniVisionError;
import static net.gini.android.vision.util.AndroidHelper.isMarshmallowOrLater;
import static net.gini.android.vision.util.ContextHelper.getClientApplicationId;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import net.gini.android.vision.Document;
import net.gini.android.vision.GiniVisionError;
import net.gini.android.vision.R;
import net.gini.android.vision.camera.api.CameraController;
import net.gini.android.vision.camera.api.CameraController2;
import net.gini.android.vision.camera.api.CameraInterface;
import net.gini.android.vision.camera.photo.Photo;
import net.gini.android.vision.camera.view.CameraPreviewSurface;
import net.gini.android.vision.ui.FragmentImplCallback;
import net.gini.android.vision.ui.ViewStubSafeInflater;
import net.gini.android.vision.util.promise.Promises;
import net.gini.android.vision.util.promise.SimpleDeferred;
import net.gini.android.vision.util.promise.SimplePromise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class CameraFragmentImpl implements CameraFragmentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CameraFragmentImpl.class);

    private static final CameraFragmentListener NO_OP_LISTENER = new CameraFragmentListener() {
        @Override
        public void onDocumentAvailable(@NonNull Document document) {
        }

        @Override
        public void onError(@NonNull GiniVisionError error) {
        }
    };

    private final FragmentImplCallback mFragment;
    private CameraFragmentListener mListener = NO_OP_LISTENER;

    private CameraInterface mCameraController;

    private RelativeLayout mLayoutRoot;
    private CameraPreviewSurface mCameraPreview;
    private ImageView mCameraFocusIndicator;
    private ImageView mImageCorners;
    private ImageButton mButtonCameraTrigger;
    private LinearLayout mLayoutNoPermission;

    private ViewStubSafeInflater mViewStubInflater;

    private SimpleDeferred mSurfaceCreatedDeferred = new SimpleDeferred();

    CameraFragmentImpl(@NonNull FragmentImplCallback fragment) {
        mFragment = fragment;
    }

    public void setListener(CameraFragmentListener listener) {
        if (listener == null) {
            mListener = NO_OP_LISTENER;
        } else {
            mListener = listener;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gv_fragment_camera, container, false);
        bindViews(view);
        setInputHandlers();
        setSurfaceViewCallback();
        return view;
    }

    public void onStart() {
        if (mFragment.getActivity() == null) {
            return;
        }
        initCameraController(mFragment.getActivity());
        Promises.bundle(openCamera(), mSurfaceCreatedDeferred.promise())
                .done(new SimplePromise.DoneCallback() {
                    @Nullable
                    @Override
                    public SimplePromise onDone(@Nullable Object result) {
                        List<Promises.Resolution> results = (List<Promises.Resolution>) result;
                        if (results != null && results.get(0) != null && results.get(1) != null) {
                            SurfaceHolder holder = (SurfaceHolder) results.get(1).getResult();
                            if (holder != null) {
                                mCameraPreview.setPreviewSize(mCameraController.getPreviewSize());
                                startPreview(holder);
                                enableTapToFocus();
                            } else {
                                String message = "Cannot start preview: no SurfaceHolder received for SurfaceView";
                                LOG.error(message);
                                mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_NO_PREVIEW, message));
                            }
                        }
                        return null;
                    }
                })
                .fail(new SimplePromise.FailCallback() {
                    @Nullable
                    @Override
                    public SimplePromise onFailed(@Nullable Object failure) {
                        List<Promises.Failure> failures = (List<Promises.Failure>) failure;
                        if (failures != null && failures.get(1) != null) {
                            String message = "Cannot start preview: Could not create SurfaceView";
                            LOG.error(message);
                            mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_NO_PREVIEW, message));
                        }
                        return null;
                    }
                });
    }

    private void startPreview(SurfaceHolder holder) {
        mCameraController.startPreview(holder)
                .fail(new SimplePromise.FailCallback() {
                    @Nullable
                    @Override
                    public SimplePromise onFailed(@Nullable Object failure) {
                        if (failure instanceof Exception) {
                            Exception exception = (Exception) failure;
                            LOG.error("Cannot start preview", exception);
                            mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_NO_PREVIEW, exception.getMessage()));
                        } else if (failure instanceof String) {
                            String message = (String) failure;
                            LOG.error("Cannot start preview", message);
                            mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_NO_PREVIEW, message));
                        } else {
                            String message = "Cannot start preview";
                            LOG.error(message);
                            mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_NO_PREVIEW, message));
                        }
                        return null;
                    }
                });
    }

    private void enableTapToFocus() {
        mCameraController.enableTapToFocus(mCameraPreview, new CameraInterface.TapToFocusListener() {
            @Override
            public void onFocusing(Point point) {
                int top = Math.round((mLayoutRoot.getHeight() - mCameraPreview.getHeight()) / 2.0f);
                int left = Math.round((mLayoutRoot.getWidth() - mCameraPreview.getWidth()) / 2.0f);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraFocusIndicator.getLayoutParams();
                layoutParams.leftMargin = (int) Math.round(left + point.x - (mCameraFocusIndicator.getWidth() / 2.0));
                layoutParams.topMargin = (int) Math.round(top + point.y - (mCameraFocusIndicator.getHeight() / 2.0));
                mCameraFocusIndicator.setLayoutParams(layoutParams);
                mCameraFocusIndicator.animate().setDuration(200).alpha(1.0f);
            }

            @Override
            public void onFocused(boolean success) {
                mCameraFocusIndicator.animate().setDuration(200).alpha(0.0f);
            }
        });
    }

    private SimplePromise openCamera() {
        LOG.info("Opening camera");
        return mCameraController.open()
                .done(new SimplePromise.DoneCallback() {
                    @Nullable
                    @Override
                    public SimplePromise onDone(@Nullable Object result) {
                        LOG.info("Camera opened");
                        hideNoPermissionView();
                        return null;
                    }
                })
                .fail(new SimplePromise.FailCallback() {
                    @Nullable
                    @Override
                    public SimplePromise onFailed(@Nullable Object failure) {
                        if (failure instanceof Exception) {
                            Exception exception = (Exception) failure;
                            handleCameraException(exception);
                        } else if (failure instanceof String) {
                            String message = (String) failure;
                            LOG.error("Failed to open camera: {}", message);
                            mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_OPEN_FAILED, message));
                        } else {
                            String message = "Failed to open camera";
                            LOG.error(message);
                            mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_OPEN_FAILED, message));
                        }
                        return null;
                    }
                });
    }

    private void handleCameraException(@NonNull Exception e) {
        LOG.error("Failed to open camera", e);
        GiniVisionError error = cameraExceptionToGiniVisionError(e);
        if (error.getErrorCode() == GiniVisionError.ErrorCode.CAMERA_NO_ACCESS) {
            showNoPermissionView();
        } else {
            mListener.onError(cameraExceptionToGiniVisionError(e));
        }
    }

    public void onStop() {
        closeCamera();
    }

    private void closeCamera() {
        LOG.info("Closing camera");
        mCameraController.disableTapToFocus(mCameraPreview);
        mCameraController.stopPreview();
        mCameraController.close();
        LOG.info("Camera closed");
    }

    private void bindViews(View view) {
        mLayoutRoot = (RelativeLayout) view.findViewById(R.id.gv_root);
        mCameraPreview = (CameraPreviewSurface) view.findViewById(R.id.gv_camera_preview);
        mCameraFocusIndicator = (ImageView) view.findViewById(R.id.gv_camera_focus_indicator);
        mImageCorners = (ImageView) view.findViewById(R.id.gv_image_corners);
        mButtonCameraTrigger = (ImageButton) view.findViewById(R.id.gv_button_camera_trigger);
        ViewStub stubNoPermission = (ViewStub) view.findViewById(R.id.gv_stub_camera_no_permission);
        mViewStubInflater = new ViewStubSafeInflater(stubNoPermission);
    }

    private void setInputHandlers() {
        mButtonCameraTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOG.info("Taking picture");
                mCameraController.takePicture()
                        .done(new SimplePromise.DoneCallback() {
                            @Nullable
                            @Override
                            public SimplePromise onDone(@Nullable Object result) {
                                Photo photo = (Photo) result;
                                if (photo != null) {
                                    LOG.info("Picture taken");
                                    mListener.onDocumentAvailable(Document.fromPhoto(photo));
                                } else {
                                    String message = "Failed to take picture: no picture from the camera";
                                    LOG.error(message);
                                    mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_SHOT_FAILED, message));
                                }
                                return null;
                            }
                        })
                        .fail(new SimplePromise.FailCallback() {
                            @Nullable
                            @Override
                            public SimplePromise onFailed(@Nullable Object failure) {
                                String message = (String) failure;
                                LOG.error("Failed to take picture: {}", message);
                                mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_SHOT_FAILED, message));
                                return null;
                            }
                        });
            }
        });
    }

    private void setSurfaceViewCallback() {
        mCameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LOG.debug("Surface created");
                mSurfaceCreatedDeferred.resolve(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LOG.debug("Surface changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LOG.debug("Surface destroyed");
                mSurfaceCreatedDeferred = new SimpleDeferred();
            }
        });
    }

    @Override
    public void showDocumentCornerGuides() {
        if (isNoPermissionViewVisible()) {
            return;
        }
        showDocumentCornerGuidesAnimated();
    }

    private void showDocumentCornerGuidesAnimated() {
        LOG.debug("Showing document corner guides");
        mImageCorners.animate().alpha(1.0f);
    }

    @Override
    public void hideDocumentCornerGuides() {
        if (isNoPermissionViewVisible()) {
            return;
        }
        hideDocumentCornerGuidesAnimated();
    }

    private void hideDocumentCornerGuidesAnimated() {
        LOG.debug("Hiding document corner guides");
        mImageCorners.animate().alpha(0.0f);
    }

    @Override
    public void showCameraTriggerButton() {
        if (isNoPermissionViewVisible()) {
            return;
        }
        showCameraTriggerButtonAnimated();
    }

    private void showCameraTriggerButtonAnimated() {
        LOG.debug("Showing camera trigger button");
        mButtonCameraTrigger.animate().alpha(1.0f);
        mButtonCameraTrigger.setEnabled(true);
    }

    @Override
    public void hideCameraTriggerButton() {
        if (isNoPermissionViewVisible()) {
            return;
        }
        hideCameraTriggerButtonAnimated();
    }

    private void hideCameraTriggerButtonAnimated() {
        LOG.debug("Hiding camera trigger button");
        mButtonCameraTrigger.animate().alpha(0.0f);
        mButtonCameraTrigger.setEnabled(false);
    }

    private void showNoPermissionView() {
        hideCameraPreviewAnimated();
        hideCameraTriggerButtonAnimated();
        hideDocumentCornerGuidesAnimated();
        inflateNoPermissionStub();
        setUpNoPermissionButton();
        if (mLayoutNoPermission != null) {
            mLayoutNoPermission.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNoPermissionViewVisible() {
        return mLayoutNoPermission != null &&
                mLayoutNoPermission.getVisibility() == View.VISIBLE;
    }

    private void inflateNoPermissionStub() {
        if (mLayoutNoPermission == null) {
            LOG.debug("Inflating no permission view");
            mLayoutNoPermission = (LinearLayout) mViewStubInflater.inflate();
        }
    }

    public void hideNoPermissionView() {
        LOG.debug("Hiding no permission view");
        showCameraPreviewAnimated();
        showCameraTriggerButtonAnimated();
        showDocumentCornerGuidesAnimated();
        if (mLayoutNoPermission != null) {
            mLayoutNoPermission.setVisibility(View.GONE);
        }
    }

    private void setUpNoPermissionButton() {
        if (isMarshmallowOrLater()) {
            handleNoPermissionButtonClick();
        } else {
            hideNoPermissionButton();
        }
    }

    private void hideCameraPreviewAnimated() {
        LOG.debug("Hiding camera preview");
        mCameraPreview.animate().alpha(0.0f);
        mCameraPreview.setEnabled(false);
    }

    private void showCameraPreviewAnimated() {
        LOG.debug("Showing camera preview");
        mCameraPreview.animate().alpha(1.0f);
        mCameraPreview.setEnabled(true);
    }

    private void handleNoPermissionButtonClick() {
        View view = mFragment.getView();
        if (view == null) {
            return;
        }
        Button button = (Button) view.findViewById(R.id.gv_button_camera_no_permission);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startApplicationDetailsSettings();
            }
        });
    }

    private void hideNoPermissionButton() {
        View view = mFragment.getView();
        if (view == null) {
            return;
        }
        LOG.debug("Hiding no permission button");
        Button button = (Button) view.findViewById(R.id.gv_button_camera_no_permission);
        button.setVisibility(View.GONE);
    }

    private void startApplicationDetailsSettings() {
        Activity activity = mFragment.getActivity();
        if (activity == null) {
            return;
        }
        LOG.debug("Starting Application Details");
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getClientApplicationId(activity), null);
        intent.setData(uri);
        mFragment.startActivity(intent);
    }

    private CameraInterface initCameraController(@NonNull Activity activity) {
        if (mCameraController == null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LOG.debug("CameraController2 created");
                mCameraController = new CameraController2(activity);
            } else {
                LOG.debug("CameraController created");
                mCameraController = new CameraController(activity);
            }
        }
        return mCameraController;
    }
}

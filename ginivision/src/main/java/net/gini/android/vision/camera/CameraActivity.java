package net.gini.android.vision.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import net.gini.android.vision.ActivityHelpers;
import net.gini.android.vision.Document;
import net.gini.android.vision.GiniVisionCoordinator;
import net.gini.android.vision.GiniVisionError;
import net.gini.android.vision.R;
import net.gini.android.vision.analysis.AnalysisActivity;
import net.gini.android.vision.onboarding.OnboardingActivity;
import net.gini.android.vision.onboarding.OnboardingPage;
import net.gini.android.vision.review.ReviewActivity;

import java.util.ArrayList;

/**
 * <h3>Screen API</h3>
 *
 * <p>
 * {@code CameraActivity} is the main entry point to the Gini Vision Library when using the Screen API.
 * </p>
 * <p>
 *     It shows a camera preview with tap-to-focus functionality and a trigger button. The camera preview also shows document corner guides to which the user should align the document.
 * </p>
 * <p>
 *     Start the {@code CameraActivity} with {@link android.app.Activity#startActivityForResult(Intent, int)} to receive the {@link GiniVisionError} in case there was an error.
 * </p>
 * <p>
 *     These extras are mandatory:
 *     <ul>
 *         <li>{@link CameraActivity#EXTRA_IN_REVIEW_ACTIVITY} - use the {@link CameraActivity#setReviewActivityExtra(Intent, Context, Class)} helper to set it. Must contain an explicit Intent to the {@link ReviewActivity} subclass from your application</li>
 *         <li>{@link CameraActivity#EXTRA_IN_ANALYSIS_ACTIVITY} - use the {@link CameraActivity#setAnalysisActivityExtra(Intent, Context, Class)} helper to set it. Must contain an explicit Intent to the {@link AnalysisActivity} subclass from your application</li>
 *     </ul>
 * </p>
 * <p>
 *     Optional extras are:
 *     <ul>
 *         <li>{@link CameraActivity#EXTRA_IN_SHOW_ONBOARDING_AT_FIRST_RUN} - if set and {@code true} the Onboarding Screen is shown the first time Gini Vision Library is started</li>
 *         <li>{@link CameraActivity#EXTRA_IN_ONBOARDING_PAGES} - custom pages for the Onboarding Screen as an {@link ArrayList} containing {@link OnboardingPage} objects</li>
 *     </ul>
 * </p>
 * <p>
 *     The following result codes need to be handled:
 *     <ul>
 *         <li>{@link CameraActivity#RESULT_OK} - image of a document was taken, reviewed and analyzed</li>
 *         <li>{@link CameraActivity#RESULT_CANCELED} - image of document was not taken, user canceled the Gini Vision Library</li>
 *         <li>{@link CameraActivity#RESULT_ERROR} - an error occured</li>
 *     </ul>
 * </p>
 * <p>
 *     Result extra returned by the {@code CameraActivity}:
 *     <ul>
 *         <li>{@link CameraActivity#EXTRA_OUT_ERROR} - set when result is {@link CameraActivity#RESULT_ERROR}, contains a {@link GiniVisionError} object detailing what went wrong</li>
 *     </ul>
 * </p>
 * <p>
 *     <b>Note:</b> For returning the extractions from the Gini API you can add your own extras in {@link ReviewActivity#onAddDataToResult(Intent)} or {@link AnalysisActivity#onAddDataToResult(Intent)}.
 * </p>
 * <p>
 *     If the camera could not be opened due to missing permissions, the content of the Camera Screen is replaced with a no-camera icon, a short message and an optional button. The button is shown only on Android 6.0+ and tapping the button leads the user to the Application Details page in the Settings. If these are shown on Android 5.0 and earlier means that the camera permission was not declared in your manifest.
 * </p>
 *
 * <h3>Customizing the Camera Screen</h3>
 *
 * <p>
 *   Customizing the look of the Camera Screen is done via overriding of app resources.
 * </p>
 * <p>
 *     The following items are customizable:
 *     <ul>
 *         <li>
 *             <b>Document corner guides:</b> via images for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi named {@code gv_camera_preview_corners.png}
 *         </li>
 *         <li>
 *             <b>Camera trigger button:</b> via images for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi named {@code gv_camera_trigger_default.png} and {@code gv_camera_trigger_pressed.png}
 *         </li>
 *         <li>
 *             <b>Tap-to-focus indicator:</b> via images for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi named {@code gv_camera_focus_rect.png}
 *         </li>
 *         <li>
 *             <b>Onboarding menu item icon:</b> via images for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi named {@code gv_icon_onboarding.png}
 *         </li>
 *         <li>
 *             <b>Onboarding menu item title:</b> via the string resource named {@code gv_show_onboarding}
 *         </li>
 *         <li>
 *             <b>Background color:</b> via the color resource named {@code gv_background}. <b>Note:</b> this color resource is global to all Activities ({@link CameraActivity}, {@link OnboardingActivity}, {@link ReviewActivity}, {@link AnalysisActivity})
 *         </li>
 *         <li>
 *             <b>No-camera icon:</b> via images for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi named {@code gv_no_camera.png}
 *         </li>
 *         <li>
 *             <b>No camera permission text:</b> via the string resource named {@code gv_camera_error_no_permission}
 *         </li>
 *         <li>
 *             <b>No camera permission text color:</b> via the color resource named {@code gv_camera_error_no_permission}
 *         </li>
 *         <li>
 *             <b>No camera permission button title:</b> via the string resource named {@code gv_camera_error_no_permission_button_title}
 *         </li>
 *         <li>
 *             <b>No camera permission button title color:</b> via the color resources named {@code gv_camera_error_no_permission_button_title} and {@code gv_camera_error_no_permission_button_title_pressed}
 *         </li>
 *     </ul>
 * </p>
 *
 * <h3>Customizing the Action Bar</h3>
 *
 * <p>
 *     Customizing the Action Bar is also done via overriding of app resources and each one - except the title string resource - is global to all Activities ({@link CameraActivity}, {@link OnboardingActivity}, {@link ReviewActivity}, {@link AnalysisActivity}).
 * </p>
 * <p>
 *     The following items are customizable:
 *     <ul>
 *         <li>
 *             <b>Background color:</b> via the color resource named {@code gv_action_bar} (highly recommended for Android 5+: customize the status bar color via {@code gv_status_bar})
 *         </li>
 *         <li>
 *             <b>Title:</b> via the string resource name {@code gv_title_camera}
 *         </li>
 *         <li>
 *             <b>Title color:</b> via the color resource named {@code gv_action_bar_title}
 *         </li>
 *     </ul>
 * </p>
 */
public class CameraActivity extends AppCompatActivity implements CameraFragmentListener {

    /**
     * <p>
     * Mandatory extra which must contain an explicit Intent to the {@link ReviewActivity} subclass from your application.
     * </p>
     * <p>
     *     Use the {@link CameraActivity#setReviewActivityExtra(Intent, Context, Class)} helper to set it.
     * </p>
     */
    public static final String EXTRA_IN_REVIEW_ACTIVITY = "GV_EXTRA_IN_REVIEW_ACTIVITY";
    /**
     * <p>
     * Mandatory extra which must contain an explicit Intent to the {@link AnalysisActivity} subclass from your application.
     * </p>
     * <p>
     *     Use the {@link CameraActivity#setAnalysisActivityExtra(Intent, Context, Class)} helper to set it.
     * </p>
     */
    public static final String EXTRA_IN_ANALYSIS_ACTIVITY = "GV_EXTRA_IN_ANALYSIS_ACTIVITY";
    /**
     * <p>
     *     Optional extra which must contain an {@code ArrayList} with {@link OnboardingPage} objects.
     * </p>
     */
    public static final String EXTRA_IN_ONBOARDING_PAGES = "GV_EXTRA_IN_ONBOARDING_PAGES";
    /**
     * <p>
     *     Optional extra which must contain a boolean and indicates whether the Onboarding Screen should be shown when the Gini Vision Library is started for the first time.
     * </p>
     * <p>
     *     Default value is {@code true}.
     * </p>
     */
    public static final String EXTRA_IN_SHOW_ONBOARDING_AT_FIRST_RUN = "GV_EXTRA_IN_SHOW_ONBOARDING_AT_FIRST_RUN";

    /**
     * <p>
     *     Returned when the result code is {@link CameraActivity#RESULT_ERROR} and contains a {@link GiniVisionError} object detailing what went wrong.
     * </p>
     */
    public static final String EXTRA_OUT_ERROR = "GV_EXTRA_OUT_ERROR";

    /**
     * <p>
     *     Returned result code in case something went wrong. You should retrieve the {@link CameraActivity#EXTRA_OUT_ERROR} extra to find out what went wrong.
     * </p>
     */
    public static final int RESULT_ERROR = RESULT_FIRST_USER + 1;

    private static final int REVIEW_DOCUMENT_REQUEST = 1;
    private static final int ANALYSE_DOCUMENT_REQUEST = 2;

    private ArrayList<OnboardingPage> mOnboardingPages;
    private Intent mReviewDocumentActivityIntent;
    private Intent mAnalyzeDocumentActivityIntent;
    private boolean mShowOnboardingAtFirstRun = true;
    private GiniVisionCoordinator mGiniVisionCoordinator;
    private Document mDocument;

    private RelativeLayout mLayoutRoot;

    /**
     * <p>
     * Helper for setting the {@link CameraActivity#EXTRA_IN_REVIEW_ACTIVITY}.
     * </p>
     *
     * @param target              your explicit {@link Intent} used to start the {@link CameraActivity}
     * @param context             {@link Context} used to create the explicit {@link Intent} for your {@link
     *                            ReviewActivity} subclass
     * @param reviewActivityClass class of your {@link ReviewActivity} subclass
     * @param <T>                 type of your {@link ReviewActivity} subclass
     */
    public static <T extends ReviewActivity> void setReviewActivityExtra(Intent target,
                                                                         Context context,
                                                                         Class<T> reviewActivityClass) {
        ActivityHelpers.setActivityExtra(target, EXTRA_IN_REVIEW_ACTIVITY, context, reviewActivityClass);
    }

    /**
     * <p>
     * Helper for setting the {@link CameraActivity#EXTRA_IN_ANALYSIS_ACTIVITY}.
     * </p>
     *
     * @param target                your explicit {@link Intent} used to start the {@link CameraActivity}
     * @param context               {@link Context} used to create the explicit {@link Intent} for your {@link
     *                              AnalysisActivity} subclass
     * @param analysisActivityClass class of your {@link AnalysisActivity} subclass
     * @param <T>                   type of your {@link AnalysisActivity} subclass
     */
    public static <T extends AnalysisActivity> void setAnalysisActivityExtra(Intent target,
                                                                             Context context,
                                                                             Class<T> analysisActivityClass) {
        ActivityHelpers.setActivityExtra(target, EXTRA_IN_ANALYSIS_ACTIVITY, context, analysisActivityClass);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gv_activity_camera);
        readExtras();
        createGiniVisionCoordinator();
        bindViews();
    }

    private void bindViews() {
        mLayoutRoot = (RelativeLayout) findViewById(R.id.gv_root);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGiniVisionCoordinator.onCameraStarted();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMemory();
    }

    @VisibleForTesting
    void readExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mOnboardingPages = extras.getParcelableArrayList(EXTRA_IN_ONBOARDING_PAGES);
            mReviewDocumentActivityIntent = extras.getParcelable(EXTRA_IN_REVIEW_ACTIVITY);
            mAnalyzeDocumentActivityIntent = extras.getParcelable(EXTRA_IN_ANALYSIS_ACTIVITY);
            mShowOnboardingAtFirstRun = extras.getBoolean(EXTRA_IN_SHOW_ONBOARDING_AT_FIRST_RUN, true);
        }
        checkRequiredExtras();
    }

    @VisibleForTesting
    void checkRequiredExtras() {
        if (mReviewDocumentActivityIntent == null) {
            throw new IllegalStateException("CameraActivity requires a ReviewActivity class. Call setReviewDocumentActivityExtra() to set it.");
        }
        if (mAnalyzeDocumentActivityIntent == null) {
            throw new IllegalStateException("CameraActivity requires an AnalyzeDocumentActivity class. Call setAnalyzeDocumentActivityExtra() to set it.");
        }
    }

    private void createGiniVisionCoordinator() {
        mGiniVisionCoordinator = GiniVisionCoordinator.createInstance(this);
        mGiniVisionCoordinator
                .setShowOnboardingAtFirstRun(mShowOnboardingAtFirstRun)
                .setListener(new GiniVisionCoordinator.Listener() {
                    @Override
                    public void onShowOnboarding() {
                        startOnboardingActivity();
                    }
                });
    }

    /**
     * @exclude
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gv_camera, menu);
        return true;
    }

    /**
     * @exclude
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.gv_action_show_onboarding) {
            startOnboardingActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startOnboardingActivity() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        if (mOnboardingPages != null) {
            intent.putParcelableArrayListExtra(OnboardingActivity.EXTRA_ONBOARDING_PAGES, mOnboardingPages);
        }
        startActivity(intent);
    }

    @Override
    public void onDocumentAvailable(@NonNull Document document) {
        mDocument = document;
        // Start ReviewActivity
        mReviewDocumentActivityIntent.putExtra(ReviewActivity.EXTRA_IN_DOCUMENT, document);
        startActivityForResult(mReviewDocumentActivityIntent, REVIEW_DOCUMENT_REQUEST);
    }

    @Override
    public void onError(@NonNull GiniVisionError error) {
        Intent result = new Intent();
        result.putExtra(EXTRA_OUT_ERROR, error);
        setResult(RESULT_ERROR, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REVIEW_DOCUMENT_REQUEST) {
            switch (resultCode) {
                case ReviewActivity.RESULT_PHOTO_WAS_REVIEWED:
                    if (data != null) {
                        Document document = data.getParcelableExtra(ReviewActivity.EXTRA_OUT_DOCUMENT);
                        mAnalyzeDocumentActivityIntent.putExtra(AnalysisActivity.EXTRA_IN_DOCUMENT, document);
                        startActivityForResult(mAnalyzeDocumentActivityIntent, ANALYSE_DOCUMENT_REQUEST);
                    }
                    break;
                case ReviewActivity.RESULT_PHOTO_WAS_REVIEWED_AND_ANALYZED:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                case ReviewActivity.RESULT_ERROR:
                    setResult(RESULT_ERROR, data);
                    finish();
                    break;
            }
        } else if (requestCode == ANALYSE_DOCUMENT_REQUEST) {
            switch (resultCode) {
                case RESULT_OK:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                case AnalysisActivity.RESULT_ERROR:
                    setResult(RESULT_ERROR, data);
                    finish();
                    break;
            }
        }
        clearMemory();
    }

    private void clearMemory() {
        mDocument = null;
    }
}

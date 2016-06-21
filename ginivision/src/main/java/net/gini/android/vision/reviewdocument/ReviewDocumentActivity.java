package net.gini.android.vision.reviewdocument;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import net.gini.android.vision.GiniVisionError;
import net.gini.android.vision.R;
import net.gini.android.vision.scanner.Document;
import net.gini.android.vision.scanner.photo.Photo;

public abstract class ReviewDocumentActivity extends AppCompatActivity implements ReviewDocumentFragmentListener {

    public static final String EXTRA_IN_PHOTO = "GV_EXTRA_IN_PHOTO";
    public static final String EXTRA_OUT_DOCUMENT = "GV_EXTRA_OUT_DOCUMENT";
    public static final String EXTRA_OUT_ERROR = "GV_EXTRA_OUT_ERROR";

    public static final int RESULT_PHOTO_WAS_REVIEWED = RESULT_FIRST_USER + 1;
    public static final int RESULT_ERROR = RESULT_FIRST_USER + 2;

    private ReviewDocumentFragmentCompat mFragment;
    private Photo mPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gv_activity_review_document);
        bindViews();
        if (savedInstanceState == null) {
            readExtras();
            createFragment();
            showFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhoto = null;
    }

    private void bindViews() {
        mFragment = (ReviewDocumentFragmentCompat) getSupportFragmentManager().findFragmentById(R.id.gv_fragment_review_document);
    }

    private void readExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mPhoto = extras.getParcelable(EXTRA_IN_PHOTO);
        }
        checkRequiredExtras();
    }

    private void checkRequiredExtras() {
        if (mPhoto == null) {
            throw new IllegalStateException("ReviewDocumentActivity requires a Photo. Set it as an extra using the EXTRA_PHOTO key.");
        }
    }

    private void createFragment() {
        mFragment = ReviewDocumentFragmentCompat.createInstance(mPhoto);
    }

    private void showFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.gv_fragment_review_document, mFragment)
                .commit();
    }

    // callback for subclasses for uploading the photo before it was reviewed, if the photo is not changed
    // no new upload is required
    @Override
    public abstract void onShouldAnalyzeDocument(Document document);

    @Override
    public void onProceedToAnalyzeScreen(Document document) {
        // TODO: start analyze screen
        Toast.makeText(this, "Should proceed to Analyze Screen", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDocumentReviewedAndAnalyzed(Document document) {
        Intent result = new Intent();
        result.putExtra(EXTRA_OUT_DOCUMENT, document);
        onAddDataToResult(result);
        setResult(RESULT_PHOTO_WAS_REVIEWED, result);
        finish();
    }

    // Photo was already analyzed, add the extractions to the result
    public abstract void onAddDataToResult(Intent result);

    // TODO: call this, if the photo was analyzed before the review was completed, it prevents the analyze activity to
    // be started, if the photo was already analyzed and the user didn't change it
    protected void onPhotoAnalyzed() {
        mFragment.onPhotoAnalyzed();
    }

    @Override
    public void onError(GiniVisionError error) {
        Intent result = new Intent();
        result.putExtra(EXTRA_OUT_ERROR, error);
        setResult(RESULT_ERROR, result);
        finish();
    }
}

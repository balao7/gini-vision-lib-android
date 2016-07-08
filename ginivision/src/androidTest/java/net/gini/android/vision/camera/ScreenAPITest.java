package net.gini.android.vision.camera;

import static net.gini.android.vision.testhelper.Util.prepareLooper;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import net.gini.android.vision.testhelper.AnalysisActivitySubclass;
import net.gini.android.vision.testhelper.ReviewActivitySubclass;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ScreenAPITest {

    @Before
    public void setup() {
        prepareLooper();
    }

    @Test(expected=IllegalStateException.class)
    public void should_throwException_whenReviewActivityClass_wasNotGiven() {
        CameraActivity cameraActivity = new CameraActivity();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(CameraActivity.EXTRA_IN_ANALYSIS_ACTIVITY, AnalysisActivitySubclass.class);
        cameraActivity.setIntent(intent);

        cameraActivity.readExtras();
    }

    @Test(expected=IllegalStateException.class)
    public void should_throwException_whenAnalysisActivityClass_wasNotGiven() {
        CameraActivity cameraActivity = new CameraActivity();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(CameraActivity.EXTRA_IN_REVIEW_ACTIVITY, ReviewActivitySubclass.class);
        cameraActivity.setIntent(intent);

        cameraActivity.readExtras();
    }
}

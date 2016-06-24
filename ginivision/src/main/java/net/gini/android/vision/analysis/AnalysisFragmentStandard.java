package net.gini.android.vision.analysis;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.gini.android.vision.camera.Document;
import net.gini.android.vision.review.ReviewFragmentListener;
import net.gini.android.vision.ui.FragmentImplCallback;

/**
 * <p>
 *     When using the Component API the {@code AnalyzeDocumentFragmentStandard} displays the captured document and an activity indicator while the document is being analyzed by the Gini API.
 * </p>
 * <p>
 *     Include the {@code AnalyzeDocumentFragmentStandard} into your layout by using the {@link AnalysisFragmentStandard#createInstance(Document)} factory method to create an instance and display it using the {@link android.app.FragmentManager}.
 * </p>
 * <p>
 *     Your Activity must implement the {@link AnalysisFragmentListener} interface to receive events from the Analyze Document Fragment. Failing to do so will throw an exception.
 * </p>
 * <p>
 *     Your Activity is automatically set as the listener in {@link AnalysisFragmentStandard#onCreate(Bundle)}.
 * </p>
 *
 * <h3>Customising the Analysis Screen</h3>
 *
 * <p>
 *     See the {@link AnalysisActivity} for details.
 * </p>
 */
public class AnalysisFragmentStandard extends Fragment implements FragmentImplCallback, AnalysisFragmentInterface {

    private AnalysisFragmentImpl mFragmentImpl;

    /**
     * <p>
     *     Factory method for creating a new instance of the Fragment using the provided document.
     * </p>
     * <p>
     *     <b>Note:</b> Always use this method to create new instances. Document is required and an exception is thrown if it's missing.
     * </p>
     * @param document must be the {@link Document} from {@link ReviewFragmentListener#onProceedToAnalyzeScreen(Document)}
     * @return a new instance of the Fragment
     */
    public static AnalysisFragmentStandard createInstance(Document document) {
        AnalysisFragmentStandard fragment = new AnalysisFragmentStandard();
        fragment.setArguments(AnalysisFragmentHelper.createArguments(document));
        return fragment;
    }

    /**
     * @exclude
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentImpl = AnalysisFragmentHelper.createFragmentImpl(this, getArguments());
        AnalysisFragmentHelper.setListener(mFragmentImpl, getActivity());
        mFragmentImpl.onCreate(savedInstanceState);
    }

    /**
     * @exclude
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mFragmentImpl.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * @exclude
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mFragmentImpl.onDestroy();
        mFragmentImpl = null;
    }

    @Override
    public void startScanAnimation() {
        mFragmentImpl.startScanAnimation();
    }

    @Override
    public void stopScanAnimation() {
        mFragmentImpl.stopScanAnimation();
    }

    @Override
    public void onDocumentAnalyzed() {
        mFragmentImpl.onDocumentAnalyzed();
    }
}

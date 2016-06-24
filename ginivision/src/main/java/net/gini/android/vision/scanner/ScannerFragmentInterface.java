package net.gini.android.vision.scanner;

/**
 * <p>
 *     Methods which both Scanner Fragment's must implement.
 * </p>
 */
public interface ScannerFragmentInterface {
    /**
     * <p>
     *     Call this method to show the document corner guides.
     * </p>
     * <p>
     *     <b>Note:</b> the document corner guides are shown by default.
     * </p>
     */
    void onShowDocumentCornerGuides();

    /**
     * <p>
     *     Call this method to hide the document corner guides.
     * </p>
     * <p>
     *     <b>Note:</b> the document corner guides are shown by default.
     * </p>
     */
    void onHideDocumentCornerGuides();

    /**
     * <p>
     *     Call this method to show the camera trigger button.
     * </p>
     * <p>
     *     <b>Note:</b> the camera trigger button is shown by default.
     * </p>
     */
    void onShowCameraTriggerButton();

    /**
     * <p>
     *     Call this method to hide the camera trigger button.
     * </p>
     * <p>
     *     <b>Note:</b> the camera trigger button is shown by default.
     * </p>
     */
    void onHideCameraTriggerButton();
}

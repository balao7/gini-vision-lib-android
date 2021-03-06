=========
Changelog
=========

2.2.2 (2017-07-03)
==================

- Fixed image rotation bug.

2.2.1 (2017-06-30)
==================

- Fixed image meta information handling bug impacting Android 4.4 and later.

2.2.0 (2017-03-22)
==================

- Added meta information to images to be able to differentiate between Review Screen uploads and Analysis Screen uploads.
- Updated to Android Support Library version 25.3.0.

2.1.0 (2017-01-30)
==================

- Removed the 4:3 aspect ratio requirement for photos. The default camera aspect ratio will be used from now on. An 8MP minimum resolution is still required.
- Removed the continuous-focus mode requirement. Only auto-focus is required.
- If no continuous-focus mode is available then an auto-focus run is triggered when the user activates the capture button.
- Trigger button is aligned to the bottom of the preview area.
- The back button in the ReviewActivity and AnalysisActivity (in the navigation bar and in the ActionBar) leads back to the previous Activity instead of closing the library. The previous behavior can be requested by setting the `CameraActivity#EXTRA_IN_BACK_BUTTON_SHOULD_CLOSE_LIBRARY` to `true`.
- Fixed an issue regarding ReviewActivity and AnalysisActivity restart in the Screen API after the app had been killed while in the background.

2.0.1 (2016-10-18)
==================

- Updated Sanselan to Commons Imaging.

2.0.0 (2016-08-25)
==================

- Finalized documentation and example apps.
- Reorganized internal (non-public API) packages and classes.
- Finalized release process.

2.0.0-alpha.1 (2016-08-18)
==========================

Features
--------

- Feature complete version.
- Using the Screen API a picture can be taken with the `CameraActivity`. It can be reviewed with the `ReviewActvitiy` with the possibility to start document analysis. If the document analysis didn't complete or the document was rotated the document analysis can be continued or started again in the `AnalysisActivity`.
- Using the Component API a picture can be taken with one of the Camera Fragments. Showing the picture with one of the Review Fragments allows review and rotation of the picture. You could also start the document analysis when showing one of the Review Fragments. If the document analysis didn't complete or the document was rotated you should show one of the Analysis Fragments and continue or restart the document analysis.
- Consult the example apps for details on how to use the Gini Vision Library.
- Logging with SLF4J.
- Checking if the device meets the Gini Vision Library requirements with GiniVisionRequirements.

2.0.0-stub.1 (2016-07-15)
=========================

Features
--------

- Stub version of the completely rewritten Gini Vision Library.
- Provides two integration options: 1) A Screen API that can be easily implemented using Activities. 2) A more complex but at the same time more flexible Component API using Fragments. 
- For the communication between your app and the Library use the `CameraActivity`, `ReviewActivity` and `AnalysisActivity` for the Screen API or implement the listener methods for the Fragments when using the Component API.
- This stub release implements all calls for the future 2.0.0 release. It allows the user to capture a simulated document and review it. Also screens for onboarding and further analysis are provided. For the final release the UI will be further improved and minor changes are made in the implementation if really necessary.

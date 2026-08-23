# GitHub Actions APK Build Guide

## What this project contains
This ZIP is the combined Phase 1 + Phase 2 project with a GitHub Actions workflow.

The app is designed to run offline:
- No backend
- No server
- No internet needed for normal app use
- Local Room/SQLite database
- English UI

## Upload to GitHub
1. Sign in to GitHub.
2. Click the + icon and choose New repository.
3. Repository name example: OfflineTuitionManager.
4. Select Public or Private.
5. Click Create repository.
6. Open the repository.
7. Click Add file > Upload files.
8. Extract this ZIP on your computer first.
9. Upload ALL extracted files and folders, including:
   - app
   - .github
   - build.gradle.kts
   - settings.gradle.kts
   - .gitignore
   - README.md
10. Commit the uploaded files to the main branch.

## Build the APK
1. Open the Actions tab in your GitHub repository.
2. Select Build Android APK.
3. If GitHub shows a security warning, click the button to enable workflows.
4. Click Run workflow.
5. Select the main branch.
6. Click the green Run workflow button.
7. Wait for the build to finish.
8. Open the completed workflow run.
9. Scroll to Artifacts.
10. Download OfflineTuitionManager-debug-apk.
11. Extract the downloaded artifact ZIP.
12. The APK is named app-debug.apk.

## Install
Copy app-debug.apk to your Android phone and install it.
Android may ask you to allow installation from the browser or file manager.

## Important
GitHub Actions builds the source code in the cloud. The APK build itself does not require Android Studio on your computer.

If the workflow fails, open the failed step and copy the error log. That error can then be fixed in the project source.

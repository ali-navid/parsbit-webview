# ParsBit WebView Android App

Application name: پارس بیت

Website: https://pars-bit.com/

Package: com.parsbit.online

Target SDK: 36

Compile SDK: 36

Java: 17

## GitHub Actions

This project intentionally does not require Android Studio or a local Android SDK.
GitHub Actions installs/configures the Android build environment and Gradle 8.13.

### Required Repository Secrets

Create these four GitHub Actions Repository Secrets:

- PARSBIT_KEYSTORE_BASE64
- KEYSTORE_PASSWORD
- KEY_ALIAS
- KEY_PASSWORD

The repository must contain a Base64-encoded release keystore in PARSBIT_KEYSTORE_BASE64.
Do not commit the .jks file or signing.properties to Git.

## Build

Open GitHub -> Actions -> ParsBit Android Build -> Run workflow.

The workflow produces:

- ParsBit-APK / app-release.apk
- ParsBit-AAB / app-release.aab

APK is for device testing. AAB is the preferred release artifact for store publication.

## Important

Keep a secure offline backup of the release keystore and its passwords. The same signing key is required for future updates of the app.

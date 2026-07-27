# Turboautism Dose Log

🚧 **Status: Under Active Development (Alpha)**

Turboautism Dose Log is a simple Android application for logging drug or medication administration events locally on your device.

The project is currently in an early alpha stage, so features and database structure may change between releases.

## ✨ Current Features

- Log drug name
- Log route of administration
- Log dosage
- Automatic timestamp using device time
- Local persistent storage (Room database)
- Scrollable log view
- Swipe-to-delete entries with undo
- Edit existing entries
- CSV export
- CSV import (for backups)
- **App lock** — biometric (fingerprint/face) or device PIN/pattern/password required to open the app, with automatic re-lock when backgrounded
- **Encrypted database** — the on-device database is encrypted at rest (AES-256 via SQLCipher), with the passphrase generated automatically and stored securely via the Android Keystore
- Basic statistics:
  - total entries
  - entries today
  - entries last 7 days
  - most used drug
  - average doses per day
  - last dose timestamp

## 🔒 Privacy & Security

This application is designed to work completely offline.

- No internet communication
- No Google services
- No analytics or tracking
- No user accounts
- All data is stored locally on the device
- App access is protected by biometric/device-credential lock
- The local database is encrypted at rest

The application does not request network permission.

**Note:** CSV export files are plaintext. Keep this in mind if you back up or share exported CSVs.

## 📱 Compatibility

The application has been tested on Android 16.
It is expected to work on Android 15 and newer, based on the `minSdk` version defined in the project.
Older Android versions are not officially supported.

## 📦 Download Prebuilt APK (Recommended for Testing)

You can download the latest compiled alpha APK from:

➡ [GitHub Releases](https://github.com/emmikal/Turboautism-dose-log-/releases)

Download the latest `.apk` file and install it manually.

### Installing the APK

1. Transfer the APK to your Android device
2. Open the file
3. Allow installation from unknown sources if prompted
4. Install

Minimum supported Android version depends on the `minSdk` defined in the project.

**Upgrading from an older version:** since v0.8, the database format has changed (encryption). If updating from a pre-0.8 install, export your data to CSV first, then uninstall the old version before installing the new one — see the CHANGELOG or release notes for details.

## 🛠 Build From Source

### Requirements

- Android Studio (latest stable version recommended)
- Android SDK installed
- Java 17+

### Steps

Clone the repository:
git clone https://github.com/emmikal/Turboautism-dose-log-.git
Open the project in Android Studio and build the APK.

## 🗺 Roadmap

- Option to disable/configure the app lock
- Manual "lock now" action
- Encrypted CSV export

## ⚠️ Disclaimer

This software is provided for informational and personal logging purposes only.
It is not a medical device and should not be used as a substitute for professional medical advice.

## 💬 Feedback

Suggestions, bug reports, and pull requests are welcome.
If you encounter problems or have feature ideas, please open an issue on GitHub.

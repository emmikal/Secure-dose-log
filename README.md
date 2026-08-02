# Turboautism Dose Log

✅ **Status:2.0 — Stable Release**

Turboautism Dose Log is a simple Android application for logging drug or medication administration events locally on your device.

## Why this project exists

People sometimes find it difficult to remember exactly what substances they've taken, when they took them, or how much they used—especially over longer sessions or when multiple substances are involved. Keeping an accurate log can support safer decision-making and provide useful information to healthcare professionals if medical treatment is needed.

The idea for this app came from users themselves, describing exactly this problem.

This application follows a harm reduction philosophy. It does not encourage or promote drug use. Instead, it recognizes that some people will use drugs regardless and aims to provide a private, secure way to keep accurate records. All data remains on the user's device by default, and users can export their records if they choose to share them with emergency medical services, physicians, or detoxification clinics.

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
- CSV import (for backups), with duplicate detection and partial-failure reporting
- **Sessions** — group related doses together under a named session (e.g. "Friday night"). Start a session from the toolbar (name it, or leave blank for an automatic "Friday 15:20"-style name), and new doses are automatically linked to whichever session is active. Multiple sessions can run at once — if more than one is active when you log a dose, you'll be asked which session(s) it belongs to.
- **Sessions list & detail view** — see all past and active sessions, and tap into one to see its start/end time, duration, total entries, the substances used with their logged doses, and a full chronological timeline of the session
- **Notes** — add an optional free-text note when logging a dose (e.g. "this weed makes me paranoid"), shown inline in the main log
- **App lock** — biometric (fingerprint/face) or device PIN/pattern/password required to open the app, with automatic re-lock when backgrounded or when the screen turns off. Cannot be disabled.
- **Encrypted database** — the on-device database is encrypted at rest (AES-256 via SQLCipher), with the passphrase generated automatically and stored securely via the Android Keystore
- **Screenshot/screen-recording prevention** — the app blocks screenshots, screen recording, and hides its content from the recent-apps switcher preview
- **Copy/cut disabled** on text input fields, to reduce the chance of sensitive text ending up on the clipboard
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
- App access is protected by a mandatory biometric/device-credential lock
- The local database is encrypted at rest
- Screenshots and screen recording of the app are blocked
- Copy/cut is disabled on input fields
- The database, preferences, and encryption key use non-descriptive names, so they don't reveal what the app is about even from a file listing

The application does not request network permission.

**Note:** CSV export files are plaintext by design — this is intentional, so records can be readily shared with emergency medical services, physicians, or detoxification clinics without any extra steps in a situation where that matters. Keep this in mind if you back up or store exported CSVs elsewhere. Also, since screenshots are blocked app-wide, there's no way to screenshot your own log — use CSV export if you need to get data out.

## 📱 Compatibility

The application has been tested on Android 16 and Android 11.
It supports Android 11 (API 30) and newer, based on the `minSdk` version defined in the project.
Older Android versions are not officially supported.

## 📦 Download Prebuilt APK

You can download the latest compiled APK from:

➡ [GitHub Releases](https://github.com/emmikal/Turboautism-dose-log-/releases)

Download the latest `.apk` file and install it manually.

### Installing the APK

1. Transfer the APK to your Android device
2. Open the file
3. Allow installation from unknown sources if prompted
4. Install

Minimum supported Android version depends on the `minSdk` defined in the project.

**Upgrading from an older version:** database schema changes prior to 1.0 (v0.8 encryption, v0.10 sessions) are not backward-compatible with older installs. If updating from a pre-1.0 version, export your data to CSV first — updating will reset the database to the new schema. Re-import the CSV afterward to restore your entries. From 1.0 onward, future schema changes will ship with proper database migrations rather than resetting your data.

## 🛠 Build From Source

### Requirements

- Android Studio (latest stable version recommended)
- Android SDK installed
- Java 17+

### Steps

Clone the repository:
git clone https://github.com/emmikal/Turboautism-dose-log-.git
Open the project in Android Studio and build the APK.

## ❤️ Support the project

Turboautism Dose Log is a free and open-source harm reduction app developed in my free time.

If you find the project useful and would like to support its continued development, you can sponsor me on GitHub:

https://github.com/sponsors/emmikal

Sponsorships help cover:

- 🤖 Development tools (such as AI assistants)
- 📱 Google Play developer account and app publishing
- 📚 Ongoing maintenance of the PsychonautWiki-based substance database
- 🛠️ General maintenance and future improvements

Thank you for helping keep the project free and open source!

## 🙏 Acknowledgements

Turboautism Dose Log is built on the work of many excellent open-source projects and communities.

Please see [ACKNOWLEDGEMENTS.md](ACKNOWLEDGEMENTS.md) for credits and thanks.

## ⚠️ Disclaimer

This is a harm reduction tool. It does not encourage, promote, or endorse drug use.

This software is provided for informational and personal logging purposes only.
It is not a medical device and should not be used as a substitute for professional medical advice.
## 💬 Feedback

Suggestions, bug reports, and pull requests are welcome.
If you encounter problems or have feature ideas, please open an issue on GitHub.

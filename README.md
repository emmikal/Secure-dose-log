Turboautism Dose Log

«A private, offline harm reduction journal for logging substance use and medication administration on Android.»

Turboautism Dose Log is a free and open-source Android application designed to help users maintain an accurate record of substance or medication use. Every record is stored locally on the device, with no accounts, analytics, cloud services, or network communication.

The project is built around three principles:

- Privacy first — your data belongs to you.
- Offline by default — everything works without an internet connection.
- Harm reduction — provide information, never judgement.

---

Why this project exists

People often find it difficult to remember exactly:

- what they took,
- how much they took,
- when they took it,
- or what combinations they used.

This becomes even harder during longer sessions or when multiple substances are involved.

Keeping an accurate log can help users make more informed decisions and can also provide valuable information to healthcare professionals if medical treatment becomes necessary.

Turboautism Dose Log is designed to make that process simple while keeping sensitive information private.

The application does not encourage or promote drug use. It recognizes that some people will use psychoactive substances regardless, and aims to reduce harm by helping them maintain accurate records.

---

Features

Dose logging

- Drug or medication name
- Route of administration
- Dosage
- Automatic timestamp
- Optional notes
- Edit existing entries
- Swipe-to-delete with undo

---

Sessions

Group doses into sessions such as:

- Friday night
- Festival
- Medication adjustment
- Recovery

Features include:

- Multiple active sessions
- Automatic dose assignment
- Manual session selection when several sessions are active
- Session timeline
- Session duration
- Substance summary
- Session statistics

---

Harm reduction

Turboautism Dose Log includes an entirely offline interaction detection system.

Before a new dose is logged, the application can compare it against substances that are still likely to be active and warn about combinations classified by PsychonautWiki as:

- 🔴 Dangerous
- 🟠 Unsafe
- 🟡 Uncertain

Interaction detection:

- Works completely offline
- Uses a bundled reference database generated from PsychonautWiki
- Estimates whether previously logged substances are still active using route-specific duration data
- Never blocks logging
- Always allows the user to continue if they choose

Warnings are advisory only and should not be considered medical advice.

---

Statistics

Built-in statistics include:

- Total entries
- Entries today
- Entries during the last 7 days
- Most frequently logged substance
- Average doses per day
- Last recorded dose

---

Privacy & Security

Privacy is a core design goal.

Turboautism Dose Log:

- Never communicates over the internet
- Does not include analytics
- Does not include advertising
- Does not require an account
- Stores all data locally
- Encrypts the database using SQLCipher (AES-256)
- Protects access using the Android Keystore
- Requires biometric authentication or device credentials
- Prevents screenshots and screen recording
- Hides app contents from the recent apps preview
- Disables copy/cut in text fields to reduce clipboard exposure

The application intentionally does not request Android's network permission.

---

Data export

The application supports:

- CSV export
- CSV import
- Duplicate detection
- Partial import failure reporting

CSV exports are intentionally stored as plaintext so they can be shared quickly with emergency medical services or healthcare providers when needed.

---

Offline reference database

The bundled substance database is generated automatically from the PsychonautWiki GraphQL API using tooling included in this repository.

The database currently includes:

- Substance names
- Aliases
- Routes of administration
- Duration information
- Chemical classes
- Psychoactive classes
- Interaction classifications

The generated database is bundled with the application and never downloaded at runtime.

---

Compatibility

Minimum supported Android version:

Android 11 (API 30)

The application has been tested on Android 11 and Android 16.

---

Download

Prebuilt APKs are available from the GitHub Releases page.

Install the latest release manually on your Android device.

---

Building from source

Requirements:

- Android Studio
- Java 17+
- Android SDK

Clone the repository:

git clone https://github.com/emmikal/Turboautism-dose-log-.git

Open the project in Android Studio and build normally.

To regenerate the bundled substance database, see:

"tools/README.md"

---

Open source

Turboautism Dose Log is completely open source.

Contributions, bug reports, feature requests and pull requests are always welcome.

---

Supporting development

Development is entirely volunteer-driven.

If you find the project useful, you can support continued development through GitHub Sponsors.

Support helps cover:

- Development tools
- Google Play developer fees
- Maintenance of the bundled PsychonautWiki database
- Testing devices
- Ongoing development

---

Acknowledgements

Turboautism Dose Log builds upon the work of many excellent open-source projects and communities.

See ACKNOWLEDGEMENTS.md for full credits.

---

Disclaimer

Turboautism Dose Log is a harm reduction tool.

It does not encourage, promote or endorse drug use.

The information provided by the application is for personal record-keeping only and is not a substitute for professional medical advice, diagnosis or treatment.

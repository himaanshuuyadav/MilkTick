# MilkTick

A modern Android application for dairy consumers to track milk deliveries, payments, and records with a clean Material Design 3 interface.

## Core Features
* **Daily Milk Tracking:** Record daily milk deliveries with date and quantity inputs.
* **Smart Notifications:** Exact, reliable daily reminders and monthly rate alerts using AlarmManager.
* **Cloud Sync:** Offline-first architecture with automatic synchronization to Firebase Firestore.
* **Analytics & Records:** View monthly calendars, yearly charts, and export data to CSV.
* **Theme Customization:** Complete light and dark mode support with dynamic system bars.
* **Secure Authentication:** Firebase Authentication with secure profile management.

## Tech Stack
* **Language:** Kotlin 1.9.0
* **UI:** Jetpack Compose, Material Design 3
* **Architecture:** MVVM, Kotlin Coroutines, Flow
* **Database:** Room (Local), Firebase Firestore (Remote)
* **Minimum SDK:** 24 (Android 7.0) | **Target SDK:** 34 (Android 14)

## Setup & Installation

### 1. Firebase Configuration (Required)
1. Create a Firebase project and add an Android app with the package name `com.prantiux.milktick`.
2. Enable **Firestore Database** and **Authentication** (Email/Password).
3. Download the `google-services.json` file and place it in both the root directory and the `app/` directory.

**Firestore Security Rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /{document=**} { allow read, write: if false; }
  }
}
```

### 2. Building the Project
Clone the repository and open it in Android Studio (Hedgehog or newer). Gradle sync will run automatically.
To build a release APK via command line:
```bash
./gradlew assembleRelease
```
The APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

## License
© 2025 MilkTick. All rights reserved.

# MilkTick 🥛

A modern Android application for dairy consumers to track milk deliveries, payments, and records with an elegant Material Design 3 interface.

<div align="center">
  
![Android](https://img.shields.io/badge/Android-SDK%2024+-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple)
![Firebase](https://img.shields.io/badge/Firebase-Firestore-orange)

</div>

---

## 📋 Table of Contents

- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Firebase Configuration](#firebase-configuration)
- [Building the App](#building-the-app)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## ✨ Features

### Core Features
- 📅 **Daily Milk Tracking** - Record daily milk deliveries with date picker and quantity input
- 📆 **Monthly Calendar View** - Visual calendar showing delivery history
- 📊 **Yearly Records** - View and analyze yearly delivery data
- 💰 **Payment Management** - Track monthly payments and payment status
- 📈 **Analytics Dashboard** - Comprehensive statistics and data visualization

### Smart Features
- 🔔 **Smart Notifications** - Daily reminders, evening notifications, and monthly rate alerts
- 📥 **CSV Export** - Export monthly and yearly data to Excel-compatible CSV files
- 💾 **Cloud Sync** - Automatic data synchronization with Firebase Firestore
- 🎨 **Theme Customization** - Light and dark mode with smooth transitions
- 🚀 **Onboarding Experience** - First-time user guide

### User Management
- 🔐 **Secure Authentication** - Firebase Authentication with email/password
- 👤 **Profile Management** - Edit profile details and change password
- 🔒 **Data Privacy** - User-specific data isolation

---

## 📱 Screenshots

_Coming soon..._

---

## 🛠️ Tech Stack

### Core Technologies
- **Language:** Kotlin 1.9.0
- **UI Framework:** Jetpack Compose with Material Design 3
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

### Architecture & Libraries
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Firebase Firestore
- **Authentication:** Firebase Auth
- **Dependency Injection:** Manual DI with ViewModelProvider
- **Asynchronous:** Kotlin Coroutines + Flow
- **Navigation:** Jetpack Navigation Compose
- **Notifications:** WorkManager + NotificationManager
- **CSV Export:** OpenCSV

### Build & Development
- **Build System:** Gradle with Kotlin DSL
- **Build Tool Version:** 8.1.1
- **Gradle Version:** 8.0

---

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture pattern with clean separation of concerns:

```
app/src/main/java/com/prantiux/milktick/
├── data/                    # Data models
│   ├── MilkEntry.kt
│   ├── MonthlyRate.kt
│   ├── MonthlyPayment.kt
│   └── MonthlySummary.kt
│
├── repository/              # Data layer
│   ├── AuthRepository.kt
│   └── FirestoreRepository.kt
│
├── viewmodel/              # Business logic
│   ├── AuthViewModel.kt
│   ├── HomeViewModel.kt
│   ├── CalendarViewModel.kt
│   └── ...
│
├── ui/                     # Presentation layer
│   ├── screens/           # Composable screens
│   ├── components/        # Reusable UI components
│   └── theme/            # Theme configuration
│
├── notification/          # Background workers
│   ├── DailyMilkWorker.kt
│   └── NotificationScheduler.kt
│
├── navigation/           # Navigation setup
│   ├── NavGraph.kt
│   └── Screen.kt
│
└── utils/               # Utilities
    └── CsvExporter.kt
```

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio:** Hedgehog (2023.1.1) or later
- **JDK:** Version 17 or higher
- **Android SDK:** API Level 34
- **Git:** For version control
- **Firebase Account:** For backend services

---

## 🚀 Installation & Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/himaanshuuyadav/MilkTick.git
cd MilkTick
```

### Step 2: Set Up Firebase

This is the **most critical step** for the app to function properly.

#### 2.1 Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add Project"** or **"Create a Project"**
3. Enter project name: `MilkTick` (or your preferred name)
4. (Optional) Enable Google Analytics
5. Click **"Create Project"**

#### 2.2 Add Android App to Firebase

1. In Firebase Console, click the **Android icon** to add an Android app
2. Enter the following details:
   - **Package name:** `com.prantiux.milktick` ⚠️ **MUST MATCH EXACTLY**
   - **App nickname:** MilkTick (optional)
   - **Debug signing certificate SHA-1:** (optional, for now)
3. Click **"Register app"**

#### 2.3 Download google-services.json

1. Firebase will generate a `google-services.json` file
2. Download this file
3. Place it in **two locations**:
   ```
   MilkTick/google-services.json           (root directory)
   MilkTick/app/google-services.json       (app directory)
   ```

⚠️ **SECURITY WARNING:** 
- This file contains API keys and should **NEVER** be committed to Git
- The `.gitignore` file already excludes it
- **Never share this file publicly**

#### 2.4 Enable Firebase Services

##### Enable Firestore Database:
1. In Firebase Console, go to **Firestore Database**
2. Click **"Create database"**
3. Choose **"Start in test mode"** (for development)
4. Select your preferred region (e.g., `us-central`)
5. Click **"Enable"**

##### Update Firestore Security Rules:
After enabling Firestore, update the security rules:

1. Go to **Firestore Database** → **Rules** tab
2. Replace the default rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read/write their own data
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

3. Click **"Publish"**

##### Enable Firebase Authentication:
1. Go to **Authentication** → **Sign-in method**
2. Enable **"Email/Password"** provider
3. Click **"Save"**

### Step 3: Build Configuration

#### 3.1 Check local.properties

Ensure `local.properties` exists in the root directory with your SDK path:

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

This file is auto-generated by Android Studio and is **already excluded from Git**.

#### 3.2 Keystore for Release Builds (Optional)

To build a signed release APK, you need a keystore file.

**If you already have a keystore:**
- Place it in the root directory
- Update `app/build.gradle.kts` with your keystore details

**To create a new keystore:**
```bash
keytool -genkey -v -keystore milktick-release-key.jks -alias milktick -keyalg RSA -keysize 2048 -validity 10000
```

⚠️ **IMPORTANT:** 
- Keystore files (`.jks`, `.keystore`) are excluded from Git
- **Never commit keystore files or passwords**
- Store keystore credentials securely

---

## 🔨 Building the App

### Option 1: Using Android Studio (Recommended)

1. **Open Project:**
   - Launch Android Studio
   - Select **"Open"**
   - Navigate to the cloned `MilkTick` directory
   - Click **"OK"**

2. **Sync Gradle:**
   - Android Studio will automatically start Gradle sync
   - Wait for sync to complete (may take 2-5 minutes on first build)
   - If sync fails, check Firebase configuration

3. **Select Build Variant:**
   - Go to **Build** → **Select Build Variant**
   - Choose `debug` for development or `release` for production

4. **Run the App:**
   - Connect an Android device (enable USB debugging) **OR** start an emulator
   - Click the **Run** button (green play icon) or press `Shift + F10`
   - Select your device/emulator
   - Wait for build and installation

### Option 2: Using Command Line

#### Build Debug APK:
```bash
# Windows
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Build Release APK:
```bash
# Windows
gradlew.bat assembleRelease

# macOS/Linux
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

#### Install on Connected Device:
```bash
# Debug
gradlew.bat installDebug

# Release
gradlew.bat installRelease
```

#### Run Tests:
```bash
gradlew.bat test
```

---

## 📂 Project Structure

```
MilkTick/
├── .gitignore                          # Git ignore rules
├── README.md                           # This file
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Gradle settings
├── gradle.properties                   # Gradle properties
├── local.properties                    # Local SDK path (git-ignored)
├── google-services.json               # Firebase config (git-ignored)
│
├── app/
│   ├── build.gradle.kts               # App module build config
│   ├── proguard-rules.pro             # ProGuard rules
│   ├── google-services.json           # Firebase config (git-ignored)
│   │
│   └── src/main/
│       ├── AndroidManifest.xml        # App manifest
│       │
│       ├── java/com/prantiux/milktick/
│       │   ├── MainActivity.kt        # Entry point
│       │   │
│       │   ├── data/                  # Data models
│       │   │   ├── MilkEntry.kt
│       │   │   ├── MonthlyRate.kt
│       │   │   ├── MonthlyPayment.kt
│       │   │   └── MonthlySummary.kt
│       │   │
│       │   ├── repository/            # Data repositories
│       │   │   ├── AuthRepository.kt
│       │   │   └── FirestoreRepository.kt
│       │   │
│       │   ├── viewmodel/             # ViewModels
│       │   │   ├── AuthViewModel.kt
│       │   │   ├── HomeViewModel.kt
│       │   │   ├── CalendarViewModel.kt
│       │   │   ├── RecordsViewModel.kt
│       │   │   └── ...
│       │   │
│       │   ├── ui/
│       │   │   ├── screens/           # App screens
│       │   │   │   ├── AuthScreen.kt
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   ├── CalendarScreen.kt
│       │   │   │   ├── RecordsScreen.kt
│       │   │   │   ├── SettingsScreen.kt
│       │   │   │   └── ...
│       │   │   │
│       │   │   ├── components/        # Reusable components
│       │   │   │   ├── BottomNavigation.kt
│       │   │   │   ├── AnimatedTopAppBar.kt
│       │   │   │   └── ...
│       │   │   │
│       │   │   └── theme/             # Theme configuration
│       │   │       ├── Theme.kt
│       │   │       ├── Color.kt
│       │   │       ├── Type.kt
│       │   │       └── ThemePreferences.kt
│       │   │
│       │   ├── navigation/            # Navigation
│       │   │   ├── NavGraph.kt
│       │   │   └── Screen.kt
│       │   │
│       │   ├── notification/          # Notification system
│       │   │   ├── DailyMilkWorker.kt
│       │   │   ├── EveningReminderWorker.kt
│       │   │   ├── MonthlyRateWorker.kt
│       │   │   ├── NotificationScheduler.kt
│       │   │   └── NotificationHelper.kt
│       │   │
│       │   └── utils/                 # Utilities
│       │       ├── CsvExporter.kt
│       │       └── NotificationPreferences.kt
│       │
│       └── res/                       # Resources
│           ├── drawable/              # Images & icons
│           ├── font/                  # Custom fonts
│           ├── mipmap/                # Launcher icons
│           ├── values/                # Strings, colors, themes
│           └── xml/                   # XML configs
│
└── gradle/                            # Gradle wrapper
    └── wrapper/
```

---

## 🔐 Security Checklist

Before committing or sharing your code, verify:

- [ ] ✅ `google-services.json` is **NOT** in Git (check with `git ls-files`)
- [ ] ✅ `.env` file (if created) is **NOT** in Git
- [ ] ✅ Keystore files (`.jks`, `.keystore`) are **NOT** in Git
- [ ] ✅ `local.properties` is **NOT** in Git
- [ ] ✅ `.gitignore` includes all sensitive files
- [ ] ✅ Firebase security rules are properly configured
- [ ] ✅ No hardcoded passwords or API keys in source code

**To verify what's tracked by Git:**
```bash
git ls-files | Select-String "google-services|keystore|.env|local.properties"
```

This command should return **no results** if everything is secure.

---

## 🐛 Troubleshooting

### Build Fails with "google-services.json not found"
- Ensure `google-services.json` exists in both root and `app/` directories
- Verify the file is valid JSON (check Firebase Console)
- Clean and rebuild: `gradlew clean build`

### Firebase Connection Fails
- Check your internet connection
- Verify Firebase project is enabled
- Ensure Firestore and Authentication are enabled
- Check security rules allow authenticated access

### Gradle Sync Fails
- Update Android Studio to latest version
- Check `local.properties` has correct SDK path
- Run `gradlew clean` and sync again
- Invalidate caches: **File** → **Invalidate Caches / Restart**

### App Crashes on Launch
- Check Logcat for detailed error messages
- Verify `google-services.json` package name matches: `com.prantiux.milktick`
- Ensure Firebase services are enabled
- Try uninstalling and reinstalling the app

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add some AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request

**Please ensure:**
- Code follows Kotlin coding conventions
- No sensitive data (credentials, keys) is included
- New features include proper documentation
- All tests pass

---

## 📄 License

© 2025 MilkTick. All rights reserved.

This project is for educational and personal use. Commercial use requires permission.

---

## 👤 Author

**Himanshu Yadav**

- GitHub: [@himaanshuuyadav](https://github.com/himaanshuuyadav)
- Repository: [MilkTick](https://github.com/himaanshuuyadav/MilkTick)

---

## 🙏 Acknowledgments

- **Firebase** - Backend infrastructure
- **Jetpack Compose** - Modern Android UI toolkit
- **Material Design 3** - Design system
- **OpenCSV** - CSV export functionality

---

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Troubleshooting](#troubleshooting) section
2. Search existing [GitHub Issues](https://github.com/himaanshuuyadav/MilkTick/issues)
3. Create a new issue with detailed description and logs

---

<div align="center">

**Made with ❤️ for dairy consumers**

⭐ Star this repository if you find it helpful!

</div>

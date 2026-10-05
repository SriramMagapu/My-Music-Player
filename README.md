# My Music

A modern, offline-first Android music player built for Android 13+ (API 33+) featuring a distinctive **Black + Modern Music Green** visual identity, AndroidX Media3 background playback, on-device voice commands, custom playlists, safe volume boosting, and one-handed ergonomics.

---

## 🌟 Key Features

- **100% Offline & Private**: Zero cloud uploads, no logins, no tracking, zero advertisements. All media indexing and voice recognition occur strictly on-device.
- **AndroidX Media3 & ExoPlayer**: Seamless background playback with lock-screen controls, notification media controls, Bluetooth audio integration, and automatic audio focus ducking/transient handling.
- **Dynamic App Logo & Brand Monogram**: Personalized logo letter system (customizable in Profile) integrated into the launcher, splash, and ambient visualizations.
- **On-Device Voice Commands**: Control your music naturally using speech recognition (`"Play"`, `"Next song"`, `"Volume up"`, `"Play favorites"`) or teach personalized custom trigger phrases.
- **Volume Swipe Gesture & Glass HUD**: Vertical swipe gesture on the left half of Now Playing triggers a semi-transparent dark HUD (`🔊 72%`) with smooth auto-fade.
- **Audio Equalizer & Safe Volume Boost**: Sound frequency presets (Rock, Pop, Jazz, Classical, etc.), Bass Boost, and safe volume boosting for quiet local recordings.
- **Smart Local Library Scanning**: Supports fast system MediaStore indexing and Android Storage Access Framework (SAF) folder picker with persistable URI permissions.
- **Custom Playlists & Favorites**: Create, manage, and play custom playlists, track queue reordering, and one-tap favorite toggling.
- **Sleep Timer**: Countdown timer with gentle audio fade-out in the final seconds.
- **Contextual Onboarding Tour**: 7-step interactive guide with SKIP and NEXT flows.

---

## 📱 Technology Stack & Architecture

- **Platform**: Android (minSdk 33, targetSdk 36, compileSdk 36)
- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose & Material 3
- **Audio Engine**: AndroidX Media3 (ExoPlayer 1.5.1, MediaSessionService)
- **Database**: SQLite / Room Architecture (local metadata, history, playlists, voice commands)
- **Image Loading**: Coil for Compose
- **Design System**: Deep Near-Black (`#0A0E0C`), Surface Elevated (`#1A221E`), Brand Electric Green (`#00E676`)

---

## 🛠️ How to Build

### Prerequisites
- JDK 17 or higher
- Android SDK with Platform 33+ (API 33-36)

### Build Commands

```bash
# Clone the repository
git clone https://github.com/SriramMagapu/My-Music-Player.git
cd My-Music-Player

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

The compiled APKs will be located at:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk` (or `MyMusic-release.apk`)

---

## 📥 How to Install APK

1. Transfer `MyMusic-release.apk` to your Android 13+ device via WhatsApp, Telegram, Google Drive, or USB.
2. Tap the APK file to install.
3. If prompted, enable "Install unknown apps" for your file manager or browser.
4. Open **My Music**, grant local audio permission, and enjoy your music!

---

## 🔒 Privacy & Permissions

| Permission | Purpose |
| :--- | :--- |
| `READ_MEDIA_AUDIO` | Access local audio files on Android 13+ |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Uninterrupted background playback and notification controls |
| `POST_NOTIFICATIONS` | Playback control notifications on Android 13+ |
| `RECORD_AUDIO` | On-device voice command recognition (only active when mic is tapped) |
| `MODIFY_AUDIO_SETTINGS` | Equalizer and sound boost effects |

---

## 📄 License

Open source personal project created for music lovers.

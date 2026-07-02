<div align="center">

# 🌍 OneMinute Language

**Learn a language without opening an app.**

A home-screen widget that quietly teaches you one word at a time — every time you unlock your phone.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-24-blue)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![ML Kit](https://img.shields.io/badge/translation-ML%20Kit-EA4335?logo=googletranslate&logoColor=white)

</div>

---

## ✨ What it does

No lessons, no streaks, no notifications to dismiss. Just a word, sitting quietly on your home screen, changing every time you glance at your phone.

| | |
|---|---|
| 🖼️ **Widget-first** | Your learning language shown large, its translation just below — refreshes instantly on every screen unlock |
| ⚡ **Zero-friction adding** | Tap **+** on the widget to add a new word straight from your home screen |
| 🧠 **On-device translation** | Powered by Google ML Kit — no network round-trip, works offline once models are downloaded |
| 🗂️ **Full word database** | Search, review, and delete anything you've added, right in the app |
| 🔁 **Live language switching** | Change your language pair in Settings and every saved word re-translates automatically |
| 📚 **Starter pack** | Optional 600+ word default list, one tap to import, one tap to cleanly remove later |

## 🈺 Supported languages

English · German · French · Italian · Spanish · Polish · Romanian · Portuguese · Dutch · Ukrainian

## 🛠️ Tech stack

- **UI** — Jetpack Compose + Material3, Navigation Compose
- **Widget** — classic `AppWidgetProvider` + `RemoteViews` with `ViewFlipper` for smooth slide animations (chosen over Glance for reliable cross-launcher rendering)
- **Instant refresh** — a lightweight foreground service listening for `ACTION_SCREEN_ON`
- **Persistence** — Room database with migrations
- **Translation** — Google ML Kit Translate, fully on-device
- **Language** — Kotlin + Coroutines

## 📋 Requirements

- Android Studio (latest)
- Android SDK platform 37
- `minSdk` 24 · `targetSdk` 37 · `compileSdk` 37

## 🚀 Building

Open the project in Android Studio and let Gradle sync, or from the command line:

```bash
./gradlew assembleDebug
```

For a signed, optimized release build, use **Build → Generate Signed Bundle / APK** in Android Studio. R8 full-mode optimization and resource shrinking are enabled for the `release` build type.

## 📁 Project structure

```
app/src/main/java/com/example/oneminutelanguage/
├── data/         # Room entities, DAOs, database
├── translation/  # ML Kit wrapper, language settings, default word list import
├── ui/           # Compose screens (Add Word, Database, Settings) and theme
└── widget/       # AppWidgetProvider, RemoteViews rendering, foreground service
```

---

<div align="center">

📬 **tuesdofsund@gmail.com**

*Coded with help of Claude 🤖*

</div>

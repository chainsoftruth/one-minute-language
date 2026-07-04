<div align="center">

# 🌍 OneMinute Language

**Learn a language without opening an app.**

A home-screen widget that quietly teaches you one word at a time — every time you unlock your phone.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-24-blue)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![ML Kit](https://img.shields.io/badge/translation-ML%20Kit-EA4335?logo=googletranslate&logoColor=white)
![Version](https://img.shields.io/badge/version-1.1-success)

</div>

---

## ✨ What it does

No lessons, no streaks, no notifications to dismiss. Just a word, sitting quietly on your home screen, changing every time you glance at your phone.

| | |
|---|---|
| 🖼️ **Widget-first** | Your learning language shown large, its translation just below — refreshes instantly on every screen unlock |
| 🔊 **Tap to hear it** | Tap the widget to hear the word spoken aloud with on-device text-to-speech, fully offline |
| 🧪 **Check Progress quiz** | Pick 15 / 30 / 60 / all words and test yourself, multiple-choice, with a repeat-audio button and an honest "I don't know" |
| ⚡ **Zero-friction adding** | Tap **+** on the widget to add a new word straight from your home screen — translated automatically, editable before you save |
| 🧠 **On-device translation** | Powered by Google ML Kit — no network round-trip, works offline once models are downloaded |
| 🗂️ **Full word database** | Search, review, delete, and toggle any word on or off for the widget rotation |
| 🔁 **Instant language swap** | Flip your language pair in Settings — reverse pairs swap instantly, no re-translation or model download needed |
| 📚 **Starter pack** | 623 hand-checked starter words across all supported languages, one tap to import, one tap to cleanly remove later |

## 🆕 What's new in v1.2

Two focused fixes: translations you can trust, and the last word on what actually gets saved.

### Highlights

- 🗂️ **Default words now translate from hand-written files, not the on-device translator** — all 623 starter words for every supported language (Dutch, Ukrainian, French, German, Italian, Spanish, Portuguese, Polish, Romanian) are pulled from curated, hand-translated word lists at import time instead of Google ML Kit. No more awkward machine-translated phrasing in the starter pack — quality no longer depends on how well ML Kit handles a given language pair.

- ✏️ **Adjust the translation before you save** — adding a new word still translates it automatically, but the suggested translation now lands in an editable field instead of plain text, so you can correct it before it's saved to your database.

### Upgrade notes

- Already-imported default words are unaffected until you re-import them: Settings → toggle "Include default word list" off, then on.
- Words you add yourself still go through the on-device translator as before — only the bundled starter pack switched to hand-written translations.

## 🆕 What's new in v1.1

Smarter translations, a pronunciation feature, and your first progress quiz — this release makes the widget both easier to read and harder to ignore.

### Highlights

- 🎯 **Massively improved translation quality** — all 623 starter words rewritten as self-disambiguating phrases: verbs as *to teach*, nouns with articles (*the house* → *het huis*, so you learn noun gender for free), and ~50 ambiguous words clarified (*light (not heavy)*, *May (the month)*, *the mouse (animal)*). Low-resource languages like Ukrainian benefit the most.

- 🔊 **Tap the widget to hear the word** — on-device text-to-speech pronounces the currently shown word in the language you're learning. Works offline with installed voice packs.

- 🧪 **New "Check Progress" quiz** — pick 15 / 30 / 60 / all words, get each word in your learning language (auto-pronounced, with a 🔊 Repeat button) and 4 answers in your native language. Honest "I don't know" option that lights up amber instead of red. Finish with a score and one-tap removal of the words you already know from the widget rotation.

- ✅ **Per-word widget control** — enable/disable any word in the Database view with a switch, plus Select all / Deselect all with confirmation. Disabled words stay in your database but leave the rotation.

- 🔁 **Instant language swap** — one tap in Settings flips English ↔ Dutch (or any pair). Existing words swap columns directly: no re-translation, no model download, no quality loss.

- 👁️ **Readable widget, always** — long words now wrap to a second line (or scroll where the launcher supports marquee) at full font size instead of shrinking to unreadable sizes.

- 🔢 **Clean numbers** — digits are used internally to nail translations (*five (5)*), but brackets are hidden everywhere you see or hear the word.

- ✏️ **Edit before you save** — the Add Word screen still translates automatically, but the suggested translation now lands in an editable field so you can correct it before saving.

- 👋 **Friendlier main screen** — time-of-day greeting and warmer stats ("You've seen the widget 12 times today").

### Upgrade notes

- Words imported in v1.0 keep their old bare-word forms. To get the improved phrasing: Settings → toggle "Include default word list" off, then on (re-import preserves your enabled/disabled choices).
- Pronunciation requires a voice pack for your learning language (Android Settings → Google Text-to-speech → install voice data). No pack — no sound, no crash.
- Database schema upgraded automatically (adds per-word enable flag); no action needed.

## 🈺 Supported languages

English · German · French · Italian · Spanish · Polish · Romanian · Portuguese · Dutch · Ukrainian

All 623 starter words are hand-translated per language (not machine-translated) so the default list is accurate from the very first import.

## 🛠️ Tech stack

- **UI** — Jetpack Compose + Material3, Navigation Compose
- **Widget** — classic `AppWidgetProvider` + `RemoteViews` with `ViewFlipper` for smooth slide animations (chosen over Glance for reliable cross-launcher rendering)
- **Instant refresh** — a lightweight foreground service listening for `ACTION_SCREEN_ON`
- **Pronunciation** — Android `TextToSpeech`, fully on-device, graceful no-op when a voice pack is missing
- **Persistence** — Room database with migrations
- **Translation** — Google ML Kit Translate, fully on-device
- **Language** — Kotlin + Coroutines

## 📋 Requirements

- Android with `minSdk` 24+ (Android 7.0 Nougat or newer)
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
├── speech/       # Text-to-speech wrapper and the widget's "speak on tap" activity
├── translation/  # ML Kit wrapper, language settings, default word list import
├── ui/           # Compose screens (Add Word, Database, Quiz, Settings) and theme
└── widget/       # AppWidgetProvider, RemoteViews rendering, foreground service
```

---

<div align="center">

📬 **tuesdofsund@gmail.com**

*Coded with help of Claude 🤖*

</div>

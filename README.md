<div align="center">

# 🌍 OneMinute Language

**Learn a language without opening an app, or sit down and learn it properly.**

A home-screen widget that quietly teaches you one word at a time, plus a full Dutch course from A1 to B1 when you want to go deeper.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-24-blue)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![ML Kit](https://img.shields.io/badge/translation-ML%20Kit-EA4335?logo=googletranslate&logoColor=white)
![Version](https://img.shields.io/badge/version-2.1-success)

</div>

---

## ✨ What it does

Two modes in one app. **Quick** is the widget: a word sitting quietly on your home screen, changing every time you glance at your phone. **Deep learning** is a structured Dutch course (A1 → B1) with lessons, spaced-repetition review, listening, speaking, reading and writing.

| | |
|---|---|
| 🖼️ **Widget-first** | Your learning language shown large, its translation just below — refreshes instantly on every screen unlock |
| 🎯 **Focus mode** | The widget repeats a set of 20 words until you know them; each word you get right in a quiz makes room for a new one |
| 🔊 **Tap to hear it** | Tap the widget to hear the word spoken aloud with on-device text-to-speech, fully offline |
| 🧪 **Check Progress quiz** | Three modes (meaning, reverse, and de / het for Dutch nouns), 15 / 30 / 60 / all words, with a repeat-audio button and an honest "I don't know" |
| ⚡ **Zero-friction adding** | Tap **+** on the widget to add a new word straight from your home screen — in either direction, translated automatically, editable before you save |
| 🧠 **On-device translation** | Powered by Google ML Kit — no network round-trip, works offline once models are downloaded |
| 🗂️ **Full word database** | Search, review, delete, and toggle any word on or off for the widget rotation |
| 🔁 **Instant language swap** | Flip your language pair in Settings — reverse pairs swap instantly, no re-translation or model download needed |
| 🎓 **Dutch course A1 → B1** | 37 grammar units, 16 theme units and 3 checkpoints, about 4,100 words, every B1 grammar point, plus reading, listening, speaking and writing practice. Works offline |
| 🔁 **Spaced-repetition review** | Words and mistakes come back at the right time; a daily goal, streak and activity grid keep you going |
| 🧭 **Placement test** | 30 questions suggest where to start, so you can skip what you already know |
| 📊 **Progress and can-do list** | Level and skill meters, words mastered, and a B1 "I can…" checklist linked to the units that train each skill |
| 📚 **Starter pack** | 679 hand-checked starter words for English–Dutch (583–590 for the other languages), one tap to import, one tap to cleanly remove later |

## 🆕 What's new in v2.1

Topics, a calmer dictionary and a tidier widget.

- 🗂️ **Topics for your words** — every word from the starter list or the dictionary now belongs to a topic. The **My words** tab has topic chips; pick one and *Select all* / *Deselect all* switch just that topic on or off.
- 🎯 **Widget topic** — Settings → Widget → *Widget topic* limits the widget (and its focus set of 20) to one topic.
- ⚡ **Faster dictionary** — searching runs in the background, with a spinner while it loads.
- 🎨 **My words** restyled to match the rest of the app; the **words in your collection** tile on Today opens it.
- 🔘 The widget's **+** button no longer gets clipped by the rounded corner.
- Upgrade: the database upgrades itself; your words and settings are kept.

## 🆕 What's new in v2.0

"Dutch": the widget stays, and a complete course joins it.

### Highlights

- 🎓 **Deep learning** — the new **Learn** tab holds a Dutch course for English speakers, from A1 to the B1 level of the *inburgeringsexamen* and *Staatsexamen NT2 programma I*. Grammar units explain a rule in plain English, then practise it with choice, gap, word-order, transformation, translation and matching exercises. Theme units (work, health, housing, travel and more) add vocabulary, a reading text, a dialogue, speaking practice and a writing task. Each level ends with a checkpoint; B1 has a practice exam in the official format.
- 📖 **About 4,100 words** — every noun with its article and plural, every verb with its forms, an example sentence for each. Search them in the new dictionary by any inflected form.
- 🔁 **Review** — spaced repetition for words and for the exercises you got wrong.
- 🎧 **Listening and speaking** — dialogues with two voices, dictation, repeat-after-me with speech recognition, and role-play where you play one of the speakers.
- 📰 **Reading and writing** — tap any word in a text for its meaning; writing tasks have a word counter, a model answer and an optional LanguageTool check.
- 🧭 **Placement test, progress and can-do list** — see where you stand and what to do next.
- 🔗 **Resources** — a hub of free outside material (news in easy Dutch, grammar sites, official practice exams).
- 🎨 **A modern interface** — bottom navigation (Today, Learn, Practice, Words), a Today dashboard with a daily goal and streak, light and dark themes.
- 🌐 **Mobile data prompt** — if a translation model has to be downloaded and you are not on Wi-Fi, Add Word now asks before using mobile data.

### Upgrade notes

- Your words, their enabled flags and learned state are kept; the database upgrades itself.
- The course works offline. Only the translation model download, links you tap and the opt-in LanguageTool check use the network. The LanguageTool check sends your text to languagetool.org, so it is off until you turn it on.
- For audio you need the Google text-to-speech Dutch voice; for speaking exercises a speech recogniser. Settings → Speech shows what is missing.

## 🆕 What's new in v1.5

"Actually learn": the widget stops being a random word generator and starts drilling the words you don't know yet.

### Highlights

- 🎯 **Focus mode** — instead of a random pick from ~680 words (each one back roughly once every 680 unlocks), the widget rotates through a set of 20. Answer a word correctly in any quiz and it leaves the set; the next word takes its place. Your own added words come first. On by default; switch it off in Settings to get the old random pick.

- 🔄 **Reverse quiz** — see the word in your native language and pick the one in the language you're learning. The correct answer is spoken after you answer.

- 🇳🇱 **de / het quiz** — for Dutch: see the noun without its article (*ziekenhuis*) and pick **de** or **het**. Uses the ~370 nouns in your list, no extra data.

- ⇄ **Add words in either direction** — heard a Dutch word? Flip the Add Word screen and type it Dutch-first to get the translation. Duplicates are caught on both sides.

- 🖼️ **Polish** — a proper monochrome status-bar icon for the widget service, and a preview and description in the widget picker (Android 12+).

### Upgrade notes

- **New app ID** (`io.github.chainsoftruth.oneminutelanguage`, ready for Google Play): v1.5 installs as a new app next to v1.2 and starts with an empty word list. Turn on "Include default word list" in Settings, re-add your own words, then uninstall the old version.
- Changing the language pair re-imports the starter words, so their "learned" state starts over; your own words keep it.

## 🆕 What's new in v1.3 and v1.4

- 👁️ **The widget always fits** — text size is computed per word and widget size, so long words shrink instead of breaking mid-word; hints like *(in het ziekenhuis)* stay on the native side only.
- 🔁 **Steadier widget** — the word only changes on unlock (not on resize or settings changes), and the widget keeps refreshing after a reboot or app update.
- 📚 **Word base v2** — one keyed word file for all languages, synonym collisions fixed (no more "big" vs "large" both being *groot* in the quiz), ~90 high-frequency Dutch words added (*zijn, hebben, wat, waarom, misschien…*), US units replaced by euro / kilometer / liter / gram.
- ✏️ **Edit and undo** — tap a word in the Database screen to edit it; deleting shows an Undo.

## 🆕 What's new in v1.2

Two focused fixes: translations you can trust, and the last word on what actually gets saved.

### Highlights

- 🗂️ **Default words now translate from hand-written files, not the on-device translator** — all starter words for every supported language (Dutch, Ukrainian, French, German, Italian, Spanish, Portuguese, Polish, Romanian) are pulled from curated, hand-translated word lists at import time instead of Google ML Kit. No more awkward machine-translated phrasing in the starter pack — quality no longer depends on how well ML Kit handles a given language pair.

- ✏️ **Adjust the translation before you save** — adding a new word still translates it automatically, but the suggested translation now lands in an editable field instead of plain text, so you can correct it before it's saved to your database.

### Upgrade notes

- Already-imported default words are unaffected until you re-import them: Settings → toggle "Include default word list" off, then on.
- Words you add yourself still go through the on-device translator as before — only the bundled starter pack switched to hand-written translations.

## 🆕 What's new in v1.1

Smarter translations, a pronunciation feature, and your first progress quiz — this release makes the widget both easier to read and harder to ignore.

### Highlights

- 🎯 **Massively improved translation quality** — all starter words rewritten as self-disambiguating phrases: verbs as *to teach*, nouns with articles (*the house* → *het huis*, so you learn noun gender for free), and ~50 ambiguous words clarified (*light (not heavy)*, *May (the month)*, *the mouse (animal)*). Low-resource languages like Ukrainian benefit the most.

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

All starter words are hand-translated per language (not machine-translated) so the default list is accurate from the very first import. English and Dutch have 679 words; the other languages have 583–590, because the newest high-frequency words ship in Dutch first.

## 🛠️ Tech stack

- **UI** — Jetpack Compose + Material3, Navigation Compose
- **Course content** — JSON files in `assets/courses/`, parsed with kotlinx.serialization; checked by unit tests that read the files from disk
- **Review** — a small SM-2-style spaced-repetition scheduler (plain Kotlin, unit tested)
- **Writing check** — optional, via the free LanguageTool API (`HttpURLConnection`)
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
app/src/main/assets/courses/   # the Dutch course: units, lexicon, resources (JSON)
app/src/main/java/com/example/oneminutelanguage/
├── course/       # Course model, content loading, answer checking, review scheduling, placement (plain Kotlin)
├── data/         # Room entities, DAOs, database
├── speech/       # Text-to-speech wrapper and the widget's "speak on tap" activity
├── translation/  # ML Kit wrapper, language settings, default word list import
├── ui/           # Compose screens (Today, Practice, Words, Quiz, Settings) and theme
│   └── learn/    # Learn tab: course path, lessons, review, dictionary, progress, resources
└── widget/       # AppWidgetProvider, RemoteViews rendering, foreground service
```

## 🔗 Attribution & sources

The course text, examples, exercises, readings and dialogues are original. External sites were used to decide which topics to teach and to check rules, and the app links to them; nothing is copied from them. All of them are free to use:

- **NT2 TaalMenu** (nt2taalmenu.nl): free grammar and practice pages, linked from every grammar lesson
- **Woordenlijst Nederlandse Taal** (woordenlijst.org) and **Taaladvies.net**, both from the Taalunie: official spelling and usage
- **E-ANS** (e-ans.ivdnt.org): the reference grammar of Dutch
- **Dutchgrammar.com** and **Wiktionary**: grammar explained in English, forms checked
- **NOS Journaal in Makkelijke Taal**, **NOS Jeugdjournaal**, **NOS** and **Wablieft**: news to read and listen to
- **Taalklas.nl**, **Oefenen.nl** and YouTube channels **Easy Dutch**, **Bart de Pau** and **Dutchies to be**: courses and videos
- **DUO Inburgeren** (inburgeren.nl), **Staatsexamens NT2** and **Oefenexamens NT2**, **Inburgering.nl**: official exam information and free practice exams
- **LanguageTool** (languagetool.org): the optional grammar and spelling check for writing tasks
- **Google ML Kit** (translation) and Android text-to-speech and speech recognition

---

<div align="center">

📬 **tuesdofsund@gmail.com**

*Coded with help of Claude 🤖*

</div>

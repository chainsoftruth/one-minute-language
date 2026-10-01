package com.example.oneminutelanguage.course

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CourseInfo(
    val id: String, val name: String, val nativeName: String, val flag: String,
    val ttsLocale: String, val levels: List<String>, val available: Boolean = true
)

@Serializable data class CourseOutline(val id: String, val topics: List<Topic>, val levels: List<LevelOutline>)
@Serializable data class Topic(val id: String, val title: String)
@Serializable data class LevelOutline(val level: String, val title: String, val units: List<String>)

@Serializable
enum class UnitKind {
    @SerialName("grammar") GRAMMAR,
    @SerialName("theme") THEME,
    @SerialName("checkpoint") CHECKPOINT
}

@Serializable
enum class LessonKind {
    @SerialName("grammar") GRAMMAR,
    @SerialName("vocab") VOCAB,
    @SerialName("reading") READING,
    @SerialName("listening") LISTENING,
    @SerialName("speaking") SPEAKING,
    @SerialName("writing") WRITING,
    @SerialName("test") TEST
}

/** `CourseUnit`, not `Unit`, to avoid clashing with `kotlin.Unit`. */
@Serializable
data class CourseUnit(
    val id: String, val level: String, val kind: UnitKind, val title: String,
    val subtitle: String = "", val lessons: List<Lesson>
)

@Serializable
data class Lesson(
    val id: String, val title: String, val kind: LessonKind,
    val explain: List<Block> = emptyList(),
    val vocab: List<String> = emptyList(),     // lexicon ids (vocab lessons; exercises are generated)
    val text: String? = null,                  // reading lessons
    val dialogue: List<Line> = emptyList(),    // listening / roleplay lessons
    val items: List<Item> = emptyList(),
    val sources: List<Source> = emptyList()
)

/** who = "A" | "B" */
@Serializable data class Line(val who: String, val nl: String, val en: String)
@Serializable data class Source(val title: String, val url: String)

@Serializable
sealed interface Block {
    /** **bold** and *italic* only. */
    @Serializable @SerialName("p") data class Paragraph(val text: String) : Block
    @Serializable @SerialName("tip") data class Tip(val text: String) : Block
    /** Tap = speak. */
    @Serializable @SerialName("ex") data class Example(val nl: String, val en: String) : Block
    @Serializable @SerialName("table") data class Table(val head: List<String>, val rows: List<List<String>>) : Block
}

@Serializable
sealed interface Item {
    val explain: String?

    @Serializable @SerialName("choice")
    data class Choice(
        val q: String, val options: List<String>, val answer: Int,
        val en: String? = null, override val explain: String? = null
    ) : Item

    /** Exactly one "___". */
    @Serializable @SerialName("gap")
    data class Gap(
        val text: String, val answers: List<String>, val hint: String? = null,
        val en: String? = null, override val explain: String? = null
    ) : Item

    @Serializable @SerialName("order")
    data class Order(
        val answer: String, val alt: List<String> = emptyList(), val en: String? = null,
        val extra: List<String> = emptyList(), override val explain: String? = null
    ) : Item

    @Serializable @SerialName("transform")
    data class Transform(
        val q: String, val instruction: String, val answers: List<String>, override val explain: String? = null
    ) : Item

    @Serializable @SerialName("translate")
    data class Translate(val en: String, val answers: List<String>, override val explain: String? = null) : Item

    @Serializable @SerialName("match")
    data class Match(val pairs: List<List<String>>, override val explain: String? = null) : Item

    /** No options = dictation. */
    @Serializable @SerialName("listen")
    data class Listen(
        val nl: String, val options: List<String> = emptyList(), val answer: Int = -1,
        val q: String? = null, override val explain: String? = null
    ) : Item

    @Serializable @SerialName("speak")
    data class Speak(val nl: String, val en: String, override val explain: String? = null) : Item

    /** Open speaking. */
    @Serializable @SerialName("prompt")
    data class OpenPrompt(
        val task: String, val model: String, val seconds: Int = 30,
        val points: List<String> = emptyList(), override val explain: String? = null
    ) : Item

    @Serializable @SerialName("write")
    data class Write(
        val task: String, val points: List<String>, val minWords: Int, val maxWords: Int,
        val model: String, override val explain: String? = null
    ) : Item
}

@Serializable
data class LexEntry(
    val id: String, val nl: String, val en: String, val pos: String, val lvl: String, val topic: String,
    val art: String? = null, val pl: String? = null, val dim: String? = null,           // nouns
    val pres: String? = null, val past: String? = null, val pp: String? = null,          // verbs: "loopt", "liep/liepen", "gelopen"
    val aux: String? = null, val sep: Boolean = false, val refl: Boolean = false,        // aux: "heeft" | "is" | "heeft/is"
    val prep: String? = null,                                                            // fixed preposition: "wachten op"
    val cmp: String? = null, val sup: String? = null,                                    // adjectives
    val ex: String? = null, val exEn: String? = null
)

val CourseJson = Json { classDiscriminator = "type"; ignoreUnknownKeys = false; explicitNulls = false }

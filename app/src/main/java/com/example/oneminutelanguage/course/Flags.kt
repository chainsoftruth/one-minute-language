package com.example.oneminutelanguage.course

/** What the learner says is wrong with an item. The id goes into the exported line. */
enum class FlagType(val id: String, val label: String) {
    WRONG_ANSWER("wrong_answer", "Wrong answer"),
    WRONG_DUTCH("wrong_dutch", "Wrong Dutch"),
    UNCLEAR("unclear", "Unclear"),
    AUDIO("audio", "Audio"),
    OTHER("other", "Other")
}

/** `<lessonId>|<itemIndex>|<type>|<text>`; itemIndex is -1 for the explanation, reading text or dialogue. */
fun flagLine(lessonId: String, itemIndex: Int, type: FlagType, text: String): String =
    "$lessonId|$itemIndex|${type.id}|${text.replace(Regex("\\s+"), " ").replace('|', '/').trim()}"

/** The text the learner shares from Settings; saved as `docs/flags.txt` it is the fix list for the next run. */
fun flagsExport(flags: Collection<String>): String = flags.sorted().joinToString("\n")

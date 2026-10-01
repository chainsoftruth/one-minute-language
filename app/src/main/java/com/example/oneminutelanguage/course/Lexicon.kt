package com.example.oneminutelanguage.course

/** Nouns that take a single, known article. Proper nouns (`art = "-"`) and `de/het` words don't. */
fun LexEntry.hasSingleArticle(): Boolean = pos == "noun" && (art == "de" || art == "het")

/** How the word is shown: "de fiets", "het meisje", "lopen". Proper nouns have no article. */
fun LexEntry.display(): String =
    if (pos == "noun" && art != null && art != "-") "$art $nl" else nl

/** Every accepted typed answer for the word, article included for nouns ("de/het" words accept both). */
fun LexEntry.answers(): List<String> =
    if (pos == "noun" && art != null && art != "-") art.split('/').map { "$it $nl" } else listOf(nl)

/** Plural, verb forms or adjective forms on one line: "lopen · liep · gelopen (is/heeft)". Empty when there are none. */
fun LexEntry.formsLine(): String = when (pos) {
    "noun" -> listOfNotNull(pl?.takeIf { it != "-" }?.let { "pl. $it" }, dim?.let { "dim. $it" }).joinToString(" · ")
    "verb" -> if (past == null || pp == null) "" else
        listOf(nl, past.substringBefore('/'), pp).joinToString(" · ") + (aux?.takeIf { it != "-" }?.let { " ($it)" } ?: "")
    "adj" -> listOfNotNull(cmp, sup).joinToString(" · ")
    else -> ""
}

/** Every spelling of the word a learner might meet or type, one entry per variant ("liep/liepen" gives two). */
fun LexEntry.forms(): List<String> = buildList {
    add(nl)
    listOfNotNull(pl, dim, pres, past, pp, cmp, sup).forEach { f -> f.split('/').forEach { add(it.trim()) } }
}.filter { it.isNotBlank() && it != "-" }

private fun capitalize(s: String) = s.trim().replaceFirstChar { it.titlecase() }

/** The pair stored in "My words" (language1 = English, language2 = Dutch), the way the v1 starter words look. */
fun LexEntry.widgetPair(): Pair<String, String> = capitalize(en) to capitalize(display())

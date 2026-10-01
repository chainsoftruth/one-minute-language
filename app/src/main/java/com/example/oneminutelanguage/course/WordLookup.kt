package com.example.oneminutelanguage.course

private val WORD = Regex("""[\p{L}\p{N}]+(?:['’\-][\p{L}\p{N}]+)*""")

// A sentence ends at . ! ? followed by a space or the line end (so "10.30 uur" stays whole), or at a line break.
private val SENTENCE = Regex("""(?m).+?(?:[.!?]+(?=\s|$)|$)""")

/** Where the words of [text] are (tap-to-translate). */
fun wordRanges(text: String): List<IntRange> = WORD.findAll(text).map { it.range }.toList()

/** Where the sentences of [text] are, without surrounding spaces (read aloud and highlighted one by one). */
fun sentenceRanges(text: String): List<IntRange> = SENTENCE.findAll(text).mapNotNull { m ->
    var start = m.range.first
    var end = m.range.last
    while (start <= end && text[start].isWhitespace()) start++
    while (end >= start && text[end].isWhitespace()) end--
    if (start <= end) start..end else null
}.toList()

private const val MAX_RESULTS = 3

// ponytail: the lexicon keeps only the 3rd person singular in `pres`. The regular ik / jij forms come from dropping
// the final -t; these are the irregular ones that dropping can't give. Upgrade path: a `forms` list on LexEntry.
private val IRREGULAR_PRESENT = mapOf(
    "zijn" to listOf("ben", "bent"), "hebben" to listOf("heb", "hebt"), "kunnen" to listOf("kunt", "kun"),
    "willen" to listOf("wil", "wilt"), "moeten" to listOf("moet"), "mogen" to listOf("mag"),
    "zullen" to listOf("zal", "zult", "zul"), "gaan" to listOf("ga"), "staan" to listOf("sta"), "slaan" to listOf("sla")
)

private val SUFFIXES = listOf("'s", "etje", "tje", "pje", "kje", "je", "en", "e", "s", "t")
private val VOWELS = "aeiou"

private fun key(s: String) = normalize(s).replace(Regex("[,;:]+$"), "")

/**
 * Finds the lexicon entries behind a word as it appears in a text: *liep* -> lopen, *huizen* -> huis,
 * *grote* -> groot, *belt* -> bellen and opbellen. Used for tap-to-translate in reading texts.
 */
class WordLookup(entries: Collection<LexEntry>) {
    private val index = HashMap<String, MutableList<LexEntry>>()

    init {
        for (e in entries) for (form in formsOf(e)) {
            val list = index.getOrPut(form) { mutableListOf() }
            if (e !in list) list += e
        }
    }

    /** Up to three entries: the exact form if there is one, else the first spelling of a stripped ending that matches. */
    fun lookup(token: String): List<LexEntry> {
        val k = key(token)
        if (k.isEmpty()) return emptyList()
        for (candidate in candidates(k)) {
            val found = index[candidate] ?: continue
            return found.sortedBy { it.pos == "phrase" }.take(MAX_RESULTS)
        }
        return emptyList()
    }

    private fun formsOf(e: LexEntry): List<String> = buildList {
        // A phrase is found by each of its words ("tot ziens" by ziens); a separable verb by its first word only.
        if (e.pos == "phrase") addAll(e.nl.split(' ')) else add(e.nl.removePrefix("zich "))
        listOfNotNull(e.pl, e.dim, e.pres, e.past, e.pp, e.cmp, e.sup).forEach { addAll(it.split('/')) }
        e.pres?.split('/')?.forEach { p -> if (p.endsWith("t") && p.length > 2) add(p.dropLast(1)) }
        IRREGULAR_PRESENT[e.nl]?.let { addAll(it) }
    }.map { key(it.substringBefore(' ')) }.filter { it.isNotEmpty() && it != "-" }
}

/** The word itself, then what is left after dropping a common ending, each with the spelling changes Dutch makes. */
private fun candidates(word: String): List<String> = buildList {
    add(word)
    for (suffix in SUFFIXES) {
        if (!word.endsWith(suffix) || word.length - suffix.length < 2) continue
        val base = word.dropLast(suffix.length)
        add(base)
        if (base.length >= 2 && base.last() == base[base.length - 2] && base.last() !in VOWELS) add(base.dropLast(1)) // witt -> wit
        if (base.length >= 3 && base.last() !in VOWELS && base[base.length - 2] in VOWELS && base[base.length - 3] !in VOWELS) {
            add(base.dropLast(1) + base[base.length - 2] + base.last()) // grot -> groot
        }
        if (base.endsWith("v")) add(base.dropLast(1) + "f") // liev -> lief
        if (base.endsWith("z")) add(base.dropLast(1) + "s") // huiz -> huis
    }
}

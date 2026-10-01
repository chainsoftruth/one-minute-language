package com.example.oneminutelanguage.course

private fun key(s: String) = stripDiacritics(s.lowercase().trim())

/**
 * Dictionary search over `nl`, `en` and every form (`liep` finds `lopen`, `fietsen` finds `fiets`).
 * Exact matches first, then forms, English words, and prefixes; [level] and [topic] filter when set.
 */
fun searchLexicon(entries: Collection<LexEntry>, query: String, level: String? = null, topic: String? = null): List<LexEntry> {
    val q = key(query)
    val scoped = entries.filter { (level == null || it.lvl == level) && (topic == null || it.topic == topic) }
    if (q.isEmpty()) return scoped.sortedBy { key(it.nl) }
    return scoped.mapNotNull { e -> rank(e, q)?.let { it to e } }
        .sortedWith(compareBy({ it.first }, { key(it.second.nl) }))
        .map { it.second }
}

private fun rank(e: LexEntry, q: String): Int? {
    val nl = key(e.nl)
    val en = key(e.en)
    val forms = e.forms().map { key(it) }
    return when {
        nl == q -> 0
        q in forms -> 1
        en == q || q in en.split(Regex("[ /,()]+")) -> 2
        nl.startsWith(q) -> 3
        en.startsWith(q) -> 4
        forms.any { it.startsWith(q) } -> 5
        nl.contains(q) || en.contains(q) -> 6
        else -> null
    }
}

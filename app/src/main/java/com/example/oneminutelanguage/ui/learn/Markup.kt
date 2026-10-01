package com.example.oneminutelanguage.ui.learn

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private val markupPattern = Regex("""\*\*(.+?)\*\*|\*(.+?)\*""")

/** Course text supports `**bold**` and `*italic*` only, so two regexes do instead of a markdown library. */
fun markup(text: String): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (m in markupPattern.findAll(text)) {
        append(text.substring(last, m.range.first))
        val bold = m.groups[1]
        if (bold != null) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold.value) }
        else withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(m.groups[2]!!.value) }
        last = m.range.last + 1
    }
    append(text.substring(last))
}

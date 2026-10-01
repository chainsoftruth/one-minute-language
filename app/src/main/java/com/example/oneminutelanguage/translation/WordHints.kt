package com.example.oneminutelanguage.translation

private val hints = Regex("""\s*\([^)]*\)""")
private val numericHints = Regex("""\s*\([^)]*\d[^)]*\)""")

/** "De patiënt (in het ziekenhuis)" -> "De patiënt" */
fun String.withoutHints() = hints.replace(this, "").trim()

/** "Tweeënzeventig (72)" -> "Tweeënzeventig", other hints stay */
fun String.withoutNumericHints() = numericHints.replace(this, "").trim()

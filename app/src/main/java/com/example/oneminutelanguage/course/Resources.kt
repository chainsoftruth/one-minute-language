package com.example.oneminutelanguage.course

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/** A free outside page for the Resources hub. `verified` is "curl" (checked by script) or "browser" (checked by hand). */
@Serializable data class Resource(
    val title: String, val url: String, val skill: String,
    val levels: List<String> = emptyList(), val lang: String = "en", val note: String = "", val verified: String = "curl"
)

/** The skills in the order the hub shows them, with their headings. */
val RESOURCE_SKILLS = listOf(
    "grammar" to "Grammar", "reading" to "Reading", "listening" to "Listening", "speaking" to "Speaking",
    "writing" to "Writing", "exam" to "Exam practice", "dictionary" to "Dictionaries", "society" to "Society"
)

fun parseResources(json: String, file: String = "resources.json"): List<Resource> =
    try { CourseJson.decodeFromString(json) } catch (e: Exception) { throw IllegalStateException("Cannot parse $file: ${e.message}", e) }

/** The resources for one skill, optionally only those that fit [level] (a resource with no levels fits all). */
fun resourcesFor(all: List<Resource>, skill: String, level: String?): List<Resource> =
    all.filter { it.skill == skill && (level == null || it.levels.isEmpty() || level in it.levels) }

package com.example.oneminutelanguage.course

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import java.util.concurrent.ConcurrentHashMap

// Pure parsers: tests call them on the asset files straight from disk. A failure names the file.
private inline fun <T> parsing(file: String, block: () -> T): T =
    try { block() } catch (e: Exception) { throw IllegalStateException("Cannot parse $file: ${e.message}", e) }

fun parseCourses(json: String, file: String = "index.json"): List<CourseInfo> =
    parsing(file) { CourseJson.decodeFromString(json) }

fun parseOutline(json: String, file: String = "course.json"): CourseOutline =
    parsing(file) { CourseJson.decodeFromString(json) }

fun parseUnit(json: String, file: String = "unit"): CourseUnit =
    parsing(file) { CourseJson.decodeFromString(json) }

fun parseLexicon(json: String, file: String = "lexicon"): List<LexEntry> =
    parsing(file) { CourseJson.decodeFromString(json) }

/** `a1.g04.l1` -> `a1.g04`. */
fun unitIdOf(lessonId: String): String = lessonId.substringBeforeLast('.')

object CourseRepository {
    private const val ROOT = "courses"
    private val cache = ConcurrentHashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> cached(key: String, load: () -> T): T =
        (cache[key] as T?) ?: withContext(Dispatchers.IO) { load() }.also { cache[key] = it }

    private fun Context.readAsset(path: String): String =
        assets.open(path).bufferedReader().use { it.readText() }

    suspend fun courses(context: Context): List<CourseInfo> =
        cached("index") { parseCourses(context.readAsset("$ROOT/index.json")) }

    suspend fun outline(context: Context, courseId: String): CourseOutline =
        cached("outline:$courseId") { parseOutline(context.readAsset("$ROOT/$courseId/course.json"), "$courseId/course.json") }

    /** Null when the unit is listed in course.json but its file doesn't exist yet ("Coming soon"). */
    suspend fun unit(context: Context, courseId: String, unitId: String): CourseUnit? {
        if (!unitExists(context, courseId, unitId)) return null
        return cached("unit:$courseId:$unitId") {
            parseUnit(context.readAsset("$ROOT/$courseId/units/$unitId.json"), "$courseId/units/$unitId.json")
        }
    }

    /** The whole course in order; units without a file come back with `unit = null`. */
    suspend fun path(context: Context, courseId: String): List<PathLevel> =
        outline(context, courseId).levels.map { level ->
            PathLevel(level, level.units.map { PathUnit(it, unit(context, courseId, it)) })
        }

    suspend fun lesson(context: Context, courseId: String, lessonId: String): Lesson? =
        unit(context, courseId, unitIdOf(lessonId))?.lessons?.firstOrNull { it.id == lessonId }

    suspend fun unitExists(context: Context, courseId: String, unitId: String): Boolean =
        unitFiles(context, courseId).contains("$unitId.json")

    private suspend fun unitFiles(context: Context, courseId: String): Set<String> =
        cached("unitfiles:$courseId") {
            context.assets.list("$ROOT/$courseId/units").orEmpty().toSet()
        }

    /** Lexicon id -> entry, from every file in `lexicon/`. */
    suspend fun lexicon(context: Context, courseId: String): Map<String, LexEntry> =
        cached("lexicon:$courseId") {
            val dir = "$ROOT/$courseId/lexicon"
            context.assets.list(dir).orEmpty()
                .flatMap { parseLexicon(context.readAsset("$dir/$it"), "$dir/$it") }
                .associateBy { it.id }
        }
}

package com.example.oneminutelanguage.course

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

@Serializable data class LtReplacement(val value: String)
@Serializable data class LtRule(val issueType: String = "")
@Serializable data class LtMatch(
    val message: String, val offset: Int, val length: Int,
    val replacements: List<LtReplacement> = emptyList(), val rule: LtRule = LtRule()
)
@Serializable data class LtResponse(val matches: List<LtMatch> = emptyList())

sealed interface LtResult {
    data class Ok(val matches: List<LtMatch>) : LtResult
    data object Offline : LtResult
    data object RateLimited : LtResult
    data class Failed(val message: String) : LtResult
}

// The API answers with much more than we read (software, language, sentence ranges).
private val LtJson = Json(from = CourseJson) { ignoreUnknownKeys = true }

fun parseLanguageTool(json: String): List<LtMatch> = LtJson.decodeFromString<LtResponse>(json).matches

/**
 * One match fixed: [text] with the suggestion put in, and the other matches moved to their new offsets.
 * A match that overlaps the fixed one is dropped (its range no longer means anything).
 */
fun applyFix(text: String, matches: List<LtMatch>, fixed: LtMatch, replacement: String): Pair<String, List<LtMatch>> {
    val end = fixed.offset + fixed.length
    val delta = replacement.length - fixed.length
    val rest = matches.filter { it !== fixed && (it.offset + it.length <= fixed.offset || it.offset >= end) }
        .map { if (it.offset >= end) it.copy(offset = it.offset + delta) else it }
    return text.replaceRange(fixed.offset, end, replacement) to rest
}

/**
 * Checks [text] with the public LanguageTool server (free: 20 requests a minute, 20 KB a request).
 * The text leaves the phone, which is why the writing screen asks first.
 */
suspend fun checkWithLanguageTool(text: String, language: String = "nl"): LtResult = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        connection = (URL("https://api.languagetool.org/v2/check").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        val body = "text=${URLEncoder.encode(text, "UTF-8")}&language=$language"
        connection.outputStream.use { it.write(body.toByteArray()) }
        when (val code = connection.responseCode) {
            200 -> LtResult.Ok(parseLanguageTool(connection.inputStream.bufferedReader().use { it.readText() }))
            429 -> LtResult.RateLimited
            else -> LtResult.Failed("LanguageTool answered with error $code.")
        }
    } catch (e: UnknownHostException) {
        LtResult.Offline
    } catch (e: ConnectException) {
        LtResult.Offline
    } catch (e: SocketTimeoutException) {
        LtResult.Offline
    } catch (e: IOException) {
        LtResult.Failed(e.message ?: "The check did not work.")
    } catch (e: Exception) {
        LtResult.Failed("The answer could not be read.")
    } finally {
        connection?.disconnect()
    }
}

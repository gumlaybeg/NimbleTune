package com.gumlaybeg.nimbletune.lyrics

import android.content.Context
import com.gumlaybeg.nimbletune.constants.EnablePaxsenixKey
import com.gumlaybeg.nimbletune.utils.dataStore
import com.gumlaybeg.nimbletune.utils.get
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber
import java.io.StringReader
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.abs

object PaxSenixLyricsProvider : LyricsProvider {
    private const val TAG = "PaxSenixProvider"

    override val name = "Paxsenix"

    override fun isEnabled(context: Context): Boolean {
        val enabled = context.dataStore[EnablePaxsenixKey] ?: true
        if (enabled) {
            PaxsenixApi.init(context)
        }
        return enabled
    }

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> {
        return try {
            PaxsenixApi.getLyrics(title, artist, duration, album)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Exception in getLyrics")
            Result.failure(e)
        }
    }

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
        callback: (String) -> Unit,
    ) {
        try {
            PaxsenixApi.getAllLyrics(title, artist, duration, album, callback)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error fetching lyrics from Paxsenix")
        }
    }
}

private object PaxsenixApi {
    @Volatile
    private var client: HttpClient? = null
    private var appVersion: String = "Unknown"

    fun init(context: Context) {
        if (client != null) return

        synchronized(this) {
            if (client != null) return

            appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    ?: "Unknown"
            } catch (e: Exception) {
                Timber.e(e, "Failed to get app version")
                "Unknown"
            }

            client = HttpClient(OkHttp) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 15000
                    connectTimeoutMillis = 10000
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }

                defaultRequest {
                    url("https://lyrics.paxsenix.org")
                    header("User-Agent", "NimbleTune/$appVersion")
                }

                expectSuccess = true
            }
        }
    }

    private val httpClient: HttpClient
        get() = client ?: throw IllegalStateException("PaxsenixApi.init() must be called before using it")

    private const val APPLE_MUSIC_API_BASE = "https://amp-api.music.apple.com/v1/catalog/us"
    private val appleJson = Json { ignoreUnknownKeys = true }

    @Volatile
    private var appleTokenManager: AppleTokenManager? = null
    private val tokenManager: AppleTokenManager
        get() = appleTokenManager ?: synchronized(this) {
            appleTokenManager ?: AppleTokenManager(httpClient).also { appleTokenManager = it }
        }

    private val titleCleanupPatterns = listOf(
        Regex("""\s*\(.*?(official|video|audio|lyrics|lyric|visualizer|hd|hq|4k|remaster|remix|live|acoustic|version|edit|extended|radio|clean|explicit).*?\)""", RegexOption.IGNORE_CASE),
        Regex("""\s*\[.*?(official|video|audio|lyrics|lyric|visualizer|hd|hq|4k|remaster|remix|live|acoustic|version|edit|extended|radio|clean|explicit).*?\]""", RegexOption.IGNORE_CASE),
        Regex("""\s*【.*?】"""),
        Regex("""\s*\|.*$"""),
        Regex("""\s*-\s*(official|video|audio|lyrics|lyric|visualizer).*$""", RegexOption.IGNORE_CASE),
        Regex("""\s*\(feat\..*?\)""", RegexOption.IGNORE_CASE),
        Regex("""\s*\(ft\..*?\)""", RegexOption.IGNORE_CASE),
        Regex("""\s*feat\..*$""", RegexOption.IGNORE_CASE),
        Regex("""\s*ft\..*$""", RegexOption.IGNORE_CASE),
        Regex("""\s*\([^)]*\d{4}[^)]*\)""", RegexOption.IGNORE_CASE),
    )

    private val artistSeparators = listOf(
        " & ", " and ", ", ", " x ", " X ", " feat. ", " feat ", " ft. ", " ft ", " featuring ", " with "
    )

    private fun cleanTitle(title: String): String {
        var cleaned = title.trim()
        for (pattern in titleCleanupPatterns) {
            cleaned = cleaned.replace(pattern, "")
        }
        return cleaned.trim()
    }

    private fun cleanArtist(artist: String): String {
        var cleaned = artist.trim()
        for (separator in artistSeparators) {
            if (cleaned.contains(separator, ignoreCase = true)) {
                cleaned = cleaned.split(separator, ignoreCase = true, limit = 2)[0]
                break
            }
        }
        return cleaned.trim()
    }

    private suspend fun search(query: String): List<PaxsenixSearchResult> = runCatching {
        val token = tokenManager.getToken()
        return@runCatching searchWithToken(token, query)
    }.getOrElse { e ->
        if (e is ClientRequestException && e.response.status.value == 401) {
            tokenManager.clearToken()
            return@getOrElse runCatching {
                val newToken = tokenManager.getToken()
                searchWithToken(newToken, query)
            }.getOrElse { emptyList() }
        }
        emptyList()
    }

    private suspend fun searchWithToken(token: String, query: String): List<PaxsenixSearchResult> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")

        val response = httpClient.get(
            "$APPLE_MUSIC_API_BASE/search?term=$encodedQuery&types=songs&limit=25&l=en-US&platform=web&format[resources]=map&include[songs]=artists&extend=artistUrl"
        ) {
            header("Authorization", "Bearer $token")
            header("Origin", "https://music.apple.com")
            header("Referer", "https://music.apple.com/")
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:95.0) Gecko/20100101 Firefox/95.0")
            header("Accept", "application/json")
            header("Accept-Language", "en-US,en;q=0.5")
            header("x-apple-renewal", "true")
        }

        val body = try {
            appleJson.decodeFromString<PaxsenixAppleMusicSearchResponse>(response.bodyAsText())
        } catch (e: Exception) {
            return emptyList()
        }

        val songs = body.results.songs?.data ?: return emptyList()

        return songs.mapNotNull { songData ->
            val detail = body.resources?.songs?.get(songData.id) ?: return@mapNotNull null
            val attr = detail.attributes
            PaxsenixSearchResult(
                id = songData.id,
                trackName = attr.name,
                artistName = attr.artistName,
                albumName = attr.albumName,
                duration = attr.durationInMillis?.toInt()?.div(1000),
                artwork = attr.artwork?.url?.replace("{w}", "100")?.replace("{h}", "100")?.replace("{f}", "png")
            )
        }
    }

    suspend fun getLyrics(
        title: String,
        artist: String,
        duration: Int,
        album: String? = null,
    ): Result<String> = runCatching {
        val cleanedTitle = cleanTitle(title)
        val cleanedArtist = cleanArtist(artist)

        val searchQueries = buildList {
            add("$cleanedTitle $cleanedArtist")
            add(cleanedTitle)
            if (!album.isNullOrBlank()) {
                add("$cleanedTitle $cleanedArtist $album")
            }
        }

        var allResults: List<Pair<PaxsenixSearchResult, Double>> = emptyList()

        for (query in searchQueries) {
            if (allResults.isEmpty()) {
                val searchResults = search(query)
                if (searchResults.isNotEmpty()) {
                    allResults = scoreAndFilterResults(searchResults, title, artist, duration)
                }
            }
        }

        if (allResults.isEmpty()) {
            throw IllegalStateException("No tracks found on Paxsenix")
        }

        var bestLyrics: String? = null
        var bestQuality = 0

        for ((result, _) in allResults.take(10)) {
            val lrc = fetchLyricsForTrack(result.id).getOrNull() ?: continue
            if (lrc.isEmpty()) continue

            val quality = getQuality(lrc)
            if (quality > bestQuality) {
                bestQuality = quality
                bestLyrics = lrc
            }
            if (bestQuality == 3) break
        }

        bestLyrics?.let {
            return Result.success(it)
        }

        return Result.failure(IllegalStateException("No lyrics available from Paxsenix"))
    }

    private fun getQuality(lrc: String): Int {
        if (lrc.isBlank()) return 0
        val hasWordTimings = (lrc.contains("<") && lrc.contains(">") && (lrc.contains("|") || lrc.contains(":"))) ||
            lrc.contains(Regex("<\\d{1,2}:\\d{2}\\.\\d{2,3}>"))

        if (hasWordTimings) return 3

        val hasLineTimings = lrc.contains(Regex("\\[\\d\\d:\\d\\d\\.\\d{2,3}\\]")) ||
            lrc.contains(Regex("^\\[bg:.*\\]", RegexOption.MULTILINE))

        return if (hasLineTimings) 2 else 1
    }

    private fun scoreAndFilterResults(
        results: List<PaxsenixSearchResult>,
        title: String,
        artist: String,
        duration: Int,
    ): List<Pair<PaxsenixSearchResult, Double>> {
        val durationMs = duration * 1000
        val cleanupRegex = Regex("""\s*\(.*?\)|\s*\[.*?\]""")

        val cleanedTitle = title.replace(cleanupRegex, "").lowercase().trim()
        val cleanedArtist = cleanArtist(artist).lowercase()

        val targetIsMixed = title.contains("mixed", ignoreCase = true)
        val targetIsRemix = title.contains("remix", ignoreCase = true)

        return results.map { result ->
            var score = 0.0
            val resultTitle = result.displayName
            val resultArtist = result.displayArtist

            result.duration?.let { d ->
                val diff = abs(d - durationMs)
                when {
                    diff <= 2000 -> score += 100
                    diff <= 5000 -> score += 50
                    diff <= 10000 -> score += 10
                    else -> score -= 50
                }
            }

            val resultTitleCleaned = resultTitle.replace(cleanupRegex, "").lowercase().trim()

            when {
                resultTitleCleaned == cleanedTitle -> score += 80
                resultTitleCleaned.contains(cleanedTitle) || cleanedTitle.contains(resultTitleCleaned) -> score += 40
            }

            if (resultTitle.contains("mixed", ignoreCase = true) && !targetIsMixed) score -= 60
            if (resultTitle.contains("remix", ignoreCase = true) && !targetIsRemix) score -= 40

            val resultArtistLower = resultArtist.lowercase()
            when {
                resultArtistLower.contains(cleanedArtist) -> score += 50
                else -> {
                    val artistWords = cleanedArtist.split(Regex("\\s+")).filter { it.length > 2 }
                    if (artistWords.any { resultArtistLower.contains(it) }) {
                        score += 25
                    }
                }
            }

            result to score
        }.sortedByDescending { it.second }.filter { it.second > 0 }.take(10)
    }

    private suspend fun fetchLyricsForTrack(id: String): Result<String> = runCatching {
        val response = httpClient.get("/apple-music/lyrics") {
            parameter("id", id)
        }.body<PaxsenixLyricsResponse>()

        val lyricsType = response.type

        if (!response.ttmlContent.isNullOrBlank()) {
            val lrc = convertTTMLToAppFormat(response.ttmlContent)
            if (lrc.isNotEmpty()) {
                return@runCatching lrc
            }
        }

        if (!response.elrcMultiPerson.isNullOrBlank()) return@runCatching response.elrcMultiPerson
        if (!response.elrc.isNullOrBlank()) return@runCatching response.elrc
        if (!response.plain.isNullOrBlank()) return@runCatching response.plain

        if (response.content.isEmpty()) {
            throw IllegalStateException("No lyrics found")
        }

        val hasWordLevel = lyricsType == "Syllable"
        if (!hasWordLevel) {
            return@runCatching response.content
                .map { line -> line.text.joinToString(" ") { it.text } }
                .filter { it.isNotBlank() }
                .joinToString("\n")
        }

        buildString {
            response.content.forEach { line ->
                val timeMs = line.timestamp
                val minutes = timeMs / 1000 / 60
                val seconds = (timeMs / 1000) % 60
                val centiseconds = (timeMs % 1000) / 10

                val agent = when {
                    line.background -> "{bg}"
                    line.oppositeTurn -> "{agent:v2}"
                    else -> "{agent:v1}"
                }

                val lineText = line.text.joinToString(" ") { it.text }

                if (lineText.isNotBlank()) {
                    appendLine(String.format(Locale.US, "[%02d:%02d.%02d]%s%s", minutes, seconds, centiseconds, agent, lineText))

                    if (line.text.isNotEmpty()) {
                        val wordsData = line.text.joinToString("|") { word ->
                            "${word.text}:${word.timestamp.toDouble() / 1000}:${word.endtime.toDouble() / 1000}"
                        }
                        if (wordsData.isNotEmpty()) {
                            appendLine("<$wordsData>")
                        }
                    }
                }
            }
        }
    }

    suspend fun getAllLyrics(
        title: String,
        artist: String,
        duration: Int,
        album: String? = null,
        callback: (String) -> Unit,
    ) {
        val cleanedTitle = cleanTitle(title)
        val cleanedArtist = cleanArtist(artist)

        val searchQueries = listOf(
            "$cleanedTitle $cleanedArtist",
            cleanedTitle
        )

        var scoredResults: List<Pair<PaxsenixSearchResult, Double>> = emptyList()
        for (query in searchQueries) {
            val results = search(query)
            if (results.isEmpty()) continue
            val filtered = scoreAndFilterResults(results, title, artist, duration)
            if (filtered.isNotEmpty()) {
                scoredResults = filtered
                break
            }
        }

        val collectedLyrics = mutableListOf<Pair<String, Int>>()
        for ((result, _) in scoredResults.take(5)) {
            val lrc = fetchLyricsForTrack(result.id).getOrNull() ?: continue
            if (lrc.isNotEmpty()) {
                val quality = getQuality(lrc)
                collectedLyrics.add(lrc to quality)
                if (quality == 3) break
            }
        }

        collectedLyrics.sortedByDescending { it.second }.forEach { (lrc, _) ->
            callback(lrc)
        }
    }

    private fun convertTTMLToAppFormat(ttml: String): String {
        return try {
            val parsedLines = PaxsenixTTMLParser.parseTTML(ttml)
            PaxsenixTTMLParser.toLRC(parsedLines)
        } catch (e: Exception) {
            Timber.e(e, "TTML conversion failed: ${e.message}")
            ""
        }
    }

    private class AppleTokenManager(private val httpClient: HttpClient) {
        private var cachedToken: String? = null
        private val mutex = Mutex()

        suspend fun getToken(): String = mutex.withLock {
            cachedToken?.let { return it }

            val mainPageBody = httpClient.get("https://beta.music.apple.com").bodyAsText()
            val indexJsUri = Regex("""/assets/index~[^/]+\.js""").find(mainPageBody)?.value
                ?: throw Exception("Could not find index JS URL")

            val indexJsBody = httpClient.get("https://beta.music.apple.com$indexJsUri").bodyAsText()
            val token = Regex("""eyJ[A-Za-z0-9\-_=]+\.[A-Za-z0-9\-_=]+\.[A-Za-z0-9\-_=]+""")
                .find(indexJsBody)?.value
                ?: throw Exception("Could not find token")

            cachedToken = token
            return token
        }

        fun clearToken() {
            cachedToken = null
        }
    }
}

private object PaxsenixTTMLParser {
    data class ParsedWord(
        val text: String,
        val startTimeSec: Double,
        val endTimeSec: Double,
    )

    data class ParsedLine(
        val startTimeMs: Long,
        val text: String,
        val words: List<ParsedWord>,
        val agent: String? = null,
        val isBackground: Boolean = false,
    )

    fun parseTTML(ttml: String): List<ParsedLine> {
        val lines = mutableListOf<ParsedLine>()
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = false }
        val parser = factory.newPullParser()
        parser.setInput(StringReader(ttml))

        var inP = false
        var lineStartMs = 0L
        var lineAgent: String? = null
        var lineBg = false
        var lineTextBuilder = StringBuilder()
        var currentWords = mutableListOf<ParsedWord>()

        var inWordSpan = false
        var inBgSpan = false
        var spanStartSec = 0.0
        var spanEndSec = 0.0
        var spanTextBuilder = StringBuilder()
        var spanDepth = 0

        var bgStartMs = 0L
        var bgTextBuilder = StringBuilder()
        var bgWords = mutableListOf<ParsedWord>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase()) {
                        "p" -> {
                            inP = true
                            lineStartMs = parseTtmlTimeMs(parser.getAttributeValue(null, "begin")) ?: 0L
                            lineAgent = parser.getAttributeValue(null, "ttm:agent")
                                ?: parser.getAttributeValue(null, "agent")
                            val role = parser.getAttributeValue(null, "ttm:role")
                                ?: parser.getAttributeValue(null, "role")
                            lineBg = role == "x-bg"
                            lineTextBuilder = StringBuilder()
                            currentWords = mutableListOf()
                        }
                        "span" -> if (inP) {
                            spanDepth++
                            val role = parser.getAttributeValue(null, "ttm:role")
                                ?: parser.getAttributeValue(null, "role")
                            val begin = parseTtmlTimeSec(parser.getAttributeValue(null, "begin"))
                            val end = parseTtmlTimeSec(parser.getAttributeValue(null, "end"))

                            if (role == "x-bg") {
                                inBgSpan = true
                                bgStartMs = ((begin ?: (lineStartMs / 1000.0)) * 1000.0).toLong()
                                bgTextBuilder = StringBuilder()
                                bgWords = mutableListOf()
                            } else if (begin != null && end != null) {
                                inWordSpan = true
                                spanStartSec = begin
                                spanEndSec = end
                                spanTextBuilder = StringBuilder()
                            }
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    if (inP) {
                        val txt = parser.text ?: ""
                        when {
                            inWordSpan -> spanTextBuilder.append(txt)
                            inBgSpan -> bgTextBuilder.append(txt)
                            else -> lineTextBuilder.append(txt)
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    when (parser.name.lowercase()) {
                        "span" -> if (inP) {
                            if (inWordSpan) {
                                val wordText = spanTextBuilder.toString()
                                if (wordText.isNotBlank()) {
                                    val word = ParsedWord(wordText.trim(), spanStartSec, spanEndSec)
                                    if (inBgSpan) {
                                        bgWords.add(word)
                                        bgTextBuilder.append(wordText)
                                    } else {
                                        currentWords.add(word)
                                        lineTextBuilder.append(wordText)
                                    }
                                } else {
                                    if (inBgSpan) bgTextBuilder.append(wordText) else lineTextBuilder.append(wordText)
                                }
                                inWordSpan = false
                            } else if (inBgSpan && spanDepth == 1) {
                                val bgText = bgTextBuilder.toString().trim()
                                if (bgText.isNotEmpty()) {
                                    lines.add(
                                        ParsedLine(
                                            startTimeMs = bgStartMs,
                                            text = bgText,
                                            words = bgWords.toList(),
                                            agent = lineAgent,
                                            isBackground = true
                                        )
                                    )
                                }
                                inBgSpan = false
                            }
                            spanDepth = (spanDepth - 1).coerceAtLeast(0)
                        }

                        "p" -> if (inP) {
                            val text = lineTextBuilder.toString().trim()
                            if (text.isNotEmpty()) {
                                lines.add(
                                    ParsedLine(
                                        startTimeMs = lineStartMs,
                                        text = text,
                                        words = currentWords.toList(),
                                        agent = lineAgent,
                                        isBackground = lineBg
                                    )
                                )
                            }
                            inP = false
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return lines.sortedBy { it.startTimeMs }
    }

    fun toLRC(lines: List<ParsedLine>): String {
        if (lines.isEmpty()) return ""
        return buildString {
            lines.forEach { line ->
                val minutes = line.startTimeMs / 1000 / 60
                val seconds = (line.startTimeMs / 1000) % 60
                val centiseconds = (line.startTimeMs % 1000) / 10
                val marker = buildString {
                    if (line.isBackground) append("{bg}")
                    if (!line.agent.isNullOrBlank()) append("{agent:${line.agent}}")
                }
                appendLine(String.format(Locale.US, "[%02d:%02d.%02d]%s%s", minutes, seconds, centiseconds, marker, line.text))
                if (line.words.isNotEmpty()) {
                    val wordsData = line.words.joinToString("|") { w ->
                        "${w.text}:${w.startTimeSec}:${w.endTimeSec}"
                    }
                    appendLine("<$wordsData>")
                }
            }
        }
    }

    private fun parseTtmlTimeSec(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        if (trimmed.endsWith("s")) {
            return trimmed.removeSuffix("s").toDoubleOrNull()
        }
        val parts = trimmed.split(":")
        return when (parts.size) {
            1 -> parts[0].toDoubleOrNull()
            2 -> {
                val m = parts[0].toDoubleOrNull() ?: return null
                val s = parts[1].toDoubleOrNull() ?: return null
                m * 60.0 + s
            }
            3 -> {
                val h = parts[0].toDoubleOrNull() ?: return null
                val m = parts[1].toDoubleOrNull() ?: return null
                val s = parts[2].toDoubleOrNull() ?: return null
                h * 3600.0 + m * 60.0 + s
            }
            else -> null
        }
    }

    private fun parseTtmlTimeMs(raw: String?): Long? =
        parseTtmlTimeSec(raw)?.let { (it * 1000.0).toLong() }
}

@Serializable
private data class PaxsenixSearchResult(
    val id: String,
    val songName: String? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Int? = null,
    val artwork: String? = null,
) {
    val displayName: String get() = trackName ?: songName ?: ""
    val displayArtist: String get() = artistName ?: ""
}

@Serializable
private data class PaxsenixLyricsContent(
    val timestamp: Long,
    val endtime: Long,
    val duration: Long,
    val structure: String? = null,
    val text: List<PaxsenixLyricText> = emptyList(),
    val background: Boolean = false,
    val backgroundText: List<PaxsenixLyricText> = emptyList(),
    val oppositeTurn: Boolean = false,
)

@Serializable
private data class PaxsenixLyricText(
    val text: String,
    val timestamp: Long,
    val endtime: Long,
    val duration: Long,
    val part: Boolean = false,
)

@Serializable
private data class PaxsenixLyricsMetadata(
    val songwriters: List<String> = emptyList(),
)

@Serializable
private data class PaxsenixLyricsResponse(
    val type: String? = null,
    val metadata: PaxsenixLyricsMetadata? = null,
    val content: List<PaxsenixLyricsContent> = emptyList(),
    val elrc: String? = null,
    val elrcMultiPerson: String? = null,
    val ttmlContent: String? = null,
    val plain: String? = null,
)

@Serializable
private data class PaxsenixAppleMusicSearchResponse(
    val results: PaxsenixAppleMusicResults,
    val resources: PaxsenixAppleMusicResources? = null,
)

@Serializable
private data class PaxsenixAppleMusicResults(
    val songs: PaxsenixAppleMusicSongsResult? = null,
)

@Serializable
private data class PaxsenixAppleMusicSongsResult(
    val data: List<PaxsenixAppleMusicSongData> = emptyList(),
)

@Serializable
private data class PaxsenixAppleMusicSongData(
    val id: String,
    val type: String,
)

@Serializable
private data class PaxsenixAppleMusicResources(
    val songs: Map<String, PaxsenixAppleMusicSongDetail>? = null,
)

@Serializable
private data class PaxsenixAppleMusicSongDetail(
    val attributes: PaxsenixAppleMusicSongAttributes,
)

@Serializable
private data class PaxsenixAppleMusicSongAttributes(
    val name: String,
    val artistName: String,
    val albumName: String? = null,
    val artwork: PaxsenixAppleMusicArtwork? = null,
    val url: String? = null,
    val durationInMillis: Long? = null,
)

@Serializable
private data class PaxsenixAppleMusicArtwork(
    val url: String,
)
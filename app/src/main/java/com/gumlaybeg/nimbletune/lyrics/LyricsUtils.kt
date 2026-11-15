package com.gumlaybeg.nimbletune.lyrics

import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.VisibleForTesting
import androidx.media3.common.Metadata
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.BinaryFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import com.gumlaybeg.nimbletune.lyrics.SemanticLyrics.Word
import com.gumlaybeg.nimbletune.ui.component.animateScrollDuration
import java.io.File
import java.nio.charset.Charset
import kotlin.math.min

object LyricsUtils {

    private const val TAG = "LrcUtils"

    enum class SyncType {
        NONE,
        LINE,
        WORD,
    }

    data class LrcParserOptions(val trim: Boolean, val multiLine: Boolean, val errorText: String)

    private val LINE_TAG_REGEX = "\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]".toRegex()
    private val WORD_ELRC_REGEX = "<(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?>([^<]*)".toRegex()
    private val AGENT_TAG_REGEX = "\\{agent:([^\\}]+)\\}".toRegex()

    fun getSyncType(lyrics: String): SyncType {
        if (lyrics.isBlank()) return SyncType.NONE
        val hasWordTimings = (lyrics.contains("<") && lyrics.contains(">") && (lyrics.contains("|") || lyrics.contains(":"))) ||
            lyrics.contains(Regex("<\\d{1,2}:\\d{2}[.:]\\d{2,3}>"))
        if (hasWordTimings) return SyncType.WORD
        if (LINE_TAG_REGEX.containsMatchIn(lyrics)) return SyncType.LINE
        return SyncType.NONE
    }

    fun parseLyrics(lyrics: String): List<LyricsEntry> {
        if (lyrics.isBlank()) return emptyList()

        val rawLines = lyrics.lines()
        val entries = mutableListOf<LyricsEntry>()
        var offsetMs = 0L
        var i = 0

        while (i < rawLines.size) {
            val rawLine = rawLines[i].trim()
            if (rawLine.isEmpty()) {
                i++
                continue
            }

            // Handle [offset:...] metadata
            if (rawLine.startsWith("[offset:", ignoreCase = true) && rawLine.endsWith("]")) {
                offsetMs = -(rawLine.substringAfter(":").removeSuffix("]").trim().toLongOrNull() ?: 0L)
                i++
                continue
            }

            val lineMatches = LINE_TAG_REGEX.findAll(rawLine).toList()
            if (lineMatches.isEmpty()) {
                i++
                continue
            }

            var body = rawLine.substring(lineMatches.last().range.last + 1).trimStart()
            var isBackground = false
            var agent: String? = null

            if (body.contains("{bg}")) {
                isBackground = true
                body = body.replace("{bg}", "")
            }

            AGENT_TAG_REGEX.find(body)?.let { match ->
                agent = match.groupValues[1]
                body = body.replace(match.value, "")
            }

            // Support Walaoke / iTunes prefixes if present
            when {
                body.startsWith("v1:", ignoreCase = true) -> {
                    agent = agent ?: "v1"
                    body = body.substring(3).trimStart()
                }
                body.startsWith("v2:", ignoreCase = true) -> {
                    agent = agent ?: "v2"
                    body = body.substring(3).trimStart()
                }
            }

            // Check if next line is Paxsenix <word:start:end|...> metadata
            var paxWords: List<WordTimestamp>? = null
            if (i + 1 < rawLines.size) {
                val nextTrimmed = rawLines[i + 1].trim()
                if (nextTrimmed.startsWith("<") && nextTrimmed.endsWith(">") && nextTrimmed.contains("|")) {
                    paxWords = parsePaxsenixWordsLine(nextTrimmed, offsetMs)
                    i++
                } else if (nextTrimmed.startsWith("<") && nextTrimmed.endsWith(">") && nextTrimmed.count { it == ':' } >= 2 && !WORD_ELRC_REGEX.containsMatchIn(nextTrimmed)) {
                    paxWords = parsePaxsenixWordsLine(nextTrimmed, offsetMs)
                    i++
                }
            }

            // Check if body contains inline ELRC word timestamps <mm:ss.xxx>word
            val elrcMatches = WORD_ELRC_REGEX.findAll(body).toList()
            var finalMainText = body
            var finalWords: List<WordTimestamp>? = paxWords

            if (elrcMatches.isNotEmpty()) {
                val textBuilder = StringBuilder()
                val parsedWords = mutableListOf<WordTimestamp>()
                elrcMatches.forEachIndexed { idx, match ->
                    val startSec = parseTimestampMs(match.groupValues[1], match.groupValues[2], match.groupValues[3]) + offsetMs
                    val tokenText = match.groupValues[4]
                    textBuilder.append(tokenText)
                    val cleanWord = tokenText.trim()
                    if (cleanWord.isNotEmpty()) {
                        val nextStartMs = elrcMatches.getOrNull(idx + 1)?.let { nextMatch ->
                            parseTimestampMs(nextMatch.groupValues[1], nextMatch.groupValues[2], nextMatch.groupValues[3]) + offsetMs
                        }
                        val startD = startSec.coerceAtLeast(0L) / 1000.0
                        val endD = ((nextStartMs ?: (startSec + 600L)).coerceAtLeast(startSec + 50L)) / 1000.0
                        parsedWords.add(WordTimestamp(cleanWord, startD, endD))
                    }
                }
                finalMainText = textBuilder.toString().trim()
                if (parsedWords.isNotEmpty()) {
                    finalWords = parsedWords
                }
            }

            lineMatches.forEach { match ->
                val ts = (parseTimestampMs(match.groupValues[1], match.groupValues[2], match.groupValues[3]) + offsetMs)
                    .coerceAtLeast(0L)
                entries.add(
                    LyricsEntry(
                        time = ts,
                        text = finalMainText.trim(),
                        words = finalWords,
                        agent = agent,
                        isBackground = isBackground,
                    )
                )
            }

            i++
        }

        entries.sortBy { it.time }

        // Mark duplicate timestamps as translations for legacy modes
        var prevTs: Long? = null
        entries.forEach { entry ->
            entry.isTranslation = (entry.time == prevTs)
            prevTs = entry.time
        }

        return entries
    }

    private fun parsePaxsenixWordsLine(line: String, offsetMs: Long): List<WordTimestamp>? {
        val inner = line.removePrefix("<").removeSuffix(">")
        if (inner.isBlank()) return null
        val offsetSec = offsetMs / 1000.0
        val result = mutableListOf<WordTimestamp>()
        inner.split("|").forEach { item ->
            val lastColon = item.lastIndexOf(':')
            if (lastColon <= 0) return@forEach
            val secondLastColon = item.lastIndexOf(':', lastColon - 1)
            if (secondLastColon <= 0) return@forEach

            val wordText = item.substring(0, secondLastColon).trim()
            val startSec = item.substring(secondLastColon + 1, lastColon).toDoubleOrNull() ?: return@forEach
            val endSec = item.substring(lastColon + 1).toDoubleOrNull() ?: return@forEach

            if (wordText.isNotEmpty()) {
                result.add(
                    WordTimestamp(
                        text = wordText,
                        startTime = (startSec + offsetSec).coerceAtLeast(0.0),
                        endTime = (endSec + offsetSec).coerceAtLeast(startSec + offsetSec + 0.05)
                    )
                )
            }
        }
        return result.ifEmpty { null }
    }

    private fun parseTimestampMs(minStr: String, secStr: String, fracStr: String): Long {
        val minutes = minStr.toLongOrNull() ?: 0L
        val seconds = secStr.toLongOrNull() ?: 0L
        val millis = when (fracStr.length) {
            0 -> 0L
            1 -> (fracStr.toLongOrNull() ?: 0L) * 100L
            2 -> (fracStr.toLongOrNull() ?: 0L) * 10L
            else -> fracStr.take(3).toLongOrNull() ?: 0L
        }
        return minutes * 60_000L + seconds * 1_000L + millis
    }

    fun findActiveLineIndices(lines: List<LyricsEntry>, position: Long): Set<Int> {
        if (lines.isEmpty()) return emptySet()
        val active = mutableSetOf<Int>()

        for (i in lines.indices) {
            val entry = lines[i]
            if (entry.text.isBlank()) continue

            val words = entry.words
            if (!words.isNullOrEmpty()) {
                val startMs = minOf(entry.time, (words.first().startTime * 1000.0).toLong())
                val endMs = (words.last().endTime * 1000.0).toLong().coerceAtLeast(startMs + 200L)
                if (position in startMs..endMs) {
                    active.add(i)
                }
            } else {
                var nextMainTime = Long.MAX_VALUE
                for (j in i + 1 until lines.size) {
                    if (lines[j].text.isNotBlank() && !lines[j].isBackground && lines[j].time > entry.time) {
                        nextMainTime = lines[j].time
                        break
                    }
                }
                val endMs = if (nextMainTime == Long.MAX_VALUE) entry.time + 5000L else nextMainTime
                if (position >= entry.time && position < endMs) {
                    active.add(i)
                }
            }
        }

        return active
    }

    fun findCurrentLineIndex(lines: List<LyricsEntry>, position: Long): Int {
        for (index in lines.indices) {
            if (lines[index].time >= position + animateScrollDuration) {
                return index - 1
            }
        }
        return lines.lastIndex
    }

    // ---- Existing Semantic / Legacy Helpers Preserved ----

    @VisibleForTesting
    fun parseLyrics(lyrics: String, parserOptions: LrcParserOptions): SemanticLyrics? {
        return (try {
            parseTtml(lyrics, parserOptions.trim)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            SemanticLyrics.UnsyncedLyrics(listOf(parserOptions.errorText))
        } ?: try {
            parseSrt(lyrics, parserOptions.trim)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            SemanticLyrics.UnsyncedLyrics(listOf(parserOptions.errorText))
        } ?: try {
            parseLrc(lyrics, parserOptions.trim, parserOptions.multiLine)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            SemanticLyrics.UnsyncedLyrics(listOf(parserOptions.errorText))
        })?.let {
            if (it is SemanticLyrics.SyncedLyrics) splitBidirectionalWords(it) else it
        }
    }

    @OptIn(UnstableApi::class)
    fun extractAndParseLyrics(
        metadata: Metadata,
        parserOptions: LrcParserOptions
    ): SemanticLyrics? {
        for (i in 0..<metadata.length()) {
            val meta = metadata.get(i)
            val plainTextData =
                if (meta is VorbisComment && meta.key == "LYRICS") meta.value
                else if (meta is BinaryFrame && (meta.id == "USLT" || meta.id == "SYLT")) UsltFrameDecoder.decode(ParsableByteArray(meta.data))
                else if (meta is TextInformationFrame && (meta.id == "USLT" || meta.id == "SYLT")) meta.values.joinToString("\n")
                else null
            return plainTextData?.let { parseLyrics(it, parserOptions) } ?: continue
        }
        return null
    }

    @OptIn(UnstableApi::class)
    fun loadAndParseLyricsString(lyrics: String, parserOptions: LrcParserOptions): List<LyricsEntry> {
        return emptyList<LyricsEntry>() + parseLyrics(lyrics, parserOptions).convertForLegacy()
    }

    @OptIn(UnstableApi::class)
    fun loadAndParseLyricsFile(musicFile: File?, parserOptions: LrcParserOptions): SemanticLyrics? {
        val lrcFile = musicFile?.let { File(it.parentFile, it.nameWithoutExtension + ".lrc") }
        return loadTextFile(lrcFile, parserOptions.errorText)?.let {
            parseLyrics(it, parserOptions)
        }
    }

    private fun loadTextFile(lrcFile: File?, errorText: String?): String? {
        return try {
            if (lrcFile?.exists() == true) lrcFile.readBytes().toString(Charset.defaultCharset())
            else null
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            return errorText
        }
    }

    private fun splitBidirectionalWords(syncedLyrics: SemanticLyrics.SyncedLyrics): SemanticLyrics.SyncedLyrics {
        return SemanticLyrics.SyncedLyrics(syncedLyrics.text.map { line ->
            if (line.lyric.words.isNullOrEmpty()) return@map line
            val bidirectionalBarriers = findBidirectionalBarriers(line.lyric.text)
            val wordsWithBarriers = line.lyric.words.toMutableList()
            var lastWasRtl = false
            bidirectionalBarriers.forEach { barrier ->
                val evilWordIndex =
                    if (barrier.first == -1) -1 else wordsWithBarriers.indexOfFirst {
                        it.charRange.contains(barrier.first) && it.charRange.start != barrier.first
                    }
                if (evilWordIndex == -1) {
                    val wordIndex = if (barrier.first == -1) 0 else wordsWithBarriers.indexOfFirst { it.charRange.start == barrier.first }
                    wordsWithBarriers.replaceAllSupport(skipFirst = wordIndex) {
                        if (it.isRtl != barrier.second) it.copy(isRtl = barrier.second) else it
                    }
                    lastWasRtl = barrier.second
                    return@forEach
                }
                val evilWord = wordsWithBarriers[evilWordIndex]
                val barrierTime = min(evilWord.timeRange.first + ((line.lyric.words.map {
                    it.timeRange.count() / it.charRange.count().toFloat()
                }.average().let { if (it.isNaN()) 100.0 else it } * (barrier.first - evilWord.charRange.first))).toULong(), evilWord.timeRange.last - 1uL)
                val firstPart = Word(
                    charRange = evilWord.charRange.first..<barrier.first,
                    timeRange = evilWord.timeRange.first..<barrierTime, isRtl = lastWasRtl
                )
                val secondPart = Word(
                    charRange = barrier.first..evilWord.charRange.last,
                    timeRange = barrierTime..evilWord.timeRange.last, isRtl = barrier.second
                )
                wordsWithBarriers[evilWordIndex] = firstPart
                wordsWithBarriers.add(evilWordIndex + 1, secondPart)
                lastWasRtl = barrier.second
            }
            line.copy(line.lyric.copy(words = wordsWithBarriers))
        })
    }

    private val ltr = arrayOf(
        Character.DIRECTIONALITY_LEFT_TO_RIGHT,
        Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
        Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE
    )
    private val rtl = arrayOf(
        Character.DIRECTIONALITY_RIGHT_TO_LEFT,
        Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
        Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
        Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
    )

    private fun findBidirectionalBarriers(text: String): List<Pair<Int, Boolean>> {
        val barriers = mutableListOf<Pair<Int, Boolean>>()
        if (text.isEmpty()) return barriers
        var previousDirection = text.find {
            val dir = Character.getDirectionality(it)
            dir in ltr || dir in rtl
        }?.let { Character.getDirectionality(it) in rtl } == true
        barriers.add(Pair(-1, previousDirection))
        for (i in 0 until text.length) {
            val currentDirection = Character.getDirectionality(text[i])
            val isRtl = currentDirection in rtl
            if (currentDirection !in ltr && !isRtl) continue
            if (previousDirection != isRtl) barriers.add(Pair(i, isRtl))
            previousDirection = isRtl
        }
        return barriers
    }
}

inline fun <reified T> MutableList<T>.replaceAllSupport(skipFirst: Int = 0, operator: (T) -> T) {
    val li = listIterator()
    var skip = skipFirst
    while (skip-- > 0) li.next()
    while (li.hasNext()) li.set(operator(li.next()))
}

@OptIn(UnstableApi::class)
private class UsltFrameDecoder {
    companion object {
        private const val ID3_TEXT_ENCODING_ISO_8859_1 = 0
        private const val ID3_TEXT_ENCODING_UTF_16 = 1
        private const val ID3_TEXT_ENCODING_UTF_16BE = 2
        private const val ID3_TEXT_ENCODING_UTF_8 = 3

        fun decode(id3Data: ParsableByteArray): String? {
            if (id3Data.limit() < 4) return null
            val encoding = id3Data.readUnsignedByte()
            val charset = getCharsetName(encoding)
            val lang = ByteArray(3)
            id3Data.readBytes(lang, 0, 3)
            val rest = ByteArray(id3Data.limit() - 4)
            id3Data.readBytes(rest, 0, id3Data.limit() - 4)
            val descriptionEndIndex = indexOfEos(rest, 0, encoding)
            val textStartIndex = descriptionEndIndex + delimiterLength(encoding)
            val textEndIndex = indexOfEos(rest, textStartIndex, encoding)
            return decodeStringIfValid(rest, textStartIndex, textEndIndex, charset)
        }

        private fun getCharsetName(encodingByte: Int): Charset {
            val name = when (encodingByte) {
                ID3_TEXT_ENCODING_UTF_16 -> "UTF-16"
                ID3_TEXT_ENCODING_UTF_16BE -> "UTF-16BE"
                ID3_TEXT_ENCODING_UTF_8 -> "UTF-8"
                else -> "ISO-8859-1"
            }
            return Charset.forName(name)
        }

        private fun indexOfEos(data: ByteArray, fromIndex: Int, encoding: Int): Int {
            var terminationPos = indexOfZeroByte(data, fromIndex)
            if (encoding == ID3_TEXT_ENCODING_ISO_8859_1 || encoding == ID3_TEXT_ENCODING_UTF_8) {
                return terminationPos
            }
            while (terminationPos < data.size - 1) {
                if (terminationPos % 2 == 0 && data[terminationPos + 1] == 0.toByte()) {
                    return terminationPos
                }
                terminationPos = indexOfZeroByte(data, terminationPos + 1)
            }
            return data.size
        }

        private fun indexOfZeroByte(data: ByteArray, fromIndex: Int): Int {
            for (i in fromIndex until data.size) if (data[i] == 0.toByte()) return i
            return data.size
        }

        private fun delimiterLength(encodingByte: Int): Int =
            if (encodingByte == ID3_TEXT_ENCODING_ISO_8859_1 || encodingByte == ID3_TEXT_ENCODING_UTF_8) 1 else 2

        private fun decodeStringIfValid(data: ByteArray, from: Int, to: Int, charset: Charset): String =
            if (to <= from || to > data.size) "" else String(data, from, to - from, charset)
    }
}

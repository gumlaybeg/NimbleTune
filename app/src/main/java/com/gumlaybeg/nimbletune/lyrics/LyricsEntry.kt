package com.gumlaybeg.nimbletune.lyrics

import kotlinx.coroutines.flow.MutableStateFlow

data class WordTimestamp(
    val text: String,
    val startTime: Double,
    val endTime: Double,
)

data class LyricsEntry(
    val time: Long,
    val text: String,
    val words: List<WordTimestamp>? = null,
    val romanizedTextFlow: MutableStateFlow<String?> = MutableStateFlow(null),
    val translatedTextFlow: MutableStateFlow<String?> = MutableStateFlow(null),
    val agent: String? = null,
    val isBackground: Boolean = false,
    var isTranslation: Boolean = false,
) : Comparable<LyricsEntry> {
    constructor(
        timeStamp: Long,
        content: String,
        isTranslation: Boolean,
    ) : this(
        time = timeStamp,
        text = content,
        isTranslation = isTranslation,
    )

    val timeStamp: Long get() = time
    val content: String get() = text

    override fun compareTo(other: LyricsEntry): Int = (time - other.time).toInt()

    companion object {
        val HEAD_LYRICS_ENTRY = LyricsEntry(0L, "")
    }
}
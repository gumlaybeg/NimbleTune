package com.gumlaybeg.nimbletune.lyrics

import android.content.Context
import java.nio.file.Files
import java.nio.file.Paths

object LocalLyricsProvider : LyricsProvider {
    override val name = "Local LRC"
    override fun isEnabled(context: Context) = true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> = runCatching {
        String(
            Files.readAllBytes(
                Paths.get(title.substringBeforeLast('.') + ".lrc")
            )
        )
    }
}
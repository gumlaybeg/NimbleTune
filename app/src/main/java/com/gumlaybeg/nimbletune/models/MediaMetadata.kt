package com.gumlaybeg.nimbletune.models

import androidx.compose.runtime.Immutable
import com.gumlaybeg.nimbletune.db.entities.*
import com.gumlaybeg.nimbletune.ui.utils.resize
import com.music.innertube.models.SongItem
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset

@Immutable
data class MediaMetadata(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val duration: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
    val genre: List<Genre>?,
    val year: Int? = null,
    private val date: LocalDateTime? = null,
    private val dateModified: LocalDateTime? = null,
    val inLibrary: LocalDateTime? = null,
    val setVideoId: String? = null,
    val isLocal: Boolean = false,
    val localPath: String? = null,
    val liked: Boolean = false,
    val composeUidWorkaround: Double = Math.random(),
    var shuffleIndex: Int = -1
) : Serializable {
    data class Artist(
        val id: String?,
        val name: String,
        val isLocal: Boolean = false,
    ) : Serializable

    data class Album(
        val id: String,
        val title: String,
        val isLocal: Boolean = false,
    ) : Serializable

    data class Genre(
        val id: String?,
        val title: String,
        val isLocal: Boolean = false,
    ) : Serializable

    fun toSongEntity() = SongEntity(
        id = id,
        title = title,
        duration = duration,
        thumbnailUrl = thumbnailUrl,
        albumId = album?.id,
        albumName = album?.title,
        year = year,
        date = date,
        dateModified = dateModified,
        liked = liked,
        isLocal = isLocal,
        inLibrary = if (isLocal) LocalDateTime.now() else null,
        localPath = localPath
    )

    fun getDateString(): String? {
        return date?.toLocalDate()?.toString()
            ?: if (year != null) {
                year.toString()
            } else {
                null
            }
    }

    fun getDateModifiedString(): String? {
        return dateModified?.toLocalDate()?.toString()
    }

    fun getDateLong(): Long? = date?.toEpochSecond(ZoneOffset.UTC)

    fun getDateModifiedLong(): Long? = dateModified?.toEpochSecond(ZoneOffset.UTC)
}

fun Song.toMediaMetadata() = MediaMetadata(
    id = song.id,
    title = song.title,
    artists = artists.map {
        MediaMetadata.Artist(
            id = it.id,
            name = it.name,
            isLocal = it.isLocal
        )
    },
    duration = song.duration,
    thumbnailUrl = song.thumbnailUrl,
    album = album?.let {
        MediaMetadata.Album(
            id = it.id,
            title = it.title,
            isLocal = it.isLocal
        )
    } ?: song.albumId?.let { albumId ->
        MediaMetadata.Album(
            id = albumId,
            title = song.albumName.orEmpty(),
        )
    },
    genre = genre?.map {
        MediaMetadata.Genre(
            id = it.id,
            title = it.title,
            isLocal = it.isLocal
        )
    },
    year = song.year,
    date = song.date,
    dateModified = song.dateModified,
    inLibrary = song.inLibrary,
    liked = song.liked,
    isLocal = song.isLocal,
    localPath = song.localPath
)

fun SongItem.toMediaMetadata() = MediaMetadata(
    id = id,
    title = title,
    artists = artists.map {
        MediaMetadata.Artist(
            id = it.id,
            name = it.name
        )
    },
    duration = duration ?: -1,
    thumbnailUrl = thumbnail.resize(544, 544),
    album = album?.let {
        MediaMetadata.Album(
            id = it.id,
            title = it.name
        )
    },
    genre = null,
    setVideoId = setVideoId
)
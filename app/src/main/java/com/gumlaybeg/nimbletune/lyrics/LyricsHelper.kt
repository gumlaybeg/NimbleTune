package com.gumlaybeg.nimbletune.lyrics

import android.content.Context
import android.util.LruCache
import com.gumlaybeg.nimbletune.constants.LyricSourcePrefKey
import com.gumlaybeg.nimbletune.db.MusicDatabase
import com.gumlaybeg.nimbletune.db.entities.LyricsEntity
import com.gumlaybeg.nimbletune.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.gumlaybeg.nimbletune.models.MediaMetadata
import com.gumlaybeg.nimbletune.utils.dataStore
import com.gumlaybeg.nimbletune.utils.get
import com.gumlaybeg.nimbletune.utils.reportException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val lyricsProviders = listOf(
        PaxSenixLyricsProvider,
        YouLyPlusLyricsProvider,
        LrcLibLyricsProvider,
        KuGouLyricsProvider,
        YouTubeSubtitleLyricsProvider,
        YouTubeLyricsProvider
    )

    private val cache = LruCache<String, List<LyricsResult>>(MAX_CACHE_SIZE)
    private val helperScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeFetches = mutableMapOf<String, Deferred<LyricsWithProvider>>()
    private val fetchesMutex = Mutex()

    private val _lyricsProvidersMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val lyricsProvidersFlow: StateFlow<Map<String, String>> = _lyricsProvidersMap.asStateFlow()

    suspend fun getLyrics(mediaMetadata: MediaMetadata, database: MusicDatabase? = null): String {
        val prefLocal = context.dataStore.get(LyricSourcePrefKey, true)

        val cached = cache.get(mediaMetadata.id)?.firstOrNull()
        if (cached != null) {
            recordProvider(mediaMetadata.id, cached.providerName)
            return cached.lyrics
        }

        val dbLyrics = database?.lyrics(mediaMetadata.id)?.first()?.lyrics
        if (dbLyrics != null && !prefLocal) {
            return dbLyrics
        }

        val localLyrics = getLocalLyrics(mediaMetadata)

        if (prefLocal) {
            if (localLyrics != null) {
                recordProvider(mediaMetadata.id, LocalLyricsProvider.name)
                return localLyrics
            }
            if (dbLyrics != null) {
                return dbLyrics
            }

            val remote = getRemoteLyricsWithSyncPriority(mediaMetadata)
            if (remote.lyrics != LYRICS_NOT_FOUND) {
                recordProvider(mediaMetadata.id, remote.provider)
                database?.query {
                    upsert(LyricsEntity(id = mediaMetadata.id, lyrics = remote.lyrics))
                }
                return remote.lyrics
            }
        } else {
            val remote = getRemoteLyricsWithSyncPriority(mediaMetadata)
            if (remote.lyrics != LYRICS_NOT_FOUND) {
                recordProvider(mediaMetadata.id, remote.provider)
                database?.query {
                    upsert(LyricsEntity(id = mediaMetadata.id, lyrics = remote.lyrics))
                }
                return remote.lyrics
            } else if (localLyrics != null) {
                recordProvider(mediaMetadata.id, LocalLyricsProvider.name)
                return localLyrics
            }
        }

        database?.query {
            upsert(LyricsEntity(id = mediaMetadata.id, lyrics = LYRICS_NOT_FOUND))
        }
        return LYRICS_NOT_FOUND
    }

    private suspend fun getRemoteLyricsWithSyncPriority(mediaMetadata: MediaMetadata): LyricsWithProvider {
        val cacheKey = mediaMetadata.id
        val deferred = fetchesMutex.withLock {
            activeFetches.getOrPut(cacheKey) {
                helperScope.async {
                    val channel = Channel<Triple<Int, String, String>>(Channel.UNLIMITED)

                    val jobs = lyricsProviders.mapIndexedNotNull { index, provider ->
                        if (provider.isEnabled(context)) {
                            launch {
                                try {
                                    withTimeoutOrNull(4500L) {
                                        val result = provider.getLyrics(
                                            mediaMetadata.id,
                                            mediaMetadata.title,
                                            mediaMetadata.artists.joinToString { it.name },
                                            mediaMetadata.duration,
                                            mediaMetadata.album?.title,
                                        )
                                        result.onSuccess { lyrics ->
                                            if (lyrics.isNotBlank() && lyrics != LYRICS_NOT_FOUND) {
                                                channel.send(Triple(index, provider.name, lyrics))
                                            }
                                        }.onFailure {
                                            reportException(it)
                                        }
                                    }
                                } catch (e: Exception) {
                                    reportException(e)
                                }
                            }
                        } else null
                    }

                    launch {
                        jobs.joinAll()
                        channel.close()
                    }

                    var bestLyrics = LYRICS_NOT_FOUND
                    var bestProvider = "Unknown"
                    var bestSyncType = LyricsUtils.SyncType.NONE
                    var bestIndex = Int.MAX_VALUE

                    try {
                        var deadline = System.currentTimeMillis() + 5500L
                        while (System.currentTimeMillis() < deadline) {
                            val remainingTime = (deadline - System.currentTimeMillis()).coerceAtLeast(1L)
                            val item = withTimeoutOrNull(remainingTime) {
                                channel.receiveCatching()
                            } ?: break

                            val triple = item.getOrNull() ?: break
                            val (index, providerName, lyrics) = triple

                            val syncType = runCatching { LyricsUtils.getSyncType(lyrics) }
                                .getOrDefault(LyricsUtils.SyncType.NONE)

                            if (syncType == LyricsUtils.SyncType.WORD) {
                                if (bestSyncType != LyricsUtils.SyncType.WORD || index < bestIndex) {
                                    bestLyrics = lyrics
                                    bestProvider = providerName
                                    bestSyncType = syncType
                                    bestIndex = index
                                }
                                if (index == 0) break
                                deadline = minOf(deadline, System.currentTimeMillis() + 400L)
                            } else if (syncType == LyricsUtils.SyncType.LINE) {
                                val isBetter = bestSyncType == LyricsUtils.SyncType.NONE ||
                                    (bestSyncType == LyricsUtils.SyncType.LINE && index < bestIndex)
                                if (isBetter) {
                                    bestLyrics = lyrics
                                    bestProvider = providerName
                                    bestSyncType = syncType
                                    bestIndex = index
                                }
                                deadline = minOf(
                                    deadline,
                                    System.currentTimeMillis() + if (index == 0) 500L else 800L
                                )
                            } else {
                                if (bestLyrics == LYRICS_NOT_FOUND) {
                                    bestLyrics = lyrics
                                    bestProvider = providerName
                                    bestSyncType = syncType
                                    bestIndex = index
                                    deadline = minOf(deadline, System.currentTimeMillis() + 1200L)
                                } else if (bestSyncType == LyricsUtils.SyncType.NONE && index < bestIndex) {
                                    bestLyrics = lyrics
                                    bestProvider = providerName
                                    bestIndex = index
                                }
                            }
                        }
                    } catch (e: Exception) {
                        reportException(e)
                    } finally {
                        jobs.forEach { it.cancel() }
                        channel.close()
                    }

                    if (bestLyrics != LYRICS_NOT_FOUND) {
                        cache.put(cacheKey, listOf(LyricsResult(bestProvider, bestLyrics)))
                    }
                    LyricsWithProvider(bestLyrics, bestProvider)
                }
            }
        }

        return try {
            deferred.await()
        } finally {
            fetchesMutex.withLock {
                activeFetches.remove(cacheKey)
            }
        }
    }

    private suspend fun getLocalLyrics(mediaMetadata: MediaMetadata): String? {
        if (LocalLyricsProvider.isEnabled(context)) {
            LocalLyricsProvider.getLyrics(
                mediaMetadata.id,
                "" + mediaMetadata.localPath,
                mediaMetadata.artists.joinToString { it.name },
                mediaMetadata.duration,
                mediaMetadata.album?.title
            ).onSuccess { lyrics ->
                return lyrics
            }
        }
        return null
    }

    suspend fun getAllLyrics(
        mediaId: String,
        songTitle: String,
        songArtists: String,
        duration: Int,
        album: String? = null,
        callback: (LyricsResult) -> Unit,
    ) {
        val cacheKey = "$songArtists-$songTitle".replace(" ", "")
        cache.get(cacheKey)?.let { results ->
            results.forEach(callback)
            return
        }
        val allResult = mutableListOf<LyricsResult>()
        lyricsProviders.forEach { provider ->
            if (provider.isEnabled(context)) {
                runCatching {
                    provider.getAllLyrics(mediaId, songTitle, songArtists, duration, album) { lyrics ->
                        val result = LyricsResult(provider.name, lyrics)
                        allResult += result
                        callback(result)
                    }
                }.onFailure { reportException(it) }
            }
        }
        cache.put(cacheKey, allResult)
    }

    private fun recordProvider(mediaId: String, provider: String) {
        _lyricsProvidersMap.update { it + (mediaId to provider) }
    }

    companion object {
        private const val MAX_CACHE_SIZE = 50
    }
}

data class LyricsResult(
    val providerName: String,
    val lyrics: String,
)

data class LyricsWithProvider(
    val lyrics: String,
    val provider: String,
)
package com.gumlaybeg.nimbletune.viewmodels

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gumlaybeg.nimbletune.constants.HideExplicitKey
import com.gumlaybeg.nimbletune.constants.HideVideoSongsKey
import com.gumlaybeg.nimbletune.constants.HideYoutubeShortsKey
import com.gumlaybeg.nimbletune.constants.InnerTubeCookieKey
import com.gumlaybeg.nimbletune.constants.PinnedSpeedDialKey
import com.gumlaybeg.nimbletune.constants.PlaylistFilter
import com.gumlaybeg.nimbletune.constants.PlaylistSortType
import com.gumlaybeg.nimbletune.constants.QuickPicks
import com.gumlaybeg.nimbletune.constants.QuickPicksKey
import com.gumlaybeg.nimbletune.db.MusicDatabase
import com.gumlaybeg.nimbletune.db.entities.Album
import com.gumlaybeg.nimbletune.db.entities.ArtistEntity
import com.gumlaybeg.nimbletune.db.entities.LocalItem
import com.gumlaybeg.nimbletune.db.entities.Song
import com.gumlaybeg.nimbletune.db.entities.SongEntity
import com.gumlaybeg.nimbletune.extensions.isSyncEnabled
import com.gumlaybeg.nimbletune.extensions.toEnum
import com.gumlaybeg.nimbletune.models.SimilarRecommendation
import com.gumlaybeg.nimbletune.models.toMediaMetadata
import com.gumlaybeg.nimbletune.utils.SyncUtils
import com.gumlaybeg.nimbletune.utils.dataStore
import com.gumlaybeg.nimbletune.utils.get
import com.gumlaybeg.nimbletune.utils.reportException
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.Artist
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.BrowseEndpoint
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.models.filterYoutubeShorts
import com.music.innertube.pages.ExplorePage
import com.music.innertube.pages.HomePage
import com.music.innertube.utils.completed
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Collections
import javax.inject.Inject
import kotlin.random.Random

data class DailyDiscoverItem(
    val seed: Song,
    val recommendation: YTItem,
    val relatedEndpoint: BrowseEndpoint?
)

@Serializable
data class PinnedSpeedDialEntry(
    val id: String,
    val title: String,
    val type: String,
    val thumbnail: String? = null,
    val subtitle: String? = null,
    val playlistId: String? = null,
) {
    fun toYTItem(): YTItem = when (type) {
        "ALBUM" -> AlbumItem(
            browseId = id,
            playlistId = playlistId ?: "",
            title = title,
            artists = subtitle?.let { listOf(Artist(name = it, id = null)) },
            thumbnail = thumbnail ?: ""
        )
        "ARTIST" -> ArtistItem(
            id = id,
            title = title,
            thumbnail = thumbnail,
            shuffleEndpoint = null,
            radioEndpoint = null
        )
        "PLAYLIST", "LOCAL_PLAYLIST" -> PlaylistItem(
            id = id,
            title = title,
            author = subtitle?.let { Artist(name = it, id = null) },
            songCountText = null,
            thumbnail = thumbnail,
            playEndpoint = null,
            shuffleEndpoint = null,
            radioEndpoint = null
        )
        else -> SongItem(
            id = id,
            title = title,
            artists = subtitle?.let { listOf(Artist(name = it, id = null)) } ?: emptyList(),
            thumbnail = thumbnail ?: ""
        )
    }

    companion object {
        fun fromYTItem(item: YTItem, typeOverride: String? = null): PinnedSpeedDialEntry = when (item) {
            is SongItem -> PinnedSpeedDialEntry(
                id = item.id,
                title = item.title,
                type = typeOverride ?: "SONG",
                thumbnail = item.thumbnail,
                subtitle = item.artists.joinToString(", ") { it.name }
            )
            is AlbumItem -> PinnedSpeedDialEntry(
                id = item.browseId,
                title = item.title,
                type = typeOverride ?: "ALBUM",
                thumbnail = item.thumbnail,
                subtitle = item.artists?.joinToString(", ") { it.name },
                playlistId = item.playlistId
            )
            is ArtistItem -> PinnedSpeedDialEntry(
                id = item.id,
                title = item.title,
                type = typeOverride ?: "ARTIST",
                thumbnail = item.thumbnail
            )
            is PlaylistItem -> PinnedSpeedDialEntry(
                id = item.id,
                title = item.title,
                type = typeOverride ?: "PLAYLIST",
                thumbnail = item.thumbnail,
                subtitle = item.author?.name
            )
        }
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val database: MusicDatabase,
    val syncUtils: SyncUtils
) : ViewModel() {
    val isRefreshing = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)
    val isRandomizing = MutableStateFlow(false)

    private val quickPicksEnum = context.dataStore.data.map {
        it[QuickPicksKey].toEnum(QuickPicks.QUICK_PICKS)
    }.distinctUntilChanged()

    val quickPicks = MutableStateFlow<List<Song>?>(null)
    val dailyDiscover = MutableStateFlow<List<DailyDiscoverItem>?>(null)
    val forgottenFavorites = MutableStateFlow<List<Song>?>(null)
    val keepListening = MutableStateFlow<List<LocalItem>?>(null)
    val similarRecommendations = MutableStateFlow<List<SimilarRecommendation>?>(null)
    val accountPlaylists = MutableStateFlow<List<PlaylistItem>?>(null)
    val homePage = MutableStateFlow<HomePage?>(null)
    val explorePage = MutableStateFlow<ExplorePage?>(null)
    val communityPlaylists = MutableStateFlow<List<PlaylistItem>?>(null)
    val coversAndRemixes = MutableStateFlow<HomePage.Section?>(null)
    val selectedChip = MutableStateFlow<HomePage.Chip?>(null)
    private val previousHomePage = MutableStateFlow<HomePage?>(null)

    val playlists = database.playlists(PlaylistFilter.LIBRARY, PlaylistSortType.NAME, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val recentActivity = database.recentActivity()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allLocalItems = MutableStateFlow<List<LocalItem>>(emptyList())
    val allYtItems = MutableStateFlow<List<YTItem>>(emptyList())

    val pinnedSpeedDialItems: StateFlow<List<PinnedSpeedDialEntry>> =
        context.dataStore.data
            .map { prefs ->
                val raw = prefs[PinnedSpeedDialKey] ?: return@map emptyList()
                runCatching { Json.decodeFromString<List<PinnedSpeedDialEntry>>(raw) }.getOrDefault(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val speedDialItems: StateFlow<List<YTItem>> =
        combine(
            pinnedSpeedDialItems,
            keepListening,
            quickPicks
        ) { pinned, keepListening, quick ->
            val filled = pinned.map { it.toYTItem() }.toMutableList()
            val targetSize = 27

            if (filled.size < targetSize) {
                keepListening?.let { k ->
                    val needed = targetSize - filled.size
                    val available = k.filter { item ->
                        filled.none { p -> p.id == item.id }
                    }.mapNotNull { item ->
                        when (item) {
                            is Song -> SongItem(
                                id = item.id,
                                title = item.title,
                                artists = item.artists.map { Artist(name = it.name, id = it.id) },
                                thumbnail = item.thumbnailUrl ?: "",
                                explicit = false
                            )
                            is Album -> AlbumItem(
                                browseId = item.id,
                                playlistId = item.album.playlistId ?: "",
                                title = item.title,
                                artists = item.artists.map { Artist(name = it.name, id = it.id) },
                                year = item.album.year,
                                thumbnail = item.thumbnailUrl ?: ""
                            )
                            is com.gumlaybeg.nimbletune.db.entities.Artist -> ArtistItem(
                                id = item.id,
                                title = item.title,
                                thumbnail = item.thumbnailUrl,
                                shuffleEndpoint = null,
                                radioEndpoint = null
                            )
                            else -> null
                        }
                    }
                    filled.addAll(available.take(needed))
                }
            }

            if (filled.size < targetSize) {
                quick?.let { q ->
                    val needed = targetSize - filled.size
                    val available = q.filter { song ->
                        filled.none { p -> p.id == song.id }
                    }.map { song ->
                        SongItem(
                            id = song.id,
                            title = song.title,
                            artists = song.artists.map { Artist(name = it.name, id = it.id) },
                            thumbnail = song.thumbnailUrl ?: "",
                            explicit = false
                        )
                    }
                    filled.addAll(available.take(needed))
                }
            }

            filled.take(targetSize)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val accountName = MutableStateFlow("Guest")
    val accountImageUrl = MutableStateFlow<String?>(null)

    private var lastProcessedCookie: String? = null
    private var isProcessingAccountData = false

    fun togglePin(item: YTItem) {
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                val current = prefs[PinnedSpeedDialKey]?.let {
                    runCatching { Json.decodeFromString<List<PinnedSpeedDialEntry>>(it) }.getOrDefault(emptyList())
                } ?: emptyList()

                val updated = if (current.any { it.id == item.id }) {
                    current.filterNot { it.id == item.id }
                } else {
                    current + PinnedSpeedDialEntry.fromYTItem(item)
                }
                prefs[PinnedSpeedDialKey] = Json.encodeToString(updated)
            }
        }
    }

    suspend fun getRandomItem(): YTItem? {
        return try {
            isRandomizing.value = true
            kotlinx.coroutines.delay(1000)

            val userSongs = mutableListOf<YTItem>()
            val otherSources = mutableListOf<YTItem>()

            quickPicks.value?.let { songs ->
                userSongs.addAll(songs.map { song ->
                    SongItem(
                        id = song.id,
                        title = song.title,
                        artists = song.artists.map { Artist(name = it.name, id = it.id) },
                        thumbnail = song.thumbnailUrl ?: "",
                        explicit = false
                    )
                })
            }

            keepListening.value?.let { items ->
                items.forEach { item ->
                    when (item) {
                        is Song -> userSongs.add(
                            SongItem(
                                id = item.id,
                                title = item.title,
                                artists = item.artists.map { Artist(name = it.name, id = it.id) },
                                thumbnail = item.thumbnailUrl ?: "",
                                explicit = false
                            )
                        )
                        is Album -> otherSources.add(
                            AlbumItem(
                                browseId = item.id,
                                playlistId = item.album.playlistId ?: "",
                                title = item.title,
                                artists = item.artists.map { Artist(name = it.name, id = it.id) },
                                year = item.album.year,
                                thumbnail = item.thumbnailUrl ?: ""
                            )
                        )
                        is com.gumlaybeg.nimbletune.db.entities.Artist -> otherSources.add(
                            ArtistItem(
                                id = item.id,
                                title = item.title,
                                thumbnail = item.thumbnailUrl,
                                shuffleEndpoint = null,
                                radioEndpoint = null
                            )
                        )
                        else -> {}
                    }
                }
            }

            otherSources.addAll(allYtItems.value)

            if (userSongs.isNotEmpty() && (otherSources.isEmpty() || Random.nextFloat() < 0.8f)) {
                userSongs.distinctBy { it.id }.shuffled().firstOrNull()
            } else {
                otherSources.distinctBy { it.id }.shuffled().firstOrNull()
            } ?: userSongs.firstOrNull() ?: otherSources.firstOrNull()
        } finally {
            isRandomizing.value = false
        }
    }

    private suspend fun getDailyDiscover() {
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val likedSongs = database.likedSongsByCreateDateAsc().first()
        var seeds = likedSongs.shuffled().distinctBy { it.id }.take(5)

        if (seeds.isEmpty()) {
            seeds = database.events().first().map { it.song }.shuffled().distinctBy { it.id }.take(5)
        }

        if (seeds.isEmpty()) {
            val fallbackSeeds = mutableListOf<Song>()
            YouTube.searchSummary("top songs").onSuccess { result ->
                val songs = result.summaries.flatMap { it.items }.filterIsInstance<SongItem>()
                songs.filter { !hideVideoSongs || !it.isVideoSong }.shuffled().take(5).forEach { song ->
                    fallbackSeeds.add(
                        Song(
                            song = SongEntity(
                                id = song.id,
                                title = song.title,
                                duration = song.duration ?: -1,
                                thumbnailUrl = song.thumbnail,
                                localPath = null
                            ),
                            artists = song.artists.map { ArtistEntity(id = it.id ?: "", name = it.name) }
                        )
                    )
                }
            }
            seeds = fallbackSeeds
        }

        if (seeds.isEmpty()) return

        val items = Collections.synchronizedList(mutableListOf<DailyDiscoverItem>())

        coroutineScope {
            seeds.map { seed ->
                launch(Dispatchers.IO) {
                    val nextResult = YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()
                    if (nextResult != null && nextResult.items.isNotEmpty()) {
                        val recommendations = nextResult.items
                            .filter { item ->
                                if (hideVideoSongs && item.isVideoSong) return@filter false
                                if (item.explicit) return@filter false
                                true
                            }
                            .shuffled()

                        val recommendation = recommendations.firstOrNull { rec -> rec.id != seed.id }
                        if (recommendation != null) {
                            items.add(
                                DailyDiscoverItem(
                                    seed = seed,
                                    recommendation = recommendation,
                                    relatedEndpoint = nextResult.relatedEndpoint
                                )
                            )
                        }
                    }
                }
            }.forEach { it.join() }
        }

        dailyDiscover.value = items.toList().distinctBy { it.recommendation.id }.shuffled()
    }

    private suspend fun getQuickPicksLocal() {
        when (quickPicksEnum.first()) {
            QuickPicks.QUICK_PICKS -> {
                val relatedSongs = database.quickPicks().first()
                val forgotten = database.forgottenFavorites().first().take(8)

                var combined = (relatedSongs + forgotten)
                    .distinctBy { it.id }
                    .shuffled()
                    .take(20)

                if (combined.isEmpty()) {
                    combined = database.likedSongsByCreateDateAsc().first().shuffled().take(20)
                }

                if (combined.isEmpty()) {
                    val fromTimeStamp = System.currentTimeMillis() - 86400000L * 365
                    combined = database.mostPlayedSongs(fromTimeStamp, limit = 20).first().shuffled().take(20)
                }

                if (combined.isEmpty()) {
                    combined = database.songsByCreateDateAsc().first().shuffled().take(20)
                }

                quickPicks.value = combined.ifEmpty { relatedSongs.shuffled().take(20) }
            }
            QuickPicks.LAST_LISTEN -> {
                val song = database.events().first().firstOrNull()?.song
                if (song != null && database.hasRelatedSongs(song.id)) {
                    quickPicks.value = database.relatedSongs(song.id).shuffled().take(20)
                }
            }
        }
    }

    private suspend fun enrichQuickPicksFromNetwork() {
        if (quickPicksEnum.first() != QuickPicks.QUICK_PICKS) return
        val recentSong = database.events().first().firstOrNull()?.song ?: return
        val endpoint = YouTube.next(WatchEndpoint(videoId = recentSong.id)).getOrNull()?.relatedEndpoint ?: return

        val ytSimilarSongs = mutableListOf<Song>()
        YouTube.related(endpoint).onSuccess { page ->
            page.songs.take(10).forEach { ytSong ->
                val localSong = database.song(ytSong.id).first() ?: run {
                    database.insert(ytSong.toMediaMetadata())
                    database.song(ytSong.id).first()
                }
                if (localSong != null) {
                    ytSimilarSongs.add(localSong)
                }
            }
        }

        if (ytSimilarSongs.isNotEmpty()) {
            val existing = quickPicks.value.orEmpty()
            quickPicks.value = (existing + ytSimilarSongs)
                .distinctBy { it.id }
                .shuffled()
                .take(20)
        }
    }

    private suspend fun getCommunityPlaylists() {
        val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 4
        var artistSeeds = database.mostPlayedArtists(fromTimeStamp, limit = 10).first()
            .filter { it.artist.isYouTubeArtist }
            .map { it.artist.name }
        if (artistSeeds.isEmpty()) {
            artistSeeds = database.artistsInLibraryAsc().first().take(5).map { it.artist.name }
        }

        var songSeeds = database.mostPlayedSongs(fromTimeStamp, limit = 5).first()
        if (songSeeds.isEmpty()) {
            songSeeds = database.likedSongsByCreateDateAsc().first().take(5)
        }

        val candidatePlaylists = Collections.synchronizedList(mutableListOf<PlaylistItem>())

        val queries = mutableListOf<String>()
        if (artistSeeds.isNotEmpty()) {
            queries.addAll(artistSeeds.shuffled().take(3).map { "$it playlist" })
        }
        if (songSeeds.isNotEmpty()) {
            queries.addAll(songSeeds.shuffled().take(2).map { it.title })
        }

        coroutineScope {
            queries.map { query ->
                launch(Dispatchers.IO) {
                    YouTube.search(query, YouTube.SearchFilter.FILTER_COMMUNITY_PLAYLIST).onSuccess { result ->
                        result.items.filterIsInstance<PlaylistItem>().forEach { playlist ->
                            if (playlist.author?.name != "YouTube Music" &&
                                playlist.author?.name != "YouTube" &&
                                playlist.author?.name != "Playlist" &&
                                !playlist.id.startsWith("RD") &&
                                !playlist.id.startsWith("OLAK")
                            ) {
                                candidatePlaylists.add(playlist)
                            }
                        }
                    }
                }
            }

            songSeeds.shuffled().take(2).map { seed ->
                launch(Dispatchers.IO) {
                    val endpoint = YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()?.relatedEndpoint
                    if (endpoint != null) {
                        YouTube.related(endpoint).onSuccess { page ->
                            page.playlists.forEach { playlist ->
                                if (playlist.author?.name != "YouTube Music" &&
                                    playlist.author?.name != "YouTube" &&
                                    playlist.author?.name != "Playlist" &&
                                    !playlist.id.startsWith("RD") &&
                                    !playlist.id.startsWith("OLAK")
                                ) {
                                    candidatePlaylists.add(playlist)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (candidatePlaylists.size < 20) {
            val fallbackQueries = listOf("top playlist", "hits playlist", "best music playlist", "chill playlist").shuffled()
            for (fallbackQuery in fallbackQueries) {
                if (candidatePlaylists.size >= 25) break
                YouTube.search(fallbackQuery, YouTube.SearchFilter.FILTER_COMMUNITY_PLAYLIST).onSuccess { result ->
                    result.items.filterIsInstance<PlaylistItem>().forEach { playlist ->
                        if (playlist.author?.name != "YouTube Music" &&
                            playlist.author?.name != "YouTube" &&
                            playlist.author?.name != "Playlist" &&
                            !playlist.id.startsWith("RD") &&
                            !playlist.id.startsWith("OLAK")
                        ) {
                            candidatePlaylists.add(playlist)
                        }
                    }
                }
            }
        }

        if (candidatePlaylists.isEmpty()) {
            YouTube.searchSummary("top playlists").getOrNull()?.let { result ->
                candidatePlaylists.addAll(
                    result.summaries.flatMap { it.items }.filterIsInstance<PlaylistItem>()
                        .filter { it.author?.name != "YouTube Music" && it.author?.name != "YouTube" && it.author?.name != "Playlist" && !it.id.startsWith("RD") && !it.id.startsWith("OLAK") }
                )
            }
        }

        communityPlaylists.value = candidatePlaylists.distinctBy { it.id }.shuffled().take(25)
    }

    private suspend fun loadLocalDataPhase() {
        getQuickPicksLocal()

        forgottenFavorites.value = database.forgottenFavorites().first().shuffled().take(20)

        val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 2
        val keepListeningSongs = database.mostPlayedSongs(fromTimeStamp, limit = 15, offset = 5).first()
            .shuffled().take(10)
        val keepListeningAlbums = database.mostPlayedAlbums(fromTimeStamp, limit = 8, offset = 2).first()
            .filter { it.album.thumbnailUrl != null }.shuffled().take(5)
        val keepListeningArtists = database.mostPlayedArtists(fromTimeStamp).first()
            .filter { it.artist.isYouTubeArtist && it.artist.thumbnailUrl != null }.shuffled().take(5)
        keepListening.value = (keepListeningSongs + keepListeningAlbums + keepListeningArtists).shuffled()

        allLocalItems.value = (quickPicks.value.orEmpty() + forgottenFavorites.value.orEmpty() + keepListening.value.orEmpty())
            .filter { it is Song || it is Album }
    }

    private suspend fun loadSimilarRecommendations() {
        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 2

        coroutineScope {
            val artistDeferreds = database.mostPlayedArtists(fromTimeStamp, limit = 15).first()
                .filter { it.artist.isYouTubeArtist }
                .shuffled().take(4)
                .map { artist ->
                    async(Dispatchers.IO) {
                        val items = mutableListOf<YTItem>()
                        YouTube.artist(artist.id).onSuccess { page ->
                            page.sections.takeLast(3).forEach { section -> items += section.items }
                        }
                        SimilarRecommendation(
                            title = artist,
                            items = items
                                .distinctBy { item -> item.id }
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .shuffled()
                                .take(12)
                                .ifEmpty { return@async null }
                        )
                    }
                }

            val songDeferreds = database.mostPlayedSongs(fromTimeStamp, limit = 15).first()
                .filter { it.album != null }
                .shuffled().take(3)
                .map { song ->
                    async(Dispatchers.IO) {
                        val endpoint = YouTube.next(WatchEndpoint(videoId = song.id)).getOrNull()?.relatedEndpoint
                            ?: return@async null
                        val page = YouTube.related(endpoint).getOrNull() ?: return@async null
                        SimilarRecommendation(
                            title = song,
                            items = (page.songs.shuffled().take(10) +
                                    page.albums.shuffled().take(5) +
                                    page.artists.shuffled().take(3) +
                                    page.playlists.shuffled().take(3))
                                .distinctBy { it.id }
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .shuffled()
                                .ifEmpty { return@async null }
                        )
                    }
                }

            val albumDeferreds = database.mostPlayedAlbums(fromTimeStamp, limit = 10).first()
                .filter { it.album.thumbnailUrl != null }
                .shuffled().take(2)
                .map { album ->
                    async(Dispatchers.IO) {
                        val items = mutableListOf<YTItem>()
                        YouTube.album(album.id).onSuccess { page ->
                            items += page.otherVersions
                        }
                        album.artists.firstOrNull()?.id?.let { artistId ->
                            YouTube.artist(artistId).onSuccess { page ->
                                page.sections.lastOrNull()?.items?.let { items += it }
                            }
                        }
                        SimilarRecommendation(
                            title = album,
                            items = items
                                .distinctBy { it.id }
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .shuffled()
                                .take(10)
                                .ifEmpty { return@async null }
                        )
                    }
                }

            val results = (artistDeferreds + songDeferreds + albumDeferreds).awaitAll()
            similarRecommendations.value = results.filterNotNull().shuffled()
        }
    }

    private suspend fun loadFallbackCoversAndRemixes() {
        YouTube.searchSummary("music covers and remixes").onSuccess { result ->
            val songs = result.summaries.find { it.title == "Songs" }?.items?.filterIsInstance<SongItem>().orEmpty()
            val videos = result.summaries.find { it.title == "Videos" }?.items?.filterIsInstance<SongItem>().orEmpty()

            val combined = (songs + videos)
                .filter { !it.musicVideoType.toString().contains("PODCAST", ignoreCase = true) }
                .filter { (it.duration ?: 0) < 600 }
                .distinctBy { it.id }
                .shuffled()

            if (combined.isNotEmpty()) {
                coversAndRemixes.value = HomePage.Section(
                    title = "Covers and remixes",
                    label = null,
                    thumbnail = null,
                    endpoint = null,
                    items = combined.take(20)
                )
            }
        }.onFailure { reportException(it) }
    }

    private suspend fun loadNetworkDataPhase() {
        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)

        coroutineScope {
            launch(Dispatchers.IO) { enrichQuickPicksFromNetwork() }
            launch(Dispatchers.IO) { getDailyDiscover() }
            launch(Dispatchers.IO) { getCommunityPlaylists() }
            launch(Dispatchers.IO) { loadSimilarRecommendations() }
            launch(Dispatchers.IO) {
                YouTube.home().onSuccess { initialPage ->
                    val initialCnrSection = initialPage.sections.find {
                        it.title.contains("cover", true) && it.title.contains("remix", true)
                    }
                    if (initialCnrSection != null) {
                        val filteredItems = initialCnrSection.items
                            .filterExplicit(hideExplicit)
                            .filterYoutubeShorts(hideYoutubeShorts)
                        if (filteredItems.isNotEmpty()) {
                            coversAndRemixes.value = initialCnrSection.copy(items = filteredItems)
                        }
                    } else {
                        launch(Dispatchers.IO) { loadFallbackCoversAndRemixes() }
                    }

                    homePage.value = initialPage.copy(
                        sections = initialPage.sections.mapNotNull { section ->
                            if (section == initialCnrSection) return@mapNotNull null
                            val filteredItems = section.items
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .filterYoutubeShorts(hideYoutubeShorts)
                            if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
                        }
                    )

                    launch(Dispatchers.IO) {
                        var hasQuickPicks = initialPage.sections.any {
                            it.title.equals("Quick picks", ignoreCase = true) ||
                                it.label?.contains("START RADIO", ignoreCase = true) == true ||
                                (it.items.isNotEmpty() && it.items.all { item -> item is SongItem } && it.label != null)
                        }
                        var currentContinuation = initialPage.continuation
                        var fetchCount = 0

                        while (!hasQuickPicks && currentContinuation != null && fetchCount < 2) {
                            val nextSectionsResponse = YouTube.home(currentContinuation).getOrNull() ?: break

                            val nextCnrSection = nextSectionsResponse.sections.find {
                                it.title.contains("cover", true) && it.title.contains("remix", true)
                            }
                            if (nextCnrSection != null && coversAndRemixes.value == null) {
                                val filteredItems = nextCnrSection.items
                                    .filterExplicit(hideExplicit)
                                    .filterYoutubeShorts(hideYoutubeShorts)
                                if (filteredItems.isNotEmpty()) coversAndRemixes.value = nextCnrSection.copy(items = filteredItems)
                            }

                            homePage.value = nextSectionsResponse.copy(
                                chips = homePage.value?.chips,
                                sections = (homePage.value?.sections.orEmpty() + nextSectionsResponse.sections).mapNotNull { section ->
                                    if (section == nextCnrSection) return@mapNotNull null
                                    val filteredItems = section.items
                                        .filterExplicit(hideExplicit)
                                        .filterVideoSongs(hideVideoSongs)
                                        .filterYoutubeShorts(hideYoutubeShorts)
                                    if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
                                }
                            )

                            hasQuickPicks = nextSectionsResponse.sections.any {
                                it.title.equals("Quick picks", ignoreCase = true) ||
                                    it.label?.contains("START RADIO", ignoreCase = true) == true ||
                                    (it.items.isNotEmpty() && it.items.all { item -> item is SongItem } && it.label != null)
                            }
                            currentContinuation = nextSectionsResponse.continuation
                            fetchCount++
                        }
                    }
                }.onFailure { reportException(it) }
            }
            launch(Dispatchers.IO) {
                YouTube.explore().onSuccess { page ->
                    explorePage.value = page.copy(
                        newReleaseAlbums = page.newReleaseAlbums.filterExplicit(hideExplicit)
                    )
                }.onFailure { reportException(it) }
            }
            if (YouTube.cookie != null) {
                launch(Dispatchers.IO) { loadAccountPlaylists() }
            }
            launch(Dispatchers.IO) { syncUtils.syncRecentActivity() }
        }

        allYtItems.value = similarRecommendations.value?.flatMap { it.items }.orEmpty() +
                homePage.value?.sections?.flatMap { it.items }.orEmpty()
    }

    private suspend fun load() {
        isLoading.value = true
        loadLocalDataPhase()
        isLoading.value = false
        loadNetworkDataPhase()
    }

    private val _isLoadingMore = MutableStateFlow(false)

    fun loadMoreYouTubeItems(continuation: String?) {
        if (continuation == null || _isLoadingMore.value) return
        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)

        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingMore.value = true
            val nextSections = YouTube.home(continuation).getOrNull() ?: run {
                _isLoadingMore.value = false
                return@launch
            }

            val cnrSection = nextSections.sections.find {
                it.title.contains("cover", true) && it.title.contains("remix", true)
            }
            if (cnrSection != null && coversAndRemixes.value == null) {
                val filteredItems = cnrSection.items
                    .filterExplicit(hideExplicit)
                    .filterYoutubeShorts(hideYoutubeShorts)
                if (filteredItems.isNotEmpty()) {
                    coversAndRemixes.value = cnrSection.copy(items = filteredItems)
                }
            }

            homePage.value = nextSections.copy(
                chips = homePage.value?.chips,
                sections = (homePage.value?.sections.orEmpty() + nextSections.sections).mapNotNull { section ->
                    if (section == cnrSection) return@mapNotNull null
                    val filteredItems = section.items
                        .filterExplicit(hideExplicit)
                        .filterVideoSongs(hideVideoSongs)
                        .filterYoutubeShorts(hideYoutubeShorts)
                    if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
                }
            )
            _isLoadingMore.value = false
        }
    }

    fun toggleChip(chip: HomePage.Chip?) {
        if (chip == null || (chip == selectedChip.value && previousHomePage.value != null)) {
            homePage.value = previousHomePage.value
            previousHomePage.value = null
            selectedChip.value = null
            return
        }

        if (selectedChip.value == null) {
            previousHomePage.value = homePage.value
        }

        viewModelScope.launch(Dispatchers.IO) {
            val hideExplicit = context.dataStore.get(HideExplicitKey, false)
            val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
            val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
            val nextSections = YouTube.home(params = chip.endpoint?.params).getOrNull() ?: return@launch

            homePage.value = nextSections.copy(
                chips = homePage.value?.chips,
                sections = nextSections.sections.map { section ->
                    section.copy(
                        items = section.items
                            .filterExplicit(hideExplicit)
                            .filterVideoSongs(hideVideoSongs)
                            .filterYoutubeShorts(hideYoutubeShorts)
                    )
                }
            )
            selectedChip.value = chip
        }
    }

    private suspend fun loadAccountPlaylists() {
        val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
        YouTube.library("FEmusic_liked_playlists").completed().onSuccess {
            accountPlaylists.value = it.items.filterIsInstance<PlaylistItem>()
                .filterNot { it.id == "SE" }
                .filterYoutubeShorts(hideYoutubeShorts)
        }.onFailure {
            reportException(it)
        }
    }

    fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isRefreshing.value = true
                load()
            } finally {
                isRefreshing.value = false
            }
        }
        if (context.isSyncEnabled()) {
            viewModelScope.launch(Dispatchers.IO) {
                syncUtils.syncAll()
            }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val cookie = context.dataStore.data.map { it[InnerTubeCookieKey] }.firstOrNull()
            if (!cookie.isNullOrEmpty()) {
                YouTube.cookie = cookie
            }
            load()
        }

        viewModelScope.launch(Dispatchers.IO) {
            database.events()
                .drop(1)
                .mapNotNull { it.firstOrNull()?.event?.id }
                .distinctUntilChanged()
                .collect {
                    loadLocalDataPhase()
                    enrichQuickPicksFromNetwork()
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            quickPicksEnum
                .drop(1)
                .collect {
                    loadLocalDataPhase()
                    enrichQuickPicksFromNetwork()
                }
        }

        if (context.isSyncEnabled()) {
            viewModelScope.launch(Dispatchers.IO) {
                syncUtils.syncAll()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data
                .map { it[InnerTubeCookieKey] }
                .collect { cookie ->
                    if (isProcessingAccountData) return@collect
                    lastProcessedCookie = cookie
                    isProcessingAccountData = true

                    try {
                        if (!cookie.isNullOrEmpty()) {
                            YouTube.cookie = cookie
                            YouTube.accountInfo().onSuccess { info ->
                                accountName.value = info.name
                                accountImageUrl.value = info.thumbnailUrl
                            }.onFailure {
                                reportException(it)
                            }
                        } else {
                            accountName.value = "Guest"
                            accountImageUrl.value = null
                            accountPlaylists.value = null
                        }
                    } finally {
                        isProcessingAccountData = false
                    }
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data
                .map { it[HideYoutubeShortsKey] ?: false }
                .distinctUntilChanged()
                .collect {
                    if (YouTube.cookie != null && accountPlaylists.value != null) {
                        loadAccountPlaylists()
                    }
                }
        }
    }
}

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.gumlaybeg.nimbletune.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gumlaybeg.nimbletune.constants.AlbumFilter
import com.gumlaybeg.nimbletune.constants.AlbumFilterKey
import com.gumlaybeg.nimbletune.constants.AlbumSortDescendingKey
import com.gumlaybeg.nimbletune.constants.AlbumSortType
import com.gumlaybeg.nimbletune.constants.AlbumSortTypeKey
import com.gumlaybeg.nimbletune.constants.ArtistFilter
import com.gumlaybeg.nimbletune.constants.ArtistFilterKey
import com.gumlaybeg.nimbletune.constants.ArtistSongSortDescendingKey
import com.gumlaybeg.nimbletune.constants.ArtistSongSortType
import com.gumlaybeg.nimbletune.constants.ArtistSongSortTypeKey
import com.gumlaybeg.nimbletune.constants.ArtistSortDescendingKey
import com.gumlaybeg.nimbletune.constants.ArtistSortType
import com.gumlaybeg.nimbletune.constants.ArtistSortTypeKey
import com.gumlaybeg.nimbletune.constants.ExcludedScanPathsKey
import com.gumlaybeg.nimbletune.constants.LibrarySortDescendingKey
import com.gumlaybeg.nimbletune.constants.LibrarySortType
import com.gumlaybeg.nimbletune.constants.LibrarySortTypeKey
import com.gumlaybeg.nimbletune.constants.PlaylistFilter
import com.gumlaybeg.nimbletune.constants.PlaylistFilterKey
import com.gumlaybeg.nimbletune.constants.PlaylistSortDescendingKey
import com.gumlaybeg.nimbletune.constants.PlaylistSortType
import com.gumlaybeg.nimbletune.constants.PlaylistSortTypeKey
import com.gumlaybeg.nimbletune.constants.ScanPathsKey
import com.gumlaybeg.nimbletune.constants.SongFilter
import com.gumlaybeg.nimbletune.constants.SongFilterKey
import com.gumlaybeg.nimbletune.constants.SongSortDescendingKey
import com.gumlaybeg.nimbletune.constants.SongSortType
import com.gumlaybeg.nimbletune.constants.SongSortTypeKey
import com.gumlaybeg.nimbletune.db.MusicDatabase
import com.gumlaybeg.nimbletune.db.entities.Album
import com.gumlaybeg.nimbletune.db.entities.Artist
import com.gumlaybeg.nimbletune.db.entities.Playlist
import com.gumlaybeg.nimbletune.db.entities.Song
import com.gumlaybeg.nimbletune.extensions.toEnum
import com.gumlaybeg.nimbletune.models.DirectoryTree
import com.gumlaybeg.nimbletune.ui.utils.DEFAULT_SCAN_PATH
import com.gumlaybeg.nimbletune.ui.utils.cacheDirectoryTree
import com.gumlaybeg.nimbletune.ui.utils.getDirectoryTree
import com.gumlaybeg.nimbletune.ui.utils.uninitializedDirectoryTree
import com.gumlaybeg.nimbletune.utils.SyncUtils
import com.gumlaybeg.nimbletune.utils.dataStore
import com.gumlaybeg.nimbletune.utils.get
import com.gumlaybeg.nimbletune.utils.reportException
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.refreshLocal
import com.music.innertube.YouTube
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LibrarySongsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {

    val allSongs = getSyncedSongs(context, database)
    val isSyncingRemoteLikedSongs = syncUtils.isSyncingRemoteLikedSongs
    val isSyncingRemoteSongs = syncUtils.isSyncingRemoteSongs

    private val scanPaths = context.dataStore[ScanPathsKey]?: DEFAULT_SCAN_PATH
    private val excludedScanPaths = context.dataStore[ExcludedScanPathsKey]?: ""
    val localSongDirectoryTree: MutableStateFlow<DirectoryTree?> = getLocalSongs(database)

    fun syncLibrarySongs() { viewModelScope.launch(Dispatchers.IO) { syncUtils.syncRemoteSongs() } }
    fun syncLikedSongs() { viewModelScope.launch(Dispatchers.IO) { syncUtils.syncRemoteLikedSongs() } }

    /**
     * Get local songs asynchronously, as a full directory tree
     *
     * @return DirectoryTree
     */
    fun getLocalSongs(database: MusicDatabase): MutableStateFlow<DirectoryTree?> {
        CoroutineScope(Dispatchers.IO).launch {
            val directoryStructure: DirectoryTree
            var cachedTree = getDirectoryTree().value
            if (cachedTree == uninitializedDirectoryTree) {
                directoryStructure = refreshLocal(database, scanPaths.split('\n'), excludedScanPaths.split('\n'))
                cacheDirectoryTree(directoryStructure)
            } else {
                directoryStructure = cachedTree!!
            }
        }

        return getDirectoryTree()
    }

    private fun getSyncedSongs(context: Context, database: MusicDatabase): StateFlow<List<Song>?> {

        return context.dataStore.data
                .map {
                    Triple(
                            it[SongFilterKey].toEnum(SongFilter.LIKED),
                            it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE),
                            (it[SongSortDescendingKey] != false)
                    )
                }
                .distinctUntilChanged()
                .flatMapLatest { (filter, sortType, descending) ->
                    when (filter) {
                        SongFilter.LIBRARY -> database.songs(sortType, descending)
                        SongFilter.LIKED -> database.likedSongs(sortType, descending)
                        SongFilter.DOWNLOADED -> database.downloadSongs(sortType, descending)
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    }
}

@HiltViewModel
class LibraryArtistsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    val isSyncingRemoteArtists = syncUtils.isSyncingRemoteArtists

    val allArtists = context.dataStore.data
        .map {
            Triple(
                it[ArtistFilterKey].toEnum(ArtistFilter.LIKED),
                it[ArtistSortTypeKey].toEnum(ArtistSortType.CREATE_DATE),
                it[ArtistSortDescendingKey] ?: true
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { (filter, sortType, descending) ->
            database.artists(filter, sortType, descending)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun syncArtists() { viewModelScope.launch(Dispatchers.IO) { syncUtils.syncRemoteArtists() } }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allArtists.collect { artists ->
                artists
                    ?.map { it.artist }
                    ?.filter {
                        it.thumbnailUrl == null || Duration.between(it.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)
                    }
                    ?.forEach { artist ->
                        YouTube.artist(artist.id).onSuccess { artistPage ->
                            database.query {
                                update(artist, artistPage)
                            }
                        }
                    }
            }
        }
    }
}

@HiltViewModel
class LibraryAlbumsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    val isSyncingRemoteAlbums = syncUtils.isSyncingRemoteAlbums

    val allAlbums = context.dataStore.data
        .map {
            Triple(
                it[AlbumFilterKey].toEnum(AlbumFilter.LIKED),
                it[AlbumSortTypeKey].toEnum(AlbumSortType.CREATE_DATE),
                it[AlbumSortDescendingKey] ?: true
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { (filter, sortType, descending) ->
            database.albums(filter, sortType, descending)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun syncAlbums() { viewModelScope.launch(Dispatchers.IO) { syncUtils.syncRemoteAlbums() } }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allAlbums.collect { albums ->
                albums
                    ?.filter {
                    it.album.songCount == 0
                }?.forEach { album ->
                    YouTube.album(album.id).onSuccess { albumPage ->
                        database.query {
                            update(album.album, albumPage)
                        }
                    }.onFailure {
                        reportException(it)
                        if (it.message?.contains("NOT_FOUND") == true) {
                            database.query {
                                delete(album.album)
                            }
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class LibraryPlaylistsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    val isSyncingRemotePlaylists = syncUtils.isSyncingRemotePlaylists

    val allPlaylists = context.dataStore.data
        .map {
            Triple(
                it[PlaylistFilterKey].toEnum(PlaylistFilter.LIBRARY),
                it[PlaylistSortTypeKey].toEnum(PlaylistSortType.CREATE_DATE),
                it[PlaylistSortDescendingKey] ?: true
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { (filter, sortType, descending) ->
            database.playlists(filter, sortType, descending)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun syncPlaylists() { viewModelScope.launch(Dispatchers.IO) { syncUtils.syncRemotePlaylists() } }
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    syncUtils: SyncUtils
) : ViewModel() {

    val isSyncingRemoteLikedSongs = syncUtils.isSyncingRemoteLikedSongs
    val isSyncingRemoteSongs = syncUtils.isSyncingRemoteSongs
    val isSyncingRemoteAlbums = syncUtils.isSyncingRemoteAlbums
    val isSyncingRemoteArtists = syncUtils.isSyncingRemoteArtists
    val isSyncingRemotePlaylists = syncUtils.isSyncingRemotePlaylists

    var artists = database.artistsBookmarkedAsc().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var albums = database.albumsLikedAsc().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var playlists = database.playlistInLibraryAsc().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allItems = context.dataStore.data
        .map {
            it[LibrarySortTypeKey].toEnum(LibrarySortType.CREATE_DATE) to (it[LibrarySortDescendingKey] != false)
        }
        .distinctUntilChanged()
        .flatMapLatest { (sortType, descending) ->
            combine(artists, albums, playlists) { artists, albums, playlists ->
                val items = artists + albums + playlists
                items.sortedBy { item ->
                    when (sortType) {
                        LibrarySortType.CREATE_DATE -> when (item) {
                            is Album -> item.album.bookmarkedAt
                            is Artist -> item.artist.bookmarkedAt
                            is Playlist -> item.playlist.bookmarkedAt
                            else -> LocalDateTime.now()
                        }

                        else -> when (item) {
                            is Album -> item.album.title.lowercase()
                            is Artist -> item.artist.name.lowercase()
                            is Playlist -> item.playlist.name.lowercase()
                            else -> ""
                        }
                    }.toString()
                }.let { if (descending) it.reversed() else it }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

@HiltViewModel
class ArtistSongsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val artistId = savedStateHandle.get<String>("artistId")!!
    val artist = database.artist(artistId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val songs = context.dataStore.data
        .map {
            it[ArtistSongSortTypeKey].toEnum(ArtistSongSortType.CREATE_DATE) to (it[ArtistSongSortDescendingKey] ?: true)
        }
        .distinctUntilChanged()
        .flatMapLatest { (sortType, descending) ->
            database.artistSongs(artistId, sortType, descending)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

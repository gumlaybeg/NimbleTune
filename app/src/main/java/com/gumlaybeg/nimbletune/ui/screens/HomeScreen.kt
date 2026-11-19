package com.gumlaybeg.nimbletune.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.gumlaybeg.nimbletune.LocalDatabase
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.AccountNameKey
import com.gumlaybeg.nimbletune.constants.GridThumbnailHeight
import com.gumlaybeg.nimbletune.constants.InnerTubeCookieKey
import com.gumlaybeg.nimbletune.constants.ListItemHeight
import com.gumlaybeg.nimbletune.constants.ListThumbnailSize
import com.gumlaybeg.nimbletune.constants.ThumbnailCornerRadius
import com.gumlaybeg.nimbletune.db.entities.Album
import com.gumlaybeg.nimbletune.db.entities.Artist
import com.gumlaybeg.nimbletune.db.entities.LocalItem
import com.gumlaybeg.nimbletune.db.entities.Playlist
import com.gumlaybeg.nimbletune.db.entities.RecentActivityType.ALBUM
import com.gumlaybeg.nimbletune.db.entities.RecentActivityType.ARTIST
import com.gumlaybeg.nimbletune.db.entities.RecentActivityType.PLAYLIST
import com.gumlaybeg.nimbletune.db.entities.Song
import com.gumlaybeg.nimbletune.extensions.togglePlayPause
import com.gumlaybeg.nimbletune.models.toMediaMetadata
import com.gumlaybeg.nimbletune.playback.queues.ListQueue
import com.gumlaybeg.nimbletune.playback.queues.YouTubeAlbumRadio
import com.gumlaybeg.nimbletune.playback.queues.YouTubeQueue
import com.gumlaybeg.nimbletune.ui.component.AlbumGridItem
import com.gumlaybeg.nimbletune.ui.component.ArtistGridItem
import com.gumlaybeg.nimbletune.ui.component.ChipsRow
import com.gumlaybeg.nimbletune.ui.component.HideOnScrollFAB
import com.gumlaybeg.nimbletune.ui.component.LocalMenuState
import com.gumlaybeg.nimbletune.ui.component.NavigationTile
import com.gumlaybeg.nimbletune.ui.component.NavigationTitle
import com.gumlaybeg.nimbletune.ui.component.SongGridItem
import com.gumlaybeg.nimbletune.ui.component.SongListItem
import com.gumlaybeg.nimbletune.ui.component.YouTubeCardItem
import com.gumlaybeg.nimbletune.ui.component.YouTubeGridItem
import com.gumlaybeg.nimbletune.ui.component.YouTubeListItem
import com.gumlaybeg.nimbletune.ui.component.listItemShape
import com.gumlaybeg.nimbletune.ui.component.shimmer.GridItemPlaceHolder
import com.gumlaybeg.nimbletune.ui.component.shimmer.ShimmerHost
import com.gumlaybeg.nimbletune.ui.component.shimmer.TextPlaceholder
import com.gumlaybeg.nimbletune.ui.menu.AlbumMenu
import com.gumlaybeg.nimbletune.ui.menu.ArtistMenu
import com.gumlaybeg.nimbletune.ui.menu.SongMenu
import com.gumlaybeg.nimbletune.ui.menu.YouTubeAlbumMenu
import com.gumlaybeg.nimbletune.ui.menu.YouTubeArtistMenu
import com.gumlaybeg.nimbletune.ui.menu.YouTubePlaylistMenu
import com.gumlaybeg.nimbletune.ui.menu.YouTubeSongMenu
import com.gumlaybeg.nimbletune.ui.utils.SnapLayoutInfoProvider
import com.gumlaybeg.nimbletune.utils.rememberPreference
import com.gumlaybeg.nimbletune.utils.reportException
import com.gumlaybeg.nimbletune.viewmodels.HomeViewModel
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.pages.HomePage
import com.music.innertube.utils.parseCookieString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.random.Random

sealed class HomeSection(val id: String, val baseWeight: Int) {
    data object QuickPicks : HomeSection("quick_picks", 90)
    data object KeepListening : HomeSection("keep_listening", 50)
    data object AccountPlaylists : HomeSection("account_playlists", 40)
    data object ForgottenFavorites : HomeSection("forgotten_favorites", 30)
    data class SimilarRecommendation(val index: Int) : HomeSection("similar_recommendation_$index", 10)
    data class HomePageSection(val index: Int) : HomeSection("home_page_section_$index", 10)
    data object MoodAndGenres : HomeSection("mood_and_genres", 5)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current
    if (playerConnection == null) {
        ShimmerHost(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                TextPlaceholder(
                    height = 36.dp,
                    modifier = Modifier
                        .padding(12.dp)
                        .width(250.dp),
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(2) {
                        GridItemPlaceHolder()
                    }
                }
            }
        }
        return
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val queuePlaylistId by playerConnection.queuePlaylistId.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val similarRecommendations by viewModel.similarRecommendations.collectAsState()
    val accountPlaylists by viewModel.accountPlaylists.collectAsState()
    val baseHomePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val recentActivity by viewModel.recentActivity.collectAsState()

    val allLocalItems by viewModel.allLocalItems.collectAsState()
    val allYtItems by viewModel.allYtItems.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    val quickPicksLazyGridState = rememberLazyGridState()
    val forgottenFavoritesLazyGridState = rememberLazyGridState()
    val recentActivityGridState = rememberLazyGridState()
    val lazylistState = rememberLazyListState()

    val accountName by rememberPreference(AccountNameKey, "")
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }

    var selectedChip by remember { mutableStateOf<HomePage.Chip?>(null) }
    var chipHomePage by remember { mutableStateOf<HomePage?>(null) }
    var continuationSections by remember { mutableStateOf<List<HomePage.Section>>(emptyList()) }
    var currentContinuation by remember { mutableStateOf<String?>(null) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val activeHomePage = remember(selectedChip, chipHomePage, baseHomePage, continuationSections, currentContinuation) {
        val base = if (selectedChip != null) chipHomePage else baseHomePage
        base?.copy(
            sections = base.sections + continuationSections,
            continuation = currentContinuation ?: base.continuation
        )
    }

    LaunchedEffect(baseHomePage, selectedChip) {
        if (selectedChip == null) {
            continuationSections = emptyList()
            currentContinuation = baseHomePage?.continuation
        }
    }

    fun selectChip(chip: HomePage.Chip?) {
        val nextChip = if (chip == selectedChip) null else chip
        selectedChip = nextChip
        continuationSections = emptyList()
        if (nextChip == null) {
            chipHomePage = null
            currentContinuation = baseHomePage?.continuation
            return
        }
        val params = nextChip.endpoint?.params
        scope.launch(Dispatchers.IO) {
            YouTube.home(params = params)
                .onSuccess { page ->
                    withContext(Dispatchers.Main) {
                        chipHomePage = page.copy(chips = baseHomePage?.chips ?: page.chips)
                        currentContinuation = page.continuation
                    }
                }
                .onFailure {
                    reportException(it)
                }
        }
    }

    fun loadMoreHomeSections() {
        val token = activeHomePage?.continuation ?: return
        if (isLoadingMore) return
        isLoadingMore = true
        scope.launch(Dispatchers.IO) {
            YouTube.home(continuation = token)
                .onSuccess { nextPage ->
                    withContext(Dispatchers.Main) {
                        continuationSections = continuationSections + nextPage.sections
                        currentContinuation = nextPage.continuation
                        isLoadingMore = false
                    }
                }
                .onFailure {
                    reportException(it)
                    withContext(Dispatchers.Main) {
                        isLoadingMore = false
                    }
                }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    var randomSeed by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            selectedChip = null
            chipHomePage = null
            continuationSections = emptyList()
            randomSeed = System.currentTimeMillis()
        }
    }

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(activeHomePage?.continuation) {
        snapshotFlow { lazylistState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val len = lazylistState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && len > 0 && lastVisibleIndex >= len - 3) {
                    loadMoreHomeSections()
                }
            }
    }

    if (selectedChip != null) {
        BackHandler {
            selectChip(selectedChip)
        }
    }

    val localGridItem: @Composable (LocalItem, String) -> Unit = { it, source ->
        when (it) {
            is Song -> SongGridItem(
                song = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (it.id == mediaMetadata?.id) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                val song = it.toMediaMetadata()
                                if (song.isLocal) {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = source,
                                            items = listOf(song)
                                        )
                                    )
                                } else {
                                    playerConnection.playQueue(YouTubeQueue.radio(song))
                                }
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                SongMenu(
                                    originalSong = it,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    ),
                isActive = it.id == mediaMetadata?.id,
                isPlaying = isPlaying,
            )

            is Album -> AlbumGridItem(
                album = it,
                isActive = it.id == mediaMetadata?.album?.id,
                isPlaying = isPlaying,
                coroutineScope = scope,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { navController.navigate("album/${it.id}") },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                AlbumMenu(
                                    originalAlbum = it,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
            )

            is Artist -> ArtistGridItem(
                artist = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { navController.navigate("artist/${it.id}") },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                ArtistMenu(
                                    originalArtist = it,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    ),
            )

            is Playlist -> {}
        }
    }

    val ytGridItem: @Composable (YTItem) -> Unit = { item ->
        YouTubeGridItem(
            item = item,
            isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
            isPlaying = isPlaying,
            coroutineScope = scope,
            thumbnailRatio = 1f,
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        when (item) {
                            is SongItem -> playerConnection.playQueue(
                                YouTubeQueue(
                                    item.endpoint ?: WatchEndpoint(videoId = item.id),
                                    item.toMediaMetadata()
                                )
                            )

                            is AlbumItem -> navController.navigate("album/${item.id}")
                            is ArtistItem -> navController.navigate("artist/${item.id}")
                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            when (item) {
                                is SongItem -> YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is AlbumItem -> YouTubeAlbumMenu(
                                    albumItem = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is ArtistItem -> YouTubeArtistMenu(
                                    artist = item,
                                    onDismiss = menuState::dismiss
                                )

                                is PlaylistItem -> YouTubePlaylistMenu(
                                    playlist = item,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    }
                )
        )
    }

    val quickPicksLocalized = stringResource(R.string.quick_picks)
    val homeSections = remember(
        randomSeed,
        selectedChip,
        quickPicks,
        keepListening,
        accountPlaylists,
        forgottenFavorites,
        similarRecommendations,
        activeHomePage?.sections,
        explorePage?.moodAndGenres,
        quickPicksLocalized
    ) {
        val list = mutableListOf<HomeSection>()
        val chipActive = selectedChip != null

        if (!chipActive && quickPicks?.isNotEmpty() == true) list.add(HomeSection.QuickPicks)
        if (!chipActive && keepListening?.isNotEmpty() == true) list.add(HomeSection.KeepListening)
        if (!chipActive && accountPlaylists?.isNotEmpty() == true) list.add(HomeSection.AccountPlaylists)
        if (!chipActive && forgottenFavorites?.isNotEmpty() == true) list.add(HomeSection.ForgottenFavorites)

        if (!chipActive) {
            similarRecommendations?.indices?.forEach { i ->
                list.add(HomeSection.SimilarRecommendation(i))
            }
        }

        activeHomePage?.sections?.indices?.forEach { i ->
            list.add(HomeSection.HomePageSection(i))
        }

        if (explorePage?.moodAndGenres != null) list.add(HomeSection.MoodAndGenres)

        val defaultOrder = mapOf(
            HomeSection.QuickPicks to 110,
            HomeSection.KeepListening to 60,
            HomeSection.AccountPlaylists to 50,
            HomeSection.ForgottenFavorites to 40,
            HomeSection.MoodAndGenres to 10
        )

        val sortedList = list.sortedByDescending { section ->
            when (section) {
                is HomeSection.SimilarRecommendation -> 30 - section.index
                is HomeSection.HomePageSection -> 20 - section.index
                else -> defaultOrder[section] ?: 0
            }
        }

        val isYouTubeQuickPicksSection: (HomePage.Section?) -> Boolean = { s ->
            s != null && (
                s.title.equals("Quick picks", ignoreCase = true) ||
                    s.title.equals(quickPicksLocalized, ignoreCase = true) ||
                    s.label?.contains("START RADIO", ignoreCase = true) == true
                )
        }

        val hasYouTubeQuickPicks = sortedList.any {
            it is HomeSection.HomePageSection && isYouTubeQuickPicksSection(activeHomePage?.sections?.getOrNull(it.index))
        }

        val deduplicatedList = if (hasYouTubeQuickPicks && quickPicks.isNullOrEmpty()) {
            sortedList.filterNot { it == HomeSection.QuickPicks }
        } else if (hasYouTubeQuickPicks) {
            sortedList.filterNot { it is HomeSection.HomePageSection && isYouTubeQuickPicksSection(activeHomePage?.sections?.getOrNull(it.index)) }
        } else {
            sortedList
        }

        val isQuickPicksSection: (HomeSection) -> Boolean = {
            it == HomeSection.QuickPicks || (it is HomeSection.HomePageSection && isYouTubeQuickPicksSection(activeHomePage?.sections?.getOrNull(it.index)))
        }
        val qpItems = deduplicatedList.filter(isQuickPicksSection)
        if (qpItems.isNotEmpty()) qpItems + deduplicatedList.filterNot(isQuickPicksSection)
        else deduplicatedList
    }

    LaunchedEffect(quickPicks) {
        quickPicksLazyGridState.scrollToItem(0)
    }

    LaunchedEffect(forgottenFavorites) {
        forgottenFavoritesLazyGridState.scrollToItem(0)
    }

    val pullDensity = LocalDensity.current
    val maxOffsetPx = with(pullDensity) { PullToRefreshDefaults.PositionalThreshold.toPx() + 16.dp.toPx() }
    val topInsetPx = with(pullDensity) {
        LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateTopPadding().toPx() + 16.dp.toPx()
    }
    val indicatorHeightPx = with(pullDensity) { 56.dp.toPx() }
    val expressiveSpring = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val targetFraction = if (isRefreshing) 1f else pullRefreshState.distanceFraction.coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = expressiveSpring,
        label = "pull_fraction"
    )

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        indicator = {
            if (animatedFraction > 0.001f) {
                val yOffset = lerp(-indicatorHeightPx, topInsetPx, animatedFraction)
                if (isRefreshing) {
                    ContainedLoadingIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset { IntOffset(0, yOffset.toInt()) }
                    )
                } else {
                    ContainedLoadingIndicator(
                        progress = { pullRefreshState.distanceFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset { IntOffset(0, yOffset.toInt()) }
                    )
                }
            }
        }
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
            val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
            val quickPicksSnapLayoutInfoProvider = remember(quickPicksLazyGridState) {
                SnapLayoutInfoProvider(
                    lazyGridState = quickPicksLazyGridState,
                    positionInLayout = { layoutSize, itemSize ->
                        (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                    }
                )
            }
            val forgottenFavoritesSnapLayoutInfoProvider = remember(forgottenFavoritesLazyGridState) {
                SnapLayoutInfoProvider(
                    lazyGridState = forgottenFavoritesLazyGridState,
                    positionInLayout = { layoutSize, itemSize ->
                        (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                    }
                )
            }

            LazyColumn(
                state = lazylistState,
                contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                modifier = Modifier.offset {
                    IntOffset(0, (animatedFraction * maxOffsetPx).toInt())
                }
            ) {
                item(key = "chips_row") {
                    val chips = activeHomePage?.chips
                        ?.filter { !it.title.equals("Podcasts", ignoreCase = true) }
                        ?.map { it to it.title }
                        ?: emptyList()
                    if (chips.isNotEmpty()) {
                        ChipsRow(
                            chips = chips,
                            currentValue = selectedChip,
                            onValueUpdate = { selectChip(it) }
                        )
                    }
                }

                if (isLoading && activeHomePage?.chips.isNullOrEmpty()) {
                    item(key = "chips_shimmer") {
                        ShimmerHost {
                            LazyRow(
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(5) {
                                    TextPlaceholder(
                                        height = 30.dp,
                                        modifier = Modifier
                                            .width(72.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedChip == null) {
                    item(key = "nav_tiles") {
                        Row(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .fillMaxWidth()
                                .animateItem()
                        ) {
                            NavigationTile(
                                title = stringResource(R.string.history),
                                icon = Icons.Rounded.History,
                                onClick = { navController.navigate("history") },
                                modifier = Modifier.weight(1f)
                            )

                            NavigationTile(
                                title = stringResource(R.string.stats),
                                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                                onClick = { navController.navigate("stats") },
                                modifier = Modifier.weight(1f)
                            )

                            if (isLoggedIn) {
                                NavigationTile(
                                    title = stringResource(R.string.account),
                                    icon = Icons.Rounded.Person,
                                    onClick = { navController.navigate("account") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (isLoggedIn && !recentActivity.isNullOrEmpty()) {
                        item(key = "recent_activity_title") {
                            NavigationTitle(
                                title = stringResource(R.string.recent_activity),
                                modifier = Modifier.animateItem()
                            )
                        }
                        item(key = "recent_activity_list") {
                            LazyHorizontalGrid(
                                state = recentActivityGridState,
                                rows = GridCells.Fixed(4),
                                flingBehavior = rememberSnapFlingBehavior(forgottenFavoritesLazyGridState),
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp * 4)
                                    .padding(6.dp)
                                    .animateItem()
                            ) {
                                items(
                                    items = recentActivity!!,
                                    key = { it.id }
                                ) { item ->
                                    YouTubeCardItem(
                                        item = item,
                                        onClick = {
                                            when (item.type) {
                                                PLAYLIST -> {
                                                    val playlistDb = playlists
                                                        ?.firstOrNull { it.playlist.browseId == item.id }

                                                    if (playlistDb != null && playlistDb.songCount != 0)
                                                        navController.navigate("local_playlist/${playlistDb.id}")
                                                    else
                                                        navController.navigate("online_playlist/${item.id}")
                                                }

                                                ALBUM -> navController.navigate("album/${item.id}")
                                                ARTIST -> navController.navigate("artist/${item.id}")
                                            }
                                        },
                                        isPlaying = isPlaying,
                                        isActive = when (item.type) {
                                            PLAYLIST -> queuePlaylistId == item.id
                                            ALBUM -> queuePlaylistId == item.playlistId
                                            ARTIST -> (queuePlaylistId == item.radioPlaylistId ||
                                                queuePlaylistId == item.shufflePlaylistId ||
                                                queuePlaylistId == item.playlistId)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                homeSections.forEach { section ->
                    when (section) {
                        HomeSection.QuickPicks -> {
                            quickPicks?.takeIf { it.isNotEmpty() }?.let { quickPicksList ->
                                item(key = "quick_picks_title") {
                                    val quickPicksTitle = stringResource(R.string.quick_picks)
                                    HomeSectionHeader(
                                        title = quickPicksTitle,
                                        modifier = Modifier.animateItem(),
                                        onPlayAllClick = {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = quickPicksTitle,
                                                    items = quickPicksList.distinctBy { it.id }.map { it.toMediaMetadata() }
                                                )
                                            )
                                        }
                                    )
                                }

                                item(key = "quick_picks_list") {
                                    val distinctQuickPicks = remember(quickPicksList) {
                                        quickPicksList.distinctBy { it.id }
                                    }
                                    val rows = min(4, distinctQuickPicks.size)
                                    LazyHorizontalGrid(
                                        state = quickPicksLazyGridState,
                                        rows = GridCells.Fixed(rows),
                                        flingBehavior = rememberSnapFlingBehavior(quickPicksSnapLayoutInfoProvider),
                                        contentPadding = WindowInsets.systemBars
                                            .only(WindowInsetsSides.Horizontal)
                                            .asPaddingValues(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(ListItemHeight * rows)
                                            .animateItem()
                                    ) {
                                        itemsIndexed(
                                            items = distinctQuickPicks,
                                            key = { _, it -> it.id }
                                        ) { index, originalSong ->
                                            val song by database.song(originalSong.id)
                                                .collectAsState(initial = originalSong)
                                            val currentSong = song ?: originalSong

                                            Box(
                                                modifier = Modifier
                                                    .width(horizontalLazyGridItemWidth)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    .clip(listItemShape(index = index % rows, count = rows))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                            ) {
                                                SongListItem(
                                                    song = currentSong,
                                                    onPlay = {
                                                        if (currentSong.id == mediaMetadata?.id) {
                                                            playerConnection.player.togglePlayPause()
                                                        } else {
                                                            val meta = currentSong.toMediaMetadata()
                                                            if (meta.isLocal) {
                                                                playerConnection.playQueue(
                                                                    ListQueue(
                                                                        title = context.getString(R.string.quick_picks),
                                                                        items = distinctQuickPicks.map { it.toMediaMetadata() },
                                                                        startIndex = index
                                                                    )
                                                                )
                                                            } else {
                                                                playerConnection.playQueue(
                                                                    YouTubeQueue.radio(meta)
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onSelectedChange = {},
                                                    inSelectMode = null,
                                                    isSelected = false,
                                                    enableSwipeToQueue = false,
                                                    navController = navController,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.KeepListening -> {
                            keepListening?.takeIf { it.isNotEmpty() }?.let { keepListeningList ->
                                item(key = "keep_listening_title") {
                                    NavigationTitle(
                                        title = stringResource(R.string.keep_listening),
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                item(key = "keep_listening_list") {
                                    val rows = if (keepListeningList.size > 6) 2 else 1
                                    LazyHorizontalGrid(
                                        state = rememberLazyGridState(),
                                        rows = GridCells.Fixed(rows),
                                        contentPadding = WindowInsets.systemBars
                                            .only(WindowInsetsSides.Horizontal)
                                            .asPaddingValues(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((GridThumbnailHeight + 24.dp + with(LocalDensity.current) {
                                                MaterialTheme.typography.bodyLarge.lineHeight.toDp() * 2 +
                                                    MaterialTheme.typography.bodyMedium.lineHeight.toDp() * 2
                                            }) * rows)
                                            .animateItem()
                                    ) {
                                        items(keepListeningList) {
                                            localGridItem(it, stringResource(R.string.keep_listening))
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.AccountPlaylists -> {
                            accountPlaylists?.takeIf { it.isNotEmpty() }?.let { playlistsList ->
                                item(key = "account_playlists_title") {
                                    NavigationTitle(
                                        label = stringResource(R.string.your_youtube_playlists),
                                        title = accountName.ifBlank { stringResource(R.string.account) },
                                        thumbnail = {
                                            Icon(
                                                imageVector = Icons.Rounded.Person,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(ListThumbnailSize)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                                    .padding(8.dp)
                                            )
                                        },
                                        onClick = { navController.navigate("account") },
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                item(key = "account_playlists_list") {
                                    LazyRow(
                                        contentPadding = WindowInsets.systemBars
                                            .only(WindowInsetsSides.Horizontal)
                                            .asPaddingValues(),
                                        modifier = Modifier.animateItem()
                                    ) {
                                        items(
                                            items = playlistsList.distinctBy { it.id },
                                            key = { it.id },
                                        ) { item ->
                                            ytGridItem(item)
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.ForgottenFavorites -> {
                            forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { forgottenFavoritesList ->
                                item(key = "forgotten_favorites_title") {
                                    val forgottenFavoritesTitle = stringResource(R.string.forgotten_favorites)
                                    HomeSectionHeader(
                                        title = forgottenFavoritesTitle,
                                        modifier = Modifier.animateItem(),
                                        onPlayAllClick = {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = forgottenFavoritesTitle,
                                                    items = forgottenFavoritesList.distinctBy { it.id }.map { it.toMediaMetadata() }
                                                )
                                            )
                                        }
                                    )
                                }

                                item(key = "forgotten_favorites_list") {
                                    val distinctForgotten = remember(forgottenFavoritesList) {
                                        forgottenFavoritesList.distinctBy { it.id }
                                    }
                                    val rows = min(4, distinctForgotten.size)
                                    LazyHorizontalGrid(
                                        state = forgottenFavoritesLazyGridState,
                                        rows = GridCells.Fixed(rows),
                                        contentPadding = WindowInsets.systemBars
                                            .only(WindowInsetsSides.Horizontal)
                                            .asPaddingValues(),
                                        flingBehavior = rememberSnapFlingBehavior(forgottenFavoritesSnapLayoutInfoProvider),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(ListItemHeight * rows)
                                            .animateItem()
                                    ) {
                                        itemsIndexed(
                                            items = distinctForgotten,
                                            key = { _, it -> it.id }
                                        ) { index, originalSong ->
                                            val song by database.song(originalSong.id)
                                                .collectAsState(initial = originalSong)
                                            val currentSong = song ?: originalSong

                                            Box(
                                                modifier = Modifier
                                                    .width(horizontalLazyGridItemWidth)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    .clip(listItemShape(index = index % rows, count = rows))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                            ) {
                                                SongListItem(
                                                    song = currentSong,
                                                    onPlay = {
                                                        if (currentSong.id == mediaMetadata?.id) {
                                                            playerConnection.player.togglePlayPause()
                                                        } else {
                                                            val meta = currentSong.toMediaMetadata()
                                                            if (meta.isLocal) {
                                                                playerConnection.playQueue(
                                                                    ListQueue(
                                                                        title = context.getString(R.string.forgotten_favorites),
                                                                        items = distinctForgotten.map { it.toMediaMetadata() },
                                                                        startIndex = index
                                                                    )
                                                                )
                                                            } else {
                                                                playerConnection.playQueue(
                                                                    YouTubeQueue.radio(meta)
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onSelectedChange = {},
                                                    inSelectMode = null,
                                                    isSelected = false,
                                                    enableSwipeToQueue = false,
                                                    navController = navController,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        is HomeSection.SimilarRecommendation -> {
                            val recommendation = similarRecommendations?.getOrNull(section.index)
                            recommendation?.let {
                                item(key = "similar_to_title_${section.index}") {
                                    NavigationTitle(
                                        label = stringResource(R.string.similar_to),
                                        title = recommendation.title.title,
                                        thumbnail = recommendation.title.thumbnailUrl?.let { thumbnailUrl ->
                                            {
                                                val shape = if (recommendation.title is Artist) CircleShape
                                                else RoundedCornerShape(ThumbnailCornerRadius)
                                                AsyncImage(
                                                    model = thumbnailUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(ListThumbnailSize)
                                                        .clip(shape)
                                                )
                                            }
                                        },
                                        onClick = {
                                            when (recommendation.title) {
                                                is Song -> navController.navigate("album/${recommendation.title.album!!.id}")
                                                is Album -> navController.navigate("album/${recommendation.title.id}")
                                                is Artist -> navController.navigate("artist/${recommendation.title.id}")
                                                is Playlist -> {}
                                            }
                                        },
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                item(key = "similar_to_list_${section.index}") {
                                    LazyRow(
                                        contentPadding = WindowInsets.systemBars
                                            .only(WindowInsetsSides.Horizontal)
                                            .asPaddingValues(),
                                        modifier = Modifier.animateItem()
                                    ) {
                                        items(
                                            items = recommendation.items.distinctBy { it.id },
                                            key = { it.id }
                                        ) { item ->
                                            ytGridItem(item)
                                        }
                                    }
                                }
                            }
                        }

                        is HomeSection.HomePageSection -> {
                            val sectionData = activeHomePage?.sections?.getOrNull(section.index)
                            sectionData?.let {
                                val sectionSongs = sectionData.items.filterIsInstance<SongItem>()
                                val hasPlayableSongs = sectionSongs.isNotEmpty()
                                val isSongsOnlySection = sectionData.items.isNotEmpty() &&
                                    sectionData.items.all { it is SongItem } &&
                                    !sectionData.title.equals("Listen together", ignoreCase = true)

                                item(key = "home_section_title_${section.index}") {
                                    HomeSectionHeader(
                                        title = sectionData.title,
                                        label = sectionData.label,
                                        thumbnail = sectionData.thumbnail?.let { thumbnailUrl ->
                                            {
                                                val shape = if (sectionData.endpoint?.isArtistEndpoint == true) CircleShape
                                                else RoundedCornerShape(ThumbnailCornerRadius)
                                                AsyncImage(
                                                    model = thumbnailUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(ListThumbnailSize)
                                                        .clip(shape)
                                                )
                                            }
                                        },
                                        onClick = sectionData.endpoint?.let { endpoint ->
                                            {
                                                when {
                                                    endpoint.browseId == "FEmusic_moods_and_genres" ->
                                                        navController.navigate("mood_and_genres")
                                                    endpoint.params != null ->
                                                        navController.navigate("youtube_browse/${endpoint.browseId}?params=${endpoint.params}")
                                                    else ->
                                                        navController.navigate("browse/${endpoint.browseId}")
                                                }
                                            }
                                        },
                                        onPlayAllClick = if (hasPlayableSongs) {
                                            {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = sectionData.title,
                                                        items = sectionSongs.map { it.toMediaMetadata() }
                                                    )
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                if (isSongsOnlySection) {
                                    item(key = "home_section_list_${section.index}") {
                                        val distinctSongs = remember(sectionSongs) {
                                            sectionSongs.distinctBy { it.id }
                                        }
                                        val rows = min(4, distinctSongs.size)
                                        LazyHorizontalGrid(
                                            state = rememberLazyGridState(),
                                            rows = GridCells.Fixed(rows),
                                            contentPadding = WindowInsets.systemBars
                                                .only(WindowInsetsSides.Horizontal)
                                                .asPaddingValues(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(ListItemHeight * rows)
                                                .animateItem()
                                        ) {
                                            itemsIndexed(
                                                items = distinctSongs,
                                                key = { _, it -> it.id }
                                            ) { index, song ->
                                                Box(
                                                    modifier = Modifier
                                                        .width(horizontalLazyGridItemWidth)
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        .clip(listItemShape(index = index % rows, count = rows))
                                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                                ) {
                                                    YouTubeListItem(
                                                        item = song,
                                                        isActive = song.id == mediaMetadata?.id,
                                                        isPlaying = isPlaying,
                                                        trailingContent = {
                                                            IconButton(
                                                                onClick = {
                                                                    menuState.show {
                                                                        YouTubeSongMenu(
                                                                            song = song,
                                                                            navController = navController,
                                                                            onDismiss = menuState::dismiss
                                                                        )
                                                                    }
                                                                }
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.MoreVert,
                                                                    contentDescription = null
                                                                )
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .combinedClickable(
                                                                onClick = {
                                                                    if (song.id == mediaMetadata?.id) {
                                                                        playerConnection.player.togglePlayPause()
                                                                    } else {
                                                                        playerConnection.playQueue(
                                                                            YouTubeQueue.radio(song.toMediaMetadata())
                                                                        )
                                                                    }
                                                                },
                                                                onLongClick = {
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    menuState.show {
                                                                        YouTubeSongMenu(
                                                                            song = song,
                                                                            navController = navController,
                                                                            onDismiss = menuState::dismiss
                                                                        )
                                                                    }
                                                                }
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else if (sectionData.title.equals("Listen together", ignoreCase = true)) {
                                    item(key = "home_section_list_${section.index}") {
                                        LazyRow(
                                            contentPadding = WindowInsets.systemBars
                                                .only(WindowInsetsSides.Horizontal)
                                                .asPaddingValues(),
                                            modifier = Modifier.animateItem()
                                        ) {
                                            items(
                                                items = sectionData.items.distinctBy { it.id },
                                                key = { it.id }
                                            ) { item ->
                                                if (item is SongItem) {
                                                    YouTubeGridItem(
                                                        item = item,
                                                        isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
                                                        isPlaying = isPlaying,
                                                        coroutineScope = scope,
                                                        thumbnailRatio = 16f / 9f,
                                                        modifier = Modifier
                                                            .width(240.dp)
                                                            .combinedClickable(
                                                                onClick = {
                                                                    if (item.id == mediaMetadata?.id) {
                                                                        playerConnection.player.togglePlayPause()
                                                                    } else {
                                                                        playerConnection.playQueue(
                                                                            YouTubeQueue(
                                                                                item.endpoint ?: WatchEndpoint(videoId = item.id),
                                                                                item.toMediaMetadata()
                                                                            )
                                                                        )
                                                                    }
                                                                },
                                                                onLongClick = {
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    menuState.show {
                                                                        YouTubeSongMenu(
                                                                            song = item,
                                                                            navController = navController,
                                                                            onDismiss = menuState::dismiss
                                                                        )
                                                                    }
                                                                }
                                                            )
                                                    )
                                                } else {
                                                    ytGridItem(item)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    item(key = "home_section_list_${section.index}") {
                                        LazyRow(
                                            contentPadding = WindowInsets.systemBars
                                                .only(WindowInsetsSides.Horizontal)
                                                .asPaddingValues(),
                                            modifier = Modifier.animateItem()
                                        ) {
                                            items(
                                                items = sectionData.items.distinctBy { it.id },
                                                key = { it.id }
                                            ) { item ->
                                                ytGridItem(item)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.MoodAndGenres -> {
                            explorePage?.moodAndGenres?.let { moodAndGenres ->
                                item(key = "mood_and_genres_title") {
                                    NavigationTitle(
                                        title = stringResource(R.string.mood_and_genres),
                                        onClick = { navController.navigate("mood_and_genres") },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                                item(key = "mood_and_genres_list") {
                                    LazyHorizontalGrid(
                                        rows = GridCells.Fixed(4),
                                        contentPadding = PaddingValues(6.dp),
                                        modifier = Modifier
                                            .height((MoodAndGenresButtonHeight + 12.dp) * 4 + 12.dp)
                                            .animateItem()
                                    ) {
                                        items(moodAndGenres) {
                                            MoodAndGenresButton(
                                                title = it.title,
                                                onClick = {
                                                    navController.navigate("youtube_browse/${it.endpoint.browseId}?params=${it.endpoint.params}")
                                                },
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .width(180.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading || (activeHomePage?.continuation != null && activeHomePage?.sections?.isNotEmpty() == true)) {
                    item(key = "loading_shimmer") {
                        ShimmerHost(
                            modifier = Modifier.animateItem()
                        ) {
                            repeat(2) {
                                TextPlaceholder(
                                    height = 36.dp,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .width(250.dp),
                                )
                                LazyRow(
                                    contentPadding = WindowInsets.systemBars
                                        .only(WindowInsetsSides.Horizontal)
                                        .asPaddingValues(),
                                ) {
                                    items(4) {
                                        GridItemPlaceHolder()
                                    }
                                }
                            }

                            TextPlaceholder(
                                height = 36.dp,
                                modifier = Modifier
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                                    .width(250.dp),
                            )
                            repeat(4) {
                                Row {
                                    repeat(2) {
                                        TextPlaceholder(
                                            height = MoodAndGenresButtonHeight,
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp)
                                                .width(200.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            HideOnScrollFAB(
                visible = allLocalItems.isNotEmpty() || allYtItems.isNotEmpty(),
                lazyListState = lazylistState,
                icon = Icons.Rounded.Casino,
                onClick = {
                    val local = when {
                        allLocalItems.isNotEmpty() && allYtItems.isNotEmpty() -> Random.nextFloat() < 0.5
                        allLocalItems.isNotEmpty() -> true
                        else -> false
                    }
                    scope.launch(Dispatchers.Main) {
                        if (local) {
                            when (val luckyItem = allLocalItems.random()) {
                                is Song -> playerConnection.playQueue(YouTubeQueue.radio(luckyItem.toMediaMetadata()))
                                is Album -> {
                                    val songs = withContext(Dispatchers.IO) {
                                        database.albumSongs(luckyItem.id).first()
                                    }
                                    if (songs.isNotEmpty()) {
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = luckyItem.title,
                                                items = songs.map(Song::toMediaMetadata)
                                            )
                                        )
                                    }
                                }
                                is Artist -> {}
                                is Playlist -> {}
                            }
                        } else {
                            when (val luckyItem = allYtItems.random()) {
                                is SongItem -> playerConnection.playQueue(YouTubeQueue.radio(luckyItem.toMediaMetadata()))
                                is AlbumItem -> playerConnection.playQueue(YouTubeAlbumRadio(luckyItem.playlistId))
                                is ArtistItem -> luckyItem.radioEndpoint?.let {
                                    playerConnection.playQueue(YouTubeQueue(it), isRadio = true)
                                }
                                is PlaylistItem -> luckyItem.playEndpoint?.let {
                                    playerConnection.playQueue(YouTubeQueue(it), isRadio = true)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    thumbnail: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onPlayAllClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        thumbnail?.invoke()

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            label?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }

        if (onPlayAllClick != null) {
            FilledTonalIconButton(
                onClick = onPlayAllClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.play)
                )
            }
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

package com.gumlaybeg.nimbletune.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gumlaybeg.nimbletune.LocalDatabase
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.GridThumbnailHeight
import com.gumlaybeg.nimbletune.constants.InnerTubeCookieKey
import com.gumlaybeg.nimbletune.constants.ListItemHeight
import com.gumlaybeg.nimbletune.constants.ListThumbnailSize
import com.gumlaybeg.nimbletune.constants.RandomizeHomeOrderKey
import com.gumlaybeg.nimbletune.constants.ThumbnailCornerRadius
import com.gumlaybeg.nimbletune.db.entities.Album
import com.gumlaybeg.nimbletune.db.entities.Artist
import com.gumlaybeg.nimbletune.db.entities.LocalItem
import com.gumlaybeg.nimbletune.db.entities.Playlist
import com.gumlaybeg.nimbletune.db.entities.Song
import com.gumlaybeg.nimbletune.extensions.togglePlayPause
import com.gumlaybeg.nimbletune.models.toMediaMetadata
import com.gumlaybeg.nimbletune.playback.queues.ListQueue
import com.gumlaybeg.nimbletune.playback.queues.YouTubeAlbumRadio
import com.gumlaybeg.nimbletune.playback.queues.YouTubeQueue
import com.gumlaybeg.nimbletune.ui.component.ActiveBoxAlpha
import com.gumlaybeg.nimbletune.ui.component.AlbumGridItem
import com.gumlaybeg.nimbletune.ui.component.ArtistGridItem
import com.gumlaybeg.nimbletune.ui.component.ChipsRow
import com.gumlaybeg.nimbletune.ui.component.HideOnScrollFAB
import com.gumlaybeg.nimbletune.ui.component.LocalMenuState
import com.gumlaybeg.nimbletune.ui.component.NavigationTitle
import com.gumlaybeg.nimbletune.ui.component.PlayingIndicatorBox
import com.gumlaybeg.nimbletune.ui.component.SongGridItem
import com.gumlaybeg.nimbletune.ui.component.SongListItem
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
import com.gumlaybeg.nimbletune.ui.utils.resize
import com.gumlaybeg.nimbletune.utils.rememberPreference
import com.gumlaybeg.nimbletune.viewmodels.DailyDiscoverItem
import com.gumlaybeg.nimbletune.viewmodels.HomeViewModel
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.pages.HomePage
import com.music.innertube.utils.parseCookieString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

sealed class HomeSection(val id: String, val baseWeight: Int) {
    data object SpeedDial : HomeSection("speed_dial", 100)
    data object QuickPicks : HomeSection("quick_picks", 90)
    data object CoversAndRemixes : HomeSection("covers_and_remixes", 85)
    data object DailyDiscover : HomeSection("daily_discover", 80)
    data object KeepListening : HomeSection("keep_listening", 50)
    data object AccountPlaylists : HomeSection("account_playlists", 40)
    data object ForgottenFavorites : HomeSection("forgotten_favorites", 30)
    data object FromTheCommunity : HomeSection("from_the_community", 20)
    data class SimilarRecommendation(val index: Int) : HomeSection("similar_recommendation_$index", 10)
    data class HomePageSection(val index: Int) : HomeSection("home_page_section_$index", 10)
    data object MoodAndGenres : HomeSection("mood_and_genres", 5)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyDiscoverCard(
    dailyDiscover: DailyDiscoverItem,
    onClick: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val database = LocalDatabase.current
    val playCount by database.getLifetimePlayCount(dailyDiscover.recommendation.id).collectAsState(initial = 0)
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current

    val song = dailyDiscover.recommendation as? SongItem

    Card(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (song != null) {
                        menuState.show {
                            YouTubeSongMenu(
                                song = song,
                                navController = navController,
                                onDismiss = { menuState.dismiss() }
                            )
                        }
                    }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            var currentThumbnailUrl by remember(dailyDiscover.recommendation.thumbnail) {
                mutableStateOf(dailyDiscover.recommendation.thumbnail?.resize(1200, 1200))
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentThumbnailUrl)
                    .crossfade(true)
                    .build(),
                onError = {
                    val url = currentThumbnailUrl
                    if (url != null && url.contains("maxresdefault.jpg")) {
                        currentThumbnailUrl = url.replace("maxresdefault.jpg", "hqdefault.jpg")
                    }
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (maxWidth > 200.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = dailyDiscover.recommendation.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = buildString {
                                append((dailyDiscover.recommendation as? SongItem)?.artists?.joinToString(", ") { it.name } ?: "")
                                if (playCount > 0) {
                                    append(" • $playCount plays")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val prefixes = listOf(
                        "Sounds like",
                        "Because you listen to",
                        "Similar to",
                        "Based on",
                        "For fans of"
                    )
                    val prefix = remember(dailyDiscover.seed.id) {
                        prefixes[abs(dailyDiscover.seed.id.hashCode()) % prefixes.size]
                    }

                    Text(
                        text = "$prefix ${dailyDiscover.seed.title} • ${dailyDiscover.seed.artists.joinToString(", ") { it.name }}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val haptic = LocalHapticFeedback.current

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val similarRecommendations by viewModel.similarRecommendations.collectAsState()
    val accountPlaylists by viewModel.accountPlaylists.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()
    val dailyDiscover by viewModel.dailyDiscover.collectAsState()
    val coversAndRemixes by viewModel.coversAndRemixes.collectAsState()
    val communityPlaylists by viewModel.communityPlaylists.collectAsState()

    val allLocalItems by viewModel.allLocalItems.collectAsState()
    val allYtItems by viewModel.allYtItems.collectAsState()
    val speedDialItems by viewModel.speedDialItems.collectAsState()
    val pinnedSpeedDialItems by viewModel.pinnedSpeedDialItems.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isRandomizing by viewModel.isRandomizing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    val quickPicksLazyGridState = rememberLazyGridState()
    val forgottenFavoritesLazyGridState = rememberLazyGridState()

    val accountName by viewModel.accountName.collectAsState()
    val accountImageUrl by viewModel.accountImageUrl.collectAsState()
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val (randomizeHomeOrder) = rememberPreference(RandomizeHomeOrderKey, true)

    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val url = if (isLoggedIn) accountImageUrl else null

    val scope = rememberCoroutineScope()
    var randomizeJob by remember { mutableStateOf<Job?>(null) }

    val lazylistState = rememberLazyListState()
    val currentGridHeight = GridThumbnailHeight
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    var randomSeed by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            randomSeed = System.currentTimeMillis()
        }
    }

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { lazylistState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val len = lazylistState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && lastVisibleIndex >= len - 3) {
                    viewModel.loadMoreYouTubeItems(homePage?.continuation)
                }
            }
    }

    if (selectedChip != null) {
        BackHandler {
            viewModel.toggleChip(selectedChip)
        }
    }

    val localGridItem: @Composable (LocalItem) -> Unit = {
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
                                playerConnection.playQueue(
                                    YouTubeQueue.radio(it.toMediaMetadata()),
                                )
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
                        onClick = {
                            navController.navigate("album/${it.id}")
                        },
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
                        onClick = {
                            navController.navigate("artist/${it.id}")
                        },
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
        randomizeHomeOrder,
        randomSeed,
        selectedChip,
        speedDialItems,
        quickPicks,
        coversAndRemixes,
        dailyDiscover,
        keepListening,
        accountPlaylists,
        forgottenFavorites,
        communityPlaylists,
        similarRecommendations,
        homePage?.sections,
        explorePage?.moodAndGenres,
        quickPicksLocalized
    ) {
        val list = mutableListOf<HomeSection>()
        val chipActive = selectedChip != null

        if (!chipActive && speedDialItems.isNotEmpty()) list.add(HomeSection.SpeedDial)
        if (!chipActive && quickPicks?.isNotEmpty() == true) list.add(HomeSection.QuickPicks)
        if (!chipActive && coversAndRemixes?.items?.isNotEmpty() == true) list.add(HomeSection.CoversAndRemixes)
        if (!chipActive && communityPlaylists?.isNotEmpty() == true) list.add(HomeSection.FromTheCommunity)
        if (!chipActive && dailyDiscover?.isNotEmpty() == true) list.add(HomeSection.DailyDiscover)
        if (!chipActive && keepListening?.isNotEmpty() == true) list.add(HomeSection.KeepListening)
        if (!chipActive && accountPlaylists?.isNotEmpty() == true) list.add(HomeSection.AccountPlaylists)
        if (!chipActive && forgottenFavorites?.isNotEmpty() == true) list.add(HomeSection.ForgottenFavorites)

        if (!chipActive) {
            similarRecommendations?.indices?.forEach { i ->
                list.add(HomeSection.SimilarRecommendation(i))
            }
        }

        homePage?.sections?.indices?.forEach { i ->
            list.add(HomeSection.HomePageSection(i))
        }

        if (explorePage?.moodAndGenres != null) list.add(HomeSection.MoodAndGenres)

        val sortedList = if (randomizeHomeOrder) {
            list.sortedByDescending { section ->
                val sectionRandom = Random(randomSeed + section.id.hashCode())
                val base = when (section) {
                    HomeSection.QuickPicks -> 700
                    HomeSection.SpeedDial,
                    HomeSection.DailyDiscover -> 500
                    HomeSection.KeepListening,
                    HomeSection.AccountPlaylists,
                    HomeSection.ForgottenFavorites,
                    HomeSection.FromTheCommunity -> 300
                    else -> 100
                }
                val modifier = when (section) {
                    HomeSection.QuickPicks -> 0
                    HomeSection.SpeedDial,
                    HomeSection.CoversAndRemixes,
                    HomeSection.DailyDiscover -> sectionRandom.nextInt(-200, 400)
                    HomeSection.KeepListening,
                    HomeSection.AccountPlaylists,
                    HomeSection.ForgottenFavorites,
                    HomeSection.FromTheCommunity -> sectionRandom.nextInt(-100, 400)
                    else -> sectionRandom.nextInt(-50, 50)
                }
                base + modifier
            }
        } else {
            val defaultOrder = mapOf(
                HomeSection.QuickPicks to 110,
                HomeSection.SpeedDial to 100,
                HomeSection.CoversAndRemixes to 85,
                HomeSection.FromTheCommunity to 80,
                HomeSection.DailyDiscover to 70,
                HomeSection.KeepListening to 60,
                HomeSection.AccountPlaylists to 50,
                HomeSection.ForgottenFavorites to 40,
                HomeSection.MoodAndGenres to 10
            )

            list.sortedByDescending { section ->
                when (section) {
                    is HomeSection.SimilarRecommendation -> 30 - section.index
                    is HomeSection.HomePageSection -> 20 - section.index
                    else -> defaultOrder[section] ?: 0
                }
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
            it is HomeSection.HomePageSection && isYouTubeQuickPicksSection(homePage?.sections?.getOrNull(it.index))
        }

        val deduplicatedList = if (hasYouTubeQuickPicks && quickPicks.isNullOrEmpty()) {
            sortedList.filterNot { it == HomeSection.QuickPicks }
        } else if (hasYouTubeQuickPicks) {
            sortedList.filterNot { it is HomeSection.HomePageSection && isYouTubeQuickPicksSection(homePage?.sections?.getOrNull(it.index)) }
        } else {
            sortedList
        }

        val isQuickPicksSection: (HomeSection) -> Boolean = {
            it == HomeSection.QuickPicks || (it is HomeSection.HomePageSection && isYouTubeQuickPicksSection(homePage?.sections?.getOrNull(it.index)))
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
                item {
                    ChipsRow(
                        chips = homePage?.chips
                            ?.filter { !it.title.equals("Podcasts", ignoreCase = true) }
                            ?.map { it to it.title } ?: emptyList(),
                        currentValue = selectedChip,
                        onValueUpdate = {
                            viewModel.toggleChip(it)
                        }
                    )
                }

                if (isLoading && homePage?.chips.isNullOrEmpty()) {
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
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.width(72.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                homeSections.forEach { section ->
                    when (section) {
                        HomeSection.SpeedDial -> {
                            speedDialItems.takeIf { it.isNotEmpty() }?.let { items ->
                                item(key = "speed_dial_title") {
                                    NavigationTitle(
                                        title = "Speed dial",
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                item(key = "speed_dial_list") {
                                    val targetItemSize = 160.dp
                                    val availableWidth = maxWidth - 32.dp
                                    val columns = (availableWidth / targetItemSize).toInt().coerceAtLeast(3)
                                    val rows = if (columns >= 6) 1 else if (columns >= 4) 2 else 3
                                    val itemsPerPage = columns * rows
                                    val itemWidth = availableWidth / columns

                                    val totalSlots = items.size + 1
                                    val pagerState = rememberPagerState(pageCount = { (totalSlots + itemsPerPage - 1) / itemsPerPage })

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem(),
                                    ) {
                                        HorizontalPager(
                                            state = pagerState,
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            pageSpacing = 16.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(itemWidth * rows),
                                        ) { page ->
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                for (row in 0 until rows) {
                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                        for (col in 0 until columns) {
                                                            val itemIndex = row * columns + col
                                                            val globalItemIndex = page * itemsPerPage + itemIndex
                                                            val isRandomizeSlot = (globalItemIndex == itemsPerPage - 1)

                                                            if (isRandomizeSlot) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .width(itemWidth)
                                                                        .height(itemWidth)
                                                                        .padding(4.dp)
                                                                ) {
                                                                    RandomizeGridItem(
                                                                        isLoading = isRandomizing,
                                                                        onClick = {
                                                                            if (isRandomizing) {
                                                                                randomizeJob?.cancel()
                                                                            } else {
                                                                                randomizeJob = scope.launch {
                                                                                    val randomItem = viewModel.getRandomItem()
                                                                                    if (randomItem != null) {
                                                                                        when (randomItem) {
                                                                                            is SongItem -> playerConnection.playQueue(
                                                                                                YouTubeQueue(
                                                                                                    randomItem.endpoint ?: WatchEndpoint(videoId = randomItem.id),
                                                                                                    randomItem.toMediaMetadata()
                                                                                                )
                                                                                            )
                                                                                            is AlbumItem -> navController.navigate("album/${randomItem.id}")
                                                                                            is ArtistItem -> navController.navigate("artist/${randomItem.id}")
                                                                                            is PlaylistItem -> {
                                                                                                val rawType = pinnedSpeedDialItems
                                                                                                    .find { it.id == randomItem.id }?.type
                                                                                                if (rawType == "LOCAL_PLAYLIST") {
                                                                                                    navController.navigate("local_playlist/${randomItem.id}")
                                                                                                } else {
                                                                                                    navController.navigate("online_playlist/${randomItem.id}")
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    )
                                                                }
                                                            } else {
                                                                val actualIndex = if (globalItemIndex < itemsPerPage - 1) globalItemIndex else globalItemIndex - 1
                                                                if (actualIndex < items.size) {
                                                                    val item = items[actualIndex]
                                                                    val isPinned = pinnedSpeedDialItems.any { it.id == item.id }

                                                                    Box(
                                                                        modifier = Modifier
                                                                            .width(itemWidth)
                                                                            .height(itemWidth)
                                                                            .padding(4.dp)
                                                                    ) {
                                                                        SpeedDialGridItem(
                                                                            item = item,
                                                                            isPinned = isPinned,
                                                                            isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
                                                                            isPlaying = isPlaying,
                                                                            modifier = Modifier
                                                                                .fillMaxSize()
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
                                                                                            is PlaylistItem -> {
                                                                                                val rawType = pinnedSpeedDialItems
                                                                                                    .find { it.id == item.id }?.type
                                                                                                if (rawType == "LOCAL_PLAYLIST") {
                                                                                                    navController.navigate("local_playlist/${item.id}")
                                                                                                } else {
                                                                                                    navController.navigate("online_playlist/${item.id}")
                                                                                                }
                                                                                            }
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
                                                                } else {
                                                                    Spacer(modifier = Modifier.width(itemWidth))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (pagerState.pageCount > 1) {
                                            Row(
                                                modifier = Modifier
                                                    .height(24.dp)
                                                    .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                repeat(pagerState.pageCount) { iteration ->
                                                    val color = if (pagerState.currentPage == iteration)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(4.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                            .size(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.QuickPicks -> {
                            quickPicks?.takeIf { it.isNotEmpty() }?.let { quickPicks ->
                                item(key = "quick_picks_title") {
                                    val quickPicksTitle = stringResource(R.string.quick_picks)
                                    NavigationTitle(
                                        title = quickPicksTitle,
                                        modifier = Modifier.animateItem(),
                                        onPlayAllClick = {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = quickPicksTitle,
                                                    items = quickPicks.distinctBy { it.id }.map { it.toMediaMetadata() }
                                                )
                                            )
                                        }
                                    )
                                }

                                item(key = "quick_picks_list") {
                                    LazyHorizontalGrid(
                                        state = quickPicksLazyGridState,
                                        rows = GridCells.Fixed(4),
                                        flingBehavior = rememberSnapFlingBehavior(quickPicksSnapLayoutInfoProvider),
                                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(ListItemHeight * 4)
                                            .animateItem()
                                    ) {
                                        itemsIndexed(
                                            items = quickPicks.distinctBy { it.id },
                                            key = { _, it -> it.id }
                                        ) { index, originalSong ->
                                            val song by database.song(originalSong.id).collectAsState(initial = originalSong)
                                            val resolvedSong = song ?: originalSong

                                            Surface(
                                                shape = listItemShape(index = index % 4, total = 4),
                                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                modifier = Modifier
                                                    .width(horizontalLazyGridItemWidth)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                SongListItem(
                                                    song = resolvedSong,
                                                    showInLibraryIcon = true,
                                                    enableSwipeToQueue = false,
                                                    onPlay = {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue.radio(resolvedSong.toMediaMetadata())
                                                        )
                                                    },
                                                    onSelectedChange = {},
                                                    inSelectMode = null,
                                                    isSelected = false,
                                                    navController = navController,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.CoversAndRemixes -> {
                            coversAndRemixes?.takeIf { it.items.isNotEmpty() }?.let { mixSection ->
                                val mixSongs = mixSection.items.filterIsInstance<SongItem>()
                                if (mixSongs.isEmpty()) return@let

                                item(key = "covers_and_remixes_title") {
                                    val mixTitle = mixSection.title
                                    NavigationTitle(
                                        title = mixTitle,
                                        modifier = Modifier.animateItem(),
                                        onPlayAllClick = {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = mixTitle,
                                                    items = mixSongs.distinctBy { it.id }.map { it.toMediaMetadata() }
                                                )
                                            )
                                        }
                                    )
                                }

                                item(key = "covers_and_remixes_list") {
                                    LazyHorizontalGrid(
                                        state = rememberLazyGridState(),
                                        rows = GridCells.Fixed(4),
                                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(ListItemHeight * 4)
                                            .animateItem()
                                    ) {
                                        itemsIndexed(
                                            items = mixSongs.distinctBy { it.id },
                                            key = { _, it -> it.id }
                                        ) { index, song ->
                                            Surface(
                                                shape = listItemShape(index = index % 4, total = 4),
                                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                modifier = Modifier
                                                    .width(horizontalLazyGridItemWidth)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
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
                            }
                        }

                        HomeSection.FromTheCommunity -> {
                            communityPlaylists?.takeIf { it.isNotEmpty() }?.let { playlists ->
                                item(key = "community_playlists_title") {
                                    NavigationTitle(
                                        title = "From the community",
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                item(key = "community_playlists_content") {
                                    LazyRow(
                                        contentPadding = WindowInsets.systemBars
                                            .only(WindowInsetsSides.Horizontal)
                                            .asPaddingValues(),
                                        modifier = Modifier.animateItem()
                                    ) {
                                        items(
                                            items = playlists.distinctBy { it.id },
                                            key = { it.id },
                                        ) { item ->
                                            ytGridItem(item)
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.DailyDiscover -> {
                            dailyDiscover?.takeIf { it.isNotEmpty() }?.let { discoverList ->
                                item(key = "daily_discover_title") {
                                    val title = "Your daily discover"
                                    NavigationTitle(
                                        title = title,
                                        onPlayAllClick = {
                                            val queueItems = discoverList.mapNotNull {
                                                (it.recommendation as? SongItem)?.toMediaMetadata()
                                            }
                                            if (queueItems.isNotEmpty()) {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = title,
                                                        items = queueItems
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                                item(key = "daily_discover_content") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(230.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val carouselState = rememberCarouselState { discoverList.size }
                                        HorizontalUncontainedCarousel(
                                            state = carouselState,
                                            itemWidth = 320.dp,
                                            itemSpacing = 8.dp,
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(210.dp)
                                        ) { i ->
                                            val item = discoverList[i]
                                            DailyDiscoverCard(
                                                dailyDiscover = item,
                                                onClick = {
                                                    val song = item.recommendation as? SongItem
                                                    val meta = song?.toMediaMetadata()
                                                    if (meta != null) {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue(
                                                                song.endpoint ?: WatchEndpoint(videoId = song.id),
                                                                meta
                                                            )
                                                        )
                                                    }
                                                },
                                                navController = navController,
                                                modifier = Modifier.maskClip(MaterialTheme.shapes.large)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.KeepListening -> {
                            keepListening?.takeIf { it.isNotEmpty() }?.let { keepListening ->
                                item(key = "keep_listening_title") {
                                    NavigationTitle(
                                        title = stringResource(R.string.keep_listening),
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                item(key = "keep_listening_list") {
                                    val rows = if (keepListening.size > 6) 2 else 1
                                    LazyHorizontalGrid(
                                        state = rememberLazyGridState(),
                                        rows = GridCells.Fixed(rows),
                                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((currentGridHeight + 24.dp + with(LocalDensity.current) {
                                                MaterialTheme.typography.bodyLarge.lineHeight.toDp() * 2 +
                                                    MaterialTheme.typography.bodyMedium.lineHeight.toDp() * 2
                                            }) * rows)
                                            .animateItem()
                                    ) {
                                        items(keepListening) {
                                            localGridItem(it)
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.AccountPlaylists -> {
                            accountPlaylists?.takeIf { it.isNotEmpty() }?.let { accountPlaylists ->
                                item(key = "account_playlists_title") {
                                    NavigationTitle(
                                        label = stringResource(R.string.your_youtube_playlists),
                                        title = accountName,
                                        thumbnail = {
                                            if (url != null) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(url)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .diskCacheKey(url)
                                                        .crossfade(false)
                                                        .build(),
                                                    placeholder = rememberVectorPainter(Icons.Rounded.Person),
                                                    error = rememberVectorPainter(Icons.Rounded.Person),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(ListThumbnailSize)
                                                        .clip(CircleShape)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(ListThumbnailSize)
                                                )
                                            }
                                        },
                                        onClick = {
                                            navController.navigate("account")
                                        },
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
                                            items = accountPlaylists.distinctBy { it.id },
                                            key = { it.id },
                                        ) { item ->
                                            ytGridItem(item)
                                        }
                                    }
                                }
                            }
                        }

                        HomeSection.ForgottenFavorites -> {
                            forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { forgottenFavorites ->
                                item(key = "forgotten_favorites_title") {
                                    val forgottenFavoritesTitle = stringResource(R.string.forgotten_favorites)
                                    NavigationTitle(
                                        title = forgottenFavoritesTitle,
                                        modifier = Modifier.animateItem(),
                                        onPlayAllClick = {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = forgottenFavoritesTitle,
                                                    items = forgottenFavorites.distinctBy { it.id }.map { it.toMediaMetadata() }
                                                )
                                            )
                                        }
                                    )
                                }

                                item(key = "forgotten_favorites_list") {
                                    val rows = min(4, forgottenFavorites.size)
                                    LazyHorizontalGrid(
                                        state = forgottenFavoritesLazyGridState,
                                        rows = GridCells.Fixed(rows),
                                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                        flingBehavior = rememberSnapFlingBehavior(forgottenFavoritesSnapLayoutInfoProvider),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(ListItemHeight * rows)
                                            .animateItem()
                                    ) {
                                        itemsIndexed(
                                            items = forgottenFavorites.distinctBy { it.id },
                                            key = { _, it -> it.id }
                                        ) { index, originalSong ->
                                            val song by database.song(originalSong.id).collectAsState(initial = originalSong)
                                            val resolvedSong = song ?: originalSong

                                            Surface(
                                                shape = listItemShape(index = index % rows, total = rows),
                                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                modifier = Modifier
                                                    .width(horizontalLazyGridItemWidth)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                SongListItem(
                                                    song = resolvedSong,
                                                    showInLibraryIcon = true,
                                                    enableSwipeToQueue = false,
                                                    onPlay = {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue.radio(resolvedSong.toMediaMetadata())
                                                        )
                                                    },
                                                    onSelectedChange = {},
                                                    inSelectMode = null,
                                                    isSelected = false,
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
                                                val shape = if (recommendation.title is Artist) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
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
                                        items(recommendation.items) { item ->
                                            ytGridItem(item)
                                        }
                                    }
                                }
                            }
                        }

                        is HomeSection.HomePageSection -> {
                            val sectionData = homePage?.sections?.getOrNull(section.index)
                            sectionData?.let {
                                val sectionSongs = sectionData.items.filterIsInstance<SongItem>()
                                val hasPlayableSongs = sectionSongs.isNotEmpty()
                                val isSongsOnlySection = sectionData.items.isNotEmpty() &&
                                    sectionData.items.all { it is SongItem } &&
                                    !sectionData.title.equals("Listen together", ignoreCase = true)

                                item(key = "home_section_title_${section.index}") {
                                    NavigationTitle(
                                        title = sectionData.title,
                                        label = sectionData.label,
                                        thumbnail = sectionData.thumbnail?.let { thumbnailUrl ->
                                            {
                                                val shape = if (sectionData.endpoint?.isArtistEndpoint == true) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
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
                                        LazyHorizontalGrid(
                                            state = rememberLazyGridState(),
                                            rows = GridCells.Fixed(4),
                                            contentPadding = WindowInsets.systemBars
                                                .only(WindowInsetsSides.Horizontal)
                                                .asPaddingValues(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(ListItemHeight * 4)
                                                .animateItem()
                                        ) {
                                            itemsIndexed(
                                                items = sectionSongs.distinctBy { it.id },
                                                key = { _, it -> it.id }
                                            ) { index, song ->
                                                Surface(
                                                    shape = listItemShape(index = index % 4, total = 4),
                                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                    modifier = Modifier
                                                        .width(horizontalLazyGridItemWidth)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
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
                                            items(sectionData.items) { item ->
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
                                            items(sectionData.items) { item ->
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
                                        onClick = {
                                            navController.navigate("mood_and_genres")
                                        },
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

                if (isLoading || (homePage?.continuation != null && homePage?.sections?.isNotEmpty() == true)) {
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
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp)
                                                .width(200.dp)
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
                icon = R.drawable.shuffle,
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
                                        database.albumWithSongs(luckyItem.id).first()?.songs.orEmpty()
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
                                    playerConnection.playQueue(YouTubeQueue(it))
                                }
                                is PlaylistItem -> luckyItem.playEndpoint?.let {
                                    playerConnection.playQueue(YouTubeQueue(it))
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
fun SpeedDialGridItem(
    item: YTItem,
    isPinned: Boolean,
    isActive: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = if (item is ArtistItem) CircleShape else RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = item.thumbnail?.resize(544, 544),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.5f to Color.Black.copy(alpha = 0.2f),
                        1.0f to Color.Black.copy(alpha = 0.85f)
                    )
                )
        )

        if (isPinned) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PushPin,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        if (item is AlbumItem || item is PlaylistItem) {
            Icon(
                imageVector = if (item is AlbumItem) Icons.Rounded.Album else Icons.AutoMirrored.Rounded.QueueMusic,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(16.dp)
            )
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (item is ArtistItem) TextAlign.Center else TextAlign.Start,
            modifier = Modifier
                .align(if (item is ArtistItem) Alignment.BottomCenter else Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )

        PlayingIndicatorBox(
            isActive = isActive,
            playWhenReady = isPlaying,
            color = Color.White,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.Black.copy(alpha = ActiveBoxAlpha),
                    shape = shape
                )
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RandomizeGridItem(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .combinedClickable(onClick = onClick)
    ) {
        if (isLoading) {
            ContainedLoadingIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Casino,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Randomize",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

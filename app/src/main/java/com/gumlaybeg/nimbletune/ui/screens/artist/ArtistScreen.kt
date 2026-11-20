package com.gumlaybeg.nimbletune.ui.screens.artist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.google.material.color.score.Score
import com.gumlaybeg.nimbletune.LocalDatabase
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.db.entities.ArtistEntity
import com.gumlaybeg.nimbletune.extensions.toMediaItem
import com.gumlaybeg.nimbletune.extensions.togglePlayPause
import com.gumlaybeg.nimbletune.models.toMediaMetadata
import com.gumlaybeg.nimbletune.playback.queues.ListQueue
import com.gumlaybeg.nimbletune.playback.queues.YouTubeQueue
import com.gumlaybeg.nimbletune.ui.component.AlbumGridItem
import com.gumlaybeg.nimbletune.ui.component.LocalMenuState
import com.gumlaybeg.nimbletune.ui.component.NavigationTitle
import com.gumlaybeg.nimbletune.ui.component.SongListItem
import com.gumlaybeg.nimbletune.ui.component.SwipeToQueueBox
import com.gumlaybeg.nimbletune.ui.component.YouTubeGridItem
import com.gumlaybeg.nimbletune.ui.component.YouTubeListItem
import com.gumlaybeg.nimbletune.ui.component.shimmer.ListItemPlaceHolder
import com.gumlaybeg.nimbletune.ui.component.shimmer.ShimmerHost
import com.gumlaybeg.nimbletune.ui.component.shimmer.TextPlaceholder
import com.gumlaybeg.nimbletune.ui.menu.AlbumMenu
import com.gumlaybeg.nimbletune.ui.menu.YouTubeAlbumMenu
import com.gumlaybeg.nimbletune.ui.menu.YouTubeArtistMenu
import com.gumlaybeg.nimbletune.ui.menu.YouTubePlaylistMenu
import com.gumlaybeg.nimbletune.ui.menu.YouTubeSongMenu
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.ui.utils.fadingEdge
import com.gumlaybeg.nimbletune.ui.utils.resize
import com.gumlaybeg.nimbletune.viewmodels.ArtistViewModel
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun ArtistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: ArtistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val artistPage = viewModel.artistPage
    val libraryArtist by viewModel.libraryArtist.collectAsState()
    val librarySongs by viewModel.librarySongs.collectAsState()
    val libraryAlbums by viewModel.libraryAlbums.collectAsState()

    val totalPlayCount = remember(librarySongs) {
        librarySongs.sumOf { song -> song.playCount?.sumOf { it.count } ?: 0 }
    }

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Gradient colors for mesh background
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface

    val thumbnail = artistPage?.artist?.thumbnail ?: libraryArtist?.artist?.thumbnailUrl
    val artistName = artistPage?.artist?.title ?: libraryArtist?.artist?.name ?: stringResource(R.string.unknown)
    val isLocalArtist = libraryArtist?.artist?.isLocalArtist == true

    // Skeleton loading state
    var isFetching by remember { mutableStateOf(true) }
    LaunchedEffect(artistPage, isLocalArtist) {
        if (artistPage != null || isLocalArtist) {
            isFetching = false
        }
    }
    LaunchedEffect(Unit) {
        delay(2500)
        isFetching = false
    }

    // Extract gradient colors from artist image
    LaunchedEffect(thumbnail) {
        if (thumbnail != null && !thumbnail.startsWith("/storage")) {
            val request = ImageRequest.Builder(context)
                .data(thumbnail)
                .size(100, 100)
                .allowHardware(false)
                .build()

            val result = runCatching {
                context.imageLoader.execute(request).drawable
            }.getOrNull()

            val bitmap = (result as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                gradientColors = withContext(Dispatchers.Default) {
                    val swatches = Palette.from(bitmap)
                        .maximumColorCount(16)
                        .resizeBitmapArea(100 * 100)
                        .generate()
                        .swatches
                        .associate { it.rgb to it.population }

                    if (swatches.isEmpty()) {
                        emptyList()
                    } else {
                        Score.score(swatches, 5, fallbackColor, true)
                            .map { Color(it) }
                            .sortedByDescending { it.luminance() }
                    }
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }

    // Calculate gradient opacity based on scroll position
    val gradientAlpha by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex == 0) {
                val offset = lazyListState.firstVisibleItemScrollOffset
                (1f - (offset / 800f)).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // Gradient background layer
        if (gradientColors.isNotEmpty() && gradientAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.65f)
                    .align(Alignment.TopCenter)
                    .zIndex(-1f)
                    .drawBehind {
                        val width = size.width
                        val height = size.height

                        if (gradientColors.size >= 3) {
                            val c0 = gradientColors[0]
                            val c1 = gradientColors[1]
                            val c2 = gradientColors[2]
                            val c3 = gradientColors.getOrElse(3) { c0 }
                            val c4 = gradientColors.getOrElse(4) { c1 }
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        c0.copy(alpha = gradientAlpha * 0.72f),
                                        c0.copy(alpha = gradientAlpha * 0.4f),
                                        Color.Transparent
                                    ),
                                    center = Offset(width * 0.5f, height * 0.2f),
                                    radius = width * 0.7f
                                )
                            )
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        c1.copy(alpha = gradientAlpha * 0.56f),
                                        c1.copy(alpha = gradientAlpha * 0.3f),
                                        Color.Transparent
                                    ),
                                    center = Offset(width * 0.15f, height * 0.35f),
                                    radius = width * 0.6f
                                )
                            )
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        c2.copy(alpha = gradientAlpha * 0.52f),
                                        c2.copy(alpha = gradientAlpha * 0.26f),
                                        Color.Transparent
                                    ),
                                    center = Offset(width * 0.85f, height * 0.45f),
                                    radius = width * 0.65f
                                )
                            )
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        c3.copy(alpha = gradientAlpha * 0.34f),
                                        c3.copy(alpha = gradientAlpha * 0.18f),
                                        Color.Transparent
                                    ),
                                    center = Offset(width * 0.35f, height * 0.6f),
                                    radius = width * 0.8f
                                )
                            )
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        c4.copy(alpha = gradientAlpha * 0.28f),
                                        c4.copy(alpha = gradientAlpha * 0.14f),
                                        Color.Transparent
                                    ),
                                    center = Offset(width * 0.55f, height * 0.85f),
                                    radius = width * 0.95f
                                )
                            )
                        } else if (gradientColors.isNotEmpty()) {
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        gradientColors[0].copy(alpha = gradientAlpha * 0.6f),
                                        gradientColors[0].copy(alpha = gradientAlpha * 0.3f),
                                        Color.Transparent
                                    ),
                                    center = Offset(width * 0.5f, height * 0.3f),
                                    radius = width * 0.8f
                                )
                            )
                        }

                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    surfaceColor.copy(alpha = gradientAlpha * 0.22f),
                                    surfaceColor.copy(alpha = gradientAlpha * 0.55f),
                                    surfaceColor
                                ),
                                startY = height * 0.4f,
                                endY = height
                            )
                        )
                    }
            )
        }

        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .asPaddingValues(),
        ) {
            if (isFetching && artistPage == null && libraryArtist == null) {
                item(key = "shimmer") {
                    ShimmerHost {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.onSurface)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            TextPlaceholder(
                                height = 32.dp,
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .padding(bottom = 16.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Spacer(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                            }

                            TextPlaceholder(modifier = Modifier.fillMaxWidth())
                            TextPlaceholder(modifier = Modifier.fillMaxWidth(0.8f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Spacer(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }

                        repeat(6) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            } else {
                item(key = "header") {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (thumbnail != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .fadingEdge(bottom = 80.dp)
                            ) {
                                AsyncImage(
                                    model = thumbnail.resize(1200, 1200),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(
                                    top = if (thumbnail == null) {
                                        LocalPlayerAwareWindowInsets.current
                                            .asPaddingValues()
                                            .calculateTopPadding() + 16.dp
                                    } else {
                                        16.dp
                                    }
                                )
                        ) {
                            // Artist Name
                            Text(
                                text = artistName,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 32.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Subscriber count, monthly listeners, and play count badges
                            if (artistPage?.subscriberCountText != null ||
                                artistPage?.monthlyListenerCount != null ||
                                totalPlayCount > 0
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    artistPage?.subscriberCountText?.let { subscribers ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = subscribers.split(" ").firstOrNull() ?: subscribers,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    artistPage?.monthlyListenerCount?.let { monthlyListeners ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = monthlyListeners.split(" ").firstOrNull() ?: monthlyListeners,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    if (totalPlayCount > 0) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.History,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = totalPlayCount.toString(),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // Expandable Artist Description
                            artistPage?.description?.takeIf { it.isNotBlank() }?.let { description ->
                                var isDescriptionExpanded by rememberSaveable { mutableStateOf(false) }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize()
                                ) {
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { isDescriptionExpanded = !isDescriptionExpanded }
                                            ),
                                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Action Buttons Row
                            val isSubscribed = libraryArtist?.artist?.bookmarkedAt != null
                            val radioEndpoint = artistPage?.artist?.radioEndpoint
                            val shuffleEndpoint = artistPage?.artist?.shuffleEndpoint ?: artistPage?.artist?.playEndpoint
                            val hasRadio = radioEndpoint != null
                            val hasShuffle = shuffleEndpoint != null || librarySongs.isNotEmpty()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            ) {
                                // Subscribe Button
                                ToggleButton(
                                    checked = isSubscribed,
                                    onCheckedChange = {
                                        val isSubscribing = !isSubscribed
                                        database.transaction {
                                            val artist = libraryArtist?.artist
                                            if (artist != null) {
                                                update(artist.localToggleLike())
                                            } else {
                                                artistPage?.artist?.let {
                                                    insert(
                                                        ArtistEntity(
                                                            id = it.id,
                                                            name = it.title,
                                                            channelId = it.channelId,
                                                            thumbnailUrl = it.thumbnail,
                                                        ).localToggleLike()
                                                    )
                                                }
                                            }
                                        }

                                        if (!isLocalArtist) {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    val artistId = artistPage?.artist?.id ?: libraryArtist?.artist?.id ?: return@launch
                                                    val channelId = artistPage?.artist?.channelId ?: libraryArtist?.artist?.channelId
                                                    val targetChannelId = channelId ?: YouTube.getChannelId(artistId)

                                                    if (targetChannelId.isNotEmpty()) {
                                                        YouTube.subscribeChannel(targetChannelId, isSubscribing)
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { role = Role.Button },
                                    shapes = if (hasRadio || hasShuffle) {
                                        ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    } else {
                                        ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.size)
                                    },
                                    colors = ToggleButtonDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                                        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isSubscribed)
                                            Icons.Rounded.NotificationsActive
                                        else
                                            Icons.Rounded.NotificationsNone,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (isSubscribed)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            LocalContentColor.current
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isSubscribed) "Subscribed" else "Subscribe",
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Radio Button
                                radioEndpoint?.let { endpoint ->
                                    ToggleButton(
                                        checked = false,
                                        onCheckedChange = {
                                            playerConnection.playQueue(
                                                YouTubeQueue(endpoint),
                                                isRadio = true,
                                                title = "Radio: $artistName"
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics { role = Role.Button },
                                        shapes = if (hasShuffle) {
                                            ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        } else {
                                            ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        },
                                        colors = ToggleButtonDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Radio,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.radio),
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Shuffle Button
                                if (hasShuffle) {
                                    ToggleButton(
                                        checked = false,
                                        onCheckedChange = {
                                            if (shuffleEndpoint != null && !isLocalArtist) {
                                                playerConnection.playQueue(
                                                    YouTubeQueue(shuffleEndpoint),
                                                    isRadio = true,
                                                    title = artistName
                                                )
                                            } else if (librarySongs.isNotEmpty()) {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = artistName,
                                                        items = librarySongs.map { it.toMediaMetadata() },
                                                        startShuffled = true,
                                                    )
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics { role = Role.Button },
                                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                        colors = ToggleButtonDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Shuffle,
                                            contentDescription = stringResource(R.string.shuffle),
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.shuffle),
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Library Songs Section
                if (librarySongs.isNotEmpty()) {
                    item(key = "library_songs_header") {
                        NavigationTitle(
                            title = stringResource(R.string.filter_library),
                            onClick = {
                                navController.navigate("artist/${viewModel.artistId}/songs")
                            }
                        )
                    }

                    itemsIndexed(
                        items = librarySongs.take(5),
                        key = { _, item -> "local_song_${item.id}" },
                    ) { index, song ->
                        SongListItem(
                            song = song,
                            onPlay = {
                                if (song.id == mediaMetadata?.id) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = artistName,
                                            items = librarySongs.map { it.toMediaMetadata() },
                                            startIndex = index
                                        )
                                    )
                                }
                            },
                            onSelectedChange = {},
                            inSelectMode = false,
                            isSelected = false,
                            navController = navController,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                        )
                    }

                    if (librarySongs.size > 5) {
                        item(key = "library_songs_view_all") {
                            Surface(
                                onClick = {
                                    navController.navigate("artist/${viewModel.artistId}/songs")
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "View all",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Library Albums Section (shown for local artists or offline)
                if (isLocalArtist && libraryAlbums.isNotEmpty()) {
                    item(key = "library_albums_header") {
                        NavigationTitle(
                            title = stringResource(R.string.albums),
                            onClick = {
                                navController.navigate("artist/${viewModel.artistId}/albums")
                            }
                        )
                    }

                    item(key = "library_albums_grid") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(
                                items = libraryAlbums,
                                key = { "local_album_${it.id}" }
                            ) { album ->
                                AlbumGridItem(
                                    album = album,
                                    isActive = album.id == mediaMetadata?.album?.id,
                                    isPlaying = isPlaying,
                                    coroutineScope = coroutineScope,
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = {
                                                navController.navigate("album/${album.id}")
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    AlbumMenu(
                                                        originalAlbum = album,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        )
                                        .animateItem()
                                )
                            }
                        }
                    }
                }

                // YouTube/Remote content sections
                val topSongsSection = artistPage?.sections?.firstOrNull { s -> s.items.all { it is SongItem } }

                artistPage?.sections?.fastForEach { section ->
                    if (section.items.isNotEmpty()) {
                        item(
                            key = "youtube_section_header_${section.title}_${section.items.firstOrNull()?.id.orEmpty()}_${section.moreEndpoint?.browseId.orEmpty()}",
                        ) {
                            NavigationTitle(
                                title = section.title,
                                onClick = section.moreEndpoint?.let {
                                    {
                                        navController.navigate(
                                            "artist/${viewModel.artistId}/items?browseId=${it.browseId}?params=${it.params}",
                                        )
                                    }
                                },
                            )
                        }
                    }

                    if (section === topSongsSection) {
                        val distinctSongs = section.items.distinctBy { it.id }.filterIsInstance<SongItem>()
                        items(
                            items = distinctSongs,
                            key = { "youtube_song_${it.id}" },
                        ) { song ->
                            SwipeToQueueBox(
                                item = song.toMediaItem(),
                                snackbarHostState = snackbarHostState,
                                content = {
                                    YouTubeListItem(
                                        item = song,
                                        isActive = mediaMetadata?.id == song.id,
                                        isPlaying = isPlaying,
                                        trailingContent = {
                                            IconButton(
                                                onClick = {
                                                    menuState.show {
                                                        YouTubeSongMenu(
                                                            song = song,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.MoreVert,
                                                    contentDescription = null,
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    if (song.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            ListQueue(
                                                                title = "${artistPage.artist.title}: ${section.title}",
                                                                items = distinctSongs.map { it.toMediaMetadata() },
                                                                startIndex = distinctSongs.indexOf(song).coerceAtLeast(0)
                                                            ),
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubeSongMenu(
                                                            song = song,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                    )
                                }
                            )
                        }
                    } else {
                        item(
                            key = "youtube_section_grid_${section.title}_${section.items.firstOrNull()?.id.orEmpty()}_${section.moreEndpoint?.browseId.orEmpty()}",
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(
                                    items = section.items.distinctBy { it.id },
                                    key = {
                                        val type = when (it) {
                                            is SongItem -> "song"
                                            is AlbumItem -> "album"
                                            is ArtistItem -> "artist"
                                            is PlaylistItem -> "playlist"
                                        }
                                        "youtube_${type}_${it.id}"
                                    },
                                ) { item ->
                                    YouTubeGridItem(
                                        item = item,
                                        isActive = when (item) {
                                            is SongItem -> mediaMetadata?.id == item.id
                                            is AlbumItem -> mediaMetadata?.album?.id == item.id
                                            else -> false
                                        },
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is SongItem ->
                                                            playerConnection.playQueue(
                                                                YouTubeQueue(
                                                                    WatchEndpoint(videoId = item.id),
                                                                    item.toMediaMetadata()
                                                                ),
                                                                title = artistName
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
                                                                onDismiss = menuState::dismiss,
                                                            )

                                                            is AlbumItem -> YouTubeAlbumMenu(
                                                                albumItem = item,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss,
                                                            )

                                                            is ArtistItem -> YouTubeArtistMenu(
                                                                artist = item,
                                                                onDismiss = menuState::dismiss,
                                                            )

                                                            is PlaylistItem -> YouTubePlaylistMenu(
                                                                playlist = item,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom spacing
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Top App Bar with circular translucent buttons
        TopAppBar(
            title = { },
            navigationIcon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    com.gumlaybeg.nimbletune.ui.component.IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            actions = {
                artistPage?.artist?.shareLink?.let { link ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Artist Link", link)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter)
        )
    }
}
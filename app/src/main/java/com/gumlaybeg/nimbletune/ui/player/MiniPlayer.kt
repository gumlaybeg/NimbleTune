@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.gumlaybeg.nimbletune.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explicit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.DarkModeKey
import com.gumlaybeg.nimbletune.constants.MiniPlayerHeight
import com.gumlaybeg.nimbletune.constants.PlayerBackgroundStyleKey
import com.gumlaybeg.nimbletune.constants.PureBlackKey
import com.gumlaybeg.nimbletune.constants.SwipeToSkip
import com.gumlaybeg.nimbletune.constants.ThumbnailCornerRadius
import com.gumlaybeg.nimbletune.extensions.currentMetadata
import com.gumlaybeg.nimbletune.extensions.metadata
import com.gumlaybeg.nimbletune.extensions.togglePlayPause
import com.gumlaybeg.nimbletune.models.MediaMetadata
import com.gumlaybeg.nimbletune.models.toMediaMetadata
import com.gumlaybeg.nimbletune.playback.PlayerConnection
import com.gumlaybeg.nimbletune.ui.component.AsyncImageLocal
import com.gumlaybeg.nimbletune.ui.screens.settings.DarkMode
import com.gumlaybeg.nimbletune.ui.screens.settings.PlayerBackgroundStyle
import com.gumlaybeg.nimbletune.ui.utils.imageCache
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference
import com.music.innertube.YouTube
import com.music.innertube.models.WatchEndpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

enum class MiniPlayerStyle {
    DEFAULT, MODERN, APPLE
}

private val MiniPlayerStyleKey = stringPreferencesKey("miniPlayerStyle")

private val miniPlayerExplicitStatusCache = ConcurrentHashMap<String, Boolean>()

@Composable
private fun MiniPlayerExplicitIcon(
    isExplicit: Boolean,
    mediaId: String?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val explicitTransitionState = remember(mediaId) {
        MutableTransitionState(false)
    }

    LaunchedEffect(isExplicit, mediaId) {
        explicitTransitionState.targetState = isExplicit
    }

    AnimatedVisibility(
        visibleState = explicitTransitionState,
        enter = fadeIn(tween(350)) +
            expandHorizontally(
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                expandFrom = Alignment.Start
            ) +
            scaleIn(
                initialScale = 0.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        exit = fadeOut(tween(200)) +
            shrinkHorizontally(
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                shrinkTowards = Alignment.Start
            ) +
            scaleOut(
                targetScale = 0.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        Icon(
            imageVector = Icons.Rounded.Explicit,
            contentDescription = "Explicit",
            tint = tint,
            modifier = modifier
        )
    }
}

@Composable
fun MiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
) {
    val pureBlack by rememberPreference(PureBlackKey, defaultValue = false)
    val miniPlayerStyle by rememberEnumPreference(MiniPlayerStyleKey, MiniPlayerStyle.DEFAULT)

    val playerConnection = LocalPlayerConnection.current ?: return
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    val swipeSensitivity = 0.73f
    val swipeThumbnail by rememberPreference(SwipeToSkip, true)

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT
    )
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val rawMediaMetadata = mediaMetadata ?: playerConnection.player.currentMediaItem?.metadata ?: playerConnection.player.currentMetadata
    val currentSongMetadata = currentSong?.toMediaMetadata()
    val currentMediaId = rawMediaMetadata?.id ?: currentSongMetadata?.id
    val currentAlbumId = rawMediaMetadata?.album?.id ?: currentSongMetadata?.album?.id

    var fetchedExplicit by remember(currentMediaId) {
        mutableStateOf(currentMediaId?.let { miniPlayerExplicitStatusCache[it] })
    }

    LaunchedEffect(currentMediaId, currentAlbumId) {
        val id = currentMediaId ?: return@LaunchedEffect
        if (id.startsWith("LA")) {
            fetchedExplicit = false
            return@LaunchedEffect
        }
        miniPlayerExplicitStatusCache[id]?.let { cached ->
            fetchedExplicit = cached
            return@LaunchedEffect
        }

        val resolvedExplicit = withContext(Dispatchers.IO) {
            val queueResult = YouTube.queue(listOf(id)).getOrNull()
                ?.firstOrNull { it.id == id }
            if (queueResult?.explicit == true) {
                return@withContext true
            }

            val fromAlbum = currentAlbumId?.takeIf { it.isNotBlank() && !it.startsWith("LA") }?.let { albumId ->
                YouTube.album(albumId).getOrNull()?.songs?.find { it.id == id }?.explicit
            }
            if (fromAlbum == true) {
                return@withContext true
            }

            if (queueResult == null) {
                val fromNext = YouTube.next(WatchEndpoint(videoId = id)).getOrNull()
                    ?.items?.firstOrNull { it.id == id }?.explicit
                if (fromNext != null) {
                    return@withContext fromNext
                }
            }

            if (queueResult != null || fromAlbum != null) false else null
        }

        if (resolvedExplicit != null) {
            miniPlayerExplicitStatusCache[id] = resolvedExplicit
            fetchedExplicit = resolvedExplicit
        }
    }

    val isExplicit = fetchedExplicit == true ||
        currentSong?.song?.title?.contains("explicit", ignoreCase = true) == true ||
        rawMediaMetadata?.title?.contains("explicit", ignoreCase = true) == true

    val isLiveMesh = playerBackground == PlayerBackgroundStyle.LIVE_MESH

    SwipeableMiniPlayerBox(
        modifier = modifier,
        swipeSensitivity = swipeSensitivity,
        swipeThumbnail = swipeThumbnail,
        playerConnection = playerConnection,
        layoutDirection = layoutDirection,
        coroutineScope = coroutineScope,
        pureBlack = pureBlack,
        useLegacyBackground = false
    ) { offsetX ->
        when (miniPlayerStyle) {
            MiniPlayerStyle.APPLE -> {
                AppleMiniPlayerContent(
                    offsetX = offsetX,
                    position = position,
                    duration = duration,
                    playerConnection = playerConnection,
                    pureBlack = pureBlack,
                    isLiveMesh = isLiveMesh,
                    isExplicit = isExplicit
                )
            }
            MiniPlayerStyle.MODERN -> {
                ModernMiniPlayerContent(
                    offsetX = offsetX,
                    position = position,
                    duration = duration,
                    playerConnection = playerConnection,
                    pureBlack = pureBlack,
                    isLiveMesh = isLiveMesh,
                    isExplicit = isExplicit
                )
            }
            MiniPlayerStyle.DEFAULT -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            color = if (isLiveMesh) Color.Black else MaterialTheme.colorScheme.surfaceContainer
                        )
                ) {
                    if (isLiveMesh && mediaMetadata?.thumbnailUrl != null && mediaMetadata?.isLocal != true) {
                        val infiniteTransition = rememberInfiniteTransition(label = "mini_mesh")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing)),
                            label = "mini_mesh_rot"
                        )
                        val saturationMatrix = remember { ColorMatrix().apply { setToSaturation(1.6f) } }

                        Box(modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 1.5f; scaleY = 1.5f }) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(mediaMetadata?.thumbnailUrl)
                                    .size(128, 128)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                colorFilter = ColorFilter.colorMatrix(saturationMatrix),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(40.dp)
                                    .graphicsLayer { rotationZ = rotation }
                            )
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                        }
                    }

                    NewMiniPlayerContent(
                        pureBlack = pureBlack,
                        position = position,
                        duration = duration,
                        playerConnection = playerConnection,
                        isLiveMesh = isLiveMesh,
                        isExplicit = isExplicit
                    )
                }
            }
        }
    }
}

@Composable
fun ModernMiniPlayerContent(
    offsetX: Float,
    position: Long,
    duration: Long,
    playerConnection: PlayerConnection,
    pureBlack: Boolean,
    isLiveMesh: Boolean,
    isExplicit: Boolean = false
) {
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val backgroundColor = if (pureBlack && useDarkTheme) Color.Black else MaterialTheme.colorScheme.surfaceContainerHigh

    val primaryColor = if (isLiveMesh) Color.White else MaterialTheme.colorScheme.primary
    val outlineColor = if (isLiveMesh) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
    val onSurfaceColor = if (isLiveMesh) Color.White else MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(64.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .clip(RoundedCornerShape(32.dp))
            .background(color = backgroundColor)
    ) {
        if (isLiveMesh && mediaMetadata?.thumbnailUrl != null && mediaMetadata?.isLocal != true) {
            val infiniteTransition = rememberInfiniteTransition(label = "mini_mesh")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing)),
                label = "mini_mesh_rot"
            )
            val saturationMatrix = remember { ColorMatrix().apply { setToSaturation(1.6f) } }

            Box(modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 1.5f; scaleY = 1.5f }) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(mediaMetadata?.thumbnailUrl)
                        .size(128, 128)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(saturationMatrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(40.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color = outlineColor.copy(alpha = 0.2f))
            ) {
                mediaMetadata?.let { metadata ->
                    if (metadata.isLocal) {
                        AsyncImageLocal(
                            image = { imageCache.getLocalThumbnail(metadata.localPath, true) },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = metadata.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                mediaMetadata?.let { metadata ->
                    Text(
                        text = metadata.title,
                        color = onSurfaceColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MiniPlayerExplicitIcon(
                            isExplicit = isExplicit,
                            mediaId = metadata.id,
                            tint = onSurfaceColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(14.dp)
                        )
                        if (metadata.artists.any { it.name.isNotBlank() }) {
                            Text(
                                text = metadata.artists.joinToString { it.name },
                                color = onSurfaceColor.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
                            )
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(
                            text = stringResource(R.string.error_unknown),
                            color = errorColor,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isLiveMesh) Color.White.copy(0.2f) else MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.player.togglePlayPause()
                        }
                    }
            ) {
                CircularProgressIndicator(
                    progress = { if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f },
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor,
                    trackColor = outlineColor.copy(alpha = 0.2f),
                    strokeWidth = 2.dp
                )
                Icon(
                    imageVector = when {
                        playbackState == Player.STATE_ENDED -> Icons.Rounded.Replay
                        isPlaying -> Icons.Rounded.Pause
                        else -> Icons.Rounded.PlayArrow
                    },
                    contentDescription = null,
                    tint = if (isLiveMesh) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun AppleMiniPlayerContent(
    offsetX: Float,
    position: Long,
    duration: Long,
    playerConnection: PlayerConnection,
    pureBlack: Boolean,
    isLiveMesh: Boolean,
    isExplicit: Boolean = false
) {
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val error by playerConnection.error.collectAsState()

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val backgroundColor = if (pureBlack && useDarkTheme) Color.Black else MaterialTheme.colorScheme.surfaceContainer

    val primaryColor = if (isLiveMesh) Color.White else MaterialTheme.colorScheme.primary
    val outlineColor = if (isLiveMesh) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
    val onSurfaceColor = if (isLiveMesh) Color.White else MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor)
    ) {
        if (isLiveMesh && mediaMetadata?.thumbnailUrl != null && mediaMetadata?.isLocal != true) {
            val infiniteTransition = rememberInfiniteTransition(label = "mini_mesh")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing)),
                label = "mini_mesh_rot"
            )
            val saturationMatrix = remember { ColorMatrix().apply { setToSaturation(1.6f) } }

            Box(modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 1.5f; scaleY = 1.5f }) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(mediaMetadata?.thumbnailUrl)
                        .size(128, 128)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(saturationMatrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(40.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            }
        }

        // Bottom Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter)
                .drawWithContent {
                    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
                    val trackColor = outlineColor.copy(alpha = 0.2f)
                    drawRect(trackColor)
                    drawRect(primaryColor, size = Size(size.width * progress, size.height))
                }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 5.dp),
        ) {
            // Cookie 4-Sided Thumbnail
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(MaterialShapes.Cookie4Sided.toShape())
                    .background(color = outlineColor.copy(alpha = 0.2f))
            ) {
                mediaMetadata?.let { metadata ->
                    if (metadata.isLocal) {
                        AsyncImageLocal(
                            image = { imageCache.getLocalThumbnail(metadata.localPath, true) },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = metadata.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                mediaMetadata?.let { metadata ->
                    Text(
                        text = metadata.title,
                        color = onSurfaceColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MiniPlayerExplicitIcon(
                            isExplicit = isExplicit,
                            mediaId = metadata.id,
                            tint = onSurfaceColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(14.dp)
                        )
                        if (metadata.artists.any { it.name.isNotBlank() }) {
                            Text(
                                text = metadata.artists.joinToString { it.name },
                                color = onSurfaceColor.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
                            )
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(
                            text = stringResource(R.string.error_unknown),
                            color = errorColor,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.player.togglePlayPause()
                        }
                    }
            ) {
                Icon(
                    imageVector = when {
                        playbackState == Player.STATE_ENDED -> Icons.Rounded.Replay
                        isPlaying -> Icons.Rounded.Pause
                        else -> Icons.Rounded.PlayArrow
                    },
                    contentDescription = null,
                    tint = onSurfaceColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(enabled = canSkipNext) {
                        playerConnection.player.seekToNext()
                    }
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = null,
                    tint = if (canSkipNext) onSurfaceColor else onSurfaceColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun SwipeableMiniPlayerBox(
    modifier: Modifier = Modifier,
    swipeSensitivity: Float,
    swipeThumbnail: Boolean,
    playerConnection: PlayerConnection,
    layoutDirection: LayoutDirection,
    coroutineScope: CoroutineScope,
    pureBlack: Boolean = false,
    useLegacyBackground: Boolean = false,
    content: @Composable (Float) -> Unit
) {
    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    fun calculateAutoSwipeThreshold(swipeSensitivity: Float): Int {
        return (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }
    val autoSwipeThreshold = calculateAutoSwipeThreshold(swipeSensitivity)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .let { baseModifier ->
                if (useLegacyBackground) {
                    baseModifier.background(
                        if (pureBlack) Color.Black
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                } else {
                    baseModifier.padding(horizontal = 12.dp)
                }
            }
            .let { baseModifier ->
                if (swipeThumbnail) {
                    baseModifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragStartTime = System.currentTimeMillis()
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = animationSpec
                                    )
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val adjustedDragAmount =
                                    if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                val canSkipPrevious = playerConnection.player.previousMediaItemIndex != C.INDEX_UNSET
                                val canSkipNext = playerConnection.player.nextMediaItemIndex != C.INDEX_UNSET
                                val allowLeft = adjustedDragAmount < 0 && canSkipNext
                                val allowRight = adjustedDragAmount > 0 && canSkipPrevious
                                if (allowLeft || allowRight) {
                                    totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                    coroutineScope.launch {
                                        offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                    }
                                }
                            },
                            onDragEnd = {
                                val dragDuration = System.currentTimeMillis() - dragStartTime
                                val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                val currentOffset = offsetXAnimatable.value

                                val minDistanceThreshold = 50f
                                val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                                val shouldChangeSong = (
                                    kotlin.math.abs(currentOffset) > minDistanceThreshold &&
                                        velocity > velocityThreshold
                                    ) || (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                if (shouldChangeSong) {
                                    val isRightSwipe = currentOffset > 0
                                    val canSkipPrevious = playerConnection.player.previousMediaItemIndex != C.INDEX_UNSET
                                    val canSkipNext = playerConnection.player.nextMediaItemIndex != C.INDEX_UNSET

                                    if (isRightSwipe && canSkipPrevious) {
                                        playerConnection.player.seekToPrevious()
                                    } else if (!isRightSwipe && canSkipNext) {
                                        playerConnection.player.seekToNext()
                                    }
                                }

                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = animationSpec
                                    )
                                }
                            }
                        )
                    }
                } else {
                    baseModifier
                }
            }
    ) {
        content(offsetXAnimatable.value)

        if (offsetXAnimatable.value.absoluteValue > 50f) {
            Box(
                modifier = Modifier
                    .align(if (offsetXAnimatable.value > 0) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = if (offsetXAnimatable.value > 0) Icons.Rounded.SkipPrevious else Icons.Rounded.SkipNext,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(
                        alpha = (offsetXAnimatable.value.absoluteValue / autoSwipeThreshold).coerceIn(0f, 1f)
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.MiniPlayerInfo(
    mediaMetadata: MediaMetadata,
    isExplicit: Boolean = false,
    primaryTextColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = mediaMetadata.title,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "title"
        ) { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee()
            )
        }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniPlayerExplicitIcon(
                isExplicit = isExplicit,
                mediaId = mediaMetadata.id,
                tint = secondaryTextColor,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(14.dp)
            )
            AnimatedContent(
                targetState = mediaMetadata.artists,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "artist",
                modifier = Modifier.weight(1f, fill = false)
            ) { artists ->
                Text(
                    text = artists.joinToString { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
            }
        }
    }
}

@Composable
private fun MiniPlayerArtwork(
    mediaMetadata: MediaMetadata?,
    position: Long,
    duration: Long,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isLiveMesh: Boolean = false
) {
    val progressColor = if (isLiveMesh) Color.White else MaterialTheme.colorScheme.primary
    val progressTrackColor = if (isLiveMesh) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    val imageBorderColor = if (isLiveMesh) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(47.dp)
    ) {
        if (isLoading) {
            CircularWavyProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                color = progressColor,
                trackColor = progressTrackColor
            )
        } else {
            CircularWavyProgressIndicator(
                progress = { if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f },
                modifier = Modifier.fillMaxSize(),
                color = progressColor,
                trackColor = progressTrackColor
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(37.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = imageBorderColor,
                    shape = CircleShape
                )
        ) {
            when {
                mediaMetadata?.isLocal == true -> {
                    AsyncImageLocal(
                        image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, true) },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                mediaMetadata?.thumbnailUrl != null -> {
                    AsyncImage(
                        model = mediaMetadata.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerTransportButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    containerColorPrimary: Color = MaterialTheme.colorScheme.surface,
    borderColorEnabled: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    borderColorDisabled: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
) {
    val containerColor = if (isPrimary) containerColorPrimary else Color.Transparent
    val borderColor = if (enabled) borderColorEnabled else borderColorDisabled
    val iconTint = if (enabled) iconColor else disabledIconColor

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .then(modifier)
            .size(if (isPrimary) 40.dp else 36.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(if (isPrimary) 22.dp else 18.dp)
        )
    }
}

@Composable
private fun MiniPlayerTransportControls(
    isPlaying: Boolean,
    playbackState: Int,
    isLoading: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    playerConnection: PlayerConnection,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    containerColorPrimary: Color = MaterialTheme.colorScheme.surface,
    borderColorEnabled: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    borderColorDisabled: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniPlayerTransportButton(
            icon = Icons.Rounded.SkipPrevious,
            contentDescription = null,
            onClick = { playerConnection.player.seekToPrevious() },
            enabled = canSkipPrevious,
            iconColor = iconColor,
            disabledIconColor = disabledIconColor,
            containerColorPrimary = containerColorPrimary,
            borderColorEnabled = borderColorEnabled,
            borderColorDisabled = borderColorDisabled
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp)
        ) {
            MiniPlayerTransportButton(
                icon = when {
                    playbackState == Player.STATE_ENDED -> Icons.Rounded.Replay
                    isPlaying -> Icons.Rounded.Pause
                    else -> Icons.Rounded.PlayArrow
                },
                contentDescription = if (isPlaying && playbackState != Player.STATE_ENDED) "Pause" else stringResource(R.string.play),
                onClick = {
                    if (playbackState == Player.STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.player.togglePlayPause()
                    }
                },
                isPrimary = true,
                iconColor = iconColor,
                disabledIconColor = disabledIconColor,
                containerColorPrimary = containerColorPrimary,
                borderColorEnabled = borderColorEnabled,
                borderColorDisabled = borderColorDisabled
            )
        }

        MiniPlayerTransportButton(
            icon = Icons.Rounded.SkipNext,
            contentDescription = null,
            onClick = { playerConnection.player.seekToNext() },
            enabled = canSkipNext,
            iconColor = iconColor,
            disabledIconColor = disabledIconColor,
            containerColorPrimary = containerColorPrimary,
            borderColorEnabled = borderColorEnabled,
            borderColorDisabled = borderColorDisabled
        )
    }
}

@Composable
fun NewMiniPlayerContent(
    pureBlack: Boolean,
    position: Long,
    duration: Long,
    playerConnection: PlayerConnection,
    isLiveMesh: Boolean = false,
    isExplicit: Boolean = false
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    val isLoading = playbackState == Player.STATE_BUFFERING

    val primaryTextColor = if (isLiveMesh) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (isLiveMesh) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    val iconColor = if (isLiveMesh) Color.White else MaterialTheme.colorScheme.onSurface
    val disabledIconColor = if (isLiveMesh) Color.White.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val containerColorPrimary = if (isLiveMesh) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
    val borderColorEnabled = if (isLiveMesh) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val borderColorDisabled = if (isLiveMesh) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        MiniPlayerArtwork(
            mediaMetadata = mediaMetadata,
            position = position,
            duration = duration,
            isLoading = isLoading,
            isLiveMesh = isLiveMesh
        )

        Spacer(modifier = Modifier.width(5.dp))

        mediaMetadata?.let {
            MiniPlayerInfo(
                mediaMetadata = it,
                isExplicit = isExplicit,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor
            )
        } ?: Spacer(Modifier.weight(1f))

        Spacer(modifier = Modifier.width(12.dp))

        MiniPlayerTransportControls(
            isPlaying = isPlaying,
            playbackState = playbackState,
            isLoading = isLoading,
            canSkipPrevious = canSkipPrevious,
            canSkipNext = canSkipNext,
            playerConnection = playerConnection,
            iconColor = iconColor,
            disabledIconColor = disabledIconColor,
            containerColorPrimary = containerColorPrimary,
            borderColorEnabled = borderColorEnabled,
            borderColorDisabled = borderColorDisabled
        )
    }
}

@Composable
fun MiniMediaInfo(
    mediaMetadata: MediaMetadata,
    error: PlaybackException?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Box(modifier = Modifier.padding(6.dp)) {
            if (mediaMetadata.isLocal) {
                AsyncImageLocal(
                    image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, true) },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                )
            } else {
                AsyncImage(
                    model = mediaMetadata.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                )
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = error != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius),
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

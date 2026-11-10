@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
)

package com.gumlaybeg.nimbletune.ui.player

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.os.PowerManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.DarkModeKey
import com.gumlaybeg.nimbletune.constants.PlayerBackgroundStyleKey
import com.gumlaybeg.nimbletune.constants.ThumbnailCornerRadius
import com.gumlaybeg.nimbletune.db.entities.LyricsEntity
import com.gumlaybeg.nimbletune.db.entities.Song
import com.gumlaybeg.nimbletune.extensions.togglePlayPause
import com.gumlaybeg.nimbletune.extensions.toggleRepeatMode
import com.gumlaybeg.nimbletune.models.MediaMetadata
import com.gumlaybeg.nimbletune.models.toMediaMetadata
import com.gumlaybeg.nimbletune.playback.isShuffleEnabled
import com.gumlaybeg.nimbletune.ui.component.AsyncImageLocal
import com.gumlaybeg.nimbletune.ui.component.BottomSheet
import com.gumlaybeg.nimbletune.ui.component.BottomSheetState
import com.gumlaybeg.nimbletune.ui.component.LocalMenuState
import com.gumlaybeg.nimbletune.ui.component.Lyrics
import com.gumlaybeg.nimbletune.ui.component.PlayerSliderTrack
import com.gumlaybeg.nimbletune.ui.menu.LyricsMenu
import com.gumlaybeg.nimbletune.ui.menu.PlayerMenu
import com.gumlaybeg.nimbletune.ui.screens.settings.DarkMode
import com.gumlaybeg.nimbletune.ui.screens.settings.PlayerBackgroundStyle
import com.gumlaybeg.nimbletune.ui.theme.extractGradientColors
import com.gumlaybeg.nimbletune.ui.utils.imageCache
import com.gumlaybeg.nimbletune.ui.utils.resize
import com.gumlaybeg.nimbletune.utils.makeTimeString
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin
import androidx.media3.common.C
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY

enum class PlayerInternalState { COVER, LYRICS, QUEUE }
enum class SliderStyle { EXPANDING, DEFAULT, SLIM }
enum class CoverResolution(val size: Int) { RES_500(500), RES_800(800), RES_1080(1080) }

fun Modifier.customSoftShadow(
    elevation: Dp,
    cornerRadius: Dp = 12.dp,
    enabled: Boolean = true
): Modifier = if (enabled && elevation > 0.dp) {
    this.shadow(
        elevation = elevation,
        shape = RoundedCornerShape(cornerRadius),
        clip = false,
        ambientColor = Color.Black.copy(alpha = 0.25f),
        spotColor = Color.Black.copy(alpha = 0.35f)
    )
} else this

fun Modifier.SwipeGesture(
    enabled: Boolean = true,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {}
): Modifier = if (!enabled) this else this.pointerInput(Unit) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDrag = 0f },
        onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
        onDragEnd = {
            if (totalDrag > 80f) onSwipeRight()
            else if (totalDrag < -80f) onSwipeLeft()
        },
        onDragCancel = { totalDrag = 0f }
    )
}

object PlayerSliderColors {
    @Composable
    fun getSliderColors(
        activeColor: Color,
        playerBackground: PlayerBackgroundStyle,
        useDarkTheme: Boolean
    ): SliderColors = SliderDefaults.colors(
        activeTrackColor = activeColor,
        inactiveTrackColor = activeColor.copy(alpha = 0.25f),
        thumbColor = activeColor
    )
}

@Composable
fun LiveMeshBackground(
    colors: List<Color>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColors = if (colors.size >= 2) colors else listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    val infiniteTransition = rememberInfiniteTransition(label = "LiveMeshTransition")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 18000 else 32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "meshProgress"
    )

    fun colorAt(index: Int): Color {
        val count = activeColors.size
        val idx = (index.toFloat() + progress * count) % count
        val a = idx.toInt()
        val b = (a + 1) % count
        return lerp(activeColors[a], activeColors[b], idx - a)
    }

    fun osc(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val v = sin(2f * PI.toFloat() * (progress * speed + phase))
        return min + (max - min) * ((v + 1f) * 0.5f)
    }

    val c1 = colorAt(0)
    val c2 = colorAt(1)
    val c3 = colorAt(2 % activeColors.size)
    val c4 = colorAt(3 % activeColors.size)

    val o1x = osc(0.05f, 0.95f, 0.00f, 1.0f)
    val o1y = osc(0.05f, 0.55f, 0.12f, 1.0f)
    val r1 = osc(0.7f, 1.4f, 0.15f, 1.0f)

    val o2x = osc(0.95f, 0.10f, 0.25f, 0.8f)
    val o2y = osc(0.40f, 0.95f, 0.30f, 0.8f)
    val r2 = osc(0.6f, 1.3f, 0.22f, 1.0f)

    val o3x = osc(0.15f, 0.85f, 0.45f, 1.2f)
    val o3y = osc(0.75f, 0.25f, 0.50f, 1.2f)
    val r3 = osc(0.65f, 1.35f, 0.40f, 1.0f)

    val o4x = osc(0.35f, 0.65f, 0.65f, 0.9f)
    val o4y = osc(0.10f, 0.90f, 0.70f, 0.9f)
    val r4 = osc(0.8f, 1.5f, 0.60f, 1.0f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val base = Color(0xFF0A0A0E)

                val b1 = Brush.radialGradient(listOf(c1.copy(alpha = 0.85f), c1.copy(alpha = 0.35f), Color.Transparent), Offset(w * o1x, h * o1y), w * r1)
                val b2 = Brush.radialGradient(listOf(c2.copy(alpha = 0.80f), c2.copy(alpha = 0.30f), Color.Transparent), Offset(w * o2x, h * o2y), w * r2)
                val b3 = Brush.radialGradient(listOf(c3.copy(alpha = 0.75f), c3.copy(alpha = 0.25f), Color.Transparent), Offset(w * o3x, h * o3y), w * r3)
                val b4 = Brush.radialGradient(listOf(c4.copy(alpha = 0.70f), c4.copy(alpha = 0.20f), Color.Transparent), Offset(w * o4x, h * o4y), w * r4)

                onDrawBehind {
                    drawRect(base)
                    drawRect(b1)
                    drawRect(b2)
                    drawRect(b3)
                    drawRect(b4)
                }
            }
    )
}

@Composable
fun PlayerV2Slider(
    sliderStyle: SliderStyle,
    currentPos: Long,
    duration: Long,
    isPlaying: Boolean,
    adaptivePrimary: Color,
    playerBackground: PlayerBackgroundStyle,
    useDarkTheme: Boolean,
    onValueChange: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var scrubbedValue by remember { mutableStateOf<Float?>(null) }
    val maxRange = if (duration == C.TIME_UNSET || duration <= 0L) 0f else duration.toFloat()
    val displayValue = (scrubbedValue ?: currentPos.toFloat()).coerceIn(0f, if (maxRange > 0f) maxRange else 1f)

    when (sliderStyle) {
        SliderStyle.EXPANDING -> {
            val trackInteractionSource = remember { MutableInteractionSource() }
            val isTrackDragged by trackInteractionSource.collectIsDraggedAsState()
            val isTrackPressed by trackInteractionSource.collectIsPressedAsState()
            val isTrackActive = isTrackDragged || isTrackPressed

            val trackHeight by animateDpAsState(
                targetValue = if (isTrackActive) 12.dp else 6.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                label = "trackScale"
            )

            Slider(
                value = displayValue,
                valueRange = 0f..maxRange,
                onValueChange = { value ->
                    scrubbedValue = value
                    onValueChange(value.toLong())
                },
                onValueChangeFinished = {
                    scrubbedValue?.toLong()?.let(onSeekTo)
                    scrubbedValue = null
                },
                enabled = maxRange > 0f,
                interactionSource = trackInteractionSource,
                thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                track = { sliderState ->
                    PlayerSliderTrack(
                        sliderState = sliderState,
                        trackHeight = trackHeight,
                        colors = PlayerSliderColors.getSliderColors(
                            activeColor = adaptivePrimary.copy(alpha = 0.8f),
                            playerBackground = playerBackground,
                            useDarkTheme = useDarkTheme
                        )
                    )
                },
                modifier = modifier.fillMaxWidth()
            )
        }
        SliderStyle.SLIM -> {
            Slider(
                value = displayValue,
                valueRange = 0f..maxRange,
                onValueChange = { value ->
                    scrubbedValue = value
                    onValueChange(value.toLong())
                },
                onValueChangeFinished = {
                    scrubbedValue?.toLong()?.let(onSeekTo)
                    scrubbedValue = null
                },
                enabled = maxRange > 0f,
                thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                track = { sliderState ->
                    PlayerSliderTrack(
                        sliderState = sliderState,
                        colors = PlayerSliderColors.getSliderColors(
                            activeColor = adaptivePrimary,
                            playerBackground = playerBackground,
                            useDarkTheme = useDarkTheme
                        ),
                        trackHeight = 3.dp
                    )
                },
                modifier = modifier.fillMaxWidth()
            )
        }
        else -> {
            Slider(
                value = displayValue,
                valueRange = 0f..maxRange,
                onValueChange = { value ->
                    scrubbedValue = value
                    onValueChange(value.toLong())
                },
                onValueChangeFinished = {
                    scrubbedValue?.toLong()?.let(onSeekTo)
                    scrubbedValue = null
                },
                enabled = maxRange > 0f,
                colors = SliderDefaults.colors(
                    activeTrackColor = adaptivePrimary,
                    inactiveTrackColor = adaptivePrimary.copy(alpha = 0.25f),
                    thumbColor = adaptivePrimary
                ),
                modifier = modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val shuffleModeEnabled by isShuffleEnabled.collectAsState()
    val playerVolume by playerConnection.service.playerVolume.collectAsState()

    var playerState by remember { mutableStateOf(PlayerInternalState.COVER) }
    var controlsVisible by remember { mutableStateOf(true) }

    BackHandler(enabled = playerState != PlayerInternalState.COVER) {
        playerState = PlayerInternalState.COVER
    }

    LaunchedEffect(playerState) {
        if (playerState != PlayerInternalState.LYRICS) {
            controlsVisible = true
        }
    }

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var sliderPosition by remember { mutableStateOf<Long?>(null) }

    val playerBackground by rememberEnumPreference(PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.LIVE_MESH)
    val thumbnailCornerRadius = ThumbnailCornerRadius.value
    val coverResolution = CoverResolution.RES_1080
    val sliderStyle = SliderStyle.EXPANDING
    val showPlayerThumbnailShadow = true
    val playerThumbnailShadowElevation = 10f

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    LaunchedEffect(mediaMetadata) {
        if (powerManager.isPowerSaveMode) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            if (mediaMetadata?.isLocal == true) {
                imageCache.getLocalThumbnail(mediaMetadata?.localPath)?.extractGradientColors()?.let {
                    gradientColors = it
                }
            } else {
                val result = (ImageLoader(context).execute(
                    ImageRequest.Builder(context)
                        .data(mediaMetadata?.thumbnailUrl)
                        .allowHardware(false)
                        .build()
                ).drawable as? BitmapDrawable)?.bitmap?.extractGradientColors()
                result?.let { gradientColors = it }
            }
        }
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(200)
                if (sliderPosition == null) {
                    val rawDuration = playerConnection.player.duration
                    position = playerConnection.player.currentPosition.coerceAtLeast(0L)
                    duration = if (rawDuration == C.TIME_UNSET || rawDuration < 0) 0L else rawDuration
                }
            }
        }
    }

    LaunchedEffect(mediaMetadata?.id) {
        val rawDuration = playerConnection.player.duration
        position = playerConnection.player.currentPosition.coerceAtLeast(0L)
        duration = if (rawDuration == C.TIME_UNSET || rawDuration < 0) 0L else rawDuration
    }

    val adaptivePrimary by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onSurface
            else -> Color.White
        },
        label = "adaptivePrimary"
    )
    val adaptiveSecondary by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> Color.White.copy(alpha = 0.7f)
        },
        label = "adaptiveSecondary"
    )
    val adaptiveSurface by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant
            else -> Color.White.copy(alpha = 0.2f)
        },
        label = "adaptiveSurface"
    )

    BottomSheet(
        state = state,
        modifier = modifier,
        backgroundColor = if (useDarkTheme || playerBackground == PlayerBackgroundStyle.DEFAULT) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
        } else MaterialTheme.colorScheme.onSurfaceVariant,
        collapsedBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        onDismiss = {
            playerConnection.player.stop()
            playerConnection.player.clearMediaItems()
            playerConnection.service.deInitQueue()
        },
        collapsedContent = {
            MiniPlayer(
                position = position,
                duration = duration
            )
        }
    ) {
        AnimatedVisibility(
            visible = !powerManager.isPowerSaveMode && (state.value > state.collapsedBound),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(200))
        ) {
            if (playerBackground == PlayerBackgroundStyle.LIVE_MESH || playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
                LiveMeshBackground(
                    colors = gradientColors,
                    isPlaying = isPlaying
                )
            } else if (playerBackground == PlayerBackgroundStyle.BLUR) {
                if (mediaMetadata?.isLocal == true) {
                    mediaMetadata?.let {
                        AsyncImageLocal(
                            image = { imageCache.getLocalThumbnail(it.localPath) },
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(150.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = mediaMetadata?.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(150.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.40f))
                )
            } else if (playerBackground == PlayerBackgroundStyle.GRADIENT && gradientColors.size >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(gradientColors), alpha = 0.85f)
                )
            }
        }

        if (isLandscape) {
            PlayerV2Landscape(
                state = state,
                navController = navController,
                mediaMetadata = currentSong?.toMediaMetadata() ?: mediaMetadata,
                currentSong = currentSong,
                currentLyrics = currentLyrics,
                isPlaying = isPlaying,
                playbackState = playbackState,
                position = position,
                duration = duration,
                sliderPosition = sliderPosition,
                onSliderPositionChange = { sliderPosition = it },
                onSeekTo = { pos ->
                    playerConnection.player.seekTo(pos)
                    position = pos
                    sliderPosition = null
                },
                playerState = playerState,
                onPlayerStateChange = { playerState = it },
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                playerVolume = playerVolume,
                repeatMode = repeatMode,
                shuffleModeEnabled = shuffleModeEnabled,
                sliderStyle = sliderStyle,
                playerBackground = playerBackground,
                thumbnailCornerRadius = thumbnailCornerRadius,
                coverResolution = coverResolution,
                showPlayerThumbnailShadow = showPlayerThumbnailShadow,
                playerThumbnailShadowElevation = playerThumbnailShadowElevation,
                adaptivePrimary = adaptivePrimary,
                adaptiveSecondary = adaptiveSecondary,
                adaptiveSurface = adaptiveSurface,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PlayerV2Portrait(
                state = state,
                navController = navController,
                mediaMetadata = currentSong?.toMediaMetadata() ?: mediaMetadata,
                currentSong = currentSong,
                currentLyrics = currentLyrics,
                isPlaying = isPlaying,
                playbackState = playbackState,
                position = position,
                duration = duration,
                sliderPosition = sliderPosition,
                onSliderPositionChange = { sliderPosition = it },
                onSeekTo = { pos ->
                    playerConnection.player.seekTo(pos)
                    position = pos
                    sliderPosition = null
                },
                playerState = playerState,
                onPlayerStateChange = { playerState = it },
                controlsVisible = controlsVisible,
                onToggleControlsVisible = { controlsVisible = !controlsVisible },
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                playerVolume = playerVolume,
                repeatMode = repeatMode,
                shuffleModeEnabled = shuffleModeEnabled,
                sliderStyle = sliderStyle,
                playerBackground = playerBackground,
                thumbnailCornerRadius = thumbnailCornerRadius,
                coverResolution = coverResolution,
                showPlayerThumbnailShadow = showPlayerThumbnailShadow,
                playerThumbnailShadowElevation = playerThumbnailShadowElevation,
                adaptivePrimary = adaptivePrimary,
                adaptiveSecondary = adaptiveSecondary,
                adaptiveSurface = adaptiveSurface,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PlayerV2Landscape(
    state: BottomSheetState,
    navController: NavController,
    mediaMetadata: MediaMetadata?,
    currentSong: Song?,
    currentLyrics: LyricsEntity?,
    isPlaying: Boolean,
    playbackState: Int,
    position: Long,
    duration: Long,
    sliderPosition: Long?,
    onSliderPositionChange: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    playerState: PlayerInternalState,
    onPlayerStateChange: (PlayerInternalState) -> Unit,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    playerVolume: Float,
    repeatMode: Int,
    shuffleModeEnabled: Boolean,
    sliderStyle: SliderStyle,
    playerBackground: PlayerBackgroundStyle,
    thumbnailCornerRadius: Float,
    coverResolution: CoverResolution,
    showPlayerThumbnailShadow: Boolean,
    playerThumbnailShadowElevation: Float,
    adaptivePrimary: Color,
    adaptiveSecondary: Color,
    adaptiveSurface: Color,
    modifier: Modifier = Modifier
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val coverRadius = thumbnailCornerRadius.dp
    val isLiked = currentSong?.song?.liked == true
    val currentPos = sliderPosition ?: position

    var isMuted by remember { mutableStateOf(false) }
    var previousVolume by remember { mutableFloatStateOf(1f) }

    AnimatedContent(
        targetState = playerState,
        transitionSpec = {
            (fadeIn(tween(250, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.96f))
                .togetherWith(fadeOut(tween(180, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.96f))
        },
        label = "landscapeStateAnimation",
        modifier = modifier.fillMaxSize()
    ) { currentUiState ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentUiState == PlayerInternalState.COVER) {
                Box(
                    modifier = Modifier
                        .weight(0.95f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.88f)
                            .aspectRatio(1f)
                            .customSoftShadow(
                                elevation = playerThumbnailShadowElevation.dp,
                                cornerRadius = coverRadius,
                                enabled = showPlayerThumbnailShadow
                            )
                            .background(adaptiveSurface, RoundedCornerShape(coverRadius))
                            .clip(RoundedCornerShape(coverRadius))
                            .SwipeGesture(
                                enabled = true,
                                onSwipeLeft = { if (canSkipNext) playerConnection.player.seekToNext() },
                                onSwipeRight = { if (canSkipPrevious) playerConnection.player.seekToPrevious() }
                            )
                    ) {
                        if (mediaMetadata?.isLocal == true) {
                            AsyncImageLocal(
                                image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, false) },
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = mediaMetadata?.thumbnailUrl?.resize(coverResolution.size, coverResolution.size),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { state.collapseSoft() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = stringResource(R.string.action_back),
                                tint = adaptivePrimary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = playerConnection::toggleLike,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isLiked) MaterialTheme.colorScheme.error else adaptivePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    menuState.show {
                                        PlayerMenu(
                                            mediaMetadata = mediaMetadata,
                                            navController = navController,
                                            playerBottomSheetState = state,
                                            onDismiss = menuState::dismiss
                                        )
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreHoriz,
                                    contentDescription = null,
                                    tint = adaptivePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = mediaMetadata?.title ?: stringResource(R.string.unknown),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = adaptivePrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .basicMarquee(iterations = 1, initialDelayMillis = 2000, velocity = 30.dp)
                                .clickable {
                                    state.collapseSoft()
                                    mediaMetadata?.album?.id?.let { navController.navigate("album/$it") }
                                }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mediaMetadata?.artists?.firstOrNull()?.name ?: stringResource(R.string.unknown),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = adaptiveSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .basicMarquee(iterations = 1, initialDelayMillis = 2000, velocity = 30.dp)
                                .clickable {
                                    state.collapseSoft()
                                    mediaMetadata?.artists?.firstOrNull()?.id?.let { navController.navigate("artist/$it") }
                                }
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        PlayerV2Slider(
                            sliderStyle = sliderStyle,
                            currentPos = currentPos,
                            duration = duration,
                            isPlaying = isPlaying,
                            adaptivePrimary = adaptivePrimary,
                            playerBackground = playerBackground,
                            useDarkTheme = isSystemInDarkTheme(),
                            onValueChange = onSliderPositionChange,
                            onSeekTo = onSeekTo
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = makeTimeString(currentPos),
                                color = adaptiveSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "-" + makeTimeString(maxOf(0L, duration - currentPos)),
                                color = adaptiveSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { playerConnection.triggerShuffle() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = stringResource(R.string.shuffle),
                                tint = if (shuffleModeEnabled) adaptivePrimary else adaptiveSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { if (canSkipPrevious) playerConnection.player.seekToPrevious() },
                            enabled = canSkipPrevious,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = null,
                                tint = if (canSkipPrevious) adaptivePrimary else adaptivePrimary.copy(alpha = 0.35f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                if (playbackState == STATE_ENDED) {
                                    playerConnection.player.seekTo(0, 0)
                                    playerConnection.player.playWhenReady = true
                                } else {
                                    playerConnection.player.togglePlayPause()
                                }
                            },
                            shape = CircleShape,
                            color = adaptivePrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = if (playbackState == STATE_ENDED) Icons.Rounded.Replay else if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = adaptivePrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = { if (canSkipNext) playerConnection.player.seekToNext() },
                            enabled = canSkipNext,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = null,
                                tint = if (canSkipNext) adaptivePrimary else adaptivePrimary.copy(alpha = 0.35f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = { playerConnection.player.toggleRepeatMode() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                                    else -> Icons.Rounded.Repeat
                                },
                                contentDescription = null,
                                tint = if (repeatMode != REPEAT_MODE_OFF) adaptivePrimary else adaptiveSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeMute,
                            contentDescription = null,
                            tint = adaptiveSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    if (isMuted) {
                                        playerConnection.service.playerVolume.value = previousVolume
                                        isMuted = false
                                    } else {
                                        previousVolume = playerVolume
                                        playerConnection.service.playerVolume.value = 0f
                                        isMuted = true
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = (if (isMuted) 0f else playerVolume).coerceIn(0f, 1f),
                            onValueChange = { newValue ->
                                playerConnection.service.playerVolume.value = newValue
                                if (newValue > 0f) isMuted = false
                            },
                            valueRange = 0f..1f,
                            thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                            track = { sliderState ->
                                PlayerSliderTrack(
                                    sliderState = sliderState,
                                    trackHeight = 4.dp,
                                    colors = PlayerSliderColors.getSliderColors(
                                        activeColor = adaptivePrimary.copy(alpha = 0.8f),
                                        playerBackground = playerBackground,
                                        useDarkTheme = isSystemInDarkTheme()
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f).height(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = adaptiveSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    playerConnection.service.playerVolume.value = 1f
                                    isMuted = false
                                }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isQueueActive = playerState == PlayerInternalState.QUEUE
                        IconButton(onClick = { onPlayerStateChange(if (isQueueActive) PlayerInternalState.COVER else PlayerInternalState.QUEUE) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = stringResource(R.string.queue),
                                tint = if (isQueueActive) adaptivePrimary else adaptiveSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        val isLyricsActive = playerState == PlayerInternalState.LYRICS
                        IconButton(onClick = { onPlayerStateChange(if (isLyricsActive) PlayerInternalState.COVER else PlayerInternalState.LYRICS) }) {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = stringResource(R.string.lyrics_settings_title),
                                tint = if (isLyricsActive) adaptivePrimary else adaptiveSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onPlayerStateChange(PlayerInternalState.COVER) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = stringResource(R.string.action_back),
                                tint = adaptivePrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                onPlayerStateChange(
                                    if (currentUiState == PlayerInternalState.LYRICS) PlayerInternalState.QUEUE
                                    else PlayerInternalState.LYRICS
                                )
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (currentUiState == PlayerInternalState.LYRICS) Icons.AutoMirrored.Rounded.QueueMusic else Icons.Rounded.Lyrics,
                                contentDescription = null,
                                tint = adaptivePrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(adaptiveSurface)
                                .clickable { onPlayerStateChange(PlayerInternalState.COVER) }
                        ) {
                            if (mediaMetadata?.isLocal == true) {
                                AsyncImageLocal(
                                    image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, true) },
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = mediaMetadata?.thumbnailUrl?.resize(coverResolution.size, coverResolution.size),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mediaMetadata?.title ?: stringResource(R.string.unknown),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = adaptivePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mediaMetadata?.artists?.firstOrNull()?.name ?: stringResource(R.string.unknown),
                                style = MaterialTheme.typography.bodySmall,
                                color = adaptiveSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = playerConnection::toggleLike,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isLiked) MaterialTheme.colorScheme.error else adaptivePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        PlayerV2Slider(
                            sliderStyle = sliderStyle,
                            currentPos = currentPos,
                            duration = duration,
                            isPlaying = isPlaying,
                            adaptivePrimary = adaptivePrimary,
                            playerBackground = playerBackground,
                            useDarkTheme = isSystemInDarkTheme(),
                            onValueChange = onSliderPositionChange,
                            onSeekTo = onSeekTo
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = makeTimeString(currentPos),
                                color = adaptiveSecondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "-" + makeTimeString(maxOf(0L, duration - currentPos)),
                                color = adaptiveSecondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (canSkipPrevious) playerConnection.player.seekToPrevious() },
                            enabled = canSkipPrevious,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = null,
                                tint = adaptivePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                if (playbackState == STATE_ENDED) {
                                    playerConnection.player.seekTo(0, 0)
                                    playerConnection.player.playWhenReady = true
                                } else {
                                    playerConnection.player.togglePlayPause()
                                }
                            },
                            shape = CircleShape,
                            color = adaptivePrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = if (playbackState == STATE_ENDED) Icons.Rounded.Replay else if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = adaptivePrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { if (canSkipNext) playerConnection.player.seekToNext() },
                            enabled = canSkipNext,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = null,
                                tint = adaptivePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(adaptiveSurface.copy(alpha = 0.12f))
                ) {
                    if (currentUiState == PlayerInternalState.LYRICS) {
                        Lyrics(
                            sliderPositionProvider = { sliderPosition ?: position },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (currentUiState == PlayerInternalState.QUEUE) {
                        QueueV2(
                            navController = navController,
                            playerBottomSheetState = state,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerV2Portrait(
    state: BottomSheetState,
    navController: NavController,
    mediaMetadata: MediaMetadata?,
    currentSong: Song?,
    currentLyrics: LyricsEntity?,
    isPlaying: Boolean,
    playbackState: Int,
    position: Long,
    duration: Long,
    sliderPosition: Long?,
    onSliderPositionChange: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    playerState: PlayerInternalState,
    onPlayerStateChange: (PlayerInternalState) -> Unit,
    controlsVisible: Boolean,
    onToggleControlsVisible: () -> Unit,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    playerVolume: Float,
    repeatMode: Int,
    shuffleModeEnabled: Boolean,
    sliderStyle: SliderStyle,
    playerBackground: PlayerBackgroundStyle,
    thumbnailCornerRadius: Float,
    coverResolution: CoverResolution,
    showPlayerThumbnailShadow: Boolean,
    playerThumbnailShadowElevation: Float,
    adaptivePrimary: Color,
    adaptiveSecondary: Color,
    adaptiveSurface: Color,
    modifier: Modifier = Modifier
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val currentPos = sliderPosition ?: position
    val isLiked = currentSong?.song?.liked == true
    val coverRadius = thumbnailCornerRadius.dp

    var isMuted by remember { mutableStateOf(false) }
    var previousVolume by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding())
                .padding(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(adaptivePrimary.copy(alpha = 0.3f))
                        .clickable { state.collapseSoft() }
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                AnimatedContent(
                    targetState = playerState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "InternalWindow"
                ) { targetState ->
                    if (targetState == PlayerInternalState.COVER) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .customSoftShadow(
                                        elevation = playerThumbnailShadowElevation.dp,
                                        cornerRadius = coverRadius,
                                        enabled = showPlayerThumbnailShadow
                                    )
                                    .background(adaptiveSurface, RoundedCornerShape(coverRadius))
                                    .clip(RoundedCornerShape(coverRadius))
                                    .SwipeGesture(
                                        enabled = true,
                                        onSwipeLeft = { if (canSkipNext) playerConnection.player.seekToNext() },
                                        onSwipeRight = { if (canSkipPrevious) playerConnection.player.seekToPrevious() }
                                    )
                            ) {
                                if (mediaMetadata?.isLocal == true) {
                                    AsyncImageLocal(
                                        image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, false) },
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        model = mediaMetadata?.thumbnailUrl?.resize(coverResolution.size, coverResolution.size),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mediaMetadata?.title ?: stringResource(R.string.unknown),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = adaptivePrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .basicMarquee(iterations = 1, initialDelayMillis = 2000, velocity = 30.dp)
                                            .clickable {
                                                state.collapseSoft()
                                                mediaMetadata?.album?.id?.let { navController.navigate("album/$it") }
                                            }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mediaMetadata?.artists?.firstOrNull()?.name ?: stringResource(R.string.unknown),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = adaptiveSecondary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .basicMarquee(iterations = 1, initialDelayMillis = 2000, velocity = 30.dp)
                                            .clickable {
                                                state.collapseSoft()
                                                mediaMetadata?.artists?.firstOrNull()?.id?.let { navController.navigate("artist/$it") }
                                            }
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(adaptivePrimary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(onClick = playerConnection::toggleLike) {
                                            Icon(
                                                imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                                contentDescription = null,
                                                tint = if (isLiked) MaterialTheme.colorScheme.error else adaptivePrimary,
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(adaptivePrimary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    PlayerMenu(
                                                        mediaMetadata = mediaMetadata,
                                                        navController = navController,
                                                        playerBottomSheetState = state,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MoreVert,
                                                contentDescription = null,
                                                tint = adaptivePrimary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (targetState == PlayerInternalState.LYRICS || targetState == PlayerInternalState.QUEUE) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            val miniRadius = (thumbnailCornerRadius / 2f).coerceAtLeast(4f).dp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .customSoftShadow(
                                            elevation = playerThumbnailShadowElevation.dp / 2f,
                                            cornerRadius = miniRadius,
                                            enabled = showPlayerThumbnailShadow
                                        )
                                        .background(adaptiveSurface, RoundedCornerShape(miniRadius))
                                        .clip(RoundedCornerShape(miniRadius))
                                        .clickable { onPlayerStateChange(PlayerInternalState.COVER) }
                                ) {
                                    if (mediaMetadata?.isLocal == true) {
                                        AsyncImageLocal(
                                            image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, true) },
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        AsyncImage(
                                            model = mediaMetadata?.thumbnailUrl?.resize(coverResolution.size, coverResolution.size),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mediaMetadata?.title ?: stringResource(R.string.unknown),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = adaptivePrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = mediaMetadata?.artists?.firstOrNull()?.name ?: stringResource(R.string.unknown),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = adaptiveSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = playerConnection::toggleLike) {
                                        Icon(
                                            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (isLiked) MaterialTheme.colorScheme.error else adaptivePrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (targetState == PlayerInternalState.QUEUE) {
                                                menuState.show {
                                                    PlayerMenu(
                                                        mediaMetadata = mediaMetadata,
                                                        navController = navController,
                                                        playerBottomSheetState = state,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            } else {
                                                menuState.show {
                                                    LyricsMenu(
                                                        lyricsProvider = { currentLyrics },
                                                        mediaMetadataProvider = { mediaMetadata!! },
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = null,
                                            tint = adaptivePrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            ) {
                                if (targetState == PlayerInternalState.LYRICS) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Lyrics(
                                            sliderPositionProvider = { sliderPosition ?: position },
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(16.dp)
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(adaptivePrimary.copy(alpha = 0.15f))
                                                .clickable { onToggleControlsVisible() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (controlsVisible) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                                                contentDescription = null,
                                                tint = adaptivePrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                } else {
                                    QueueV2(
                                        navController = navController,
                                        playerBottomSheetState = state,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        slideInVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it } +
                        expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                       slideOutVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { it } +
                       shrinkVertically(animationSpec = tween(200, easing = FastOutSlowInEasing))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    PlayerV2Slider(
                        sliderStyle = sliderStyle,
                        currentPos = currentPos,
                        duration = duration,
                        isPlaying = isPlaying,
                        adaptivePrimary = adaptivePrimary,
                        playerBackground = playerBackground,
                        useDarkTheme = isSystemInDarkTheme(),
                        onValueChange = onSliderPositionChange,
                        onSeekTo = onSeekTo
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = makeTimeString(currentPos),
                            color = adaptiveSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "-" + makeTimeString(maxOf(0L, duration - currentPos)),
                            color = adaptiveSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (canSkipPrevious) {
                                    playerConnection.player.seekToPrevious()
                                }
                            },
                            enabled = canSkipPrevious,
                            modifier = Modifier
                                .size(64.dp)
                                .alpha(if (!canSkipPrevious) 0.4f else 1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = null,
                                tint = adaptivePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (playbackState == STATE_ENDED) {
                                    playerConnection.player.seekTo(0, 0)
                                    playerConnection.player.playWhenReady = true
                                } else {
                                    playerConnection.player.togglePlayPause()
                                }
                            },
                            modifier = Modifier.size(88.dp)
                        ) {
                            Icon(
                                imageVector = if (playbackState == STATE_ENDED) Icons.Rounded.Replay else if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = adaptivePrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                if (canSkipNext) {
                                    playerConnection.player.seekToNext()
                                }
                            },
                            enabled = canSkipNext,
                            modifier = Modifier
                                .size(64.dp)
                                .alpha(if (!canSkipNext) 0.4f else 1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = null,
                                tint = adaptivePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeMute,
                            contentDescription = null,
                            tint = adaptiveSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    if (isMuted) {
                                        playerConnection.service.playerVolume.value = previousVolume
                                        isMuted = false
                                    } else {
                                        previousVolume = playerVolume
                                        playerConnection.service.playerVolume.value = 0f
                                        isMuted = true
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Slider(
                            value = (if (isMuted) 0f else playerVolume).coerceIn(0f, 1f),
                            onValueChange = { newValue ->
                                playerConnection.service.playerVolume.value = newValue
                                if (newValue > 0f) isMuted = false
                            },
                            valueRange = 0f..1f,
                            thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                            track = { sliderState ->
                                PlayerSliderTrack(
                                    sliderState = sliderState,
                                    trackHeight = 6.dp,
                                    colors = PlayerSliderColors.getSliderColors(
                                        activeColor = adaptivePrimary.copy(alpha = 0.8f),
                                        playerBackground = playerBackground,
                                        useDarkTheme = isSystemInDarkTheme()
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f).height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = adaptiveSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    playerConnection.service.playerVolume.value = 1f
                                    isMuted = false
                                }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { playerConnection.triggerShuffle() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = stringResource(R.string.shuffle),
                                tint = if (shuffleModeEnabled) adaptivePrimary else adaptiveSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        val isQueueActive = playerState == PlayerInternalState.QUEUE
                        IconButton(
                            onClick = {
                                onPlayerStateChange(if (isQueueActive) PlayerInternalState.COVER else PlayerInternalState.QUEUE)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = stringResource(R.string.queue),
                                tint = if (isQueueActive) adaptivePrimary else adaptiveSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        val isLyricsActive = playerState == PlayerInternalState.LYRICS
                        IconButton(
                            onClick = {
                                onPlayerStateChange(if (isLyricsActive) PlayerInternalState.COVER else PlayerInternalState.LYRICS)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = stringResource(R.string.lyrics_settings_title),
                                tint = if (isLyricsActive) adaptivePrimary else adaptiveSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = { playerConnection.player.toggleRepeatMode() }
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                                    else -> Icons.Rounded.Repeat
                                },
                                contentDescription = null,
                                tint = if (repeatMode != REPEAT_MODE_OFF) adaptivePrimary else adaptiveSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.media3.common.C
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
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
import com.gumlaybeg.nimbletune.extensions.metadata
import com.gumlaybeg.nimbletune.extensions.togglePlayPause
import com.gumlaybeg.nimbletune.extensions.toggleRepeatMode
import com.gumlaybeg.nimbletune.models.MediaMetadata
import com.gumlaybeg.nimbletune.models.toMediaMetadata
import com.gumlaybeg.nimbletune.playback.PlayerConnection.Companion.queueBoard
import com.gumlaybeg.nimbletune.playback.isShuffleEnabled
import com.gumlaybeg.nimbletune.ui.component.AsyncImageLocal
import com.gumlaybeg.nimbletune.ui.component.BottomSheet
import com.gumlaybeg.nimbletune.ui.component.BottomSheetState
import com.gumlaybeg.nimbletune.ui.component.LocalMenuState
import com.gumlaybeg.nimbletune.ui.component.Lyrics
import com.gumlaybeg.nimbletune.ui.component.MediaMetadataListItem
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

enum class PlayerInternalState { COVER, LYRICS, QUEUE }

enum class SliderStyle { EXPANDING, DEFAULT, SQUIGGLY, SLIM }

enum class CoverResolution(val size: Int) {
    RES_500(500), RES_800(800), RES_1080(1080)
}

val SliderStyleKey = stringPreferencesKey("sliderStyle")
val ShowPlayerThumbnailShadowKey = booleanPreferencesKey("showPlayerThumbnailShadow")
val PlayerThumbnailShadowElevationKey = floatPreferencesKey("playerThumbnailShadowElevation")
val HidePlayerThumbnailKey = booleanPreferencesKey("hidePlayerThumbnail")
val CoverResolutionKey = stringPreferencesKey("coverResolution")

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
        onHorizontalDrag = { _, dragAmount ->
            totalDrag += dragAmount
        },
        onDragEnd = {
            if (totalDrag > 80f) {
                onSwipeRight()
            } else if (totalDrag < -80f) {
                onSwipeLeft()
            }
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
                    val targetPos = scrubbedValue?.toLong()
                    if (targetPos != null) {
                        onSeekTo(targetPos)
                    }
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
                    val targetPos = scrubbedValue?.toLong()
                    if (targetPos != null) {
                        onSeekTo(targetPos)
                    }
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
                    val targetPos = scrubbedValue?.toLong()
                    if (targetPos != null) {
                        onSeekTo(targetPos)
                    }
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

    BackHandler(enabled = state.isExpanded && playerState != PlayerInternalState.COVER) {
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

    val playerBackground by rememberEnumPreference(PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.DEFAULT)
    val thumbnailCornerRadius = ThumbnailCornerRadius.value
    val coverResolution by rememberEnumPreference(CoverResolutionKey, defaultValue = CoverResolution.RES_1080)
    val sliderStyle by rememberEnumPreference(SliderStyleKey, defaultValue = SliderStyle.EXPANDING)
    val showPlayerThumbnailShadow by rememberPreference(ShowPlayerThumbnailShadowKey, defaultValue = true)
    val playerThumbnailShadowElevation by rememberPreference(PlayerThumbnailShadowElevationKey, defaultValue = 8f)
    val hidePlayerThumbnail by rememberPreference(HidePlayerThumbnailKey, defaultValue = false)

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    LaunchedEffect(mediaMetadata) {
        if (playerBackground != PlayerBackgroundStyle.GRADIENT || powerManager.isPowerSaveMode) return@LaunchedEffect
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
            PlayerBackgroundStyle.BLUR,
            PlayerBackgroundStyle.GRADIENT -> Color.White
        },
        label = "adaptivePrimary"
    )
    val adaptiveSecondary by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onSurfaceVariant
            PlayerBackgroundStyle.BLUR,
            PlayerBackgroundStyle.GRADIENT -> Color.White.copy(alpha = 0.7f)
        },
        label = "adaptiveSecondary"
    )
    val adaptiveSurface by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant
            PlayerBackgroundStyle.BLUR,
            PlayerBackgroundStyle.GRADIENT -> Color.White.copy(alpha = 0.2f)
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
            visible = !powerManager.isPowerSaveMode && state.isExpanded,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500))
        ) {
            if (playerBackground == PlayerBackgroundStyle.BLUR) {
                if (mediaMetadata?.isLocal == true) {
                    mediaMetadata?.let {
                        AsyncImageLocal(
                            image = { imageCache.getLocalThumbnail(it.localPath) },
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(200.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = mediaMetadata?.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(200.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
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
                hidePlayerThumbnail = hidePlayerThumbnail,
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
                hidePlayerThumbnail = hidePlayerThumbnail,
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
    hidePlayerThumbnail: Boolean,
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
            (fadeIn(tween(350, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.95f))
                .togetherWith(fadeOut(tween(200, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.95f))
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
                        if (hidePlayerThumbnail) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    tint = adaptivePrimary.copy(alpha = 0.5f)
                                )
                            }
                        } else if (mediaMetadata?.isLocal == true) {
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

                    // Interactive Volume Slider Bar
                    var volumeSliderPosition by remember { mutableStateOf<Float?>(null) }
                    val currentEffectiveVolume = volumeSliderPosition ?: (if (isMuted) 0f else playerVolume)

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
                            value = currentEffectiveVolume.coerceIn(0f, 1f),
                            onValueChange = { newValue ->
                                volumeSliderPosition = newValue
                                playerConnection.service.playerVolume.value = newValue
                                if (newValue > 0f) isMuted = false
                            },
                            onValueChangeFinished = { volumeSliderPosition = null },
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
                                contentDescription = stringResource(R.string.queues_title),
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
                        QueueInlineView(
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
    hidePlayerThumbnail: Boolean,
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
            // Drag handle pill at the top
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
                        (fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) togetherWith
                         fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)))
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
                                if (hidePlayerThumbnail) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MusicNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(72.dp),
                                            tint = adaptivePrimary.copy(alpha = 0.5f)
                                        )
                                    }
                                } else if (mediaMetadata?.isLocal == true) {
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                    if (hidePlayerThumbnail) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MusicNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(32.dp),
                                                tint = adaptivePrimary.copy(alpha = 0.5f)
                                            )
                                        }
                                    } else if (mediaMetadata?.isLocal == true) {
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
                                    QueueInlineView(
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

            // Controls Section
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        slideInVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) { it } +
                        expandVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                       slideOutVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it } +
                       shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing))
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

                    // Standard Interactive Volume Slider Bar
                    var volumeSliderPosition by remember { mutableStateOf<Float?>(null) }
                    val currentEffectiveVolume = volumeSliderPosition ?: (if (isMuted) 0f else playerVolume)
                    val volumeInteractionSource = remember { MutableInteractionSource() }
                    val isVolDragged by volumeInteractionSource.collectIsDraggedAsState()
                    val isVolPressed by volumeInteractionSource.collectIsPressedAsState()
                    val isVolActive = isVolDragged || isVolPressed

                    val volHeight by animateDpAsState(
                        targetValue = if (isVolActive) 12.dp else 6.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "volHeight"
                    )

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
                            value = currentEffectiveVolume.coerceIn(0f, 1f),
                            onValueChange = { newValue ->
                                volumeSliderPosition = newValue
                                playerConnection.service.playerVolume.value = newValue
                                if (newValue > 0f) isMuted = false
                            },
                            onValueChangeFinished = {
                                volumeSliderPosition = null
                            },
                            valueRange = 0f..1f,
                            interactionSource = volumeInteractionSource,
                            thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                            track = { sliderState ->
                                PlayerSliderTrack(
                                    sliderState = sliderState,
                                    trackHeight = volHeight,
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
                                contentDescription = stringResource(R.string.queues_title),
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

@Composable
fun QueueInlineView(
    navController: NavController,
    playerBottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val lazyListState = rememberLazyListState()
    val menuState = LocalMenuState.current

    LaunchedEffect(currentWindowIndex) {
        if (currentWindowIndex in queueWindows.indices) {
            lazyListState.animateScrollToItem(currentWindowIndex)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = queueWindows,
            key = { _, window -> window.uid.hashCode() }
        ) { index, window ->
            val metadata = window.mediaItem.metadata ?: return@itemsIndexed
            val currentItem by rememberUpdatedState(window)

            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue != SwipeToDismissBoxValue.Settled) {
                        if (queueBoard.removeCurrentQueueSong(currentItem.firstPeriodIndex, playerConnection.service)) {
                            playerConnection.player.removeMediaItem(currentItem.firstPeriodIndex)
                        }
                        true
                    } else false
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {},
                content = {
                    MediaMetadataListItem(
                        mediaMetadata = metadata,
                        isActive = index == currentWindowIndex,
                        isPlaying = isPlaying,
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        PlayerMenu(
                                            mediaMetadata = metadata,
                                            navController = navController,
                                            playerBottomSheetState = playerBottomSheetState,
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
                            .clickable {
                                if (index == currentWindowIndex) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                    playerConnection.player.prepare()
                                    playerConnection.player.playWhenReady = true
                                }
                            }
                    )
                }
            )
        }
    }
}
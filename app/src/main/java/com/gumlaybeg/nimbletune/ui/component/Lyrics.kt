package com.gumlaybeg.nimbletune.ui.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.LyricFontSizeKey
import com.gumlaybeg.nimbletune.constants.LyricsAnimationStyle
import com.gumlaybeg.nimbletune.constants.LyricsAnimationStyleKey
import com.gumlaybeg.nimbletune.constants.LyricsGlowEffectKey
import com.gumlaybeg.nimbletune.constants.LyricsLineSpacingKey
import com.gumlaybeg.nimbletune.constants.LyricsTextPositionKey
import com.gumlaybeg.nimbletune.constants.PlayerBackgroundStyleKey
import com.gumlaybeg.nimbletune.constants.ShowLyricsKey
import com.gumlaybeg.nimbletune.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.gumlaybeg.nimbletune.lyrics.LyricsEntry
import com.gumlaybeg.nimbletune.lyrics.LyricsUtils.findActiveLineIndices
import com.gumlaybeg.nimbletune.lyrics.LyricsUtils.findCurrentLineIndex
import com.gumlaybeg.nimbletune.lyrics.LyricsUtils.parseLyrics
import com.gumlaybeg.nimbletune.ui.component.shimmer.ShimmerHost
import com.gumlaybeg.nimbletune.ui.component.shimmer.TextPlaceholder
import com.gumlaybeg.nimbletune.ui.menu.LyricsMenu
import com.gumlaybeg.nimbletune.ui.screens.settings.LyricsPosition
import com.gumlaybeg.nimbletune.ui.screens.settings.PlayerBackgroundStyle
import com.gumlaybeg.nimbletune.ui.utils.fadingEdge
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds

const val animateScrollDuration = 300L
private const val LYRICS_ANCHOR_RATIO = 0.35f
private val LYRICS_ITEM_FALLBACK_HEIGHT_DP = 68.dp
private val LYRICS_ITEM_GAP_DP = 16.dp
private val LYRICS_FADE_TOP_DP = 130.dp
private val LYRICS_FADE_BOTTOM_DP = 160.dp
private const val LYRICS_STAGGER_DELAY_PER_DISTANCE = 20
private const val LYRICS_STAGGER_DELAY_MAX_MS = 200
val LyricsPreviewTime = 2.seconds

sealed class LyricsListItem {
    data class Line(val index: Int, val entry: LyricsEntry) : LyricsListItem()
    data class Indicator(
        val afterLineIndex: Int,
        val gapMs: Long,
        val gapStartMs: Long,
        val gapEndMs: Long,
        val nextAgent: String?,
    ) : LyricsListItem()
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
    showLyrics: Boolean = true,
    onClose: (() -> Unit)? = null,
    bottomEndReservedPadding: Dp = 0.dp,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val context = LocalContext.current
    var showLyricsPref by rememberPreference(ShowLyricsKey, false)

    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.LEFT)
    val lyricsAnimationStyle by rememberEnumPreference(LyricsAnimationStyleKey, LyricsAnimationStyle.METRO_LYRICS)
    val lyricsGlowEffect by rememberPreference(LyricsGlowEffectKey, false)
    val lyricsTextSizeInt by rememberPreference(LyricFontSizeKey, 24)
    val lyricsTextSize = lyricsTextSizeInt.toFloat()
    val lyricsLineSpacing by rememberPreference(LyricsLineSpacingKey, 1.3f)

    val scope = rememberCoroutineScope()

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val lyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val lyricsProvidersMap by playerConnection.service.lyricsHelper.lyricsProvidersFlow.collectAsState()
    val currentProvider = mediaMetadata?.id?.let { lyricsProvidersMap[it] }
    val lyrics = remember(lyricsEntity) { lyricsEntity?.lyrics?.trim() }

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.LIVE_MESH
    )

    val lines = remember(lyrics) {
        if (lyrics.isNullOrBlank() || lyrics == LYRICS_NOT_FOUND) {
            emptyList()
        } else if (lyrics.startsWith("[")) {
            listOf(LyricsEntry.HEAD_LYRICS_ENTRY) + parseLyrics(lyrics)
        } else {
            lyrics.lines().mapIndexed { index, line -> LyricsEntry(index * 100L, line) }
        }
    }

    val isSynced = remember(lyrics) {
        !lyrics.isNullOrEmpty() && lyrics.startsWith("[")
    }

    val expressiveAccent = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.primary
        else -> Color.White
    }
    val textColor = expressiveAccent
    val providerTextColor = if (playerBackground == PlayerBackgroundStyle.DEFAULT) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }

    var currentLineIndex by remember { mutableIntStateOf(-1) }
    var currentPlaybackPosition by remember { mutableLongStateOf(0L) }
    var deferredCurrentLineIndex by rememberSaveable { mutableIntStateOf(0) }
    var previousLineIndex by rememberSaveable { mutableIntStateOf(0) }
    var lastPreviewTime by rememberSaveable { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var initialScrollDone by rememberSaveable { mutableStateOf(false) }
    var shouldScrollToFirstLine by rememberSaveable { mutableStateOf(true) }
    var isAppMinimized by rememberSaveable { mutableStateOf(false) }

    var showShareDialog by remember { mutableStateOf(false) }
    var shareDialogData by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    var isSelectionModeActive by rememberSaveable { mutableStateOf(false) }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var showMaxSelectionToast by remember { mutableStateOf(false) }
    val maxSelectionLimit = 5

    val isLyricsProviderShown = currentProvider != null && currentProvider != "Unknown" && !isSelectionModeActive

    val lazyListState = rememberLazyListState()
    var isAutoScrollEnabled by rememberSaveable { mutableStateOf(true) }

    var activeLineIndices by remember { mutableStateOf(emptySet<Int>()) }
    var scrollTargetIndex by rememberSaveable { mutableIntStateOf(-1) }
    var previousScrollActiveIndices by remember { mutableStateOf(emptySet<Int>()) }
    var lastMainMaxSeen by remember(lyrics, lines) { mutableIntStateOf(-1) }

    val mergedLyricsList = remember(lines) {
        val result = mutableListOf<LyricsListItem>()
        if (lines.isEmpty()) return@remember result

        lines.forEachIndexed { i, entry ->
            if (entry.text.isNotBlank()) {
                result.add(LyricsListItem.Line(i, entry))
            }
            if (i < lines.size - 1) {
                val nextStart = lines[i + 1].time
                val currentEnd = if (!entry.words.isNullOrEmpty()) {
                    (entry.words.last().endTime * 1000).toLong()
                } else if (entry.text.isBlank()) {
                    entry.time
                } else {
                    null
                }

                if (currentEnd != null && currentEnd < nextStart) {
                    val gap = nextStart - currentEnd
                    if (gap > 4000L) {
                        result.add(LyricsListItem.Indicator(i, gap, currentEnd, nextStart, lines[i + 1].agent))
                    }
                }
            }
        }
        result
    }

    val activeListIndex by remember(mergedLyricsList, deferredCurrentLineIndex) {
        derivedStateOf {
            mergedLyricsList.indexOfFirst {
                (it is LyricsListItem.Line && it.index == deferredCurrentLineIndex) ||
                    (it is LyricsListItem.Indicator && it.afterLineIndex == deferredCurrentLineIndex)
            }.coerceAtLeast(0)
        }
    }

    BackHandler(enabled = isSelectionModeActive) {
        isSelectionModeActive = false
        selectedIndices.clear()
    }

    LaunchedEffect(showMaxSelectionToast) {
        if (showMaxSelectionToast) {
            Toast.makeText(
                context,
                "Maximum $maxSelectionLimit lines can be selected",
                Toast.LENGTH_SHORT
            ).show()
            showMaxSelectionToast = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(showLyrics) {
        val activity = context as? Activity
        if (showLyrics) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
                val isCurrentLineVisible = visibleItemsInfo.any { it.index == currentLineIndex }
                if (isCurrentLineVisible) {
                    initialScrollDone = false
                }
                isAppMinimized = true
            } else if (event == Lifecycle.Event.ON_START) {
                isAppMinimized = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(lyrics, lines) {
        isAutoScrollEnabled = true
        scrollTargetIndex = -1
        deferredCurrentLineIndex = 0
        isSelectionModeActive = false
        selectedIndices.clear()
        previousScrollActiveIndices = emptySet()
        lastMainMaxSeen = -1
    }

    LaunchedEffect(lyrics, lines, lyricsAnimationStyle) {
        if (lyrics.isNullOrEmpty() || !lyrics.startsWith("[")) {
            currentLineIndex = -1
            activeLineIndices = emptySet()
            return@LaunchedEffect
        }
        if (lyricsAnimationStyle == LyricsAnimationStyle.METRO_LYRICS) {
            var lastPlayerPos = playerConnection.player.currentPosition
            var lastUpdateTime = System.currentTimeMillis()
            val hasWordTimings = lines.any { !it.words.isNullOrEmpty() }

            while (isActive) {
                withFrameNanos { }
                val now = System.currentTimeMillis()
                val sliderPosition = sliderPositionProvider()
                isSeeking = sliderPosition != null

                val position = if (isSeeking) {
                    sliderPosition!!
                } else {
                    val playerPos = playerConnection.player.currentPosition
                    val isPlaying = playerConnection.player.isPlaying
                    if (playerPos != lastPlayerPos || !isPlaying) {
                        lastPlayerPos = playerPos
                        lastUpdateTime = now
                    }
                    val elapsed = now - lastUpdateTime
                    lastPlayerPos + (if (isPlaying) elapsed else 0L)
                }

                currentPlaybackPosition = position

                val initialActiveIndices = findActiveLineIndices(lines, position)
                val scrollActiveIndicesRaw = findActiveLineIndices(
                    lines,
                    position + (if (hasWordTimings) 0L else 250L)
                )

                val scrollActiveIndices = scrollActiveIndicesRaw.toMutableSet()
                for (i in scrollActiveIndicesRaw) {
                    if (lines.getOrNull(i)?.isBackground == true) {
                        for (j in i - 1 downTo 0) {
                            if (lines.getOrNull(j)?.isBackground == false) {
                                scrollActiveIndices.add(j)
                                break
                            }
                        }
                    }
                }

                val newActiveIndices = initialActiveIndices.toMutableSet()
                for (i in initialActiveIndices) {
                    if (lines.getOrNull(i)?.isBackground == true) {
                        for (j in i - 1 downTo 0) {
                            if (lines.getOrNull(j)?.isBackground == false) {
                                newActiveIndices.add(j)
                                break
                            }
                        }
                    }
                }

                val scrollMax = scrollActiveIndices
                    .filter { !lines[it].isBackground }
                    .maxOrNull() ?: (scrollActiveIndices.maxOrNull() ?: -1)

                val isCurrentTargetStillActive = scrollTargetIndex in scrollActiveIndices
                val anyStillActive = scrollActiveIndices.isNotEmpty()

                val shouldScroll = when {
                    isSeeking -> true
                    !isCurrentTargetStillActive && anyStillActive && scrollMax > scrollTargetIndex -> true
                    !isCurrentTargetStillActive && !anyStillActive && previousScrollActiveIndices.isNotEmpty() -> true
                    scrollTargetIndex == -1 && anyStillActive -> true
                    previousScrollActiveIndices.isEmpty() && anyStillActive && scrollMax > scrollTargetIndex -> true
                    else -> false
                }

                if (shouldScroll) {
                    val targetToScroll = when {
                        isSeeking -> scrollMax
                        !isCurrentTargetStillActive && anyStillActive -> scrollMax
                        !isCurrentTargetStillActive && !anyStillActive -> {
                            (lastMainMaxSeen + 1 until lines.size).firstOrNull {
                                !lines[it].isBackground
                            } ?: scrollTargetIndex
                        }
                        else -> scrollMax
                    }
                    if (targetToScroll != -1 && (isSeeking || targetToScroll > scrollTargetIndex)) {
                        scrollTargetIndex = targetToScroll
                    }
                }

                if (scrollMax > lastMainMaxSeen && scrollMax != -1) {
                    lastMainMaxSeen = scrollMax
                }

                previousScrollActiveIndices = scrollActiveIndices
                activeLineIndices = newActiveIndices
                currentLineIndex = scrollMax
            }
        } else {
            while (isActive) {
                delay(8)
                val sliderPosition = sliderPositionProvider()
                isSeeking = sliderPosition != null
                val position = sliderPosition ?: playerConnection.player.currentPosition
                currentPlaybackPosition = position
                currentLineIndex = findCurrentLineIndex(lines, position)
            }
        }
    }

    LaunchedEffect(isSeeking, lastPreviewTime) {
        if (isSeeking) {
            lastPreviewTime = 0L
        } else if (lastPreviewTime != 0L) {
            delay(LyricsPreviewTime)
            lastPreviewTime = 0L
        }
    }

    suspend fun performSmoothPageScroll(targetIndex: Int, duration: Int = 700) {
        if (targetIndex < 0) return
        val lookUpIndex = if (isLyricsProviderShown) targetIndex + 1 else targetIndex
        val itemInfo = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == lookUpIndex }
        if (itemInfo != null) {
            val viewportHeight = lazyListState.layoutInfo.viewportEndOffset - lazyListState.layoutInfo.viewportStartOffset
            val center = lazyListState.layoutInfo.viewportStartOffset + (viewportHeight / 2)
            val itemCenter = itemInfo.offset + itemInfo.size / 2
            val offset = itemCenter - center
            if (kotlin.math.abs(offset) > 10) {
                lazyListState.animateScrollBy(
                    value = offset.toFloat(),
                    animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                )
            }
        } else {
            lazyListState.scrollToItem(targetIndex)
        }
    }

    LaunchedEffect(scrollTargetIndex, isAutoScrollEnabled, lyricsAnimationStyle) {
        if (lyricsAnimationStyle == LyricsAnimationStyle.METRO_LYRICS && scrollTargetIndex != -1 && isAutoScrollEnabled) {
            deferredCurrentLineIndex = scrollTargetIndex
        }
    }

    LaunchedEffect(currentLineIndex, lastPreviewTime, initialScrollDone, isAutoScrollEnabled, lyricsAnimationStyle) {
        if (lyricsAnimationStyle == LyricsAnimationStyle.METRO_LYRICS) return@LaunchedEffect
        if (!isSynced) return@LaunchedEffect
        if (isAutoScrollEnabled) {
            if ((currentLineIndex == 0 && shouldScrollToFirstLine) || !initialScrollDone) {
                shouldScrollToFirstLine = false
                val initialCenterIndex = kotlin.math.max(0, currentLineIndex)
                performSmoothPageScroll(initialCenterIndex, 800)
                if (!isAppMinimized) {
                    initialScrollDone = true
                }
            } else if (currentLineIndex != -1) {
                deferredCurrentLineIndex = currentLineIndex
                if (isSeeking) {
                    val seekCenterIndex = kotlin.math.max(0, currentLineIndex)
                    performSmoothPageScroll(seekCenterIndex, 500)
                } else if (lastPreviewTime == 0L || currentLineIndex != previousLineIndex) {
                    if (currentLineIndex != previousLineIndex) {
                        performSmoothPageScroll(currentLineIndex, 700)
                    }
                }
            }
        }
        if (currentLineIndex > 0) {
            shouldScrollToFirstLine = true
        }
        previousLineIndex = currentLineIndex
    }

    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 12.dp)
    ) {
        val displayedCurrentLineIndex = if (!isAutoScrollEnabled) {
            currentLineIndex
        } else {
            if (isSeeking || isSelectionModeActive) deferredCurrentLineIndex else currentLineIndex
        }

        if (lyrics == null) {
            ShimmerHost(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = maxHeight / 3)
            ) {
                repeat(10) {
                    Box(
                        contentAlignment = when (lyricsTextPosition) {
                            LyricsPosition.LEFT -> Alignment.CenterStart
                            LyricsPosition.CENTER -> Alignment.Center
                            LyricsPosition.RIGHT -> Alignment.CenterEnd
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                    ) {
                        TextPlaceholder()
                    }
                }
            }
        } else if (lyrics == LYRICS_NOT_FOUND || lines.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.lyrics_not_found),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(0.5f)
                )
            }
        } else if (lyricsAnimationStyle == LyricsAnimationStyle.METRO_LYRICS) {
            MetroLyricsContent(
                lyrics = lyrics,
                lines = lines,
                mergedLyricsList = mergedLyricsList,
                showLyrics = showLyrics,
                isSynced = isSynced,
                maxHeightPx = constraints.maxHeight.toFloat(),
                activeListIndex = activeListIndex,
                deferredCurrentLineIndex = deferredCurrentLineIndex,
                currentPlaybackPosition = currentPlaybackPosition,
                isAutoScrollEnabled = isAutoScrollEnabled,
                onDisableAutoScroll = {
                    isAutoScrollEnabled = false
                    lastPreviewTime = System.currentTimeMillis()
                },
                isLyricsProviderShown = isLyricsProviderShown,
                currentProvider = currentProvider,
                providerTextColor = providerTextColor,
                expressiveAccent = expressiveAccent,
                textColor = textColor,
                lyricsTextPosition = lyricsTextPosition,
                activeLineIndices = activeLineIndices,
                isSelectionModeActive = isSelectionModeActive,
                selectedIndices = selectedIndices,
                maxSelectionLimit = maxSelectionLimit,
                onMaxSelectionReached = { showMaxSelectionToast = true },
                onEnterSelectionMode = { index ->
                    isSelectionModeActive = true
                    selectedIndices.add(index)
                },
                onToggleSelection = { index ->
                    if (selectedIndices.contains(index)) {
                        selectedIndices.remove(index)
                        if (selectedIndices.isEmpty()) isSelectionModeActive = false
                    } else {
                        if (selectedIndices.size < maxSelectionLimit) selectedIndices.add(index)
                        else showMaxSelectionToast = true
                    }
                },
                onLineClick = { index, time ->
                    playerConnection.player.seekTo(time.coerceAtLeast(0))
                    scrollTargetIndex = index
                    isAutoScrollEnabled = true
                    lastPreviewTime = 0L
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp)
            )
        } else {
            LazyColumn(
                state = lazyListState,
                contentPadding = WindowInsets.systemBars
                    .only(WindowInsetsSides.Top)
                    .add(WindowInsets(top = maxHeight / 3, bottom = maxHeight / 2))
                    .asPaddingValues(),
                modifier = Modifier
                    .fadingEdge(vertical = 64.dp)
                    .nestedScroll(remember {
                        object : NestedScrollConnection {
                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource,
                            ): Offset {
                                if (source == NestedScrollSource.UserInput) {
                                    isAutoScrollEnabled = false
                                }
                                if (!isSelectionModeActive) {
                                    lastPreviewTime = System.currentTimeMillis()
                                }
                                return super.onPostScroll(consumed, available, source)
                            }

                            override suspend fun onPostFling(
                                consumed: Velocity,
                                available: Velocity,
                            ): Velocity {
                                isAutoScrollEnabled = false
                                if (!isSelectionModeActive) {
                                    lastPreviewTime = System.currentTimeMillis()
                                }
                                return super.onPostFling(consumed, available)
                            }
                        }
                    })
            ) {
                if (isLyricsProviderShown) {
                    item {
                        Text(
                            text = "Lyrics from $currentProvider",
                            fontSize = 12.sp,
                            color = providerTextColor.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                val effectivePlaybackPosition = currentPlaybackPosition

                itemsIndexed(
                    items = lines,
                    key = { index, item -> "$index-${item.time}" }
                ) { index, item ->
                    val isSelected = selectedIndices.contains(index)
                    val itemModifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(
                            enabled = true,
                            onClick = {
                                if (isSelectionModeActive) {
                                    if (isSelected) {
                                        selectedIndices.remove(index)
                                        if (selectedIndices.isEmpty()) isSelectionModeActive = false
                                    } else {
                                        if (selectedIndices.size < maxSelectionLimit) selectedIndices.add(index)
                                        else showMaxSelectionToast = true
                                    }
                                } else if (isSynced) {
                                    playerConnection.player.seekTo(item.time.coerceAtLeast(0))
                                    scope.launch {
                                        performSmoothPageScroll(index, 700)
                                    }
                                    lastPreviewTime = 0L
                                }
                            },
                            onLongClick = {
                                if (!isSelectionModeActive) {
                                    isSelectionModeActive = true
                                    selectedIndices.add(index)
                                } else if (!isSelected && selectedIndices.size < maxSelectionLimit) {
                                    selectedIndices.add(index)
                                } else if (!isSelected) {
                                    showMaxSelectionToast = true
                                }
                            }
                        )
                        .background(
                            if (isSelected && isSelectionModeActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 24.dp, vertical = (8 * lyricsLineSpacing).dp)

                    val currentLineTime = if (displayedCurrentLineIndex >= 0 && displayedCurrentLineIndex < lines.size) {
                        lines[displayedCurrentLineIndex].time
                    } else -1L
                    val isLineAtSameTime = item.time == currentLineTime
                    val isActiveByIndex = index == displayedCurrentLineIndex
                    val isActiveByTime = isLineAtSameTime && displayedCurrentLineIndex >= 0

                    val alpha by animateFloatAsState(
                        targetValue = when {
                            !isSynced || (isSelectionModeActive && isSelected) -> 1f
                            isActiveByIndex || isActiveByTime -> 1f
                            else -> 0.5f
                        },
                        animationSpec = tween(durationMillis = 400),
                        label = "classicAlpha"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isActiveByIndex || isActiveByTime) 1.05f else 1f,
                        animationSpec = tween(durationMillis = 400),
                        label = "classicScale"
                    )

                    val agentAlignment = when {
                        item.isBackground -> Alignment.CenterHorizontally
                        item.agent == "v1" -> Alignment.Start
                        item.agent == "v2" -> Alignment.End
                        item.agent == "v1000" -> Alignment.CenterHorizontally
                        else -> when (lyricsTextPosition) {
                            LyricsPosition.LEFT -> Alignment.Start
                            LyricsPosition.CENTER -> Alignment.CenterHorizontally
                            LyricsPosition.RIGHT -> Alignment.End
                        }
                    }

                    val agentTextAlign = when {
                        item.isBackground -> TextAlign.Center
                        item.agent == "v1" -> TextAlign.Left
                        item.agent == "v2" -> TextAlign.Right
                        item.agent == "v1000" -> TextAlign.Center
                        else -> when (lyricsTextPosition) {
                            LyricsPosition.LEFT -> TextAlign.Left
                            LyricsPosition.CENTER -> TextAlign.Center
                            LyricsPosition.RIGHT -> TextAlign.Right
                        }
                    }

                    val bgScale = if (item.isBackground) 0.85f else 1f

                    Column(
                        modifier = itemModifier.graphicsLayer {
                            this.alpha = if (item.isBackground) alpha * 0.8f else alpha
                            this.scaleX = scale * bgScale
                            this.scaleY = scale * bgScale
                        },
                        horizontalAlignment = agentAlignment
                    ) {
                        val isActiveLine = (isActiveByIndex || isActiveByTime) && isSynced
                        val lineColor = if (isActiveLine) {
                            if (item.isBackground) expressiveAccent.copy(alpha = 0.85f) else expressiveAccent
                        } else {
                            expressiveAccent.copy(alpha = if (item.isBackground) 0.5f else 0.7f)
                        }
                        val mainText = item.text
                        val hasWordTimings = item.words?.isNotEmpty() == true

                        if (hasWordTimings && lyricsAnimationStyle == LyricsAnimationStyle.NONE) {
                            val styledText = buildAnnotatedString {
                                item.words?.forEachIndexed { wordIndex, word ->
                                    val wordStartMs = (word.startTime * 1000).toLong()
                                    val wordEndMs = (word.endTime * 1000).toLong()
                                    val wordDuration = wordEndMs - wordStartMs
                                    val isWordActive = isActiveLine && effectivePlaybackPosition in wordStartMs..wordEndMs
                                    val hasWordPassed = isActiveLine && effectivePlaybackPosition > wordEndMs
                                    val transitionProgress = when {
                                        !isActiveLine -> 0f
                                        hasWordPassed -> 1f
                                        isWordActive && wordDuration > 0 -> {
                                            val linear = ((effectivePlaybackPosition - wordStartMs).toFloat() / wordDuration).coerceIn(0f, 1f)
                                            linear * linear * (3f - 2f * linear)
                                        }
                                        else -> 0f
                                    }
                                    val wordAlpha = when {
                                        !isActiveLine -> 0.7f
                                        hasWordPassed -> 1f
                                        isWordActive -> 0.5f + (0.5f * transitionProgress)
                                        else -> 0.35f
                                    }
                                    val wordWeight = when {
                                        !isActiveLine -> FontWeight.Bold
                                        hasWordPassed -> FontWeight.Bold
                                        isWordActive -> FontWeight.ExtraBold
                                        else -> FontWeight.Medium
                                    }
                                    withStyle(SpanStyle(color = expressiveAccent.copy(alpha = wordAlpha), fontWeight = wordWeight)) {
                                        append(word.text)
                                    }
                                    if (wordIndex < (item.words?.size ?: 0) - 1) append(" ")
                                }
                            }
                            Text(
                                text = styledText,
                                fontSize = lyricsTextSize.sp,
                                textAlign = agentTextAlign,
                                lineHeight = (lyricsTextSize * lyricsLineSpacing.coerceAtMost(1.3f)).sp
                            )
                        } else if (hasWordTimings && lyricsAnimationStyle == LyricsAnimationStyle.FADE) {
                            val styledText = buildAnnotatedString {
                                item.words?.forEachIndexed { wordIndex, word ->
                                    val wordStartMs = (word.startTime * 1000).toLong()
                                    val wordEndMs = (word.endTime * 1000).toLong()
                                    val wordDuration = wordEndMs - wordStartMs
                                    val isWordActive = isActiveLine && effectivePlaybackPosition in wordStartMs..wordEndMs
                                    val hasWordPassed = isActiveLine && effectivePlaybackPosition > wordEndMs
                                    val fadeProgress = if (isWordActive && wordDuration > 0) {
                                        val linear = ((effectivePlaybackPosition - wordStartMs).toFloat() / wordDuration).coerceIn(0f, 1f)
                                        linear * linear * (3f - 2f * linear)
                                    } else if (hasWordPassed) 1f else 0f
                                    val wordAlpha = when {
                                        !isActiveLine -> 0.55f
                                        hasWordPassed -> 1f
                                        isWordActive -> 0.4f + (0.6f * fadeProgress)
                                        else -> 0.4f
                                    }
                                    val wordWeight = when {
                                        !isActiveLine -> FontWeight.Bold
                                        hasWordPassed -> FontWeight.Bold
                                        isWordActive -> FontWeight.ExtraBold
                                        else -> FontWeight.Medium
                                    }
                                    val wordShadow = when {
                                        isWordActive && fadeProgress > 0.2f -> Shadow(
                                            color = expressiveAccent.copy(alpha = 0.35f * fadeProgress),
                                            offset = Offset.Zero,
                                            blurRadius = 10f * fadeProgress
                                        )
                                        hasWordPassed -> Shadow(
                                            color = expressiveAccent.copy(alpha = 0.15f),
                                            offset = Offset.Zero,
                                            blurRadius = 6f
                                        )
                                        else -> null
                                    }
                                    withStyle(SpanStyle(color = expressiveAccent.copy(alpha = wordAlpha), fontWeight = wordWeight, shadow = wordShadow)) {
                                        append(word.text)
                                    }
                                    if (wordIndex < (item.words?.size ?: 0) - 1) append(" ")
                                }
                            }
                            Text(
                                text = styledText,
                                fontSize = lyricsTextSize.sp,
                                textAlign = agentTextAlign,
                                lineHeight = (lyricsTextSize * lyricsLineSpacing.coerceAtMost(1.3f)).sp
                            )
                        } else if (hasWordTimings && lyricsAnimationStyle == LyricsAnimationStyle.GLOW) {
                            val styledText = buildAnnotatedString {
                                item.words?.forEachIndexed { wordIndex, word ->
                                    val wordStartMs = (word.startTime * 1000).toLong()
                                    val wordEndMs = (word.endTime * 1000).toLong()
                                    val wordDuration = wordEndMs - wordStartMs
                                    val isWordActive = isActiveLine && effectivePlaybackPosition in wordStartMs..wordEndMs
                                    val hasWordPassed = isActiveLine && effectivePlaybackPosition > wordEndMs
                                    val fillProgress = if (isWordActive && wordDuration > 0) {
                                        val linear = ((effectivePlaybackPosition - wordStartMs).toFloat() / wordDuration).coerceIn(0f, 1f)
                                        linear * linear * (3f - 2f * linear)
                                    } else if (hasWordPassed) 1f else 0f
                                    val glowIntensity = fillProgress * fillProgress
                                    val brightness = 0.45f + (0.55f * fillProgress)
                                    val wordColor = when {
                                        !isActiveLine -> expressiveAccent.copy(alpha = 0.5f)
                                        isWordActive || hasWordPassed -> expressiveAccent.copy(alpha = brightness)
                                        else -> expressiveAccent.copy(alpha = 0.35f)
                                    }
                                    val wordWeight = when {
                                        !isActiveLine -> FontWeight.Bold
                                        isWordActive -> FontWeight.ExtraBold
                                        hasWordPassed -> FontWeight.Bold
                                        else -> FontWeight.Medium
                                    }
                                    val wordShadow = if (isWordActive && glowIntensity > 0.05f) {
                                        Shadow(color = expressiveAccent.copy(alpha = 0.5f + (0.3f * glowIntensity)), offset = Offset.Zero, blurRadius = 16f + (12f * glowIntensity))
                                    } else if (hasWordPassed) {
                                        Shadow(color = expressiveAccent.copy(alpha = 0.25f), offset = Offset.Zero, blurRadius = 8f)
                                    } else null
                                    withStyle(SpanStyle(color = wordColor, fontWeight = wordWeight, shadow = wordShadow)) {
                                        append(word.text)
                                    }
                                    if (wordIndex < (item.words?.size ?: 0) - 1) append(" ")
                                }
                            }
                            Text(
                                text = styledText,
                                fontSize = lyricsTextSize.sp,
                                textAlign = agentTextAlign,
                                lineHeight = (lyricsTextSize * lyricsLineSpacing.coerceAtMost(1.3f)).sp
                            )
                        } else if (hasWordTimings && lyricsAnimationStyle == LyricsAnimationStyle.SLIDE) {
                            val styledText = buildAnnotatedString {
                                item.words?.forEachIndexed { wordIndex, word ->
                                    val wordStartMs = (word.startTime * 1000).toLong()
                                    val wordEndMs = (word.endTime * 1000).toLong()
                                    val wordDuration = wordEndMs - wordStartMs
                                    val isWordActive = isActiveLine && effectivePlaybackPosition >= wordStartMs && effectivePlaybackPosition < wordEndMs
                                    val hasWordPassed = (isActiveLine && effectivePlaybackPosition >= wordEndMs) || (!isActiveLine && item.time < currentLineTime)

                                    if (isWordActive && wordDuration > 0) {
                                        val timeElapsed = effectivePlaybackPosition - wordStartMs
                                        val fillProgress = (timeElapsed.toFloat() / wordDuration.toFloat()).coerceIn(0f, 1f)
                                        val breatheValue = (timeElapsed % 3000) / 3000f
                                        val breatheEffect = (sin(breatheValue * Math.PI.toFloat() * 2f) * 0.03f).coerceIn(0f, 0.03f)
                                        val glowIntensity = (0.3f + fillProgress * 0.7f + breatheEffect).coerceIn(0f, 1.1f)
                                        val slideBrush = Brush.horizontalGradient(
                                            0.0f to expressiveAccent,
                                            (fillProgress * 0.95f).coerceIn(0f, 1f) to expressiveAccent,
                                            fillProgress to expressiveAccent.copy(alpha = 0.9f),
                                            (fillProgress + 0.02f).coerceIn(0f, 1f) to expressiveAccent.copy(alpha = 0.5f),
                                            (fillProgress + 0.08f).coerceIn(0f, 1f) to expressiveAccent.copy(alpha = 0.35f),
                                            1.0f to expressiveAccent.copy(alpha = 0.35f)
                                        )
                                        withStyle(
                                            SpanStyle(
                                                brush = slideBrush,
                                                fontWeight = FontWeight.ExtraBold,
                                                shadow = Shadow(color = expressiveAccent.copy(alpha = 0.4f * glowIntensity), offset = Offset.Zero, blurRadius = 14f + (4f * fillProgress))
                                            )
                                        ) { append(word.text) }
                                    } else if (hasWordPassed && isActiveLine) {
                                        withStyle(
                                            SpanStyle(
                                                color = expressiveAccent,
                                                fontWeight = FontWeight.Bold,
                                                shadow = Shadow(color = expressiveAccent.copy(alpha = 0.4f), offset = Offset.Zero, blurRadius = 12f)
                                            )
                                        ) { append(word.text) }
                                    } else {
                                        val wordColor = if (!isActiveLine) lineColor else expressiveAccent.copy(alpha = 0.35f)
                                        withStyle(SpanStyle(color = wordColor, fontWeight = FontWeight.Medium)) { append(word.text) }
                                    }
                                    if (wordIndex < (item.words?.size ?: 0) - 1) append(" ")
                                }
                            }
                            Text(text = styledText, fontSize = lyricsTextSize.sp, textAlign = agentTextAlign, lineHeight = (lyricsTextSize * lyricsLineSpacing).sp)
                        } else if (hasWordTimings && lyricsAnimationStyle == LyricsAnimationStyle.KARAOKE) {
                            val styledText = buildAnnotatedString {
                                item.words?.forEachIndexed { wordIndex, word ->
                                    val wordStartMs = (word.startTime * 1000).toLong()
                                    val wordEndMs = (word.endTime * 1000).toLong()
                                    val wordDuration = wordEndMs - wordStartMs
                                    val isWordActive = isActiveLine && effectivePlaybackPosition >= wordStartMs && effectivePlaybackPosition < wordEndMs
                                    val hasWordPassed = (isActiveLine && effectivePlaybackPosition >= wordEndMs) || (!isActiveLine && item.time < currentLineTime)

                                    if (isWordActive && wordDuration > 0) {
                                        val linearProgress = ((effectivePlaybackPosition - wordStartMs).toFloat() / wordDuration).coerceIn(0f, 1f)
                                        val fillProgress = linearProgress * linearProgress * (3f - 2f * linearProgress)
                                        val glowIntensity = fillProgress * fillProgress
                                        val wordBrush = Brush.horizontalGradient(
                                            0.0f to expressiveAccent.copy(alpha = 0.4f),
                                            (fillProgress * 0.6f).coerceIn(0f, 1f) to expressiveAccent.copy(alpha = 0.75f),
                                            (fillProgress * 0.85f).coerceIn(0f, 1f) to expressiveAccent.copy(alpha = 0.95f),
                                            fillProgress to expressiveAccent,
                                            (fillProgress + 0.03f).coerceIn(0f, 1f) to expressiveAccent.copy(alpha = 0.85f),
                                            (fillProgress + 0.1f).coerceIn(0f, 1f) to expressiveAccent.copy(alpha = 0.5f),
                                            1.0f to expressiveAccent.copy(alpha = if (fillProgress >= 0.9f) 0.95f else 0.4f)
                                        )
                                        val wordShadow = Shadow(
                                            color = expressiveAccent.copy(alpha = 0.5f + (0.3f * glowIntensity)),
                                            offset = Offset.Zero,
                                            blurRadius = 16f + (12f * glowIntensity)
                                        )
                                        withStyle(SpanStyle(brush = wordBrush, fontWeight = FontWeight.ExtraBold, shadow = wordShadow)) {
                                            append(word.text)
                                        }
                                    } else if (hasWordPassed && isActiveLine) {
                                        withStyle(
                                            SpanStyle(
                                                color = expressiveAccent,
                                                fontWeight = FontWeight.Bold,
                                                shadow = Shadow(color = expressiveAccent.copy(alpha = 0.25f), offset = Offset.Zero, blurRadius = 8f)
                                            )
                                        ) { append(word.text) }
                                    } else {
                                        val wordColor = if (!isActiveLine) lineColor else expressiveAccent.copy(alpha = 0.4f)
                                        withStyle(SpanStyle(color = wordColor, fontWeight = FontWeight.Medium)) { append(word.text) }
                                    }
                                    if (wordIndex < (item.words?.size ?: 0) - 1) append(" ")
                                }
                            }
                            Text(text = styledText, fontSize = lyricsTextSize.sp, textAlign = agentTextAlign, lineHeight = (lyricsTextSize * lyricsLineSpacing.coerceAtMost(1.3f)).sp)
                        } else if (hasWordTimings && lyricsAnimationStyle == LyricsAnimationStyle.APPLE) {
                            val styledText = buildAnnotatedString {
                                item.words?.forEachIndexed { wordIndex, word ->
                                    val wordStartMs = (word.startTime * 1000).toLong()
                                    val wordEndMs = (word.endTime * 1000).toLong()
                                    val wordDuration = wordEndMs - wordStartMs
                                    val isWordActive = isActiveLine && effectivePlaybackPosition >= wordStartMs && effectivePlaybackPosition < wordEndMs
                                    val hasWordPassed = (isActiveLine && effectivePlaybackPosition >= wordEndMs) || (!isActiveLine && item.time < currentLineTime)
                                    val rawProgress = if (isWordActive && wordDuration > 0) {
                                        ((effectivePlaybackPosition - wordStartMs).toFloat() / wordDuration).coerceIn(0f, 1f)
                                    } else if (hasWordPassed) 1f else 0f
                                    val smoothProgress = rawProgress * rawProgress * (3f - 2f * rawProgress)
                                    val wordAlpha = when {
                                        !isActiveLine -> 0.55f
                                        hasWordPassed -> 1f
                                        isWordActive -> 0.55f + (0.45f * smoothProgress)
                                        else -> 0.4f
                                    }
                                    val wordWeight = when {
                                        !isActiveLine -> FontWeight.SemiBold
                                        hasWordPassed -> FontWeight.Bold
                                        isWordActive -> FontWeight.ExtraBold
                                        else -> FontWeight.Normal
                                    }
                                    val glowIntensity = smoothProgress * smoothProgress
                                    val wordShadow = when {
                                        isWordActive -> Shadow(
                                            color = expressiveAccent.copy(alpha = 0.2f + (0.4f * glowIntensity)),
                                            offset = Offset.Zero,
                                            blurRadius = 10f + (12f * glowIntensity)
                                        )
                                        hasWordPassed && isActiveLine -> Shadow(
                                            color = expressiveAccent.copy(alpha = 0.2f),
                                            offset = Offset.Zero,
                                            blurRadius = 8f
                                        )
                                        else -> null
                                    }
                                    withStyle(SpanStyle(color = expressiveAccent.copy(alpha = wordAlpha), fontWeight = wordWeight, shadow = wordShadow)) {
                                        append(word.text)
                                    }
                                    if (wordIndex < (item.words?.size ?: 0) - 1) append(" ")
                                }
                            }
                            Text(text = styledText, fontSize = lyricsTextSize.sp, textAlign = agentTextAlign, lineHeight = (lyricsTextSize * lyricsLineSpacing.coerceAtMost(1.3f)).sp)
                        } else if (isActiveLine && lyricsGlowEffect) {
                            val fillProgress = remember { Animatable(0f) }
                            val pulseProgress = remember { Animatable(0f) }
                            LaunchedEffect(index) {
                                fillProgress.snapTo(0f)
                                fillProgress.animateTo(1f, tween(durationMillis = 1200, easing = FastOutSlowInEasing))
                            }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    pulseProgress.animateTo(1f, tween(durationMillis = 3000, easing = LinearEasing))
                                    pulseProgress.snapTo(0f)
                                }
                            }
                            val fill = fillProgress.value
                            val pulse = pulseProgress.value
                            val pulseEffect = (sin(pulse * Math.PI.toFloat()) * 0.15f).coerceIn(0f, 0.15f)
                            val glowIntensity = (fill + pulseEffect).coerceIn(0f, 1.2f)
                            val glowBrush = Brush.horizontalGradient(
                                0.0f to expressiveAccent.copy(alpha = 0.3f),
                                (fill * 0.7f).coerceIn(0f, 1f) to expressiveAccent.copy(alpha = 0.9f),
                                fill to expressiveAccent,
                                (fill + 0.1f).coerceIn(0f, 1f) to expressiveAccent.copy(alpha = 0.7f),
                                1.0f to expressiveAccent.copy(alpha = if (fill >= 1f) 1f else 0.3f)
                            )
                            val styledText = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        shadow = Shadow(
                                            color = expressiveAccent.copy(alpha = 0.8f * glowIntensity),
                                            offset = Offset.Zero,
                                            blurRadius = 28f * (1f + pulseEffect)
                                        ),
                                        brush = glowBrush
                                    )
                                ) { append(mainText) }
                            }
                            val bounceScale = if (fill < 0.3f) {
                                1f + (sin(fill * 3.33f * Math.PI.toFloat()) * 0.03f)
                            } else 1f
                            Text(
                                text = styledText,
                                fontSize = lyricsTextSize.sp,
                                textAlign = agentTextAlign,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = (lyricsTextSize * lyricsLineSpacing.coerceAtMost(1.3f)).sp,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = bounceScale
                                    scaleY = bounceScale
                                }
                            )
                        } else if (isActiveLine) {
                            Text(
                                text = mainText,
                                fontSize = lyricsTextSize.sp,
                                color = expressiveAccent,
                                textAlign = agentTextAlign,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = (lyricsTextSize * lyricsLineSpacing.coerceAtMost(1.3f)).sp
                            )
                        } else {
                            Text(
                                text = mainText,
                                fontSize = lyricsTextSize.sp,
                                color = lineColor,
                                textAlign = agentTextAlign,
                                fontWeight = FontWeight.Bold,
                                lineHeight = (lyricsTextSize * lyricsLineSpacing.coerceAtMost(1.3f)).sp
                            )
                        }
                    }
                }
            }
        }

        // Sync / Selection controls at bottom center
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            AnimatedVisibility(
                visible = !isAutoScrollEnabled && isSynced && !isSelectionModeActive,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                FilledTonalButton(
                    onClick = {
                        if (lyricsAnimationStyle == LyricsAnimationStyle.METRO_LYRICS) {
                            scrollTargetIndex = currentLineIndex
                        } else {
                            scope.launch {
                                performSmoothPageScroll(currentLineIndex, 1500)
                            }
                        }
                        isAutoScrollEnabled = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sync")
                }
            }

            AnimatedVisibility(
                visible = isSelectionModeActive,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = {
                            isSelectionModeActive = false
                            selectedIndices.clear()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(android.R.string.cancel),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            if (selectedIndices.isNotEmpty()) {
                                val sortedIndices = selectedIndices.sorted()
                                val selectedLyricsText = sortedIndices
                                    .mapNotNull { lines.getOrNull(it)?.text }
                                    .joinToString("\n")
                                if (selectedLyricsText.isNotBlank()) {
                                    shareDialogData = Triple(
                                        selectedLyricsText,
                                        mediaMetadata?.title ?: "",
                                        mediaMetadata?.artists?.joinToString { it.name } ?: ""
                                    )
                                    showShareDialog = true
                                }
                                isSelectionModeActive = false
                                selectedIndices.clear()
                            }
                        },
                        enabled = selectedIndices.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.share),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.share))
                    }
                }
            }
        }

        // NimbleTune bottom-end Close & More buttons
        mediaMetadata?.let { meta ->
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp + bottomEndReservedPadding, bottom = 12.dp)
            ) {
                IconButton(
                    onClick = {
                        if (onClose != null) onClose() else showLyricsPref = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = providerTextColor
                    )
                }
                IconButton(
                    onClick = {
                        menuState.show {
                            LyricsMenu(
                                lyricsProvider = { lyricsEntity },
                                mediaMetadataProvider = { meta },
                                onDismiss = menuState::dismiss
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = null,
                        tint = providerTextColor
                    )
                }
            }
        }
    }

    if (showShareDialog && shareDialogData != null) {
        val (lyricsText, songTitle, artists) = shareDialogData!!
        BasicAlertDialog(onDismissRequest = { showShareDialog = false }) {
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.share),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clip = ClipData.newPlainText("Lyrics", lyricsText)
                                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                    .setPrimaryClip(clip)
                                Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                                showShareDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Copy to clipboard",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    val songLink = "https://music.youtube.com/watch?v=${mediaMetadata?.id}"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "\"$lyricsText\"\n\n$songTitle - $artists\n$songLink"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
                                showShareDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Share as text",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { showShareDialog = false }
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetroLyricsContent(
    lyrics: String,
    lines: List<LyricsEntry>,
    mergedLyricsList: List<LyricsListItem>,
    showLyrics: Boolean,
    isSynced: Boolean,
    maxHeightPx: Float,
    activeListIndex: Int,
    deferredCurrentLineIndex: Int,
    currentPlaybackPosition: Long,
    isAutoScrollEnabled: Boolean,
    onDisableAutoScroll: () -> Unit,
    isLyricsProviderShown: Boolean,
    currentProvider: String?,
    providerTextColor: Color,
    expressiveAccent: Color,
    textColor: Color,
    lyricsTextPosition: LyricsPosition,
    activeLineIndices: Set<Int>,
    isSelectionModeActive: Boolean,
    selectedIndices: SnapshotStateList<Int>,
    maxSelectionLimit: Int,
    onMaxSelectionReached: () -> Unit,
    onEnterSelectionMode: (Int) -> Unit,
    onToggleSelection: (Int) -> Unit,
    onLineClick: (Int, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    // Non-snapshot backing map updated during measure; synced to Compose state between frames.
    val measuredHeights = remember(lyrics, mergedLyricsList) { mutableMapOf<Int, Int>() }
    var itemHeights by remember(lyrics, mergedLyricsList) { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    val heightUpdateChannel = remember(lyrics, mergedLyricsList) { Channel<Unit>(Channel.CONFLATED) }

    var isInitialLayout by remember(lyrics, mergedLyricsList) { mutableStateOf(true) }
    var userManualOffset by remember { mutableFloatStateOf(0f) }
    var flingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val velocityTracker = remember { VelocityTracker() }
    val decayAnimSpec = remember { exponentialDecay<Float>(frictionMultiplier = 1.8f) }

    LaunchedEffect(lyrics, mergedLyricsList, heightUpdateChannel) {
        for (ignored in heightUpdateChannel) {
            withFrameNanos { }
            val snapshot = HashMap(measuredHeights)
            if (snapshot != itemHeights) {
                itemHeights = snapshot
            }
        }
    }

    LaunchedEffect(showLyrics, lyrics, mergedLyricsList.size) {
        if (showLyrics && mergedLyricsList.isNotEmpty()) {
            isInitialLayout = true
            snapshotFlow {
                val h = itemHeights
                val windowStart = (activeListIndex - 8).coerceAtLeast(0)
                val windowEnd = (activeListIndex + 12).coerceAtMost(mergedLyricsList.size - 1)
                (windowStart..windowEnd).all { h.containsKey(it) }
            }.first { it }
            delay(150)
            isInitialLayout = false
        }
    }

    LaunchedEffect(activeListIndex) {
        if (mergedLyricsList.isNotEmpty()) {
            val h = itemHeights
            val windowStart = (activeListIndex - 3).coerceAtLeast(0)
            val windowEnd = (activeListIndex + 3).coerceAtMost(mergedLyricsList.size - 1)
            val needsMeasurement = (windowStart..windowEnd).any { !h.containsKey(it) }
            if (needsMeasurement) {
                isInitialLayout = true
                snapshotFlow {
                    val hh = itemHeights
                    (windowStart..windowEnd).all { hh.containsKey(it) }
                }.first { it }
                isInitialLayout = false
            }
        }
    }

    val anchorY = maxHeightPx * LYRICS_ANCHOR_RATIO
    val lineHeightPx = with(density) { LYRICS_ITEM_FALLBACK_HEIGHT_DP.toPx() }
    val constraintLineHeightPx = with(density) { 120.dp.toPx() }
    val indicatorHeightPx = with(density) { 72.dp.toPx() }
    val itemGapPx = with(density) { LYRICS_ITEM_GAP_DP.toPx() }

    val positions = remember(itemHeights, activeListIndex, mergedLyricsList) {
        val map = mutableMapOf<Int, Float>()
        if (activeListIndex == -1 || mergedLyricsList.isEmpty()) return@remember map

        map[activeListIndex] = 0f
        var currentY = 0f
        for (i in activeListIndex - 1 downTo 0) {
            val item = mergedLyricsList[i]
            val height = itemHeights[i]?.toFloat()
                ?: (if (item is LyricsListItem.Indicator) indicatorHeightPx else lineHeightPx)
            val noGap = (item as? LyricsListItem.Line)?.entry?.isBackground == true || item is LyricsListItem.Indicator
            currentY -= (height + if (noGap) 0f else itemGapPx)
            map[i] = currentY
        }
        currentY = 0f
        for (i in activeListIndex until mergedLyricsList.size - 1) {
            val currentItem = mergedLyricsList[i]
            val nextItem = mergedLyricsList[i + 1]
            val height = itemHeights[i]?.toFloat()
                ?: (if (currentItem is LyricsListItem.Indicator) indicatorHeightPx else lineHeightPx)
            val nextNoGap = (nextItem as? LyricsListItem.Line)?.entry?.isBackground == true || nextItem is LyricsListItem.Indicator
            currentY += (height + if (nextNoGap) 0f else itemGapPx)
            map[i + 1] = currentY
        }
        map
    }

    val minOffset = remember(itemHeights, mergedLyricsList, activeListIndex, anchorY) {
        if (mergedLyricsList.isEmpty() || activeListIndex == -1) return@remember 0f
        val totalBelow = (activeListIndex until mergedLyricsList.size - 1).sumOf { i ->
            val currentItem = mergedLyricsList[i]
            val nextItem = mergedLyricsList[i + 1]
            val height = itemHeights[i]?.toFloat()
                ?: (if (currentItem is LyricsListItem.Indicator) indicatorHeightPx else constraintLineHeightPx)
            val nextNoGap = (nextItem as? LyricsListItem.Line)?.entry?.isBackground == true || nextItem is LyricsListItem.Indicator
            (height + if (nextNoGap) 0f else itemGapPx).toDouble()
        }.toFloat()
        val lastItem = mergedLyricsList.last()
        val lastHeight = itemHeights[mergedLyricsList.size - 1]?.toFloat()
            ?: (if (lastItem is LyricsListItem.Indicator) indicatorHeightPx else constraintLineHeightPx)
        with(density) { 100.dp.toPx() } - anchorY - totalBelow - lastHeight
    }

    val maxOffset = remember(itemHeights, mergedLyricsList, activeListIndex, maxHeightPx, anchorY) {
        if (mergedLyricsList.isEmpty() || activeListIndex == -1) return@remember 0f
        val totalAbove = (0 until activeListIndex).sumOf { i ->
            val item = mergedLyricsList[i]
            val height = itemHeights[i]?.toFloat()
                ?: (if (item is LyricsListItem.Indicator) indicatorHeightPx else constraintLineHeightPx)
            val noGap = (item as? LyricsListItem.Line)?.entry?.isBackground == true || item is LyricsListItem.Indicator
            (height + if (noGap) 0f else itemGapPx).toDouble()
        }.toFloat()
        maxHeightPx - with(density) { 150.dp.toPx() } - anchorY + totalAbove
    }

    val scrollClampMin = minOf(minOffset, maxOffset)
    val scrollClampMax = maxOf(minOffset, maxOffset)

    val latestScrollClampMin by rememberUpdatedState(scrollClampMin)
    val latestScrollClampMax by rememberUpdatedState(scrollClampMax)
    val latestIsAutoScrollEnabled by rememberUpdatedState(isAutoScrollEnabled)
    val latestOnDisableAutoScroll by rememberUpdatedState(onDisableAutoScroll)

    LaunchedEffect(scrollClampMin, scrollClampMax) {
        if (userManualOffset < scrollClampMin || userManualOffset > scrollClampMax) {
            userManualOffset = userManualOffset.coerceIn(scrollClampMin, scrollClampMax)
        }
    }

    LaunchedEffect(isAutoScrollEnabled, mergedLyricsList.size) {
        if (isAutoScrollEnabled) {
            val start = userManualOffset
            if (kotlin.math.abs(start) < 1f) {
                userManualOffset = 0f
                return@LaunchedEffect
            }
            val anim = Animatable(start)
            var lastValue = start
            anim.animateTo(
                0f,
                tween((kotlin.math.abs(start) / 4f).toInt().coerceIn(200, 600), easing = FastOutSlowInEasing)
            ) {
                userManualOffset += (value - lastValue)
                lastValue = value
            }
            userManualOffset = 0f
        }
    }

    Box(
        modifier = modifier
            .fadingEdge(top = LYRICS_FADE_TOP_DP, bottom = LYRICS_FADE_BOTTOM_DP)
            .clipToBounds()
            .pointerInput(Unit) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        flingJob?.cancel()
                        velocityTracker.resetTracking()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)

                        verticalDrag(down.id) { change ->
                            val dragAmount = change.positionChange().y
                            velocityTracker.addPosition(change.uptimeMillis, change.position)

                            val clampMin = latestScrollClampMin
                            val clampMax = latestScrollClampMax
                            val newOffset = userManualOffset + dragAmount
                            if (newOffset in clampMin..clampMax) {
                                userManualOffset = newOffset
                                change.consume()
                            } else {
                                val overscroll = if (newOffset < clampMin) newOffset - clampMin else newOffset - clampMax
                                val friction = 0.35f / (1f + kotlin.math.abs(overscroll) / 300f)
                                userManualOffset = (userManualOffset + dragAmount * friction).coerceIn(
                                    clampMin - 150f,
                                    clampMax + 150f
                                )
                                change.consume()
                            }
                            latestOnDisableAutoScroll()
                        }

                        if (latestIsAutoScrollEnabled) return@awaitEachGesture

                        val clampMin = latestScrollClampMin
                        val clampMax = latestScrollClampMax
                        val velocity = velocityTracker.calculateVelocity().y
                        if (kotlin.math.abs(velocity) > 100f) {
                            flingJob = launch {
                                val animState = AnimationState(
                                    initialValue = userManualOffset,
                                    initialVelocity = velocity
                                )
                                animState.animateDecay(decayAnimSpec) {
                                    val cMin = latestScrollClampMin
                                    val cMax = latestScrollClampMax
                                    if (value in cMin..cMax) {
                                        userManualOffset = value
                                    } else {
                                        val target = value.coerceIn(cMin, cMax)
                                        cancelAnimation()
                                        launch {
                                            Animatable(userManualOffset).animateTo(
                                                targetValue = target,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            ) {
                                                userManualOffset = value
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (userManualOffset < clampMin || userManualOffset > clampMax) {
                            val target = userManualOffset.coerceIn(clampMin, clampMax)
                            flingJob = launch {
                                Animatable(userManualOffset).animateTo(
                                    targetValue = target,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) {
                                    userManualOffset = value
                                }
                            }
                        }
                    }
                }
            }
    ) {
        if (isLyricsProviderShown) {
            val targetProviderBase = anchorY + (positions[0] ?: 0f) - with(density) { 32.dp.toPx() }
            val animatedProviderBase by animateFloatAsState(
                targetValue = targetProviderBase,
                animationSpec = if (isInitialLayout || !isAutoScrollEnabled) snap()
                else tween(750, 0, FastOutSlowInEasing),
                label = "lyricsProviderOffset"
            )
            Text(
                text = "Lyrics from $currentProvider",
                fontSize = 12.sp,
                color = providerTextColor.copy(alpha = if (isInitialLayout) 0f else 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, (animatedProviderBase + userManualOffset).roundToInt()) }
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }

        mergedLyricsList.forEachIndexed { listIndex, listItem ->
            val itemKey = when (listItem) {
                is LyricsListItem.Line -> "line_${listItem.index}"
                is LyricsListItem.Indicator -> "ind_${listItem.afterLineIndex}"
            }
            key(itemKey) {
                val distance = kotlin.math.abs(listIndex - activeListIndex)
                val targetOffset = anchorY + (positions[listIndex] ?: ((listIndex - activeListIndex) * lineHeightPx))

                val frozenOffset = remember { mutableFloatStateOf(targetOffset) }
                LaunchedEffect(isAutoScrollEnabled, targetOffset, isInitialLayout) {
                    if (isAutoScrollEnabled || isInitialLayout) {
                        frozenOffset.floatValue = targetOffset
                    }
                }

                val animatedOffset by animateFloatAsState(
                    targetValue = if (isAutoScrollEnabled) targetOffset else frozenOffset.floatValue,
                    animationSpec = if (isInitialLayout || !isAutoScrollEnabled) snap()
                    else tween(
                        750,
                        (distance * LYRICS_STAGGER_DELAY_PER_DISTANCE).coerceAtMost(LYRICS_STAGGER_DELAY_MAX_MS),
                        FastOutSlowInEasing
                    ),
                    label = "lyricStaggeredOffset_$listIndex"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = if (isInitialLayout) 0f else 1f
                        }
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
                            if (measuredHeights[listIndex] != placeable.height) {
                                measuredHeights[listIndex] = placeable.height
                                heightUpdateChannel.trySend(Unit)
                            }
                            layout(placeable.width, 0) {
                                placeable.place(0, 0)
                            }
                        }
                        .offset { IntOffset(0, (animatedOffset + userManualOffset).roundToInt()) }
                ) {
                    when (listItem) {
                        is LyricsListItem.Line -> {
                            val index = listItem.index
                            val item = listItem.entry
                            val isSelected = selectedIndices.contains(index)

                            val pairedMainLineIndex = if (item.isBackground) {
                                (index - 1 downTo 0).firstOrNull { lines.getOrNull(it)?.isBackground == false } ?: -1
                            } else -1
                            val isInGapWithMain = if (item.isBackground && pairedMainLineIndex != -1) {
                                val pairedMainLine = lines[pairedMainLineIndex]
                                currentPlaybackPosition >= pairedMainLine.time && currentPlaybackPosition <= item.time
                            } else false
                            val bgVisible = !item.isBackground ||
                                (activeLineIndices.contains(pairedMainLineIndex) || activeLineIndices.contains(index) || isInGapWithMain)

                            MetroLyricsLine(
                                entry = item,
                                nextEntryTime = lines.getOrNull(index + 1)?.time,
                                effectivePlaybackPosition = currentPlaybackPosition,
                                lyricsOffset = 0L,
                                isSynced = isSynced,
                                isActive = activeLineIndices.contains(index),
                                distanceFromCurrent = kotlin.math.abs(index - deferredCurrentLineIndex),
                                lyricsTextPosition = lyricsTextPosition,
                                textColor = textColor,
                                showRomanized = false,
                                showTranslated = false,
                                isAutoScrollActive = isAutoScrollEnabled,
                                isSelectionModeActive = isSelectionModeActive,
                                isSelected = isSelected,
                                expressiveAccent = expressiveAccent,
                                bgVisible = bgVisible,
                                onClick = {
                                    if (isSelectionModeActive) {
                                        onToggleSelection(index)
                                    } else if (isSynced) {
                                        onLineClick(index, item.time)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionModeActive) {
                                        onEnterSelectionMode(index)
                                    } else if (!isSelected && selectedIndices.size < maxSelectionLimit) {
                                        selectedIndices.add(index)
                                    } else if (!isSelected) {
                                        onMaxSelectionReached()
                                    }
                                }
                            )
                        }
                        is LyricsListItem.Indicator -> {
                            val indicatorVisible = isAutoScrollEnabled &&
                                currentPlaybackPosition >= listItem.gapStartMs &&
                                currentPlaybackPosition <= listItem.gapEndMs - 650L
                            IntervalIndicator(
                                gapStartMs = listItem.gapStartMs,
                                gapEndMs = listItem.gapEndMs - 650L,
                                currentPositionMs = currentPlaybackPosition,
                                visible = indicatorVisible,
                                color = expressiveAccent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun IntervalIndicator(
    gapStartMs: Long,
    gapEndMs: Long,
    currentPositionMs: Long,
    visible: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val alpha = remember { Animatable(0f) }
    val rowHeightPx = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            rowHeightPx.animateTo(1f, tween(200))
            alpha.animateTo(1f, tween(200))
        } else {
            alpha.animateTo(0f, tween(200))
            rowHeightPx.animateTo(0f, tween(200))
        }
    }

    val targetHeightDp = 72.dp

    val progress = if (gapEndMs > gapStartMs) {
        ((currentPositionMs - gapStartMs).toFloat() / (gapEndMs - gapStartMs).toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "intervalProgress"
    )

    Box(
        modifier = modifier
            .height(targetHeightDp * rowHeightPx.value)
            .padding(top = 16.dp * rowHeightPx.value)
            .graphicsLayer {
                this.alpha = alpha.value
                this.clip = true
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(36.dp)
                .alpha(alpha.value)
        ) {
            val strokePx = 3.dp.toPx()
            val baseRadius = (size.minDimension - strokePx * 2f) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = strokePx)
            )

            if (animatedProgress > 0.005f) {
                val sweepDeg = 360f * animatedProgress
                val steps = (sweepDeg / 4f).toInt().coerceAtLeast(8)
                val waveAmplitude = 1.3.dp.toPx()
                val waveCount = 8f
                val path = Path()
                for (s in 0..steps) {
                    val frac = s.toFloat() / steps
                    val angleRad = ((-90f + sweepDeg * frac) * PI / 180.0).toFloat()
                    val r = baseRadius + sin(angleRad * waveCount) * waveAmplitude
                    val x = center.x + r * cos(angleRad)
                    val y = center.y + r * sin(angleRad)
                    if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

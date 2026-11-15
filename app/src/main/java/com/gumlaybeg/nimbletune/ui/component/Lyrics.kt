package com.gumlaybeg.nimbletune.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.LyricFontSizeKey
import com.gumlaybeg.nimbletune.constants.LyricTrimKey
import com.gumlaybeg.nimbletune.constants.LyricsTextPositionKey
import com.gumlaybeg.nimbletune.constants.MultilineLrcKey
import com.gumlaybeg.nimbletune.constants.PlayerBackgroundStyleKey
import com.gumlaybeg.nimbletune.constants.ShowLyricsKey
import com.gumlaybeg.nimbletune.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.gumlaybeg.nimbletune.lyrics.LyricsEntry
import com.gumlaybeg.nimbletune.lyrics.LyricsEntry.Companion.HEAD_LYRICS_ENTRY
import com.gumlaybeg.nimbletune.lyrics.LyricsUtils
import com.gumlaybeg.nimbletune.ui.menu.LyricsMenu
import com.gumlaybeg.nimbletune.ui.screens.settings.LyricsPosition
import com.gumlaybeg.nimbletune.ui.screens.settings.PlayerBackgroundStyle
import com.gumlaybeg.nimbletune.ui.utils.fadingEdge
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference
import kotlinx.coroutines.isActive

const val animateScrollDuration = 300L
private const val LYRICS_ANCHOR_RATIO = 0.38f

sealed class LyricsListItemV2 {
    data class Line(val index: Int, val entry: LyricsEntry) : LyricsListItemV2()
    data class Indicator(val gapStartMs: Long, val gapEndMs: Long) : LyricsListItemV2()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    bottomEndReservedPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    var showLyrics by rememberPreference(ShowLyricsKey, false)

    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.LEFT)
    val lyricsFontSize by rememberPreference(LyricFontSizeKey, 24)

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val lyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val lyrics = remember(lyricsEntity) { lyricsEntity?.lyrics?.trim() }
    val multilineLrc = rememberPreference(MultilineLrcKey, defaultValue = true)
    val lyricTrim = rememberPreference(LyricTrimKey, defaultValue = false)

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.LIVE_MESH
    )
    val textColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onSurface
        else -> Color.White
    }

    val lines: List<LyricsEntry> = remember(lyrics, lyricTrim.value, multilineLrc.value) {
        if (lyrics.isNullOrBlank() || lyrics == LYRICS_NOT_FOUND) emptyList()
        else if (lyrics.startsWith("[")) {
            listOf(HEAD_LYRICS_ENTRY) + LyricsUtils.loadAndParseLyricsString(
                lyrics,
                LyricsUtils.LrcParserOptions(lyricTrim.value, multilineLrc.value, "Unable to parse lyrics")
            )
        } else {
            lyrics.lines().mapIndexed { index, line -> LyricsEntry(index * 100L, line) }
        }
    }

    val isSynced = remember(lyrics) {
        !lyrics.isNullOrEmpty() && lyrics.startsWith("[")
    }

    var isSelectionModeActive by remember(lyrics) { mutableStateOf(false) }
    val selectedIndices = remember(lyrics) { mutableStateListOf<Int>() }

    val mergedLyricsList = remember(lines) {
        val result = mutableListOf<LyricsListItemV2>()
        if (lines.isEmpty()) return@remember result

        lines.forEachIndexed { i, entry ->
            val clean = entry.content.trim()
            if (clean.isNotEmpty()) {
                result.add(LyricsListItemV2.Line(i, entry))
            }
            if (i < lines.size - 1) {
                val nextStart = lines[i + 1].timeStamp
                val currentEnd = entry.timeStamp + (clean.length * 160L).coerceIn(2000L, 4000L)
                if (nextStart - currentEnd > 4500L) {
                    result.add(LyricsListItemV2.Indicator(currentEnd, nextStart))
                }
            }
        }
        result
    }

    var currentLineIndex by remember(lyrics) {
        mutableIntStateOf(
            if (isSynced && lines.isNotEmpty()) {
                val initialPos = sliderPositionProvider() ?: playerConnection.player.currentPosition
                LyricsUtils.findCurrentLineIndex(lines, initialPos).coerceAtLeast(0)
            } else 0
        )
    }
    var smoothPosition by remember(lyrics) {
        mutableLongStateOf(sliderPositionProvider() ?: playerConnection.player.currentPosition)
    }
    var isAutoScrollEnabled by remember(lyrics) { mutableStateOf(true) }
    var hasInitiallyScrolled by remember(lyrics) { mutableStateOf(false) }

    val activeListIndex by remember(mergedLyricsList, currentLineIndex) {
        derivedStateOf {
            mergedLyricsList.indexOfFirst {
                it is LyricsListItemV2.Line && it.index == currentLineIndex
            }.coerceAtLeast(0)
        }
    }

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (isSynced && mergedLyricsList.isNotEmpty()) activeListIndex else 0
    )

    // Detect actual user drags on the LazyColumn without triggering on simple taps
    LaunchedEffect(lazyListState) {
        lazyListState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) {
                isAutoScrollEnabled = false
            }
        }
    }

    // Track playback position at frame rate
    LaunchedEffect(lines, isSynced) {
        if (!isSynced || lines.isEmpty()) return@LaunchedEffect
        while (isActive) {
            withFrameNanos { }
            val pos = sliderPositionProvider() ?: playerConnection.player.currentPosition
            smoothPosition = pos
            val nextIdx = LyricsUtils.findCurrentLineIndex(lines, pos).coerceAtLeast(0)
            if (nextIdx != currentLineIndex) {
                currentLineIndex = nextIdx
            }
        }
    }

    // Jump straight to the active lyric on first load without animating from index 0
    LaunchedEffect(mergedLyricsList, isSynced) {
        if (isSynced && mergedLyricsList.isNotEmpty() && !hasInitiallyScrolled) {
            val pos = sliderPositionProvider() ?: playerConnection.player.currentPosition
            val lineIdx = LyricsUtils.findCurrentLineIndex(lines, pos).coerceAtLeast(0)
            val listIdx = mergedLyricsList.indexOfFirst {
                it is LyricsListItemV2.Line && it.index == lineIdx
            }.coerceAtLeast(0)
            currentLineIndex = lineIdx
            smoothPosition = pos
            lazyListState.scrollToItem(listIdx, 0)
            hasInitiallyScrolled = true
        }
    }

    // Smoothly auto-scroll whenever activeListIndex advances or Sync is re-enabled
    LaunchedEffect(activeListIndex, isAutoScrollEnabled, isSynced, hasInitiallyScrolled) {
        if (isSynced && hasInitiallyScrolled && isAutoScrollEnabled && mergedLyricsList.isNotEmpty()) {
            lazyListState.animateScrollToItem(activeListIndex, 0)
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .fadingEdge(vertical = 48.dp)
            .clipToBounds()
    ) {
        val anchorOffset = maxHeight * LYRICS_ANCHOR_RATIO
        val bottomPadding = (maxHeight - anchorOffset).coerceAtLeast(96.dp)

        if (lyrics == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = textColor)
            }
        } else if (lyrics == LYRICS_NOT_FOUND || lines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.lyrics_not_found),
                    fontSize = 20.sp,
                    color = textColor.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    top = if (isSynced) anchorOffset else 24.dp,
                    bottom = if (isSynced) bottomPadding else 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = mergedLyricsList,
                    key = { listIdx, item ->
                        when (item) {
                            is LyricsListItemV2.Line -> "line_${item.index}"
                            is LyricsListItemV2.Indicator -> "ind_${listIdx}_${item.gapStartMs}"
                        }
                    }
                ) { _, item ->
                    when (item) {
                        is LyricsListItemV2.Line -> {
                            val lineEntry = item.entry
                            val isCurrent = isSynced && item.index == currentLineIndex
                            val isSelected = selectedIndices.contains(item.index)

                            val lineDurationMs = remember(lines, item.index) {
                                val nextLine = lines.drop(item.index + 1)
                                    .firstOrNull { it.timeStamp > lineEntry.timeStamp }
                                val rawGap = nextLine?.let { it.timeStamp - lineEntry.timeStamp }
                                when {
                                    rawGap != null && rawGap > 10_000L ->
                                        (lineEntry.content.length * 180L).coerceIn(2500L, 8000L)
                                    rawGap != null -> rawGap.coerceAtLeast(400L)
                                    else -> (lineEntry.content.length * 160L).coerceIn(2500L, 6000L)
                                }
                            }

                            LyricWordRow(
                                entry = lineEntry,
                                isCurrent = isCurrent,
                                isSynced = isSynced,
                                lineDurationMs = lineDurationMs,
                                fontSize = lyricsFontSize,
                                textColor = textColor,
                                alignment = lyricsTextPosition,
                                smoothPosition = smoothPosition,
                                onClick = {
                                    if (isSelectionModeActive) {
                                        if (isSelected) selectedIndices.remove(item.index)
                                        else selectedIndices.add(item.index)
                                        if (selectedIndices.isEmpty()) isSelectionModeActive = false
                                    } else if (isSynced) {
                                        playerConnection.player.seekTo(lineEntry.timeStamp)
                                        smoothPosition = lineEntry.timeStamp
                                        currentLineIndex = item.index
                                        isAutoScrollEnabled = true
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    }
                                },
                                onLongClick = {
                                    isSelectionModeActive = true
                                    if (!selectedIndices.contains(item.index)) {
                                        selectedIndices.add(item.index)
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                isSelected = isSelected,
                                isSelectionActive = isSelectionModeActive
                            )
                        }

                        is LyricsListItemV2.Indicator -> {
                            val isSingingGap = smoothPosition in item.gapStartMs..item.gapEndMs
                            val dotAlpha by animateFloatAsState(
                                targetValue = if (isSingingGap) 0.8f else 0.2f,
                                animationSpec = tween(300),
                                label = "indicatorAlpha"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .alpha(dotAlpha),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(textColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isAutoScrollEnabled && isSynced && !isSelectionModeActive,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            FilledTonalButton(
                onClick = { isAutoScrollEnabled = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "Sync", fontWeight = FontWeight.Bold)
            }
        }

        if (isSelectionModeActive) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        isSelectionModeActive = false
                        selectedIndices.clear()
                    }) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val selectedText = selectedIndices.sorted()
                                .mapNotNull { lines.getOrNull(it)?.content }
                                .joinToString("\n")
                            val clip = ClipData.newPlainText("Lyrics", selectedText)
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                .setPrimaryClip(clip)
                            Toast.makeText(context, "Lyrics copied", Toast.LENGTH_SHORT).show()
                            isSelectionModeActive = false
                            selectedIndices.clear()
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Copy (${selectedIndices.size})")
                    }
                }
            }
        }

        mediaMetadata?.let { meta ->
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp + bottomEndReservedPadding, bottom = 12.dp)
            ) {
                IconButton(
                    onClick = {
                        if (onClose != null) onClose() else showLyrics = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = textColor
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
                        tint = textColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LyricWordRow(
    entry: LyricsEntry,
    isCurrent: Boolean,
    isSynced: Boolean = true,
    lineDurationMs: Long = 3200L,
    fontSize: Int,
    textColor: Color,
    alignment: LyricsPosition,
    smoothPosition: Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    isSelectionActive: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val style = remember(fontSize, alignment) {
        TextStyle(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (fontSize * 1.35f).sp,
            textAlign = when (alignment) {
                LyricsPosition.LEFT -> TextAlign.Left
                LyricsPosition.CENTER -> TextAlign.Center
                LyricsPosition.RIGHT -> TextAlign.Right
            },
            letterSpacing = (-0.4).sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                LineHeightStyle.Alignment.Center,
                LineHeightStyle.Trim.Both
            )
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected && isSelectionActive) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                } else Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        val maxWidthPx = constraints.maxWidth
        val layoutResult = remember(entry.content, maxWidthPx, style) {
            textMeasurer.measure(
                text = entry.content,
                style = style,
                constraints = Constraints(maxWidth = maxWidthPx),
                softWrap = true
            )
        }

        val baseAlpha by animateFloatAsState(
            targetValue = when {
                !isSynced -> 0.85f
                isCurrent -> 1f
                else -> 0.35f
            },
            animationSpec = tween(250),
            label = "baseAlpha"
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { layoutResult.size.height.toDp() })
                .graphicsLayer { clip = false }
        ) {
            if (entry.content.isEmpty()) return@Canvas

            if (!isSynced || !isCurrent) {
                drawText(layoutResult, color = textColor.copy(alpha = baseAlpha))
            } else {
                val safeDuration = lineDurationMs.coerceAtLeast(1L).toFloat()
                val sungRatio = ((smoothPosition - entry.timeStamp).toFloat() / safeDuration)
                    .coerceIn(0f, 1f)

                drawText(layoutResult, color = textColor.copy(alpha = 0.35f))

                val totalLineWidth = (0 until layoutResult.lineCount).sumOf { idx ->
                    (layoutResult.getLineRight(idx) - layoutResult.getLineLeft(idx))
                        .coerceAtLeast(0f)
                        .toDouble()
                }.toFloat()

                var remainingSweep = totalLineWidth * sungRatio
                val glowShadow = if (sungRatio in 0.01f..0.98f) {
                    Shadow(
                        color = textColor.copy(alpha = 0.45f),
                        offset = Offset.Zero,
                        blurRadius = 16f
                    )
                } else null

                for (lineIdx in 0 until layoutResult.lineCount) {
                    if (remainingSweep <= 0f) break
                    val lineLeft = layoutResult.getLineLeft(lineIdx)
                    val lineRight = layoutResult.getLineRight(lineIdx)
                    val lineWidth = (lineRight - lineLeft).coerceAtLeast(0f)
                    if (lineWidth <= 0f) continue

                    val lineTop = layoutResult.getLineTop(lineIdx)
                    val lineBottom = layoutResult.getLineBottom(lineIdx)
                    val sweepOnLine = remainingSweep.coerceAtMost(lineWidth)
                    val isRtl = layoutResult.getParagraphDirection(lineIdx) == ResolvedTextDirection.Rtl

                    val clipLeft = if (isRtl) lineRight - sweepOnLine else lineLeft
                    val clipRight = if (isRtl) lineRight else lineLeft + sweepOnLine

                    clipRect(
                        left = clipLeft,
                        top = lineTop,
                        right = clipRight,
                        bottom = lineBottom
                    ) {
                        drawText(
                            textLayoutResult = layoutResult,
                            color = textColor,
                            shadow = glowShadow
                        )
                    }

                    remainingSweep -= sweepOnLine
                }
            }
        }
    }
}

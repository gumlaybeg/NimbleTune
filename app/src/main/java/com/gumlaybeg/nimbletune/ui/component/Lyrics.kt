package com.gumlaybeg.nimbletune.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.BreakIterator
import kotlin.math.roundToInt

const val animateScrollDuration = 300L
private const val LYRICS_ANCHOR_RATIO = 0.40f
private val LYRICS_ITEM_FALLBACK_HEIGHT_DP = 64.dp
private val LYRICS_ITEM_GAP_DP = 14.dp

sealed class LyricsListItemV2 {
    data class Line(val index: Int, val entry: LyricsEntry) : LyricsListItemV2()
    data class Indicator(val gapStartMs: Long, val gapEndMs: Long) : LyricsListItemV2()
}

private fun String.toGraphemeClusters(): List<String> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<String>()
    val it = BreakIterator.getCharacterInstance()
    it.setText(this)
    var start = it.first()
    var end = it.next()
    while (end != BreakIterator.DONE) {
        result.add(substring(start, end))
        start = end
        end = it.next()
    }
    return result
}

@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val density = LocalDensity.current
    var showLyrics by rememberPreference(ShowLyricsKey, false)

    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.LEFT)
    val lyricsFontSize by rememberPreference(LyricFontSizeKey, 24)

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val lyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val lyrics = remember(lyricsEntity) { lyricsEntity?.lyrics?.trim() }
    val multilineLrc = rememberPreference(MultilineLrcKey, defaultValue = true)
    val lyricTrim = rememberPreference(LyricTrimKey, defaultValue = false)

    val playerBackground by rememberEnumPreference(key = PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.LIVE_MESH)
    val textColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onSurface
        else -> Color.White
    }

    val lines: List<LyricsEntry> = remember(lyrics) {
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

    var isSelectionModeActive by remember { mutableStateOf(false) }
    val selectedIndices = remember { mutableStateListOf<Int>() }

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

    var currentLineIndex by remember { mutableIntStateOf(0) }
    var smoothPosition by remember { mutableLongStateOf(0L) }
    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    var userManualOffset by remember { mutableFloatStateOf(0f) }
    var flingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val velocityTracker = remember { VelocityTracker() }
    val decayAnimSpec = remember { exponentialDecay<Float>(frictionMultiplier = 1.8f) }
    val itemHeights = remember(lyrics, mergedLyricsList) { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(lines, isSynced) {
        if (!isSynced || lines.isEmpty()) return@LaunchedEffect
        while (isActive) {
            withFrameNanos { }
            val sliderPos = sliderPositionProvider()
            val pos = sliderPos ?: playerConnection.player.currentPosition
            smoothPosition = pos
            currentLineIndex = LyricsUtils.findCurrentLineIndex(lines, pos)
        }
    }

    val activeListIndex by remember(mergedLyricsList, currentLineIndex) {
        derivedStateOf {
            mergedLyricsList.indexOfFirst {
                it is LyricsListItemV2.Line && it.index == currentLineIndex
            }.coerceAtLeast(0)
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .fadingEdge(vertical = 48.dp)
            .clipToBounds()
    ) {
        val maxHeightPx = constraints.maxHeight.toFloat()
        val anchorY = maxHeightPx * LYRICS_ANCHOR_RATIO
        val lineHeightPx = with(density) { LYRICS_ITEM_FALLBACK_HEIGHT_DP.toPx() }
        val gapPx = with(density) { LYRICS_ITEM_GAP_DP.toPx() }

        val positions = remember(itemHeights.toMap(), activeListIndex, mergedLyricsList) {
            val map = mutableMapOf<Int, Float>()
            if (mergedLyricsList.isEmpty()) return@remember map
            map[activeListIndex] = 0f
            var curr = 0f
            for (i in activeListIndex - 1 downTo 0) {
                val h = itemHeights[i]?.toFloat() ?: lineHeightPx
                curr -= (h + gapPx)
                map[i] = curr
            }
            curr = 0f
            for (i in activeListIndex until mergedLyricsList.size - 1) {
                val h = itemHeights[i]?.toFloat() ?: lineHeightPx
                curr += (h + gapPx)
                map[i + 1] = curr
            }
            map
        }

        val minOffset = -(positions[mergedLyricsList.lastIndex] ?: (mergedLyricsList.size * lineHeightPx))
        val maxOffset = -(positions[0] ?: 0f)

        LaunchedEffect(isAutoScrollEnabled, activeListIndex) {
            if (isAutoScrollEnabled) {
                val anim = Animatable(userManualOffset)
                anim.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) {
                    userManualOffset = value
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    coroutineScope {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            flingJob?.cancel()
                            velocityTracker.resetTracking()
                            velocityTracker.addPosition(down.uptimeMillis, down.position)
                            isAutoScrollEnabled = false

                            verticalDrag(down.id) { change ->
                                val drag = change.positionChange().y
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                userManualOffset = (userManualOffset + drag).coerceIn(minOffset - 250f, maxOffset + 250f)
                                change.consume()
                            }

                            val vel = velocityTracker.calculateVelocity().y
                            if (kotlin.math.abs(vel) > 80f) {
                                flingJob = launch {
                                    val stateAnim = AnimationState(userManualOffset, vel)
                                    stateAnim.animateDecay(decayAnimSpec) {
                                        userManualOffset = value.coerceIn(minOffset, maxOffset)
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
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
                mergedLyricsList.forEachIndexed { listIdx, item ->
                    val itemPos = positions[listIdx] ?: ((listIdx - activeListIndex) * lineHeightPx)
                    val targetY = anchorY + itemPos

                    val animatedY by animateFloatAsState(
                        targetValue = targetY,
                        animationSpec = if (!isAutoScrollEnabled) snap() else tween(650, easing = FastOutSlowInEasing),
                        label = "lyricY"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
                                itemHeights[listIdx] = placeable.height
                                layout(placeable.width, 0) {
                                    placeable.place(0, 0)
                                }
                            }
                            .offset { IntOffset(0, (animatedY + userManualOffset).roundToInt()) }
                    ) {
                        when (item) {
                            is LyricsListItemV2.Line -> {
                                val lineEntry = item.entry
                                val isCurrent = item.index == currentLineIndex
                                val isSelected = selectedIndices.contains(item.index)

                                LyricWordRow(
                                    entry = lineEntry,
                                    isCurrent = isCurrent,
                                    fontSize = lyricsFontSize,
                                    textColor = textColor,
                                    alignment = lyricsTextPosition,
                                    smoothPosition = smoothPosition,
                                    onClick = {
                                        if (isSelectionModeActive) {
                                            if (isSelected) selectedIndices.remove(item.index)
                                            else selectedIndices.add(item.index)
                                            if (selectedIndices.isEmpty()) isSelectionModeActive = false
                                        } else {
                                            playerConnection.player.seekTo(lineEntry.timeStamp)
                                            isAutoScrollEnabled = true
                                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                        }
                                    },
                                    onLongClick = {
                                        isSelectionModeActive = true
                                        if (!selectedIndices.contains(item.index)) selectedIndices.add(item.index)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    isSelected = isSelected,
                                    isSelectionActive = isSelectionModeActive
                                )
                            }
                            is LyricsListItemV2.Indicator -> {
                                val isSingingGap = smoothPosition in item.gapStartMs..item.gapEndMs
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .alpha(if (isSingingGap) 0.8f else 0.2f),
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
        }

        AnimatedVisibility(
            visible = !isAutoScrollEnabled && isSynced,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        ) {
            FilledTonalButton(
                onClick = { isAutoScrollEnabled = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
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
                            val context1 = context
                            val selectedText = selectedIndices.sorted()
                                .mapNotNull { lines.getOrNull(it)?.content }
                                .joinToString("\n")
                            val clip = ClipData.newPlainText("Lyrics", selectedText)
                            (context1.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                            Toast.makeText(context1, "Lyrics copied", Toast.LENGTH_SHORT).show()
                            isSelectionModeActive = false
                            selectedIndices.clear()
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
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
                    .padding(end = 16.dp, bottom = 12.dp)
            ) {
                IconButton(onClick = { showLyrics = false }) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = null, tint = textColor)
                }
                IconButton(onClick = {
                    menuState.show {
                        LyricsMenu(
                            lyricsProvider = { lyricsEntity },
                            mediaMetadataProvider = { meta },
                            onDismiss = menuState::dismiss
                        )
                    }
                }) {
                    Icon(imageVector = Icons.Rounded.MoreHoriz, contentDescription = null, tint = textColor)
                }
            }
        }
    }
}

@Composable
fun LyricWordRow(
    entry: LyricsEntry,
    isCurrent: Boolean,
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
    val glowPaint = remember { android.graphics.Paint().apply { isAntiAlias = true } }

    val style = TextStyle(
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
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected && isSelectionActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent)
            .clickable(onClick = onClick)
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
            targetValue = if (isCurrent) 1f else 0.35f,
            animationSpec = tween(250),
            label = "baseAlpha"
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { layoutResult.size.height.toDp() })
                .graphicsLayer(clip = false, compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            if (entry.content.isEmpty()) return@Canvas

            if (!isCurrent) {
                drawText(layoutResult, color = textColor.copy(alpha = baseAlpha))
            } else {
                val sungRatio = ((smoothPosition - entry.timeStamp).toFloat() / 3200f).coerceIn(0f, 1f)
                drawText(layoutResult, color = textColor.copy(alpha = 0.35f))

                val sweepX = layoutResult.size.width * sungRatio
                clipRect(left = 0f, top = 0f, right = sweepX, bottom = layoutResult.size.height.toFloat()) {
                    drawText(layoutResult, color = textColor)
                }

                if (sungRatio in 0.01f..0.98f) {
                    drawIntoCanvas { canvas ->
                        glowPaint.maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
                        glowPaint.color = textColor.copy(alpha = 0.35f).toArgb()
                        glowPaint.textSize = style.fontSize.toPx()
                        glowPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.nativeCanvas.drawText(entry.content, 0f, layoutResult.firstBaseline, glowPaint)
                    }
                }
            }
        }
    }
}
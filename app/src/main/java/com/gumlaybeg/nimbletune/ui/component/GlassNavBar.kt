package com.gumlaybeg.nimbletune.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.models.MediaMetadata
import com.gumlaybeg.nimbletune.ui.component.floatingtabbar.FloatingTabBar
import com.gumlaybeg.nimbletune.ui.component.floatingtabbar.FloatingTabBarDefaults
import com.gumlaybeg.nimbletune.ui.component.floatingtabbar.FloatingTabBarScrollConnection
import com.gumlaybeg.nimbletune.ui.utils.fadingEdge
import com.gumlaybeg.nimbletune.ui.utils.imageCache
import com.gumlaybeg.nimbletune.ui.utils.resize
import kotlin.math.abs

@Immutable
data class GlassNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun GlassNavBar(
    tabs: List<GlassNavTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    scrollConnection: FloatingTabBarScrollConnection,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    isLoading: Boolean,
    canSkipNext: Boolean,
    canSkipPrevious: Boolean,
    swipeToSkipEnabled: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpand: () -> Unit,
    onPullDelta: (Float) -> Unit,
    onPullEnd: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = remember { RoundedCornerShape(percent = 50) }
    val contentColor = glassContentColor()
    val selectedColor = contentColor
    val unselectedColor = contentColor.copy(alpha = 0.65f)
    val haptic = LocalHapticFeedback.current

    val currentOnTabSelected by rememberUpdatedState(onTabSelected)
    val standaloneIndex = tabs.lastIndex

    val glassSurface: @Composable () -> Modifier = { Modifier.liquidGlass(shape = pillShape) }

    FloatingTabBar(
        selectedTabKey = selectedIndex,
        scrollConnection = scrollConnection,
        modifier = modifier
            .windowInsetsPadding(
                WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            )
            .padding(horizontal = NAV_BAR_GUTTER)
            .padding(bottom = NAV_BAR_LIFT)
            .fillMaxWidth(),
        tabBarContentModifier = glassSurface,
        inlineAccessory = mediaMetadata?.let { current ->
            { accessoryModifier, _ ->
                GlassNowPlaying(
                    mediaMetadata = current,
                    isInline = true,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    canSkipNext = canSkipNext,
                    canSkipPrevious = canSkipPrevious,
                    swipeToSkipEnabled = swipeToSkipEnabled,
                    contentColor = contentColor,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onExpand = onExpand,
                    onPullDelta = onPullDelta,
                    onPullEnd = onPullEnd,
                    modifier = accessoryModifier.then(glassSurface()),
                )
            }
        },
        expandedAccessory = mediaMetadata?.let { current ->
            { accessoryModifier, _ ->
                GlassNowPlaying(
                    mediaMetadata = current,
                    isInline = false,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    canSkipNext = canSkipNext,
                    canSkipPrevious = canSkipPrevious,
                    swipeToSkipEnabled = swipeToSkipEnabled,
                    contentColor = contentColor,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onExpand = onExpand,
                    onPullDelta = onPullDelta,
                    onPullEnd = onPullEnd,
                    modifier = accessoryModifier
                        .fillMaxWidth()
                        .then(glassSurface()),
                )
            }
        },
        colors = FloatingTabBarDefaults.colors(
            backgroundColor = Color.Transparent,
            accessoryBackgroundColor = Color.Transparent,
            indicatorColor = glassIndicatorColor(),
        ),
        elevations = FloatingTabBarDefaults.elevations(
            inlineElevation = 0.dp,
            expandedElevation = 0.dp,
        ),
        sizes = FloatingTabBarDefaults.sizes(
            tabBarContentPadding = PaddingValues(PILL_INSET),
            tabExpandedContentPadding = PaddingValues(vertical = TAB_VERTICAL_PADDING),
            inlineComponentSpacing = INLINE_SPACING,
            inlineBarPadding = PaddingValues(
                start = INLINE_GUTTER - NAV_BAR_GUTTER,
                end = INLINE_GUTTER - NAV_BAR_GUTTER,
                bottom = INLINE_LIFT,
            ),
        ),
        contentKey = remember(selectedIndex, tabs, contentColor) {
            listOf(selectedIndex, tabs, contentColor)
        },
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            val tint = if (isSelected) selectedColor else unselectedColor
            val onClick = {
                if (!isSelected) haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                currentOnTabSelected(index)
            }
            if (index == standaloneIndex) {
                standaloneTab(
                    key = index,
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(TAB_ICON_SIZE),
                        )
                    },
                    onClick = onClick,
                )
            } else {
                tab(
                    key = index,
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(TAB_ICON_SIZE),
                        )
                    },
                    title = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            ),
                            color = tint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = TAB_ICON_LABEL_GAP),
                        )
                    },
                    onClick = onClick,
                )
            }
        }
    }
}

@Composable
private fun GlassNowPlaying(
    mediaMetadata: MediaMetadata,
    isInline: Boolean,
    isPlaying: Boolean,
    isLoading: Boolean,
    canSkipNext: Boolean,
    canSkipPrevious: Boolean,
    swipeToSkipEnabled: Boolean,
    contentColor: Color,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpand: () -> Unit,
    onPullDelta: (Float) -> Unit,
    onPullEnd: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val pressSource = remember { MutableInteractionSource() }
    val isPressed by pressSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "accessoryPressScale",
    )

    val titleStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = ACCESSORY_TITLE_SIZE,
        fontWeight = ACCESSORY_TITLE_WEIGHT,
        letterSpacing = ACCESSORY_TITLE_TRACKING,
    )
    val artistStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = ACCESSORY_ARTIST_SIZE,
        letterSpacing = ACCESSORY_ARTIST_TRACKING,
    )
    val glyphSlot = if (isInline) 32.dp else 40.dp
    val glyphSize = if (isInline) 24.dp else EXPANDED_GLYPH_SIZE

    val currentOnNext by rememberUpdatedState(onNext)
    val currentOnPrevious by rememberUpdatedState(onPrevious)
    val currentOnPullDelta by rememberUpdatedState(onPullDelta)
    val currentOnPullEnd by rememberUpdatedState(onPullEnd)
    val currentCanSkipNext by rememberUpdatedState(canSkipNext)
    val currentCanSkipPrevious by rememberUpdatedState(canSkipPrevious)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .pointerInput(swipeToSkipEnabled, layoutDirection) {
                val velocityTracker = VelocityTracker()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    velocityTracker.resetTracking()
                    velocityTracker.addPointerInputChange(down)

                    var dragMode = 0
                    var totalDx = 0f
                    var totalDy = 0f
                    val touchSlop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            when (dragMode) {
                                1 -> currentOnPullEnd(-velocityTracker.calculateVelocity().y)
                                2 -> if (swipeToSkipEnabled && abs(totalDx) > 90f) {
                                    val dirDx = if (layoutDirection == LayoutDirection.Rtl) -totalDx else totalDx
                                    if (dirDx < 0 && currentCanSkipNext) {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        currentOnNext()
                                    } else if (dirDx > 0 && currentCanSkipPrevious) {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        currentOnPrevious()
                                    }
                                }
                            }
                            break
                        }

                        val delta = change.positionChange()
                        totalDx += delta.x
                        totalDy += delta.y

                        if (dragMode == 0) {
                            if (abs(totalDy) > touchSlop && abs(totalDy) >= abs(totalDx)) {
                                dragMode = 1
                            } else if (swipeToSkipEnabled && abs(totalDx) > touchSlop) {
                                dragMode = 2
                            }
                        }

                        if (dragMode == 1) {
                            velocityTracker.addPointerInputChange(change)
                            currentOnPullDelta(delta.y)
                            change.consume()
                        } else if (dragMode == 2) {
                            change.consume()
                        }
                    }
                }
            }
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(modifier)
            .clickable(
                interactionSource = pressSource,
                indication = null,
                onClick = onExpand,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = if (isInline) 8.dp else 12.dp,
                    top = 4.dp,
                    bottom = 4.dp,
                ),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(ACCESSORY_ART_SIZE)
                    .clip(RoundedCornerShape(ACCESSORY_ART_CORNER))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                when {
                    mediaMetadata.isLocal -> {
                        AsyncImageLocal(
                            image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, true) },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    mediaMetadata.thumbnailUrl != null -> {
                        AsyncImage(
                            model = mediaMetadata.thumbnailUrl.resize(160, 160),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.width(if (isInline) 8.dp else 9.dp))

            Column(Modifier.weight(1f)) {
                val secondary = contentColor.copy(alpha = 0.7f)
                FadingLine(
                    text = mediaMetadata.title,
                    style = titleStyle,
                    color = contentColor,
                )
                FadingLine(
                    text = mediaMetadata.artists.joinToString { it.name },
                    style = artistStyle,
                    color = secondary,
                )
            }

            if (isLoading) {
                Box(Modifier.size(glyphSlot), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(if (isInline) 18.dp else 20.dp),
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onPlayPause()
                    },
                    modifier = Modifier.size(glyphSlot),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else stringResource(R.string.play),
                        tint = contentColor,
                        modifier = Modifier.size(glyphSize),
                    )
                }
            }

            if (!isInline) {
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onNext()
                    },
                    enabled = canSkipNext,
                    modifier = Modifier.size(glyphSlot),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = if (canSkipNext) 1f else 0.3f),
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FadingLine(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = modifier
            .fillMaxWidth()
            .fadingEdge(right = 16.dp),
    )
}

private val NAV_BAR_GUTTER = 12.dp
private val NAV_BAR_LIFT = 8.dp
private val PILL_INSET = 4.dp
private val TAB_VERTICAL_PADDING = 6.dp
private val TAB_ICON_SIZE = 28.dp
private val TAB_ICON_LABEL_GAP = 2.dp

private val INLINE_GUTTER = 28.dp
private val INLINE_SPACING = 14.dp
private val INLINE_LIFT = 7.dp

private val ACCESSORY_TITLE_SIZE = 13.sp
private val ACCESSORY_TITLE_WEIGHT = FontWeight.W700
private val ACCESSORY_TITLE_TRACKING = 0.15.sp
private val ACCESSORY_ARTIST_SIZE = 12.sp
private val ACCESSORY_ARTIST_TRACKING = 0.35.sp

private val ACCESSORY_ART_SIZE = 30.4.dp
private val ACCESSORY_ART_CORNER = 5.7.dp
private val EXPANDED_GLYPH_SIZE = 29.dp
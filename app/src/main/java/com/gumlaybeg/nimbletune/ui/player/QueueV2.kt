package com.gumlaybeg.nimbletune.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.PlayerBackgroundStyleKey
import com.gumlaybeg.nimbletune.constants.PlaylistEditLockKey
import com.gumlaybeg.nimbletune.extensions.metadata
import com.gumlaybeg.nimbletune.extensions.move
import com.gumlaybeg.nimbletune.extensions.togglePlayPause
import com.gumlaybeg.nimbletune.extensions.toggleRepeatMode
import com.gumlaybeg.nimbletune.playback.PlayerConnection.Companion.queueBoard
import com.gumlaybeg.nimbletune.playback.isShuffleEnabled
import com.gumlaybeg.nimbletune.ui.component.ActionPromptDialog
import com.gumlaybeg.nimbletune.ui.component.BottomSheetState
import com.gumlaybeg.nimbletune.ui.component.LocalMenuState
import com.gumlaybeg.nimbletune.ui.component.MediaMetadataListItem
import com.gumlaybeg.nimbletune.ui.menu.PlayerMenu
import com.gumlaybeg.nimbletune.ui.screens.settings.PlayerBackgroundStyle
import com.gumlaybeg.nimbletune.utils.makeTimeString
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QueueV2(
    navController: NavController,
    playerBottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current

    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val shuffleModeEnabled by isShuffleEnabled.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()

    var locked by rememberPreference(PlaylistEditLockKey, defaultValue = true)

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.LIVE_MESH
    )
    val adaptivePrimary = if (playerBackground == PlayerBackgroundStyle.DEFAULT) MaterialTheme.colorScheme.onSurface else Color.White
    val adaptiveSecondary = if (playerBackground == PlayerBackgroundStyle.DEFAULT) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.7f)

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var sleepTimerValue by remember { mutableFloatStateOf(30f) }
    val sleepTimerEnabled = remember(
        playerConnection.service.sleepTimer.triggerTime,
        playerConnection.service.sleepTimer.pauseWhenSongEnd
    ) {
        playerConnection.service.sleepTimer.isActive
    }
    var sleepTimerTimeLeft by remember { mutableLongStateOf(0L) }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft = if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                    playerConnection.player.duration - playerConnection.player.currentPosition
                } else {
                    playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                }
                delay(1000L)
            }
        }
    }

    val lazyListState = rememberLazyListState()
    val mutableQueueWindows = remember { mutableStateListOf<Timeline.Window>() }
    var dragInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(queueWindows) {
        mutableQueueWindows.apply {
            clear()
            addAll(queueWindows)
        }
    }

    LaunchedEffect(currentWindowIndex) {
        if (currentWindowIndex in mutableQueueWindows.indices) {
            lazyListState.animateScrollToItem(currentWindowIndex)
        }
    }

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState
    ) { from, to ->
        dragInfo = (dragInfo?.first ?: from.index) to to.index
        mutableQueueWindows.move(from.index, to.index)
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging) {
            dragInfo?.let { (from, to) ->
                queueBoard.moveSong(from, to, playerConnection.service)
                playerConnection.player.moveMediaItem(from, to)
                dragInfo = null
            }
        }
    }

    CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides adaptivePrimary) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pillShape = RoundedCornerShape(percent = 50)
                val activeBg = Color.White.copy(alpha = 0.85f)
                val activeContent = Color.Black
                val inactiveBg = adaptivePrimary.copy(alpha = 0.15f)
                val inactiveContent = adaptivePrimary

                val shuffleBg by animateColorAsState(if (shuffleModeEnabled) activeBg else inactiveBg, label = "shuffleBg")
                val shuffleContent by animateColorAsState(if (shuffleModeEnabled) activeContent else inactiveContent, label = "shuffleContent")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(shuffleBg, pillShape)
                        .clip(pillShape)
                        .clickable { playerConnection.triggerShuffle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = shuffleContent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                val isRepeatActive = repeatMode != Player.REPEAT_MODE_OFF
                val repeatBg by animateColorAsState(if (isRepeatActive) activeBg else inactiveBg, label = "repeatBg")
                val repeatContent by animateColorAsState(if (isRepeatActive) activeContent else inactiveContent, label = "repeatContent")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(repeatBg, pillShape)
                        .clip(pillShape)
                        .clickable { playerConnection.player.toggleRepeatMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = repeatContent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                val timerBg by animateColorAsState(if (sleepTimerEnabled) activeBg else inactiveBg, label = "timerBg")
                val timerContent by animateColorAsState(if (sleepTimerEnabled) activeContent else inactiveContent, label = "timerContent")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(timerBg, pillShape)
                        .clip(pillShape)
                        .clickable {
                            if (sleepTimerEnabled) playerConnection.service.sleepTimer.clear()
                            else showSleepTimerDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = "Sleep Timer",
                            tint = timerContent,
                            modifier = Modifier.size(20.dp)
                        )
                        if (sleepTimerEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = makeTimeString(sleepTimerTimeLeft.coerceAtLeast(0L)),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = timerContent
                            )
                        }
                    }
                }

                val isUnlocked = !locked
                val lockBg by animateColorAsState(if (isUnlocked) activeBg else inactiveBg, label = "lockBg")
                val lockContent by animateColorAsState(if (isUnlocked) activeContent else inactiveContent, label = "lockContent")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(lockBg, pillShape)
                        .clip(pillShape)
                        .clickable { locked = !locked },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = "Lock",
                        tint = lockContent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = stringResource(R.string.queue),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = adaptivePrimary
                )
                Text(
                    text = "${mutableQueueWindows.size} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = adaptiveSecondary
                )
            }

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = mutableQueueWindows,
                    key = { _, item -> item.uid.hashCode() }
                ) { index, window ->
                    ReorderableItem(
                        state = reorderableState,
                        key = window.uid.hashCode()
                    ) {
                        val isActive = index == currentWindowIndex
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

                        val content: @Composable () -> Unit = {
                            val metadata = window.mediaItem.metadata ?: return@ReorderableItem
                            MediaMetadataListItem(
                                mediaMetadata = metadata,
                                isSelected = false,
                                isActive = isActive,
                                isPlaying = isPlaying && isActive,
                                trailingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                                contentDescription = "Options"
                                            )
                                        }

                                        if (!locked) {
                                            IconButton(
                                                onClick = {},
                                                modifier = Modifier.draggableHandle()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DragHandle,
                                                    contentDescription = "Reorder"
                                                )
                                            }
                                        }
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

                        if (locked) {
                            content()
                        } else {
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                                            else -> MaterialTheme.colorScheme.error
                                        }, label = "dismissColor"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 4.dp, horizontal = 16.dp)
                                            .background(color, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        val iconAlpha by animateFloatAsState(
                                            targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1f else 0f,
                                            label = "iconAlpha"
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.padding(end = 16.dp).alpha(iconAlpha),
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                },
                                content = { content() },
                                enableDismissFromStartToEnd = false
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSleepTimerDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.sleep_timer),
            onDismiss = { showSleepTimerDialog = false },
            onConfirm = {
                showSleepTimerDialog = false
                playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
            },
            onCancel = { showSleepTimerDialog = false },
            onReset = { sleepTimerValue = 30f },
            content = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.minute,
                            sleepTimerValue.roundToInt(),
                            sleepTimerValue.roundToInt()
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Slider(
                        value = sleepTimerValue,
                        onValueChange = { sleepTimerValue = it },
                        valueRange = 5f..120f,
                        steps = (120 - 5) / 5 - 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showSleepTimerDialog = false
                            playerConnection.service.sleepTimer.start(-1)
                        }
                    ) {
                        Text(stringResource(R.string.end_of_song))
                    }
                }
            }
        )
    }
}
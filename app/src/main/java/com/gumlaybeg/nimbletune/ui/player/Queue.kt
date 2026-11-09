@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)

package com.gumlaybeg.nimbletune.ui.player

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.db.MusicDatabase
import com.gumlaybeg.nimbletune.db.entities.Playlist
import com.gumlaybeg.nimbletune.db.entities.PlaylistEntity
import com.gumlaybeg.nimbletune.extensions.metadata
import com.gumlaybeg.nimbletune.extensions.toggleRepeatMode
import com.gumlaybeg.nimbletune.models.MediaMetadata
import com.gumlaybeg.nimbletune.playback.PlayerConnection
import com.gumlaybeg.nimbletune.playback.PlayerConnection.Companion.queueBoard
import com.gumlaybeg.nimbletune.playback.isShuffleEnabled
import com.gumlaybeg.nimbletune.ui.component.ActionPromptDialog
import com.gumlaybeg.nimbletune.ui.component.BottomSheet
import com.gumlaybeg.nimbletune.ui.component.BottomSheetState
import com.gumlaybeg.nimbletune.ui.component.LocalMenuState
import com.gumlaybeg.nimbletune.ui.component.TextFieldDialog
import com.gumlaybeg.nimbletune.ui.component.bottomSheetDraggable
import com.gumlaybeg.nimbletune.ui.menu.AddToPlaylistDialog
import com.gumlaybeg.nimbletune.ui.menu.PlayerMenu
import com.gumlaybeg.nimbletune.utils.joinByBullet
import com.gumlaybeg.nimbletune.utils.makeTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import javax.inject.Inject
import kotlin.math.roundToInt

// ==========================================
// 1. STATE & ACTION DEFINITIONS
// ==========================================

@Immutable
sealed interface QueueScreenState {
    data object Loading : QueueScreenState
    data class Success(val model: QueueUiModel) : QueueScreenState
    data object Empty : QueueScreenState
    data class Error(@param:StringRes val message: Int, val previous: QueueUiModel? = null) : QueueScreenState
}

@Immutable
data class QueueEntryUi(
    val id: String,
    val title: String,
    val artists: String,
    val artworkUrl: String?,
    val duration: String,
    val current: Boolean,
    val recommended: Boolean = false,
    val selected: Boolean = false,
)

@Immutable
data class QueueChipUi(val id: String, val title: String, val selected: Boolean)

@Immutable
data class QueueEntries(val values: List<QueueEntryUi>)

@Immutable
data class QueueChips(val values: List<QueueChipUi>)

@Immutable
data class QueueUiModel(
    val title: String?,
    val entries: QueueEntries,
    val duration: String,
    val currentId: String?,
    val playing: Boolean,
    val liked: Boolean,
    val shuffle: Boolean,
    val repeatMode: Int,
    val locked: Boolean,
    val guest: Boolean = false,
    val canControlPlayback: Boolean = true,
    val episodeQueue: Boolean = false,
    val autoplay: Boolean = false,
    val autoplayAvailable: Boolean = false,
    val loadingRecommendations: Boolean = false,
    val chips: QueueChips = QueueChips(emptyList()),
    val recommendationsFailed: Boolean = false,
    val selectionCount: Int = 0,
    val overflowOpen: Boolean = false,
    val playlistDialog: Boolean = false,
    val createPlaylistDialog: Boolean = false,
    val savingPlaylist: Boolean = false,
    val playlistName: String = "",
)

@Immutable
data class QueueChromeUi(
    val showSleepTimer: Boolean = false,
    val sleepTimerValue: Float = 30f,
    val sleepTimerEnabled: Boolean = false,
    val sleepTimerTimeLeft: Long = 0L,
)

sealed interface QueueAction {
    data class Play(val id: String) : QueueAction
    data class Select(val id: String) : QueueAction
    data class Remove(val id: String) : QueueAction
    data class Menu(val id: String) : QueueAction
    data class PlayNext(val id: String) : QueueAction
    data class SelectChip(val id: String) : QueueAction
    data class Drag(val from: String, val to: String) : QueueAction
    data class CreatePlaylist(val name: String) : QueueAction
    data class PlaylistName(val value: String) : QueueAction
    data object FinishDrag : QueueAction
    data object ToggleShuffle : QueueAction
    data object ToggleRepeat : QueueAction
    data object ToggleAutoplay : QueueAction
    data object ToggleLock : QueueAction
    data object ToggleLike : QueueAction
    data object ToggleOverflow : QueueAction
    data object Clear : QueueAction
    data object ClearSelection : QueueAction
    data object SelectAll : QueueAction
    data object RemoveSelection : QueueAction
    data object AddToPlaylist : QueueAction
    data object NewPlaylist : QueueAction
    data object DismissPlaylist : QueueAction
    data object PlaylistAdded : QueueAction
    data object Retry : QueueAction
}

sealed interface QueueEvent {
    data class Menu(val id: String, val metadata: MediaMetadata, val canPlayNext: Boolean) : QueueEvent
    data class Message(@param:StringRes val resource: Int, val arguments: List<Any> = emptyList()) : QueueEvent
    data class Removed(val count: Int, val title: String?, val token: Long) : QueueEvent
}

// ==========================================
// 2. VIEWMODEL
// ==========================================

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val database: MusicDatabase
) : ViewModel() {

    private val _state = MutableStateFlow<QueueScreenState>(QueueScreenState.Loading)
    val state = _state.asStateFlow()

    private val _chrome = MutableStateFlow(QueueChromeUi())
    val chrome = _chrome.asStateFlow()

    private val _events = MutableSharedFlow<QueueEvent>()
    val events = _events.asSharedFlow()

    private var playerConnection: PlayerConnection? = null
    private val selectedIds = mutableSetOf<String>()
    private var isLocked: Boolean = false
    private var isOverflowOpen: Boolean = false
    private var isPlaylistDialogOpen: Boolean = false
    private var isCreatePlaylistDialogOpen: Boolean = false
    private var newPlaylistName: String = ""

    fun bind(connection: PlayerConnection) {
        playerConnection = connection
        viewModelScope.launch {
            combine(
                combine(connection.queueWindows, connection.currentWindowIndex, connection.isPlaying) { windows, windowIndex, isPlaying ->
                    Triple(windows, windowIndex, isPlaying)
                },
                combine(connection.repeatMode, isShuffleEnabled, connection.queueTitle) { repeatMode, shuffle, title ->
                    Triple(repeatMode, shuffle, title)
                }
            ) { (windows, windowIndex, isPlaying), (repeatMode, shuffle, title) ->
                if (windows.isEmpty()) {
                    _state.value = QueueScreenState.Empty
                    return@combine
                }

                val currentMetadata = windows.getOrNull(windowIndex)?.mediaItem?.metadata
                val totalDurationSec = windows.sumOf { it.mediaItem.metadata?.duration ?: 0 }

                val entries = windows.mapIndexed { idx, window ->
                    val meta = window.mediaItem.metadata
                    val id = meta?.id ?: window.uid.toString()
                    QueueEntryUi(
                        id = id,
                        title = meta?.title ?: "Unknown",
                        artists = meta?.artists?.joinToString { it.name } ?: "",
                        artworkUrl = meta?.thumbnailUrl,
                        duration = makeTimeString((meta?.duration ?: 0) * 1000L),
                        current = idx == windowIndex,
                        selected = selectedIds.contains(id),
                    )
                }

                val currentLiked = currentMetadata?.liked ?: false

                val model = QueueUiModel(
                    title = title ?: "Queue",
                    entries = QueueEntries(entries),
                    duration = makeTimeString(totalDurationSec * 1000L),
                    currentId = currentMetadata?.id,
                    playing = isPlaying,
                    liked = currentLiked,
                    shuffle = shuffle,
                    repeatMode = repeatMode,
                    locked = isLocked,
                    guest = false,
                    canControlPlayback = true,
                    selectionCount = selectedIds.size,
                    overflowOpen = isOverflowOpen,
                    playlistDialog = isPlaylistDialogOpen,
                    createPlaylistDialog = isCreatePlaylistDialogOpen,
                    playlistName = newPlaylistName,
                )
                _state.value = QueueScreenState.Success(model)
            }.collectLatest {}
        }
    }

    fun unbind(connection: PlayerConnection) {
        if (playerConnection == connection) {
            playerConnection = null
        }
    }

    fun onAction(action: QueueAction) {
        val conn = playerConnection ?: return
        when (action) {
            is QueueAction.Play -> {
                val index = conn.queueWindows.value.indexOfFirst {
                    it.mediaItem.metadata?.id == action.id || it.uid.toString() == action.id
                }
                if (index >= 0) {
                    conn.player.seekToDefaultPosition(index)
                    conn.player.prepare()
                    conn.player.play()
                }
            }

            is QueueAction.Select -> {
                if (selectedIds.contains(action.id)) {
                    selectedIds.remove(action.id)
                } else {
                    selectedIds.add(action.id)
                }
                refreshSuccessState()
            }

            is QueueAction.SelectAll -> {
                val allIds = conn.queueWindows.value.map { it.mediaItem.metadata?.id ?: it.uid.toString() }
                if (selectedIds.size == allIds.size) {
                    selectedIds.clear()
                } else {
                    selectedIds.addAll(allIds)
                }
                refreshSuccessState()
            }

            QueueAction.ClearSelection -> {
                selectedIds.clear()
                refreshSuccessState()
            }

            is QueueAction.Remove -> {
                val index = conn.queueWindows.value.indexOfFirst {
                    it.mediaItem.metadata?.id == action.id || it.uid.toString() == action.id
                }
                if (index >= 0) {
                    val removedTitle = conn.queueWindows.value.getOrNull(index)?.mediaItem?.metadata?.title
                    queueBoard.removeCurrentQueueSong(index, conn.service)
                    conn.player.removeMediaItem(index)
                    viewModelScope.launch {
                        _events.emit(QueueEvent.Removed(1, removedTitle, System.currentTimeMillis()))
                    }
                }
            }

            QueueAction.RemoveSelection -> {
                val count = selectedIds.size
                val currentWindows = conn.queueWindows.value
                val indicesToRemove = currentWindows.mapIndexedNotNull { index, window ->
                    val id = window.mediaItem.metadata?.id ?: window.uid.toString()
                    if (selectedIds.contains(id)) index else null
                }.sortedDescending()

                indicesToRemove.forEach { idx ->
                    queueBoard.removeCurrentQueueSong(idx, conn.service)
                    conn.player.removeMediaItem(idx)
                }
                selectedIds.clear()
                viewModelScope.launch {
                    _events.emit(QueueEvent.Removed(count, null, System.currentTimeMillis()))
                }
                refreshSuccessState()
            }

            is QueueAction.Drag -> {
                val fromIdx = conn.queueWindows.value.indexOfFirst {
                    it.mediaItem.metadata?.id == action.from || it.uid.toString() == action.from
                }
                val toIdx = conn.queueWindows.value.indexOfFirst {
                    it.mediaItem.metadata?.id == action.to || it.uid.toString() == action.to
                }
                if (fromIdx >= 0 && toIdx >= 0 && fromIdx != toIdx) {
                    queueBoard.moveSong(fromIdx, toIdx, conn.service)
                    conn.player.moveMediaItem(fromIdx, toIdx)
                }
            }

            QueueAction.FinishDrag -> {}

            QueueAction.ToggleShuffle -> conn.triggerShuffle()

            QueueAction.ToggleRepeat -> conn.player.toggleRepeatMode()

            QueueAction.ToggleLike -> conn.toggleLike()

            QueueAction.ToggleLock -> {
                isLocked = !isLocked
                refreshSuccessState()
            }

            QueueAction.ToggleOverflow -> {
                isOverflowOpen = !isOverflowOpen
                refreshSuccessState()
            }

            QueueAction.Clear -> {
                isOverflowOpen = false
                conn.player.clearMediaItems()
                refreshSuccessState()
            }

            is QueueAction.Menu -> {
                val window = conn.queueWindows.value.firstOrNull {
                    it.mediaItem.metadata?.id == action.id || it.uid.toString() == action.id
                }
                val meta = window?.mediaItem?.metadata
                if (meta != null) {
                    viewModelScope.launch {
                        _events.emit(QueueEvent.Menu(action.id, meta, canPlayNext = true))
                    }
                }
            }

            is QueueAction.PlayNext -> {
                val fromIdx = conn.queueWindows.value.indexOfFirst {
                    it.mediaItem.metadata?.id == action.id || it.uid.toString() == action.id
                }
                val currentIdx = conn.currentWindowIndex.value
                if (fromIdx >= 0 && currentIdx >= 0 && fromIdx != currentIdx + 1) {
                    val target = (currentIdx + 1).coerceAtMost(conn.queueWindows.value.size - 1)
                    queueBoard.moveSong(fromIdx, target, conn.service)
                    conn.player.moveMediaItem(fromIdx, target)
                }
            }

            QueueAction.AddToPlaylist -> {
                isPlaylistDialogOpen = true
                refreshSuccessState()
            }

            QueueAction.NewPlaylist -> {
                isCreatePlaylistDialogOpen = true
                refreshSuccessState()
            }

            QueueAction.DismissPlaylist -> {
                isPlaylistDialogOpen = false
                isCreatePlaylistDialogOpen = false
                refreshSuccessState()
            }

            is QueueAction.PlaylistName -> {
                newPlaylistName = action.value
                refreshSuccessState()
            }

            is QueueAction.CreatePlaylist -> {
                viewModelScope.launch(Dispatchers.IO) {
                    database.insert(
                        PlaylistEntity(
                            name = action.name,
                            isLocal = true,
                        )
                    )
                    isCreatePlaylistDialogOpen = false
                    refreshSuccessState()
                }
            }

            else -> {}
        }
    }

    private fun refreshSuccessState() {
        val curr = _state.value
        if (curr is QueueScreenState.Success) {
            val updatedEntries = curr.model.entries.values.map {
                it.copy(selected = selectedIds.contains(it.id))
            }
            _state.value = QueueScreenState.Success(
                curr.model.copy(
                    entries = QueueEntries(updatedEntries),
                    locked = isLocked,
                    selectionCount = selectedIds.size,
                    overflowOpen = isOverflowOpen,
                    playlistDialog = isPlaylistDialogOpen,
                    createPlaylistDialog = isCreatePlaylistDialogOpen,
                    playlistName = newPlaylistName,
                )
            )
        }
    }

    fun finishRemoval(token: Long, undo: Boolean) {}

    suspend fun prepareSelectedSongs(targetPlaylist: Playlist): List<String> {
        val conn = playerConnection ?: return emptyList()
        val songsToAdd = conn.queueWindows.value.filter {
            selectedIds.contains(it.mediaItem.metadata?.id) || selectedIds.contains(it.uid.toString())
        }.mapNotNull { it.mediaItem.metadata }

        database.transaction {
            songsToAdd.forEach { insert(it) }
        }
        return songsToAdd.map { it.id }
    }

    fun playlistAdded() {
        selectedIds.clear()
        isPlaylistDialogOpen = false
        refreshSuccessState()
    }

    fun sleepTimerClick() {
        _chrome.update { it.copy(showSleepTimer = true) }
    }

    fun dismissSleepTimer() {
        _chrome.update { it.copy(showSleepTimer = false) }
    }

    fun startSleepTimer(minutes: Int) {
        playerConnection?.service?.sleepTimer?.start(minutes)
        _chrome.update { it.copy(showSleepTimer = false, sleepTimerEnabled = true) }
    }

    fun sleepTimerValue(value: Float) {
        _chrome.update { it.copy(sleepTimerValue = value) }
    }
}

// ==========================================
// 3. COMPONENTS
// ==========================================

@Composable
fun QueueHeader(
    model: QueueUiModel,
    onAction: (QueueAction) -> Unit,
    modifier: Modifier = Modifier,
    showPlaybackControls: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .alpha(0.4f),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.queue),
                    style = if (showPlaybackControls) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                model.title?.takeIf { it.isNotBlank() }?.let { title ->
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = if (showPlaybackControls) 2 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = { onAction(QueueAction.ToggleLock) }, enabled = !model.guest) {
                Icon(
                    imageVector = if (model.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    contentDescription = null
                )
            }
            Box {
                IconButton(onClick = { onAction(QueueAction.ToggleOverflow) }) {
                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded = model.overflowOpen,
                    onDismissRequest = { onAction(QueueAction.ToggleOverflow) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Select all") },
                        onClick = {
                            onAction(QueueAction.ToggleOverflow)
                            onAction(QueueAction.SelectAll)
                        },
                        leadingIcon = { Icon(imageVector = Icons.Rounded.SelectAll, null) },
                    )
                    model.currentId?.let { id ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.details)) },
                            onClick = {
                                onAction(QueueAction.ToggleOverflow)
                                onAction(QueueAction.Menu(id))
                            },
                            leadingIcon = { Icon(imageVector = Icons.Rounded.Info, null) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = colors.error) },
                        enabled = !model.guest,
                        onClick = { onAction(QueueAction.Clear) },
                        leadingIcon = { Icon(imageVector = Icons.Rounded.Delete, null, tint = colors.error) },
                    )
                }
            }
        }
        Text(
            text = pluralStringResource(R.plurals.n_song, model.entries.values.size, model.entries.values.size) + " · " + model.duration,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        if (showPlaybackControls) QueuePlaybackControls(model, onAction)
    }
}

@Composable
fun QueuePlaybackControls(
    model: QueueUiModel,
    onAction: (QueueAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = model.canControlPlayback) { onAction(QueueAction.ToggleShuffle) },
            color = if (model.shuffle) colors.primaryContainer else colors.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = null,
                    tint = if (model.shuffle) colors.onPrimaryContainer else colors.onSurface
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = model.canControlPlayback) { onAction(QueueAction.ToggleRepeat) },
            color = if (model.repeatMode != Player.REPEAT_MODE_OFF) colors.primaryContainer else colors.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(if (model.repeatMode == Player.REPEAT_MODE_ONE) R.drawable.repeat_one_on else R.drawable.repeat),
                    contentDescription = null,
                    tint = if (model.repeatMode != Player.REPEAT_MODE_OFF) colors.onPrimaryContainer else colors.onSurface
                )
            }
        }
    }
}

@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    onEndOfSong: () -> Unit,
    initialValue: Float = 30f,
) {
    var sleepTimerValue by remember { mutableFloatStateOf(initialValue) }

    ActionPromptDialog(
        titleBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.sleep_timer),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        onDismiss = onDismiss,
        onConfirm = { onConfirm(sleepTimerValue.roundToInt()) },
        onCancel = onDismiss,
        onReset = { sleepTimerValue = 30f },
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = pluralStringResource(R.plurals.minute, sleepTimerValue.roundToInt(), sleepTimerValue.roundToInt()),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(Modifier.height(16.dp))

                Slider(
                    value = sleepTimerValue,
                    onValueChange = { sleepTimerValue = it },
                    valueRange = 5f..120f,
                    steps = (120 - 5) / 5 - 1,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))

                OutlinedButton(onClick = onEndOfSong) {
                    Text(stringResource(R.string.end_of_song))
                }
            }
        },
    )
}

@Composable
fun QueueCollapsedContentV4(
    textBackgroundColor: Color,
    onExpandQueue: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onShowLyrics: () -> Unit,
    sleepTimerEnabled: Boolean,
    sleepTimerTimeLeft: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal,
                ),
            ),
    ) {
        val buttonSize = 48.dp
        val iconSize = 22.dp

        Box(
            modifier = Modifier
                .height(buttonSize)
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(textBackgroundColor.copy(alpha = 0.1f))
                .clickable { onExpandQueue() },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = textBackgroundColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.queue),
                    color = textBackgroundColor,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(
                    if (sleepTimerEnabled) textBackgroundColor.copy(alpha = 0.2f)
                    else textBackgroundColor.copy(alpha = 0.1f)
                )
                .clickable { onSleepTimerClick() },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                label = "sleepTimer",
                targetState = sleepTimerEnabled,
            ) { enabled ->
                if (enabled) {
                    Text(
                        text = makeTimeString(sleepTimerTimeLeft),
                        color = textBackgroundColor,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().basicMarquee(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = textBackgroundColor,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .height(buttonSize)
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(textBackgroundColor.copy(alpha = 0.1f))
                .clickable { onShowLyrics() },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lyrics,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = textBackgroundColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.lyrics),
                    color = textBackgroundColor,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
        }
    }
}

// ==========================================
// 4. QUEUE SCREEN
// ==========================================

@Composable
fun QueueScreen(
    screenState: QueueScreenState,
    visible: Boolean,
    pureBlack: Boolean,
    onAction: (QueueAction) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
) {
    val model = when (screenState) {
        is QueueScreenState.Success -> screenState.model
        is QueueScreenState.Error -> screenState.previous
        else -> null
    }
    val background = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface
    if (model == null) {
        Column(
            modifier = modifier.fillMaxSize().background(background).then(headerModifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (screenState) {
                QueueScreenState.Loading -> CircularProgressIndicator()
                QueueScreenState.Empty -> {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.QueueMusic, null, Modifier.size(48.dp))
                    Text(stringResource(R.string.queues_empty), style = MaterialTheme.typography.titleLarge)
                }
                is QueueScreenState.Error -> {
                    Text(stringResource(screenState.message))
                    TextButton(onClick = { onAction(QueueAction.Retry) }) { Text(stringResource(android.R.string.ok)) }
                }
                is QueueScreenState.Success -> Unit
            }
        }
        return
    }

    BackHandler(enabled = visible && model.selectionCount > 0) {
        onAction(QueueAction.ClearSelection)
    }

    BoxWithConstraints(modifier.fillMaxSize().background(background)) {
        val scrollingHeader = maxHeight < 560.dp || LocalDensity.current.fontScale >= 1.5f
        val currentIndex = remember(model.entries) { model.entries.values.indexOfFirst { it.current } }
        val listState = rememberLazyListState()
        val reorderable = rememberReorderableLazyListState(listState) { from, to ->
            val source = from.key as? String
            val target = to.key as? String
            if (source != null && target != null) onAction(QueueAction.Drag(source, target))
        }
        LaunchedEffect(reorderable.isAnyItemDragging) {
            if (!reorderable.isAnyItemDragging) onAction(QueueAction.FinishDrag)
        }
        LaunchedEffect(visible) {
            if (visible && currentIndex >= 0) {
                listState.scrollToItem(currentIndex + if (scrollingHeader) 1 else 0)
            }
        }

        Scaffold(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp).fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
            containerColor = background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (model.selectionCount > 0) {
                    QueueSelectionToolbar(model, onAction, Modifier.fillMaxWidth().navigationBarsPadding().padding(8.dp))
                }
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                QueueHeader(model, onAction, headerModifier, showPlaybackControls = !scrollingHeader)
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = padding.calculateBottomPadding() + 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f).navigationBarsPadding(),
                ) {
                    if (scrollingHeader) {
                        item(key = "queue_header", contentType = "queue_header") {
                            QueuePlaybackControls(model, onAction)
                        }
                    }
                    itemsIndexed(
                        items = model.entries.values,
                        key = { _, row -> row.id },
                        contentType = { _, row -> if (row.current) "queue_current" else "queue_song" }
                    ) { index, entry ->
                        ReorderableItem(
                            reorderable,
                            key = entry.id,
                            modifier = Modifier.padding(vertical = if (entry.current) 4.dp else 0.dp),
                        ) { dragging ->
                            val swipe = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.8f })
                            LaunchedEffect(swipe.currentValue) {
                                if (swipe.currentValue != SwipeToDismissBoxValue.Settled) {
                                    onAction(QueueAction.Remove(entry.id))
                                }
                            }
                            val handle = if (model.locked) Modifier else Modifier.draggableHandle()
                            val previousId = model.entries.values.getOrNull(index - 1)?.id
                            val nextId = model.entries.values.getOrNull(index + 1)?.id
                            val playing = entry.current && model.playing
                            val liked = entry.current && model.liked
                            val selecting = model.selectionCount > 0

                            val content: @Composable () -> Unit = {
                                QueueSongRow(
                                    entry = entry,
                                    playing = playing,
                                    liked = liked,
                                    locked = model.locked,
                                    selecting = selecting,
                                    previousId = previousId,
                                    nextId = nextId,
                                    dragging = dragging,
                                    pureBlack = pureBlack,
                                    loadArtwork = visible,
                                    onAction = onAction,
                                    handle = handle,
                                )
                            }

                            if (model.locked || model.selectionCount > 0 || dragging) {
                                content()
                            } else {
                                SwipeToDismissBox(
                                    state = swipe,
                                    backgroundContent = {
                                        if (swipe.dismissDirection != SwipeToDismissBoxValue.Settled) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.errorContainer)
                                                    .padding(16.dp),
                                                contentAlignment = if (swipe.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                                    Alignment.CenterStart
                                                } else {
                                                    Alignment.CenterEnd
                                                },
                                            ) {
                                                Icon(imageVector = Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                            }
                                        }
                                    },
                                ) { content() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueSongRow(
    entry: QueueEntryUi,
    playing: Boolean,
    liked: Boolean,
    locked: Boolean,
    selecting: Boolean,
    previousId: String?,
    nextId: String?,
    dragging: Boolean,
    pureBlack: Boolean,
    loadArtwork: Boolean,
    onAction: (QueueAction) -> Unit,
    handle: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val target = when {
        dragging -> colors.surfaceContainerHighest
        entry.current -> colors.primaryContainer
        pureBlack -> colors.surface
        entry.recommended -> colors.surfaceContainer
        else -> colors.surfaceContainerLow
    }
    val container by animateColorAsState(target, label = "QueueRowColor")
    val click = remember(entry.id, onAction) { { onAction(QueueAction.Play(entry.id)) } }
    val select = remember(entry.id, onAction) { { onAction(QueueAction.Select(entry.id)) } }
    val menu = remember(entry.id, onAction) { { onAction(QueueAction.Menu(entry.id)) } }
    val moveUp = "Move up"
    val moveDown = "Move down"
    val reorderDescription = "Reorder"

    val accessibility = remember(locked, previousId, nextId, entry.id, onAction) {
        buildList {
            if (!locked) {
                previousId?.let { id ->
                    add(CustomAccessibilityAction(moveUp) {
                        onAction(QueueAction.Drag(entry.id, id))
                        onAction(QueueAction.FinishDrag)
                        true
                    })
                }
                nextId?.let { id ->
                    add(CustomAccessibilityAction(moveDown) {
                        onAction(QueueAction.Drag(entry.id, id))
                        onAction(QueueAction.FinishDrag)
                        true
                    })
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = click,
                onLongClick = select,
            )
            .semantics { customActions = accessibility }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!locked) {
                Box(
                    modifier = handle.size(40.dp).semantics { contentDescription = reorderDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = Icons.Rounded.DragHandle, null, Modifier.size(24.dp))
                }
            }

            QueueArtwork(entry.artworkUrl, entry.current, loadArtwork)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                if (entry.current) {
                    Text(
                        if (playing) "Playing" else "Paused",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
                Text(
                    entry.title,
                    style = if (entry.current) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (entry.current) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    remember(entry.artists, entry.duration) { joinByBullet(entry.artists, entry.duration) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selecting) {
                Checkbox(checked = entry.selected, onCheckedChange = { select() })
            } else {
                if (entry.current) {
                    IconButton(onClick = { onAction(QueueAction.ToggleLike) }) {
                        Icon(
                            painter = painterResource(if (liked) R.drawable.favorite else R.drawable.favorite_border),
                            contentDescription = null,
                            tint = if (liked) colors.error else colors.onSurface
                        )
                    }
                }
                IconButton(onClick = menu) {
                    Icon(imageVector = Icons.Rounded.MoreVert, null)
                }
            }
        }
    }
}

@Composable
private fun QueueArtwork(url: String?, current: Boolean, load: Boolean) {
    val context = LocalContext.current
    val size = if (current) 56.dp else 48.dp
    val request = remember(context, url, load) {
        ImageRequest.Builder(context).data(url.takeIf { load }).build()
    }
    AsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    )
}

@Composable
private fun QueueSelectionToolbar(model: QueueUiModel, onAction: (QueueAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            pluralStringResource(R.plurals.n_selected, model.selectionCount, model.selectionCount),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAction(QueueAction.SelectAll) }) {
                    Icon(imageVector = Icons.Rounded.SelectAll, null)
                }
                IconButton(onClick = { onAction(QueueAction.AddToPlaylist) }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd, null)
                }
                IconButton(onClick = { onAction(QueueAction.NewPlaylist) }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.QueueMusic, null)
                }
                IconButton(onClick = { onAction(QueueAction.RemoveSelection) }, enabled = !model.guest) {
                    Icon(imageVector = Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
                FloatingActionButton(
                    onClick = { onAction(QueueAction.ClearSelection) },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Rounded.Close, null, Modifier.size(20.dp))
                }
            }
        }
    }
}

// ==========================================
// 5. MAIN QUEUE COMPOSABLE (BOTTOM SHEET)
// ==========================================

@Composable
fun Queue(
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onBackgroundColor: Color,
    TextBackgroundColor: Color,
    textButtonColor: Color,
    iconButtonColor: Color,
    onShowLyrics: () -> Unit = {},
    pureBlack: Boolean,
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val viewModel: QueueViewModel = hiltViewModel()

    val screenState by viewModel.state.collectAsStateWithLifecycle()
    val chrome by viewModel.chrome.collectAsStateWithLifecycle()
    val latestChrome by rememberUpdatedState(chrome)

    val sleepTimerEnabled = chrome.sleepTimerEnabled
    val sleepTimerTimeLeft = chrome.sleepTimerTimeLeft
    val showSleepTimerDialog = chrome.showSleepTimer

    val model = when (val screen = screenState) {
        is QueueScreenState.Success -> screen.model
        is QueueScreenState.Error -> screen.previous
        else -> null
    }

    val onAction: (QueueAction) -> Unit = remember(viewModel) {
        { action -> viewModel.onAction(action) }
    }
    val snackbar = remember { SnackbarHostState() }

    DisposableEffect(viewModel, playerConnection) {
        viewModel.bind(playerConnection)
        onDispose { viewModel.unbind(playerConnection) }
    }

    LaunchedEffect(viewModel, menuState, navController, playerBottomSheetState) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is QueueEvent.Message -> snackbar.showSnackbar(
                    context.getString(event.resource, *event.arguments.toTypedArray()),
                )
                is QueueEvent.Removed -> {
                    val message = if (event.count == 1) {
                        context.getString(R.string.delete) + ": " + (event.title ?: "")
                    } else {
                        context.getString(R.string.delete)
                    }
                    val result = snackbar.showSnackbar(
                        message = message,
                        actionLabel = context.getString(android.R.string.cancel)
                    )
                    viewModel.finishRemoval(event.token, result == SnackbarResult.ActionPerformed)
                }
                is QueueEvent.Menu -> menuState.show {
                    PlayerMenu(
                        mediaMetadata = event.metadata,
                        navController = navController,
                        playerBottomSheetState = playerBottomSheetState,
                        onDismiss = menuState::dismiss,
                    )
                }
            }
        }
    }

    AddToPlaylistDialog(
        isVisible = model?.playlistDialog == true,
        onGetSong = { playlist ->
            val ids = viewModel.prepareSelectedSongs(playlist)
            viewModel.playlistAdded()
            ids
        },
        onDismiss = { onAction(QueueAction.DismissPlaylist) }
    )

    if (model?.createPlaylistDialog == true) {
        TextFieldDialog(
            title = { Text(stringResource(R.string.create_playlist)) },
            initialTextFieldValue = TextFieldValue(model.playlistName),
            isInputValid = { it.isNotBlank() && model.selectionCount > 0 },
            onDismiss = { onAction(QueueAction.DismissPlaylist) },
            onDone = { onAction(QueueAction.CreatePlaylist(it)) },
        )
    }

    val openQueue = remember(playerBottomSheetState, state) {
        {
            if (!playerBottomSheetState.isExpandedOrExpanding) playerBottomSheetState.expandSoft()
            state.expandSoft()
        }
    }

    BottomSheet(
        state = state,
        backgroundColor = Color.Unspecified,
        modifier = modifier,
        onCollapsedContentClick = openQueue,
        collapsedContent = {
            QueueCollapsedContentV4(
                textBackgroundColor = TextBackgroundColor,
                onExpandQueue = openQueue,
                onSleepTimerClick = { viewModel.sleepTimerClick() },
                onShowLyrics = onShowLyrics,
                sleepTimerEnabled = sleepTimerEnabled,
                sleepTimerTimeLeft = sleepTimerTimeLeft,
                modifier = Modifier,
            )

            if (showSleepTimerDialog) {
                SleepTimerDialog(
                    onDismiss = viewModel::dismissSleepTimer,
                    onConfirm = { minutes -> viewModel.startSleepTimer(minutes) },
                    onEndOfSong = { viewModel.startSleepTimer(-1) },
                    initialValue = chrome.sleepTimerValue,
                )
            }
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            QueueScreen(
                screenState = screenState,
                visible = !state.isCollapsed,
                pureBlack = pureBlack && backgroundColor == Color.Black,
                onAction = onAction,
                headerModifier = Modifier.bottomSheetDraggable(state),
            )
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = (if ((model?.selectionCount ?: 0) > 0) 128.dp else 24.dp) +
                            WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
                    )
            )
        }
    }
}
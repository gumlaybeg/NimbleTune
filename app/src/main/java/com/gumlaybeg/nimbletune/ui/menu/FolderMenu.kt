package com.gumlaybeg.nimbletune.ui.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalDatabase
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.db.entities.Event
import com.gumlaybeg.nimbletune.extensions.toMediaItem
import com.gumlaybeg.nimbletune.models.DirectoryTree
import com.gumlaybeg.nimbletune.models.toMediaMetadata
import com.gumlaybeg.nimbletune.playback.PlayerConnection.Companion.queueBoard
import com.gumlaybeg.nimbletune.ui.component.GridMenu
import com.gumlaybeg.nimbletune.ui.component.GridMenuItem
import com.gumlaybeg.nimbletune.ui.component.SongFolderItem

@Composable
fun FolderMenu(
    folder: DirectoryTree,
    event: Event? = null,
    navController: NavController,
    onDismiss: () -> Unit,
) {
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val allFolderSongs = folder.toList()

    var showChooseQueueDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToQueueDialog(
        isVisible = showChooseQueueDialog,
        onAdd = { queueName ->
            queueBoard.addQueue(queueName, allFolderSongs.map { it.toMediaMetadata() }, playerConnection,
                forceInsert = true, delta = false)
            queueBoard.setCurrQueue(playerConnection)
        },
        onDismiss = {
            showChooseQueueDialog = false
        }
    )

    AddToPlaylistDialog(
        isVisible = showChoosePlaylistDialog,
        onGetSong = { allFolderSongs.map { it.id } },
        onDismiss = { showChoosePlaylistDialog = false }
    )

    // folder info
    SongFolderItem(
        folderTitle = folder.currentDir,
        modifier = Modifier,
        subtitle = folder.parent.substringAfter("//storage//"),
    )

    HorizontalDivider()

    // options
    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        GridMenuItem(
            icon = Icons.AutoMirrored.Rounded.PlaylistPlay,
            title = R.string.play_next
        ) {
            onDismiss()
            allFolderSongs.forEach {
                playerConnection.enqueueNext(it.toMediaItem())
            }
        }
        GridMenuItem(
            icon = Icons.AutoMirrored.Rounded.QueueMusic,
            title = R.string.add_to_queue
        ) {
            showChooseQueueDialog = true
        }
        GridMenuItem(
            icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
            title = R.string.add_to_playlist
        ) {
            showChoosePlaylistDialog = true
        }
    }
}

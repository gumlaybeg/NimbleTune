package com.gumlaybeg.nimbletune.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.ScannerMatchCriteria
import com.gumlaybeg.nimbletune.constants.ScannerSensitivityKey
import com.gumlaybeg.nimbletune.db.entities.Song
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.menu.AddToPlaylistDialog
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.viewmodels.BackupRestoreViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupAndRestore(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: BackupRestoreViewModel = hiltViewModel(),
) {
    val (scannerSensitivity) = rememberEnumPreference(
        key = ScannerSensitivityKey,
        defaultValue = ScannerMatchCriteria.LEVEL_2
    )

    val context = LocalContext.current
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) {
            viewModel.backup(context, uri)
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.restore(context, uri)
        }
    }

    var showChoosePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var importedTitle by remember { mutableStateOf("") }
    val importedSongs = remember { mutableStateListOf<Song>() }
    val rejectedSongs = remember { mutableStateListOf<String>() }

    val importM3uLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val result = viewModel.loadM3u(context, uri, matchStrength = scannerSensitivity)
            importedSongs.clear()
            importedSongs.addAll(result.first)
            rejectedSongs.clear()
            rejectedSongs.addAll(result.second)
            importedTitle = result.third

            if (importedSongs.isNotEmpty()) {
                showChoosePlaylistDialog = true
            }
        }
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.backup_restore),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        // Backup & Restore Group
        ExpressiveSettingGroup(
            title = stringResource(R.string.backup_restore),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Backup),
                    title = { Text(stringResource(R.string.backup)) },
                    description = { Text("Export database, preferences and playlist data") },
                    onClick = {
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        backupLauncher.launch("${context.getString(R.string.app_name)}_${LocalDateTime.now().format(formatter)}.backup")
                    }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Restore),
                    title = { Text(stringResource(R.string.restore)) },
                    description = { Text("Restore library from an existing backup file") },
                    onClick = {
                        restoreLauncher.launch(arrayOf("application/octet-stream"))
                    }
                )
            )
        )

        // Media Import Group
        ExpressiveSettingGroup(
            title = stringResource(R.string.import_m3u),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.PlaylistAdd),
                    title = { Text(stringResource(R.string.import_m3u)) },
                    description = { Text("Import playlists from local .m3u / .m3u8 audio playlist files") },
                    onClick = {
                        importM3uLauncher.launch(arrayOf("audio/*"))
                    }
                )
            )
        )

        AddToPlaylistDialog(
            isVisible = showChoosePlaylistDialog,
            allowSyncing = false,
            initialTextFieldValue = importedTitle,
            onGetSong = { importedSongs.map { it.id } },
            onDismiss = { showChoosePlaylistDialog = false }
        )

        if (rejectedSongs.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 250.dp)
                    .padding(horizontal = 8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.m3u_import_song_failed),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                itemsIndexed(items = rejectedSongs, key = { _, song -> song.hashCode() }) { _, item ->
                    Text(
                        text = item,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(4.dp)
            )
            Text(
                stringResource(R.string.import_innertune_tooltip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
package com.gumlaybeg.nimbletune.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalDatabase
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.LocalSyncUtils
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.DevSettingsKey
import com.gumlaybeg.nimbletune.constants.FirstSetupPassed
import com.gumlaybeg.nimbletune.constants.SwipeToSkip
import com.gumlaybeg.nimbletune.ui.component.ActionPromptDialog
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.component.ModernSwitch
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val syncUtils = LocalSyncUtils.current
    val coroutineScope = rememberCoroutineScope()

    val (swipeToSkip, onSwipeToSkipChange) = rememberPreference(SwipeToSkip, defaultValue = true)
    val (devSettings, onDevSettingsChange) = rememberPreference(DevSettingsKey, defaultValue = false)
    val (_, onFirstSetupPassedChange) = rememberPreference(FirstSetupPassed, defaultValue = false)

    val isSyncingRemotePlaylists by syncUtils.isSyncingRemotePlaylists.collectAsState()
    val isSyncingRemoteAlbums by syncUtils.isSyncingRemoteAlbums.collectAsState()
    val isSyncingRemoteArtists by syncUtils.isSyncingRemoteArtists.collectAsState()
    val isSyncingRemoteSongs by syncUtils.isSyncingRemoteSongs.collectAsState()
    val isSyncingRemoteLikedSongs by syncUtils.isSyncingRemoteLikedSongs.collectAsState()

    var showNukeDialog by remember { mutableStateOf(false) }

    if (showNukeDialog) {
        ActionPromptDialog(
            title = "Nuke Local Library",
            onDismiss = { showNukeDialog = false },
            onConfirm = {
                showNukeDialog = false
                Toast.makeText(context, "Clearing local library from database...", Toast.LENGTH_SHORT).show()
                coroutineScope.launch(Dispatchers.IO) {
                    database.nukeLocalData()
                }
            },
            onCancel = { showNukeDialog = false },
            content = {
                Text("This will remove all local files and artists from the database. Are you sure?")
            }
        )
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.experimental_settings_title),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        // Experimental Features Group
        ExpressiveSettingGroup(
            title = stringResource(R.string.experimental_settings_title),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Swipe),
                    title = { Text(stringResource(R.string.swipe_to_skip_title)) },
                    description = { Text(stringResource(R.string.swipe_to_skip_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = swipeToSkip,
                            onCheckedChange = onSwipeToSkipChange
                        )
                    },
                    onClick = { onSwipeToSkipChange(!swipeToSkip) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.DeveloperMode),
                    title = { Text(stringResource(R.string.dev_settings_title)) },
                    description = { Text(stringResource(R.string.dev_settings_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = devSettings,
                            onCheckedChange = onDevSettingsChange
                        )
                    },
                    onClick = { onDevSettingsChange(!devSettings) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Sync),
                    title = { Text(stringResource(R.string.scanner_manual_btn)) },
                    description = { Text("Trigger full sync across remote songs, albums, and playlists") },
                    onClick = {
                        Toast.makeText(context, context.getString(R.string.sync_progress_active), Toast.LENGTH_SHORT).show()
                        coroutineScope.launch(Dispatchers.Main) {
                            syncUtils.syncAll()
                        }
                    }
                )
            )
        )

        // Sync Status Items
        SyncProgressItem(stringResource(R.string.songs), isSyncingRemoteSongs)
        SyncProgressItem(stringResource(R.string.liked_songs), isSyncingRemoteLikedSongs)
        SyncProgressItem(stringResource(R.string.artists), isSyncingRemoteArtists)
        SyncProgressItem(stringResource(R.string.albums), isSyncingRemoteAlbums)
        SyncProgressItem(stringResource(R.string.playlists), isSyncingRemotePlaylists)

        // Developer Mode Group
        if (devSettings) {
            ExpressiveSettingGroup(
                title = stringResource(R.string.settings_debug),
                items = listOf(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Rounded.ConfirmationNumber),
                        title = { Text("Launch Setup Wizard") },
                        description = { Text("Re-run the initial onboarding configuration wizard") },
                        onClick = {
                            onFirstSetupPassedChange(false)
                            runBlocking { delay(200) }
                            navController.navigate("setup_wizard")
                        }
                    ),
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Rounded.ErrorOutline),
                        title = { Text("Nuke Local Database") },
                        description = { Text("Delete all cached local songs, artists, albums, and genres") },
                        onClick = { showNukeDialog = true }
                    )
                )
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

@Composable
fun SyncProgressItem(text: String, isSyncing: Boolean) {
    if (isSyncing) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
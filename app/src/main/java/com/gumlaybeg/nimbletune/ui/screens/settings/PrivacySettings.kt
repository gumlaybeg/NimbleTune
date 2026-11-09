package com.gumlaybeg.nimbletune.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalDatabase
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.PauseListenHistoryKey
import com.gumlaybeg.nimbletune.constants.PauseRemoteListenHistoryKey
import com.gumlaybeg.nimbletune.constants.PauseSearchHistoryKey
import com.gumlaybeg.nimbletune.ui.component.ActionPromptDialog
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.component.ModernSwitch
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val database = LocalDatabase.current
    val (pauseListenHistory, onPauseListenHistoryChange) = rememberPreference(key = PauseListenHistoryKey, defaultValue = false)
    val (pauseRemoteListenHistory, onPauseRemoteListenHistoryChange) = rememberPreference(key = PauseRemoteListenHistoryKey, defaultValue = false)
    val (pauseSearchHistory, onPauseSearchHistoryChange) = rememberPreference(key = PauseSearchHistoryKey, defaultValue = false)

    var showClearListenHistoryDialog by rememberSaveable { mutableStateOf(false) }
    var showClearSearchHistoryDialog by rememberSaveable { mutableStateOf(false) }

    if (showClearListenHistoryDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.clear_listen_history),
            onDismiss = { showClearListenHistoryDialog = false },
            onConfirm = {
                showClearListenHistoryDialog = false
                database.query { clearListenHistory() }
            },
            onCancel = { showClearListenHistoryDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.clear_listen_history_confirm),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        )
    }

    if (showClearSearchHistoryDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.clear_search_history),
            onDismiss = { showClearSearchHistoryDialog = false },
            onConfirm = {
                showClearSearchHistoryDialog = false
                database.query { clearSearchHistory() }
            },
            onCancel = { showClearSearchHistoryDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.clear_search_history_confirm),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
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
            text = stringResource(R.string.privacy),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        // Listen History Group
        ExpressiveSettingGroup(
            title = stringResource(R.string.history),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.History),
                    title = { Text(stringResource(R.string.pause_listen_history)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = pauseListenHistory,
                            onCheckedChange = onPauseListenHistoryChange
                        )
                    },
                    onClick = { onPauseListenHistoryChange(!pauseListenHistory) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.History),
                    title = { Text(stringResource(R.string.pause_remote_listen_history)) },
                    enabled = !pauseListenHistory,
                    trailingContent = {
                        ModernSwitch(
                            checked = pauseRemoteListenHistory,
                            onCheckedChange = onPauseRemoteListenHistoryChange,
                            enabled = !pauseListenHistory
                        )
                    },
                    onClick = { if (!pauseListenHistory) onPauseRemoteListenHistoryChange(!pauseRemoteListenHistory) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.ClearAll),
                    title = { Text(stringResource(R.string.clear_listen_history)) },
                    onClick = { showClearListenHistoryDialog = true }
                )
            )
        )

        // Search History Group
        ExpressiveSettingGroup(
            title = stringResource(R.string.search),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.SearchOff),
                    title = { Text(stringResource(R.string.pause_search_history)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = pauseSearchHistory,
                            onCheckedChange = onPauseSearchHistoryChange
                        )
                    },
                    onClick = { onPauseSearchHistoryChange(!pauseSearchHistory) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.ManageSearch),
                    title = { Text(stringResource(R.string.clear_search_history)) },
                    onClick = { showClearSearchHistoryDialog = true }
                )
            )
        )

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
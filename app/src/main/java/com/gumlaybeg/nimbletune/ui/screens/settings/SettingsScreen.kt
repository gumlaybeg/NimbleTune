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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.IconButton
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.player_and_audio),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Palette),
                    title = { Text(stringResource(R.string.appearance)) },
                    description = { Text(stringResource(R.string.theme)) },
                    onClick = { navController.navigate("settings/appearance") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.PlayArrow),
                    title = { Text(stringResource(R.string.player_and_audio)) },
                    description = { Text(stringResource(R.string.grp_audio)) },
                    onClick = { navController.navigate("settings/player") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Language),
                    title = { Text(stringResource(R.string.content)) },
                    description = { Text(stringResource(R.string.account)) },
                    onClick = { navController.navigate("settings/content") }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.library),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.SdCard),
                    title = { Text(stringResource(R.string.local_player_settings_title)) },
                    description = { Text(stringResource(R.string.auto_scanner_title)) },
                    onClick = { navController.navigate("settings/local") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Storage),
                    title = { Text(stringResource(R.string.storage)) },
                    description = { Text(stringResource(R.string.downloaded_songs)) },
                    onClick = { navController.navigate("settings/storage") }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.prefs_advanced),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Security),
                    title = { Text(stringResource(R.string.privacy)) },
                    description = { Text(stringResource(R.string.pause_listen_history)) },
                    onClick = { navController.navigate("settings/privacy") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Restore),
                    title = { Text(stringResource(R.string.backup_restore)) },
                    description = { Text(stringResource(R.string.backup)) },
                    onClick = { navController.navigate("settings/backup_restore") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.WarningAmber),
                    title = { Text(stringResource(R.string.experimental_settings_title)) },
                    description = { Text(stringResource(R.string.dev_settings_title)) },
                    onClick = { navController.navigate("settings/experimental") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Info),
                    title = { Text(stringResource(R.string.about)) },
                    description = { Text(stringResource(R.string.info)) },
                    onClick = { navController.navigate("settings/about") }
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
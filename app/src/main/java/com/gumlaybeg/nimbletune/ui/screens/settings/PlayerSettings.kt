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
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.NoCell
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.AudioNormalizationKey
import com.gumlaybeg.nimbletune.constants.AudioOffload
import com.gumlaybeg.nimbletune.constants.AudioQuality
import com.gumlaybeg.nimbletune.constants.AudioQualityKey
import com.gumlaybeg.nimbletune.constants.AutoLoadMoreKey
import com.gumlaybeg.nimbletune.constants.KeepAliveKey
import com.gumlaybeg.nimbletune.constants.PersistentQueueKey
import com.gumlaybeg.nimbletune.constants.SkipOnErrorKey
import com.gumlaybeg.nimbletune.constants.SkipSilenceKey
import com.gumlaybeg.nimbletune.constants.StopMusicOnTaskClearKey
import com.gumlaybeg.nimbletune.constants.minPlaybackDurKey
import com.gumlaybeg.nimbletune.ui.component.CounterDialog
import com.gumlaybeg.nimbletune.ui.component.EnumDialog
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.IconButton
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.component.ModernSwitch
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(key = AudioQualityKey, defaultValue = AudioQuality.AUTO)
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(key = PersistentQueueKey, defaultValue = true)
    val (skipSilence, onSkipSilenceChange) = rememberPreference(key = SkipSilenceKey, defaultValue = false)
    val (skipOnErrorKey, onSkipOnErrorChange) = rememberPreference(key = SkipOnErrorKey, defaultValue = true)
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(key = AudioNormalizationKey, defaultValue = true)
    val (autoLoadMore, onAutoLoadMoreChange) = rememberPreference(AutoLoadMoreKey, defaultValue = true)
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(key = StopMusicOnTaskClearKey, defaultValue = false)
    val (minPlaybackDur, onMinPlaybackDurChange) = rememberPreference(minPlaybackDurKey, defaultValue = 30)
    val (audioOffload, onAudioOffloadChange) = rememberPreference(key = AudioOffload, defaultValue = false)
    val (keepAlive, onKeepAliveChange) = rememberPreference(key = KeepAliveKey, defaultValue = false)

    var showAudioQualityDialog by rememberSaveable { mutableStateOf(false) }
    var showMinPlaybackDur by rememberSaveable { mutableStateOf(false) }

    if (showAudioQualityDialog) {
        EnumDialog(
            onDismiss = { showAudioQualityDialog = false },
            onSelect = {
                onAudioQualityChange(it)
                showAudioQualityDialog = false
            },
            title = stringResource(R.string.audio_quality),
            current = audioQuality,
            values = AudioQuality.entries.toList(),
            valueText = {
                when (it) {
                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                }
            }
        )
    }

    if (showMinPlaybackDur) {
        CounterDialog(
            title = stringResource(R.string.min_playback_duration),
            description = stringResource(R.string.min_playback_duration_description),
            initialValue = minPlaybackDur,
            upperBound = 100,
            lowerBound = 0,
            unitDisplay = "%",
            onDismiss = { showMinPlaybackDur = false },
            onConfirm = {
                showMinPlaybackDur = false
                onMinPlaybackDurChange(it)
            },
            onCancel = { showMinPlaybackDur = false }
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
            text = stringResource(R.string.player_and_audio),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_audio),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.GraphicEq),
                    title = { Text(stringResource(R.string.audio_quality)) },
                    description = {
                        Text(
                            when (audioQuality) {
                                AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                                AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                                AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                            }
                        )
                    },
                    onClick = { showAudioQualityDialog = true }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.VolumeUp),
                    title = { Text(stringResource(R.string.audio_normalization)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = audioNormalization,
                            onCheckedChange = onAudioNormalizationChange
                        )
                    },
                    onClick = { onAudioNormalizationChange(!audioNormalization) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.skip_next),
                    title = { Text(stringResource(R.string.skip_silence)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = skipSilence,
                            onCheckedChange = onSkipSilenceChange
                        )
                    },
                    onClick = { onSkipSilenceChange(!skipSilence) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.FastForward),
                    title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
                    description = { Text(stringResource(R.string.auto_skip_next_on_error_desc)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = skipOnErrorKey,
                            onCheckedChange = onSkipOnErrorChange
                        )
                    },
                    onClick = { onSkipOnErrorChange(!skipOnErrorKey) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Bolt),
                    title = { Text(stringResource(R.string.audio_offload)) },
                    description = { Text(stringResource(R.string.audio_offload_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = audioOffload,
                            onCheckedChange = onAudioOffloadChange
                        )
                    },
                    onClick = { onAudioOffloadChange(!audioOffload) }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_general),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.QueueMusic),
                    title = { Text(stringResource(R.string.persistent_queue)) },
                    description = { Text(stringResource(R.string.persistent_queue_desc_ot)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = persistentQueue,
                            onCheckedChange = onPersistentQueueChange
                        )
                    },
                    onClick = { onPersistentQueueChange(!persistentQueue) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Autorenew),
                    title = { Text(stringResource(R.string.auto_load_more)) },
                    description = { Text(stringResource(R.string.auto_load_more_desc)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = autoLoadMore,
                            onCheckedChange = onAutoLoadMoreChange
                        )
                    },
                    onClick = { onAutoLoadMoreChange(!autoLoadMore) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Lyrics),
                    title = { Text(stringResource(R.string.lyrics_settings_title)) },
                    onClick = { navController.navigate("settings/player/lyrics") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Sync),
                    title = { Text(stringResource(R.string.min_playback_duration)) },
                    description = { Text("$minPlaybackDur%") },
                    onClick = { showMinPlaybackDur = true }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.prefs_advanced),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.ClearAll),
                    title = { Text(stringResource(R.string.stop_music_on_task_clear)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = stopMusicOnTaskClear,
                            onCheckedChange = onStopMusicOnTaskClearChange
                        )
                    },
                    onClick = { onStopMusicOnTaskClearChange(!stopMusicOnTaskClear) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.NoCell),
                    title = { Text(stringResource(R.string.keep_alive_title)) },
                    description = { Text(stringResource(R.string.keep_alive_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = keepAlive,
                            onCheckedChange = onKeepAliveChange
                        )
                    },
                    onClick = { onKeepAliveChange(!keepAlive) }
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
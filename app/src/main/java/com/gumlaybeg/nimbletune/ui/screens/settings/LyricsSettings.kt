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
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.FormatAlignLeft
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.TextFields
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
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.EnableKugouKey
import com.gumlaybeg.nimbletune.constants.EnableLrcLibKey
import com.gumlaybeg.nimbletune.constants.LyricFontSizeKey
import com.gumlaybeg.nimbletune.constants.LyricSourcePrefKey
import com.gumlaybeg.nimbletune.constants.LyricTrimKey
import com.gumlaybeg.nimbletune.constants.LyricsTextPositionKey
import com.gumlaybeg.nimbletune.constants.MultilineLrcKey
import com.gumlaybeg.nimbletune.ui.component.CounterDialog
import com.gumlaybeg.nimbletune.ui.component.EnumDialog
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.component.ModernSwitch
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (enableKugou, onEnableKugouChange) = rememberPreference(key = EnableKugouKey, defaultValue = true)
    val (enableLrcLib, onEnableLrcLibChange) = rememberPreference(key = EnableLrcLibKey, defaultValue = true)
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(LyricsTextPositionKey, defaultValue = LyricsPosition.CENTER)
    val (multilineLrc, onMultilineLrcChange) = rememberPreference(MultilineLrcKey, defaultValue = true)
    val (lyricTrim, onLyricTrimChange) = rememberPreference(LyricTrimKey, defaultValue = false)
    val (lyricFontSize, onLyricFontSizeChange) = rememberPreference(LyricFontSizeKey, defaultValue = 20)
    val (preferLocalLyric, onPreferLocalLyric) = rememberPreference(LyricSourcePrefKey, defaultValue = true)

    var showFontSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showPositionDialog by rememberSaveable { mutableStateOf(false) }

    if (showFontSizeDialog) {
        CounterDialog(
            title = stringResource(R.string.lyrics_font_Size),
            initialValue = lyricFontSize,
            upperBound = 32,
            lowerBound = 8,
            unitDisplay = " sp",
            onDismiss = { showFontSizeDialog = false },
            onConfirm = {
                onLyricFontSizeChange(it)
                showFontSizeDialog = false
            },
            onReset = { onLyricFontSizeChange(20) },
            onCancel = { showFontSizeDialog = false }
        )
    }

    if (showPositionDialog) {
        EnumDialog(
            onDismiss = { showPositionDialog = false },
            onSelect = {
                onLyricsPositionChange(it)
                showPositionDialog = false
            },
            title = stringResource(R.string.lyrics_text_position),
            current = lyricsPosition,
            values = LyricsPosition.entries.toList(),
            valueText = {
                when (it) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
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
            text = stringResource(R.string.lyrics_settings_title),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        // Providers Group
        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_lyrics_source),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Lyrics),
                    title = { Text(stringResource(R.string.enable_lrclib)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = enableLrcLib,
                            onCheckedChange = onEnableLrcLibChange
                        )
                    },
                    onClick = { onEnableLrcLibChange(!enableLrcLib) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Lyrics),
                    title = { Text(stringResource(R.string.enable_kugou)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = enableKugou,
                            onCheckedChange = onEnableKugouChange
                        )
                    },
                    onClick = { onEnableKugouChange(!enableKugou) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.ContentCut),
                    title = { Text(stringResource(R.string.lyrics_prefer_local)) },
                    description = { Text(stringResource(R.string.lyrics_prefer_local_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = preferLocalLyric,
                            onCheckedChange = onPreferLocalLyric
                        )
                    },
                    onClick = { onPreferLocalLyric(!preferLocalLyric) }
                )
            )
        )

        // Parser & Formatting Group
        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_lyrics_parser),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.Sort),
                    title = { Text(stringResource(R.string.lyrics_multiline_title)) },
                    description = { Text(stringResource(R.string.lyrics_multiline_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = multilineLrc,
                            onCheckedChange = onMultilineLrcChange
                        )
                    },
                    onClick = { onMultilineLrcChange(!multilineLrc) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.ContentCut),
                    title = { Text(stringResource(R.string.lyrics_trim_title)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = lyricTrim,
                            onCheckedChange = onLyricTrimChange
                        )
                    },
                    onClick = { onLyricTrimChange(!lyricTrim) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.FormatAlignLeft),
                    title = { Text(stringResource(R.string.lyrics_text_position)) },
                    description = {
                        Text(
                            when (lyricsPosition) {
                                LyricsPosition.LEFT -> stringResource(R.string.left)
                                LyricsPosition.CENTER -> stringResource(R.string.center)
                                LyricsPosition.RIGHT -> stringResource(R.string.right)
                            }
                        )
                    },
                    onClick = { showPositionDialog = true }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.TextFields),
                    title = { Text(stringResource(R.string.lyrics_font_Size)) },
                    description = { Text("$lyricFontSize sp") },
                    onClick = { showFontSizeDialog = true }
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
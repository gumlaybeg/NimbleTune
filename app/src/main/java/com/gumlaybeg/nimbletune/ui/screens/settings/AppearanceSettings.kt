package com.gumlaybeg.nimbletune.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.Tab
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.DarkModeKey
import com.gumlaybeg.nimbletune.constants.DefaultOpenTabKey
import com.gumlaybeg.nimbletune.constants.DefaultOpenTabNewKey
import com.gumlaybeg.nimbletune.constants.DynamicThemeKey
import com.gumlaybeg.nimbletune.constants.EnabledTabsKey
import com.gumlaybeg.nimbletune.constants.FlatSubfoldersKey
import com.gumlaybeg.nimbletune.constants.ListItemHeight
import com.gumlaybeg.nimbletune.constants.NewInterfaceKey
import com.gumlaybeg.nimbletune.constants.PlayerBackgroundStyleKey
import com.gumlaybeg.nimbletune.constants.PureBlackKey
import com.gumlaybeg.nimbletune.constants.ShowLikedAndDownloadedPlaylist
import com.gumlaybeg.nimbletune.constants.SlimNavBarKey
import com.gumlaybeg.nimbletune.constants.SwipeToQueueKey
import com.gumlaybeg.nimbletune.constants.ThumbnailCornerRadius
import com.gumlaybeg.nimbletune.extensions.move
import com.gumlaybeg.nimbletune.ui.component.ActionPromptDialog
import com.gumlaybeg.nimbletune.ui.component.EnumDialog
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.IconButton
import com.gumlaybeg.nimbletune.ui.component.InfoLabel
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.component.ModernSwitch
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.utils.decodeTabString
import com.gumlaybeg.nimbletune.utils.encodeTabString
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

const val DEFAULT_ENABLED_TABS = "HSABLF"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference(key = PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.LIVE_MESH)
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)
    val (enabledTabs, onEnabledTabsChange) = rememberPreference(EnabledTabsKey, defaultValue = DEFAULT_ENABLED_TABS)
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(DefaultOpenTabKey, defaultValue = NavigationTab.HOME)
    val (defaultOpenTabNew, onDefaultOpenTabNewChange) = rememberEnumPreference(DefaultOpenTabNewKey, defaultValue = NavigationTabNew.HOME)
    val (newInterfaceStyle, onNewInterfaceStyleChange) = rememberPreference(key = NewInterfaceKey, defaultValue = true)
    val (showLikedAndDownloadedPlaylist, onShowLikedAndDownloadedPlaylistChange) = rememberPreference(key = ShowLikedAndDownloadedPlaylist, defaultValue = true)
    val (swipe2Queue, onSwipe2QueueChange) = rememberPreference(SwipeToQueueKey, defaultValue = true)
    val (slimNav, onSlimNavChange) = rememberPreference(SlimNavBarKey, defaultValue = false)
    val (flatSubfolders, onFlatSubfoldersChange) = rememberPreference(FlatSubfoldersKey, defaultValue = true)

    val availableBackgroundStyles = PlayerBackgroundStyle.entries.filter {
        it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    var showDarkModeDialog by rememberSaveable { mutableStateOf(false) }
    var showPlayerBackgroundDialog by rememberSaveable { mutableStateOf(false) }
    var showDefaultOpenTabDialog by rememberSaveable { mutableStateOf(false) }
    var showTabArrangement by rememberSaveable { mutableStateOf(false) }

    val mutableTabs = remember { mutableStateListOf<NavigationTab>() }
    val lazySongsListState = rememberLazyListState()
    var dragInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazySongsListState,
        scrollThresholdPadding = WindowInsets.systemBars.add(
            WindowInsets(top = ListItemHeight, bottom = ListItemHeight)
        ).asPaddingValues()
    ) { from, to ->
        val currentDragInfo = dragInfo
        dragInfo = if (currentDragInfo == null) from.index to to.index else currentDragInfo.first to to.index
        mutableTabs.move(from.index, to.index)
    }

    fun updateTabs() {
        mutableTabs.apply {
            clear()
            val enabled = decodeTabString(enabledTabs)
            addAll(enabled)
            add(NavigationTab.NULL)
            addAll(NavigationTab.entries.filter { item -> enabled.none { it == item || item == NavigationTab.NULL } })
        }
    }

    LaunchedEffect(showTabArrangement) {
        updateTabs()
    }

    if (showDarkModeDialog) {
        EnumDialog(
            onDismiss = { showDarkModeDialog = false },
            onSelect = {
                onDarkModeChange(it)
                showDarkModeDialog = false
            },
            title = stringResource(R.string.dark_theme),
            current = darkMode,
            values = DarkMode.entries.toList(),
            valueText = {
                when (it) {
                    DarkMode.ON -> stringResource(R.string.dark_theme_on)
                    DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                    DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
                }
            }
        )
    }

    if (showPlayerBackgroundDialog) {
        EnumDialog(
            onDismiss = { showPlayerBackgroundDialog = false },
            onSelect = {
                onPlayerBackgroundChange(it)
                showPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.player_background_style),
            current = playerBackground,
            values = availableBackgroundStyles,
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.player_background_default)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.player_background_gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    PlayerBackgroundStyle.GLOW_ANIMATED -> "Glow Animated"
                    PlayerBackgroundStyle.LIVE_MESH -> "Live Mesh"
                }
            }
        )
    }

    if (showDefaultOpenTabDialog) {
        if (newInterfaceStyle) {
            EnumDialog(
                onDismiss = { showDefaultOpenTabDialog = false },
                onSelect = {
                    onDefaultOpenTabNewChange(it)
                    showDefaultOpenTabDialog = false
                },
                title = stringResource(R.string.default_open_tab),
                current = defaultOpenTabNew,
                values = NavigationTabNew.entries.toList(),
                valueText = {
                    when (it) {
                        NavigationTabNew.HOME -> stringResource(R.string.home)
                        NavigationTabNew.LIBRARY -> stringResource(R.string.library)
                    }
                }
            )
        } else {
            EnumDialog(
                onDismiss = { showDefaultOpenTabDialog = false },
                onSelect = {
                    onDefaultOpenTabChange(it)
                    showDefaultOpenTabDialog = false
                },
                title = stringResource(R.string.default_open_tab),
                current = defaultOpenTab,
                values = NavigationTab.entries.filter { it != NavigationTab.NULL },
                valueText = {
                    when (it) {
                        NavigationTab.HOME -> stringResource(R.string.home)
                        NavigationTab.SONG -> stringResource(R.string.songs)
                        NavigationTab.FOLDERS -> stringResource(R.string.folders)
                        NavigationTab.ARTIST -> stringResource(R.string.artists)
                        NavigationTab.ALBUM -> stringResource(R.string.albums)
                        NavigationTab.PLAYLIST -> stringResource(R.string.playlists)
                        else -> ""
                    }
                }
            )
        }
    }

    if (showTabArrangement) {
        ActionPromptDialog(
            title = stringResource(R.string.tab_arrangement),
            onDismiss = { showTabArrangement = false },
            onConfirm = {
                var encoded = encodeTabString(mutableTabs)
                if (!mutableTabs.contains(defaultOpenTab)) {
                    onDefaultOpenTabChange(NavigationTab.HOME)
                }
                if (!encoded.contains('H')) {
                    encoded += "H"
                }
                onEnabledTabsChange(encoded)
                showTabArrangement = false
            },
            onReset = {
                onEnabledTabsChange(DEFAULT_ENABLED_TABS)
                updateTabs()
            },
            onCancel = { showTabArrangement = false }
        ) {
            LazyColumn(
                state = lazySongsListState,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(ThumbnailCornerRadius))
            ) {
                itemsIndexed(items = mutableTabs, key = { _, item -> item.hashCode() }) { _, tab ->
                    ReorderableItem(state = reorderableState, key = tab.hashCode()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = when (tab) {
                                    NavigationTab.HOME -> stringResource(R.string.home)
                                    NavigationTab.SONG -> stringResource(R.string.songs)
                                    NavigationTab.FOLDERS -> stringResource(R.string.folders)
                                    NavigationTab.ARTIST -> stringResource(R.string.artists)
                                    NavigationTab.ALBUM -> stringResource(R.string.albums)
                                    NavigationTab.PLAYLIST -> stringResource(R.string.playlists)
                                    else -> stringResource(R.string.tab_arrangement_disable_tip)
                                }
                            )
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = null,
                                modifier = Modifier.draggableHandle()
                            )
                        }
                    }
                }
            }
            InfoLabel(stringResource(R.string.tab_arrangement_home_required))
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
            text = stringResource(R.string.appearance),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.theme),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Palette),
                    title = { Text(stringResource(R.string.enable_dynamic_theme)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = dynamicTheme,
                            onCheckedChange = onDynamicThemeChange
                        )
                    },
                    onClick = { onDynamicThemeChange(!dynamicTheme) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.DarkMode),
                    title = { Text(stringResource(R.string.dark_theme)) },
                    description = {
                        Text(
                            when (darkMode) {
                                DarkMode.ON -> stringResource(R.string.dark_theme_on)
                                DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                                DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
                            }
                        )
                    },
                    onClick = { showDarkModeDialog = true }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Contrast),
                    title = { Text(stringResource(R.string.pure_black)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = pureBlack,
                            onCheckedChange = onPureBlackChange
                        )
                    },
                    onClick = { onPureBlackChange(!pureBlack) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.BlurOn),
                    title = { Text(stringResource(R.string.player_background_style)) },
                    description = {
                        Text(
                            when (playerBackground) {
                                PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.player_background_default)
                                PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.player_background_gradient)
                                PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                                PlayerBackgroundStyle.GLOW_ANIMATED -> "Glow Animated"
                                PlayerBackgroundStyle.LIVE_MESH -> "Live Mesh"
                            }
                        )
                    },
                    onClick = { showPlayerBackgroundDialog = true }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_interface),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Palette),
                    title = { Text(stringResource(R.string.new_interface)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = newInterfaceStyle,
                            onCheckedChange = onNewInterfaceStyleChange
                        )
                    },
                    onClick = { onNewInterfaceStyleChange(!newInterfaceStyle) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.PlaylistPlay),
                    title = { Text(stringResource(R.string.show_liked_and_downloaded_playlist)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = showLikedAndDownloadedPlaylist,
                            onCheckedChange = onShowLikedAndDownloadedPlaylistChange
                        )
                    },
                    onClick = { onShowLikedAndDownloadedPlaylistChange(!showLikedAndDownloadedPlaylist) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.PlaylistAdd),
                    title = { Text(stringResource(R.string.swipe2Queue)) },
                    description = { Text(stringResource(R.string.swipe2Queue_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = swipe2Queue,
                            onCheckedChange = onSwipe2QueueChange
                        )
                    },
                    onClick = { onSwipe2QueueChange(!swipe2Queue) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.MoreHoriz),
                    title = { Text(stringResource(R.string.slim_navbar_title)) },
                    description = { Text(stringResource(R.string.slim_navbar_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = slimNav,
                            onCheckedChange = onSlimNavChange
                        )
                    },
                    onClick = { onSlimNavChange(!slimNav) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.FolderCopy),
                    title = { Text(stringResource(R.string.flat_subfolders_title)) },
                    description = { Text(stringResource(R.string.flat_subfolders_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = flatSubfolders,
                            onCheckedChange = onFlatSubfoldersChange
                        )
                    },
                    onClick = { onFlatSubfoldersChange(!flatSubfolders) }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.tab_arrangement),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Tab),
                    title = { Text(stringResource(R.string.default_open_tab)) },
                    description = {
                        Text(
                            if (newInterfaceStyle) {
                                when (defaultOpenTabNew) {
                                    NavigationTabNew.HOME -> stringResource(R.string.home)
                                    NavigationTabNew.LIBRARY -> stringResource(R.string.library)
                                }
                            } else {
                                when (defaultOpenTab) {
                                    NavigationTab.HOME -> stringResource(R.string.home)
                                    NavigationTab.SONG -> stringResource(R.string.songs)
                                    NavigationTab.FOLDERS -> stringResource(R.string.folders)
                                    NavigationTab.ARTIST -> stringResource(R.string.artists)
                                    NavigationTab.ALBUM -> stringResource(R.string.albums)
                                    NavigationTab.PLAYLIST -> stringResource(R.string.playlists)
                                    else -> ""
                                }
                            }
                        )
                    },
                    onClick = { showDefaultOpenTabDialog = true }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Reorder),
                    title = { Text(stringResource(R.string.tab_arrangement)) },
                    onClick = { showTabArrangement = true }
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

enum class DarkMode {
    ON, OFF, AUTO
}

enum class PlayerBackgroundStyle {
    DEFAULT, GRADIENT, BLUR, GLOW_ANIMATED, LIVE_MESH
}

enum class NavigationTab {
    HOME, SONG, FOLDERS, ARTIST, ALBUM, PLAYLIST, NULL
}

enum class NavigationTabNew {
    HOME, LIBRARY
}

enum class LyricsPosition {
    LEFT, CENTER, RIGHT
}
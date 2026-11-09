package com.gumlaybeg.nimbletune.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.gumlaybeg.nimbletune.LocalDatabase
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.MaxImageCacheSizeKey
import com.gumlaybeg.nimbletune.constants.SongSortType
import com.gumlaybeg.nimbletune.extensions.tryOrNull
import com.gumlaybeg.nimbletune.playback.ExoDownloadService
import com.gumlaybeg.nimbletune.ui.component.ActionPromptDialog
import com.gumlaybeg.nimbletune.ui.component.EnumDialog
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.IconButton
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.ui.utils.formatFileSize
import com.gumlaybeg.nimbletune.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@SuppressLint("PrivateResource")
@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StorageSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val imageDiskCache = context.imageLoader.diskCache ?: return
    val downloadCache = LocalPlayerConnection.current?.service?.downloadCache ?: return
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()

    var imageCacheSize by remember { mutableLongStateOf(imageDiskCache.size) }
    var downloadCacheSize by remember { mutableLongStateOf(tryOrNull { downloadCache.cacheSpace } ?: 0) }

    LaunchedEffect(imageDiskCache) {
        while (isActive) {
            delay(500)
            imageCacheSize = imageDiskCache.size
        }
    }
    LaunchedEffect(downloadCache) {
        while (isActive) {
            delay(500)
            downloadCacheSize = tryOrNull { downloadCache.cacheSpace } ?: 0
        }
    }

    val (maxImageCacheSize, onMaxImageCacheSizeChange) = rememberPreference(
        key = MaxImageCacheSizeKey,
        defaultValue = 512
    )

    LaunchedEffect(maxImageCacheSize) {
        if (maxImageCacheSize == 0) {
            coroutineScope.launch(Dispatchers.IO) {
                imageDiskCache.clear()
            }
        }
    }

    var showClearDownloadsDialog by rememberSaveable { mutableStateOf(false) }
    var showClearImageCacheDialog by rememberSaveable { mutableStateOf(false) }
    var showMaxImageCacheDialog by rememberSaveable { mutableStateOf(false) }

    if (showClearDownloadsDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.clear_all_downloads),
            onDismiss = { showClearDownloadsDialog = false },
            onConfirm = {
                coroutineScope.launch(Dispatchers.IO) {
                    downloadCache.keys.forEach { key ->
                        downloadCache.removeResource(key)
                    }
                    database.downloadSongs(SongSortType.NAME, true).collect { songs ->
                        songs.forEach { song ->
                            DownloadService.sendRemoveDownload(
                                context,
                                ExoDownloadService::class.java,
                                song.song.id,
                                false
                            )
                        }
                    }
                }
                showClearDownloadsDialog = false
            },
            onCancel = { showClearDownloadsDialog = false },
            content = {
                Text(text = "Are you sure you want to delete all downloaded songs?")
            }
        )
    }

    if (showClearImageCacheDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.clear_image_cache),
            onDismiss = { showClearImageCacheDialog = false },
            onConfirm = {
                coroutineScope.launch(Dispatchers.IO) {
                    imageDiskCache.clear()
                }
                showClearImageCacheDialog = false
            },
            onCancel = { showClearImageCacheDialog = false },
            content = {
                Text(text = stringResource(R.string.clear_image_cache))
            }
        )
    }

    if (showMaxImageCacheDialog) {
        val cacheOptions = listOf(0, 128, 256, 512, 1024, 2048, 4096, 8192)
        EnumDialog(
            onDismiss = { showMaxImageCacheDialog = false },
            onSelect = {
                onMaxImageCacheSizeChange(it)
                showMaxImageCacheDialog = false
            },
            title = stringResource(R.string.max_cache_size),
            current = maxImageCacheSize,
            values = cacheOptions,
            valueText = {
                when (it) {
                    0 -> stringResource(androidx.compose.ui.R.string.state_off)
                    else -> formatFileSize(it * 1024 * 1024L)
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
            text = stringResource(R.string.storage),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.downloaded_songs),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Storage),
                    title = { Text(stringResource(R.string.downloaded_songs)) },
                    description = {
                        Text(stringResource(R.string.size_used, formatFileSize(downloadCacheSize)))
                    }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Delete),
                    title = { Text(stringResource(R.string.clear_all_downloads)) },
                    onClick = { showClearDownloadsDialog = true }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.image_cache),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Image),
                    title = { Text(stringResource(R.string.image_cache)) },
                    description = {
                        Column {
                            if (maxImageCacheSize > 0) {
                                LinearProgressIndicator(
                                    progress = { (imageCacheSize.toFloat() / imageDiskCache.maxSize).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                                Text(
                                    text = "${formatFileSize(imageCacheSize)} / ${formatFileSize(imageDiskCache.maxSize)}"
                                )
                            } else {
                                Text(stringResource(androidx.compose.ui.R.string.state_off))
                            }
                        }
                    },
                    onClick = { showMaxImageCacheDialog = true }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.ClearAll),
                    title = { Text(stringResource(R.string.clear_image_cache)) },
                    onClick = { showClearImageCacheDialog = true }
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
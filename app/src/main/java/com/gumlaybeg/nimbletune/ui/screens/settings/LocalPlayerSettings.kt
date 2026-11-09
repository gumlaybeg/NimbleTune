package com.gumlaybeg.nimbletune.ui.screens.settings

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.LocalDatabase
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.LocalPlayerConnection
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.AutomaticScannerKey
import com.gumlaybeg.nimbletune.constants.ExcludedScanPathsKey
import com.gumlaybeg.nimbletune.constants.LastLocalScanKey
import com.gumlaybeg.nimbletune.constants.LookupYtmArtistsKey
import com.gumlaybeg.nimbletune.constants.ScanPathsKey
import com.gumlaybeg.nimbletune.constants.ScannerImpl
import com.gumlaybeg.nimbletune.constants.ScannerImplKey
import com.gumlaybeg.nimbletune.constants.ScannerMatchCriteria
import com.gumlaybeg.nimbletune.constants.ScannerSensitivityKey
import com.gumlaybeg.nimbletune.constants.ScannerStrictExtKey
import com.gumlaybeg.nimbletune.constants.ThumbnailCornerRadius
import com.gumlaybeg.nimbletune.ui.component.ActionPromptDialog
import com.gumlaybeg.nimbletune.ui.component.EnumDialog
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.IconButton
import com.gumlaybeg.nimbletune.ui.component.InfoLabel
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.component.ModernSwitch
import com.gumlaybeg.nimbletune.ui.utils.DEFAULT_SCAN_PATH
import com.gumlaybeg.nimbletune.ui.utils.MEDIA_PERMISSION_LEVEL
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.ui.utils.imageCache
import com.gumlaybeg.nimbletune.utils.isPackageInstalled
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.destroyScanner
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.getScanner
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.scannerActive
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.scannerFinished
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.scannerProgressCurrent
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.scannerProgressTotal
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.scannerRequestCancel
import com.gumlaybeg.nimbletune.utils.scanners.LocalMediaScanner.Companion.scannerShowLoading
import com.gumlaybeg.nimbletune.utils.scanners.ScannerAbortException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current

    val isScannerActive by scannerActive.collectAsState()
    val showLoading by scannerShowLoading.collectAsState()
    val isScanFinished by scannerActive.collectAsState()
    val scannerProgressTotal by scannerProgressTotal.collectAsState()
    val scannerProgressCurrent by scannerProgressCurrent.collectAsState()

    var scannerFailure by remember { mutableStateOf(false) }
    var mediaPermission by remember { mutableStateOf(true) }
    var showAddFolderDialog by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var showSensitivityDialog by rememberSaveable { mutableStateOf(false) }
    var showScannerImplDialog by rememberSaveable { mutableStateOf(false) }

    val (scannerSensitivity, onScannerSensitivityChange) = rememberEnumPreference(
        key = ScannerSensitivityKey,
        defaultValue = ScannerMatchCriteria.LEVEL_2
    )
    val (scannerImpl, onScannerImplChange) = rememberEnumPreference(
        key = ScannerImplKey,
        defaultValue = ScannerImpl.TAGLIB
    )
    val (strictExtensions, onStrictExtensionsChange) = rememberPreference(ScannerStrictExtKey, defaultValue = false)
    val (autoScan, onAutoScanChange) = rememberPreference(AutomaticScannerKey, defaultValue = false)
    val (scanPaths, onScanPathsChange) = rememberPreference(ScanPathsKey, defaultValue = DEFAULT_SCAN_PATH)
    val (excludedScanPaths, onExcludedScanPathsChange) = rememberPreference(ExcludedScanPathsKey, defaultValue = "")
    var fullRescan by remember { mutableStateOf(false) }
    val (lookupYtmArtists, onLookupYtmArtistsChange) = rememberPreference(LookupYtmArtistsKey, defaultValue = true)
    var tempScanPaths by remember { mutableStateOf("") }
    val (_, onLastLocalScanChange) = rememberPreference(
        LastLocalScanKey,
        LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond()
    )

    val isFFmpegInstalled = rememberFFmpegAvailability()

    LaunchedEffect(isFFmpegInstalled) {
        if (scannerImpl == ScannerImpl.FFMPEG_EXT && !isFFmpegInstalled) {
            onScannerImplChange(ScannerImpl.TAGLIB)
        }
    }

    if (showSensitivityDialog) {
        EnumDialog(
            onDismiss = { showSensitivityDialog = false },
            onSelect = {
                onScannerSensitivityChange(it)
                showSensitivityDialog = false
            },
            title = stringResource(R.string.scanner_sensitivity_title),
            current = scannerSensitivity,
            values = ScannerMatchCriteria.entries.toList(),
            valueText = {
                when (it) {
                    ScannerMatchCriteria.LEVEL_1 -> stringResource(R.string.scanner_sensitivity_L1)
                    ScannerMatchCriteria.LEVEL_2 -> stringResource(R.string.scanner_sensitivity_L2)
                    ScannerMatchCriteria.LEVEL_3 -> stringResource(R.string.scanner_sensitivity_L3)
                }
            }
        )
    }

    if (showScannerImplDialog) {
        EnumDialog(
            onDismiss = { showScannerImplDialog = false },
            onSelect = {
                if (it == ScannerImpl.FFMPEG_EXT && isFFmpegInstalled) {
                    onScannerImplChange(it)
                } else if (it == ScannerImpl.FFMPEG_EXT) {
                    Toast.makeText(context, context.getString(R.string.scanner_missing_ffmpeg), Toast.LENGTH_LONG).show()
                    onScannerImplChange(ScannerImpl.TAGLIB)
                } else {
                    onScannerImplChange(it)
                }
                showScannerImplDialog = false
            },
            title = stringResource(R.string.scanner_type_title),
            current = scannerImpl,
            values = ScannerImpl.entries.toList(),
            valueText = {
                when (it) {
                    ScannerImpl.TAGLIB -> stringResource(R.string.scanner_type_taglib)
                    ScannerImpl.FFMPEG_EXT -> stringResource(R.string.scanner_type_ffmpeg_ext)
                }
            }
        )
    }

    if (showAddFolderDialog != null) {
        if (tempScanPaths.isEmpty()) {
            tempScanPaths = if (showAddFolderDialog == true) scanPaths else excludedScanPaths
        }

        ActionPromptDialog(
            titleBar = {
                Text(
                    text = stringResource(
                        if (showAddFolderDialog == true) R.string.scan_paths_incl else R.string.scan_paths_excl
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    Switch(
                        checked = showAddFolderDialog!!,
                        onCheckedChange = {
                            showAddFolderDialog = !showAddFolderDialog!!
                            tempScanPaths = if (showAddFolderDialog == true) scanPaths else excludedScanPaths
                        }
                    )
                }
            },
            onDismiss = {
                showAddFolderDialog = null
                tempScanPaths = ""
            },
            onConfirm = {
                if (showAddFolderDialog == true) {
                    onScanPathsChange(tempScanPaths)
                } else {
                    onExcludedScanPathsChange(tempScanPaths)
                }
                showAddFolderDialog = null
                tempScanPaths = ""
            },
            onReset = {
                tempScanPaths = if (showAddFolderDialog == true) DEFAULT_SCAN_PATH else " "
            },
            onCancel = {
                showAddFolderDialog = null
                tempScanPaths = ""
            }
        ) {
            val dirPickerLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocumentTree()
            ) { uri ->
                if (uri?.path != null && !("$tempScanPaths\u200B").contains(uri.path!! + "\u200B")) {
                    tempScanPaths = if (tempScanPaths.isBlank()) "${uri.path}\n" else "${tempScanPaths}${uri.path}\n"
                }
            }

            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(ThumbnailCornerRadius))
            ) {
                tempScanPaths.split('\n').filter { it.isNotBlank() }.forEach { path ->
                    Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            text = if (path.substringAfter("tree/").substringBefore(':') == "primary") {
                                "Internal Storage/${path.substringAfter(':')}"
                            } else {
                                "External (${path.substringAfter("tree/").substringBefore(':')})/${path.substringAfter(':')}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        )
                        IconButton(
                            onClick = {
                                tempScanPaths = if (tempScanPaths.substringAfter("\n").contains("\n")) {
                                    tempScanPaths.replace("$path\n", "")
                                } else {
                                    " "
                                }
                            },
                            onLongClick = {}
                        ) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { dirPickerLauncher.launch(null) }) {
                    Text(stringResource(R.string.scan_paths_add_folder))
                }
                InfoLabel(text = stringResource(R.string.scan_paths_tooltip))
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
            text = stringResource(R.string.local_player_settings_title),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.auto_scanner_title),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Autorenew),
                    title = { Text(stringResource(R.string.auto_scanner_title)) },
                    description = { Text(stringResource(R.string.auto_scanner_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = autoScan,
                            onCheckedChange = onAutoScanChange
                        )
                    },
                    onClick = { onAutoScanChange(!autoScan) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.FolderOpen),
                    title = { Text(stringResource(R.string.scan_paths_title)) },
                    description = { Text("Configure directories included or excluded from scanning") },
                    onClick = { showAddFolderDialog = true }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_manual_scanner),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Speed),
                    title = {
                        Text(
                            if (isScannerActive || showLoading) stringResource(R.string.action_cancel)
                            else if (scannerFailure) stringResource(R.string.scanner_scan_fail)
                            else if (isScanFinished) stringResource(R.string.scanner_progress_complete)
                            else if (!mediaPermission) stringResource(R.string.scanner_missing_storage_perm)
                            else stringResource(R.string.scanner_btn_idle)
                        )
                    },
                    description = {
                        if (showLoading && scannerProgressTotal != -1) {
                            val isSyncing = scannerProgressCurrent > -1
                            Text(
                                "${if (isSyncing) "Syncing" else "Scanning"} ${if (isSyncing) scannerProgressCurrent else "—"}/${scannerProgressTotal}"
                            )
                        } else {
                            Text("Manually scan and synchronize local files into your library")
                        }
                    },
                    trailingContent = {
                        if (showLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    },
                    onClick = {
                        if (isScannerActive) {
                            scannerRequestCancel = true
                            return@Material3SettingsItem
                        }

                        if (context.checkSelfPermission(MEDIA_PERMISSION_LEVEL) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(context as Activity, arrayOf(MEDIA_PERMISSION_LEVEL), PackageManager.PERMISSION_GRANTED)
                            mediaPermission = false
                            return@Material3SettingsItem
                        }
                        mediaPermission = true
                        scannerFinished.value = false
                        scannerFailure = false
                        playerConnection?.player?.pause()

                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val scanner = getScanner(context, scannerImpl)
                                val directoryStructure = scanner.scanLocal(
                                    database,
                                    scanPaths.split('\n'),
                                    excludedScanPaths.split('\n'),
                                    pathsOnly = !fullRescan
                                ).value

                                if (fullRescan) {
                                    scanner.fullSync(database, directoryStructure.toList(), scannerSensitivity, strictExtensions)
                                } else {
                                    scanner.quickSync(database, directoryStructure.toList(), scannerSensitivity, strictExtensions)
                                }

                                if (lookupYtmArtists && !isScannerActive) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        Looper.prepare()
                                        runCatching { scanner.localToRemoteArtist(database) }
                                    }
                                }
                            } catch (e: ScannerAbortException) {
                                scannerFailure = true
                            } finally {
                                destroyScanner()
                            }
                            imageCache.purgeCache()
                            playerConnection?.service?.initQueue()
                            onLastLocalScanChange(LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond())
                            scannerFinished.value = true
                        }
                    }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.options),
            items = listOf(
                Material3SettingsItem(
                    title = { Text(stringResource(R.string.scanner_variant_rescan)) },
                    trailingContent = {
                        Checkbox(
                            checked = fullRescan,
                            onCheckedChange = { fullRescan = it }
                        )
                    },
                    onClick = { fullRescan = !fullRescan }
                ),
                Material3SettingsItem(
                    title = { Text(stringResource(R.string.scanner_online_artist_linking)) },
                    trailingContent = {
                        Checkbox(
                            checked = lookupYtmArtists,
                            onCheckedChange = onLookupYtmArtistsChange
                        )
                    },
                    onClick = { onLookupYtmArtistsChange(!lookupYtmArtists) }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_extra_scanner_settings),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.GraphicEq),
                    title = { Text(stringResource(R.string.scanner_sensitivity_title)) },
                    description = {
                        Text(
                            when (scannerSensitivity) {
                                ScannerMatchCriteria.LEVEL_1 -> stringResource(R.string.scanner_sensitivity_L1)
                                ScannerMatchCriteria.LEVEL_2 -> stringResource(R.string.scanner_sensitivity_L2)
                                ScannerMatchCriteria.LEVEL_3 -> stringResource(R.string.scanner_sensitivity_L3)
                            }
                        )
                    },
                    onClick = { showSensitivityDialog = true }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.TextFields),
                    title = { Text(stringResource(R.string.scanner_strict_file_name_title)) },
                    description = { Text(stringResource(R.string.scanner_strict_file_name_description)) },
                    trailingContent = {
                        ModernSwitch(
                            checked = strictExtensions,
                            onCheckedChange = onStrictExtensionsChange
                        )
                    },
                    onClick = { onStrictExtensionsChange(!strictExtensions) }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Speed),
                    title = { Text(stringResource(R.string.scanner_type_title)) },
                    description = {
                        Text(
                            when (scannerImpl) {
                                ScannerImpl.TAGLIB -> stringResource(R.string.scanner_type_taglib)
                                ScannerImpl.FFMPEG_EXT -> stringResource(R.string.scanner_type_ffmpeg_ext)
                            }
                        )
                    },
                    onClick = { showScannerImplDialog = true }
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

@Composable
fun rememberFFmpegAvailability(): Boolean {
    val context = LocalContext.current
    var isFFmpegInstalled by remember {
        mutableStateOf(isPackageInstalled("wah.mikooomich.ffMetadataEx", context.packageManager))
    }

    DisposableEffect(context) {
        val packageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_PACKAGE_REMOVED,
                    Intent.ACTION_PACKAGE_ADDED -> {
                        isFFmpegInstalled = context?.packageManager?.let {
                            isPackageInstalled("wah.mikooomich.ffMetadataEx", it)
                        } == true
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        }

        context.registerReceiver(packageReceiver, filter)
        onDispose { context.unregisterReceiver(packageReceiver) }
    }

    return isFFmpegInstalled
}
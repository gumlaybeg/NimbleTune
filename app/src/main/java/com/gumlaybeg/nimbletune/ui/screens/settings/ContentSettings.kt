package com.gumlaybeg.nimbletune.ui.screens.settings

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Wifi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.App.Companion.forgetAccount
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.constants.AccountChannelHandleKey
import com.gumlaybeg.nimbletune.constants.AccountEmailKey
import com.gumlaybeg.nimbletune.constants.AccountNameKey
import com.gumlaybeg.nimbletune.constants.ContentCountryKey
import com.gumlaybeg.nimbletune.constants.ContentLanguageKey
import com.gumlaybeg.nimbletune.constants.CountryCodeToName
import com.gumlaybeg.nimbletune.constants.DataSyncIdKey
import com.gumlaybeg.nimbletune.constants.InnerTubeCookieKey
import com.gumlaybeg.nimbletune.constants.LanguageCodeToName
import com.gumlaybeg.nimbletune.constants.LikedAutoDownloadKey
import com.gumlaybeg.nimbletune.constants.LikedAutodownloadMode
import com.gumlaybeg.nimbletune.constants.ProxyEnabledKey
import com.gumlaybeg.nimbletune.constants.ProxyTypeKey
import com.gumlaybeg.nimbletune.constants.ProxyUrlKey
import com.gumlaybeg.nimbletune.constants.SYSTEM_DEFAULT
import com.gumlaybeg.nimbletune.constants.UseLoginForBrowse
import com.gumlaybeg.nimbletune.constants.VisitorDataKey
import com.gumlaybeg.nimbletune.constants.YtmSyncKey
import com.gumlaybeg.nimbletune.ui.component.EnumDialog
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.IconButton
import com.gumlaybeg.nimbletune.ui.component.InfoLabel
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.component.ModernSwitch
import com.gumlaybeg.nimbletune.ui.component.TextFieldDialog
import com.gumlaybeg.nimbletune.ui.utils.backToMain
import com.gumlaybeg.nimbletune.utils.rememberEnumPreference
import com.gumlaybeg.nimbletune.utils.rememberPreference
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.utils.parseCookieString
import java.net.Proxy

@SuppressLint("PrivateResource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current

    val (accountName, onAccountNameChange) = rememberPreference(AccountNameKey, "")
    val (accountEmail, onAccountEmailChange) = rememberPreference(AccountEmailKey, "")
    val (accountChannelHandle, onAccountChannelHandleChange) = rememberPreference(AccountChannelHandleKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val (visitorData, onVisitorDataChange) = rememberPreference(VisitorDataKey, "")
    val (dataSyncId, onDataSyncIdChange) = rememberPreference(DataSyncIdKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }

    val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, defaultValue = true)
    val (likedAutoDownload, onLikedAutoDownload) = rememberEnumPreference(LikedAutoDownloadKey, LikedAutodownloadMode.OFF)
    val (contentLanguage, onContentLanguageChange) = rememberPreference(key = ContentLanguageKey, defaultValue = "system")
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")

    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(key = ProxyEnabledKey, defaultValue = false)
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(key = ProxyTypeKey, defaultValue = Proxy.Type.HTTP)
    val (proxyUrl, onProxyUrlChange) = rememberPreference(key = ProxyUrlKey, defaultValue = "host:port")
    val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(UseLoginForBrowse, true)

    var showToken by remember { mutableStateOf(false) }
    var showTokenEditor by remember { mutableStateOf(false) }
    var showContentLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showContentCountryDialog by rememberSaveable { mutableStateOf(false) }
    var showAutoDownloadDialog by rememberSaveable { mutableStateOf(false) }
    var showProxyTypeDialog by rememberSaveable { mutableStateOf(false) }
    var showProxyUrlDialog by rememberSaveable { mutableStateOf(false) }

    if (showContentLanguageDialog) {
        EnumDialog(
            onDismiss = { showContentLanguageDialog = false },
            onSelect = {
                onContentLanguageChange(it)
                showContentLanguageDialog = false
            },
            title = stringResource(R.string.content_language),
            current = contentLanguage,
            values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
            valueText = {
                LanguageCodeToName.getOrElse(it) { stringResource(R.string.system_default) }
            }
        )
    }

    if (showContentCountryDialog) {
        EnumDialog(
            onDismiss = { showContentCountryDialog = false },
            onSelect = {
                onContentCountryChange(it)
                showContentCountryDialog = false
            },
            title = stringResource(R.string.content_country),
            current = contentCountry,
            values = listOf(SYSTEM_DEFAULT) + CountryCodeToName.keys.toList(),
            valueText = {
                CountryCodeToName.getOrElse(it) { stringResource(R.string.system_default) }
            }
        )
    }

    if (showAutoDownloadDialog) {
        EnumDialog(
            onDismiss = { showAutoDownloadDialog = false },
            onSelect = {
                onLikedAutoDownload(it)
                showAutoDownloadDialog = false
            },
            title = stringResource(R.string.like_autodownload),
            current = likedAutoDownload,
            values = LikedAutodownloadMode.entries.toList(),
            valueText = {
                when (it) {
                    LikedAutodownloadMode.OFF -> stringResource(androidx.compose.ui.R.string.state_off)
                    LikedAutodownloadMode.ON -> stringResource(androidx.compose.ui.R.string.state_on)
                    LikedAutodownloadMode.WIFI_ONLY -> stringResource(R.string.wifi_only)
                }
            }
        )
    }

    if (showProxyTypeDialog) {
        EnumDialog(
            onDismiss = { showProxyTypeDialog = false },
            onSelect = {
                onProxyTypeChange(it)
                showProxyTypeDialog = false
            },
            title = stringResource(R.string.proxy_type),
            current = proxyType,
            values = listOf(Proxy.Type.HTTP, Proxy.Type.SOCKS),
            valueText = { it.name }
        )
    }

    if (showProxyUrlDialog) {
        TextFieldDialog(
            title = { Text(stringResource(R.string.proxy_url)) },
            initialTextFieldValue = TextFieldValue(proxyUrl),
            onDone = {
                onProxyUrlChange(it)
                showProxyUrlDialog = false
            },
            onDismiss = { showProxyUrlDialog = false }
        )
    }

    if (showTokenEditor) {
        val text = "***INNERTUBE COOKIE*** =${innerTubeCookie}\n\n***VISITOR DATA*** =${visitorData}\n\n***DATASYNC ID*** =${dataSyncId}\n\n***ACCOUNT NAME*** =${accountName}\n\n***ACCOUNT EMAIL*** =${accountEmail}\n\n***ACCOUNT CHANNEL HANDLE*** =${accountChannelHandle}"
        TextFieldDialog(
            initialTextFieldValue = TextFieldValue(text),
            onDone = { data ->
                data.split("\n").forEach {
                    if (it.startsWith("***INNERTUBE COOKIE*** =")) onInnerTubeCookieChange(it.substringAfter("="))
                    else if (it.startsWith("***VISITOR DATA*** =")) onVisitorDataChange(it.substringAfter("="))
                    else if (it.startsWith("***DATASYNC ID*** =")) onDataSyncIdChange(it.substringAfter("="))
                    else if (it.startsWith("***ACCOUNT NAME*** =")) onAccountNameChange(it.substringAfter("="))
                    else if (it.startsWith("***ACCOUNT EMAIL*** =")) onAccountEmailChange(it.substringAfter("="))
                    else if (it.startsWith("***ACCOUNT CHANNEL HANDLE*** =")) onAccountChannelHandleChange(it.substringAfter("="))
                }
                showTokenEditor = false
            },
            onDismiss = { showTokenEditor = false },
            singleLine = false,
            maxLines = 20,
            isInputValid = {
                it.isNotEmpty() && runCatching { "SAPISID" in parseCookieString(it) }.getOrDefault(false)
            },
            extraContent = {
                InfoLabel(text = stringResource(R.string.token_adv_login_description))
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
            text = stringResource(R.string.content),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.account),
            items = buildList {
                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Rounded.Person),
                        title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
                        description = if (isLoggedIn) {
                            {
                                Text(
                                    accountEmail.ifEmpty { accountChannelHandle.ifEmpty { stringResource(R.string.account) } }
                                )
                            }
                        } else null,
                        onClick = { navController.navigate("login") }
                    )
                )

                if (isLoggedIn) {
                    add(
                        Material3SettingsItem(
                            icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.Logout),
                            title = { Text(stringResource(R.string.logout)) },
                            onClick = { forgetAccount(context) }
                        )
                    )
                }

                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Rounded.Key),
                        title = {
                            Text(
                                if (showToken) stringResource(R.string.token_shown)
                                else stringResource(R.string.token_hidden)
                            )
                        },
                        description = if (showToken) {
                            {
                                Text(
                                    text = if (isLoggedIn) innerTubeCookie else stringResource(R.string.not_logged_in),
                                    maxLines = 1
                                )
                            }
                        } else null,
                        onClick = {
                            if (!showToken) showToken = true
                            else showTokenEditor = true
                        }
                    )
                )

                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Rounded.Sync),
                        title = { Text(stringResource(R.string.ytm_sync)) },
                        enabled = isLoggedIn,
                        trailingContent = {
                            ModernSwitch(
                                checked = ytmSync,
                                onCheckedChange = onYtmSyncChange,
                                enabled = isLoggedIn
                            )
                        },
                        onClick = { if (isLoggedIn) onYtmSyncChange(!ytmSync) }
                    )
                )

                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Rounded.Favorite),
                        title = { Text(stringResource(R.string.like_autodownload)) },
                        description = {
                            Text(
                                when (likedAutoDownload) {
                                    LikedAutodownloadMode.OFF -> stringResource(androidx.compose.ui.R.string.state_off)
                                    LikedAutodownloadMode.ON -> stringResource(androidx.compose.ui.R.string.state_on)
                                    LikedAutodownloadMode.WIFI_ONLY -> stringResource(R.string.wifi_only)
                                }
                            )
                        },
                        onClick = { showAutoDownloadDialog = true }
                    )
                )

                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Rounded.Person),
                        title = { Text(stringResource(R.string.use_login_for_browse)) },
                        description = { Text(stringResource(R.string.use_login_for_browse_desc)) },
                        trailingContent = {
                            ModernSwitch(
                                checked = useLoginForBrowse,
                                onCheckedChange = {
                                    YouTube.useLoginForBrowse = it
                                    onUseLoginForBrowseChange(it)
                                }
                            )
                        },
                        onClick = {
                            val newValue = !useLoginForBrowse
                            YouTube.useLoginForBrowse = newValue
                            onUseLoginForBrowseChange(newValue)
                        }
                    )
                )
            }
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_localization),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Language),
                    title = { Text(stringResource(R.string.content_language)) },
                    description = {
                        Text(
                            LanguageCodeToName.getOrElse(contentLanguage) { stringResource(R.string.system_default) }
                        )
                    },
                    onClick = { showContentLanguageDialog = true }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.LocationOn),
                    title = { Text(stringResource(R.string.content_country)) },
                    description = {
                        Text(
                            CountryCodeToName.getOrElse(contentCountry) { stringResource(R.string.system_default) }
                        )
                    },
                    onClick = { showContentCountryDialog = true }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.grp_proxy),
            items = buildList {
                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Rounded.Wifi),
                        title = { Text(stringResource(R.string.enable_proxy)) },
                        trailingContent = {
                            ModernSwitch(
                                checked = proxyEnabled,
                                onCheckedChange = onProxyEnabledChange
                            )
                        },
                        onClick = { onProxyEnabledChange(!proxyEnabled) }
                    )
                )
                if (proxyEnabled) {
                    add(
                        Material3SettingsItem(
                            title = { Text(stringResource(R.string.proxy_type)) },
                            description = { Text(proxyType.name) },
                            onClick = { showProxyTypeDialog = true }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            title = { Text(stringResource(R.string.proxy_url)) },
                            description = { Text(proxyUrl) },
                            onClick = { showProxyUrlDialog = true }
                        )
                    )
                }
            }
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
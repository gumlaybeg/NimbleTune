package com.gumlaybeg.nimbletune.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gumlaybeg.nimbletune.BuildConfig
import com.gumlaybeg.nimbletune.LocalPlayerAwareWindowInsets
import com.gumlaybeg.nimbletune.R
import com.gumlaybeg.nimbletune.ui.component.ExpressiveSettingGroup
import com.gumlaybeg.nimbletune.ui.component.IconButton
import com.gumlaybeg.nimbletune.ui.component.Material3SettingsItem
import com.gumlaybeg.nimbletune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            ListItem(
                leadingContent = {
                    Image(
                        painter = painterResource(R.drawable.simple_tube_one),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                },
                headlineContent = {
                    Text(
                        text = "NimbleTune",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                supportingContent = {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) • ${BuildConfig.FLAVOR}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = { uriHandler.openUri("https://github.com/samyak2403/Simple-Tube") },
                        onLongClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.github),
                            contentDescription = "GitHub",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }

        ExpressiveSettingGroup(
            title = stringResource(R.string.info),
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Person),
                    title = { Text("Developer") },
                    description = { Text("Samyak Kamble") },
                    onClick = { uriHandler.openUri("https://github.com/samyak2403") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Code),
                    title = { Text("GitHub Repository") },
                    description = { Text("samyak2403/Simple-Tube") },
                    onClick = { uriHandler.openUri("https://github.com/samyak2403/Simple-Tube") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Favorite),
                    title = { Text("Special Thanks") },
                    description = { Text("Zion Huang for InnerTune base project") },
                    onClick = { uriHandler.openUri("https://github.com/z-huang/InnerTune") }
                )
            )
        )

        ExpressiveSettingGroup(
            title = "App Information",
            items = listOf(
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Description),
                    title = { Text("License") },
                    description = { Text("GNU General Public License v3.0") },
                    onClick = { uriHandler.openUri("https://github.com/samyak2403/Simple-Tube/blob/main/LICENSE") }
                ),
                Material3SettingsItem(
                    icon = rememberVectorPainter(Icons.Rounded.Info),
                    title = { Text("Version Code") },
                    trailingContent = { Text(BuildConfig.VERSION_CODE.toString()) }
                )
            )
        )

        if (BuildConfig.DEBUG) {
            val debugInfo = listOf(
                "Device: ${Build.BRAND} ${Build.DEVICE} (${Build.MODEL})",
                "Manufacturer: ${Build.MANUFACTURER}",
                "Android SDK: ${Build.VERSION.SDK_INT} (${Build.ID})",
                "ABIs: ${Build.SUPPORTED_ABIS.joinToString()}"
            )
            ExpressiveSettingGroup(
                title = "Device Info (Debug)",
                items = debugInfo.map { infoText ->
                    Material3SettingsItem(
                        title = { Text(infoText, style = MaterialTheme.typography.bodySmall) }
                    )
                }
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
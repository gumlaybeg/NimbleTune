package com.gumlaybeg.nimbletune.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun listItemShape(index: Int, total: Int, cornerRadius: Dp = 24.dp, innerRadius: Dp = 6.dp): Shape = when {
    total <= 1 -> RoundedCornerShape(cornerRadius)
    index == 0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = innerRadius, bottomEnd = innerRadius)
    index == total - 1 -> RoundedCornerShape(topStart = innerRadius, topEnd = innerRadius, bottomStart = cornerRadius, bottomEnd = cornerRadius)
    else -> RoundedCornerShape(innerRadius)
}

data class Material3SettingsItem(
    val icon: Painter? = null,
    val leadingContent: (@Composable () -> Unit)? = null,
    val title: @Composable () -> Unit,
    val description: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val enabled: Boolean = true,
    val onClick: (() -> Unit)? = null,
)

@Composable
fun ExpressiveSettingGroup(
    items: List<Material3SettingsItem>,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
            )
        }
        items.forEachIndexed { index, item ->
            val shape = listItemShape(index, items.size)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = shape,
                color = if (isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                }
            ) {
                ListItem(
                    modifier = Modifier.clickable(
                        enabled = item.enabled && item.onClick != null,
                        onClick = { item.onClick?.invoke() }
                    ),
                    leadingContent = item.leadingContent ?: item.icon?.let { icon ->
                        {
                            Icon(
                                painter = icon,
                                contentDescription = null,
                                tint = if (!item.enabled) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    },
                    headlineContent = {
                        ProvideTextStyle(
                            MaterialTheme.typography.bodyLarge.copy(
                                color = if (!item.enabled) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        ) {
                            item.title()
                        }
                    },
                    supportingContent = item.description?.let { desc ->
                        {
                            ProvideTextStyle(
                                MaterialTheme.typography.bodyMedium.copy(
                                    color = if (!item.enabled) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            ) {
                                desc()
                            }
                        }
                    },
                    trailingContent = item.trailingContent?.let { trailing ->
                        {
                            ProvideTextStyle(
                                MaterialTheme.typography.bodyMedium.copy(
                                    color = if (!item.enabled) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            ) {
                                trailing()
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun <T> EnumDialog(
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    title: String,
    current: T,
    values: List<T>,
    valueText: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    ListDialog(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        item {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
        items(values.size) { index ->
            val value = values[index]
            val isSelected = value == current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(value) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                AnimatedRadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Text(
                    text = valueText(value),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
package `in`.unartech.nearbydevs.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class NavTab(val label: String, val icon: ImageVector) {
    Discover("Discover", Icons.Filled.SettingsInputAntenna),
    Saved("Saved", Icons.Filled.Star),
    Logs("Logs", Icons.AutoMirrored.Filled.Article),
    Settings("Settings", Icons.Filled.Settings),
}

@Composable
fun BottomNav(
    selected: NavTab,
    savedCount: Int,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        NavTab.values().forEach { tab ->
            NavItem(
                tab = tab,
                active = tab == selected,
                badge = if (tab == NavTab.Saved && savedCount > 0) savedCount else null,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavItem(
    tab: NavTab,
    active: Boolean,
    badge: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor = if (active) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(
                    if (active) MaterialTheme.colorScheme.secondaryContainer
                    else androidx.compose.ui.graphics.Color.Transparent
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp, top = 2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = badge.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = iconColor,
        )
    }
}

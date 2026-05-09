package `in`.unartech.nearbydevs.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

const val NEARBY_REPO_URL = "https://github.com/rajeshpachaikani/nearby"

@Composable
fun GhPill(
    stars: String = "1.2k",
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val click = onClick ?: { uriHandler.openUri(NEARBY_REPO_URL) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CircleShape)
            .clickable(onClick = click)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Code,
            contentDescription = "Source",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Source",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "★ $stars",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

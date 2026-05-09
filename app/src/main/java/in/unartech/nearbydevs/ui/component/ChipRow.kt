package `in`.unartech.nearbydevs.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FilterChip(
    label: String,
    count: Int? = null,
    active: Boolean,
    onClick: () -> Unit,
) {
    val activeBg = MaterialTheme.colorScheme.secondaryContainer
    val inactiveBg = androidx.compose.ui.graphics.Color.Transparent
    val activeFg = MaterialTheme.colorScheme.onSecondaryContainer
    val inactiveFg = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) activeBg else inactiveBg)
            .then(
                if (active) Modifier
                else Modifier.border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    CircleShape,
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (active) activeFg else inactiveFg,
        )
        if (count != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (active) MaterialTheme.colorScheme.onPrimary else inactiveFg,
                )
            }
        }
    }
}

@Composable
fun <T> ChipRow(
    items: List<T>,
    selected: T,
    label: (T) -> String,
    count: ((T) -> Int)? = null,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.padding(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    ) {
        items(items) { item ->
            FilterChip(
                label = label(item),
                count = count?.invoke(item),
                active = item == selected,
                onClick = { onSelect(item) },
            )
        }
    }
}

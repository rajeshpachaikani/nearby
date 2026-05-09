package `in`.unartech.nearbydevs.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.ui.theme.MonoStyle

@Composable
fun SectionHeader(
    title: String,
    aside: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (aside != null) {
            Text(
                text = aside,
                style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

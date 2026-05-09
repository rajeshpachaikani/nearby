package `in`.unartech.nearbydevs.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.ui.theme.MonoStyle

@Composable
fun HexBlock(
    rows: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                MaterialTheme.shapes.medium,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        rows.forEachIndexed { i, row ->
            val offset = (i * 8).toString(16).padStart(4, '0')
            val ascii = row.split(" ").joinToString("") { byteHex ->
                val c = byteHex.toIntOrNull(16) ?: 0
                if (c in 32..126) c.toChar().toString() else "."
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "0x$offset",
                    style = MonoStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = row,
                    style = MonoStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "|$ascii|",
                    style = MonoStyle,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
fun MonoLines(
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                MaterialTheme.shapes.medium,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        lines.forEach { Text(text = it, style = MonoStyle, color = MaterialTheme.colorScheme.onSurface) }
    }
}

package `in`.unartech.nearbydevs.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.ui.theme.MonoStyle

private val EditorBg = Color(0xFF1F1A2C)
private val EditorHeaderBg = Color(0xFF1A1626)
private val EditorBorder = Color(0xFF332E40)
private val EditorFg = Color(0xFFE5E0F0)
private val Kw = Color(0xFFC4A8F2)
private val Str = Color(0xFFE5B17F)
private val Fn = Color(0xFF7BD1E0)
private val Com = Color(0xFF7C7691)
private val Num = Color(0xFF85DBA9)
private val FilenameTint = Color(0xFFE5B17F)

data class CodeSpan(val text: String, val color: Color = EditorFg)

@Composable
fun CodeBlock(
    filename: String,
    spans: List<CodeSpan>,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val text = remember(spans) {
        buildAnnotatedString {
            spans.forEach { withStyle(SpanStyle(color = it.color)) { append(it.text) } }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(EditorBg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorHeaderBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KotlinBadge()
                Text(
                    text = filename,
                    style = MonoStyle,
                    color = FilenameTint,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        clipboard.setText(AnnotatedString(spans.joinToString("") { it.text }))
                        copied = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy",
                    tint = Com,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = if (copied) "Copied" else "Copy",
                    style = MonoStyle,
                    color = Com,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(text = text, style = MonoStyle.copy(lineHeight = MonoStyle.lineHeight))
        }
    }
}

@Composable
private fun KotlinBadge() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color(0xFFE44857), Color(0xFFC711E1), Color(0xFF7F52FF)),
                )
            ),
    )
}

object Code {
    fun kw(s: String) = CodeSpan(s, Kw)
    fun str(s: String) = CodeSpan(s, Str)
    fun fn(s: String) = CodeSpan(s, Fn)
    fun com(s: String) = CodeSpan(s, Com)
    fun num(s: String) = CodeSpan(s, Num)
    fun txt(s: String) = CodeSpan(s, EditorFg)
}

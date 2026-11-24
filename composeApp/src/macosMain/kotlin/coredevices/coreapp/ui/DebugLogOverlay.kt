package coredevices.coreapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coredevices.coreapp.DebugLogs
import kotlinx.coroutines.delay

private const val VISIBLE_LINES = 200
private const val REFRESH_MILLIS = 400L
private val AccentColor = Color(0xFF7FD4FF)
private val LineColor = Color(0xFFE0E0E0)

/**
 * On-screen log panel for macOS debugging. Sits at the bottom of the window and can be
 * collapsed, so the UI underneath stays reachable.
 */
@Composable
fun DebugLogOverlay(modifier: Modifier = Modifier) {
    // Collapsed by default: expanded covers the bottom of the window, where the app puts its
    // floating action buttons.
    var expanded by remember { mutableStateOf(false) }
    var lines by remember { mutableStateOf(emptyList<String>()) }
    // Sampled on a timer: log lines arrive far faster than the panel can be recomposed.
    LaunchedEffect(Unit) {
        while (true) {
            lines = DebugLogs.lines.value
            delay(REFRESH_MILLIS)
        }
    }
    Column(
        modifier
            .fillMaxWidth()
            .height(if (expanded) 220.dp else 26.dp)
            .background(Color.Black.copy(alpha = 0.88f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "LOG ${lines.size}",
                color = AccentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                if (expanded) "收起" else "展开",
                color = AccentColor,
                fontSize = 11.sp,
                modifier = Modifier.clickable { expanded = !expanded },
            )
            Spacer(Modifier.weight(1f))
            Text(
                "清空",
                color = AccentColor,
                fontSize = 11.sp,
                modifier = Modifier.clickable { DebugLogs.clear() },
            )
        }
        if (expanded) {
            val scrollState = rememberScrollState()
            val shown = remember(lines) { lines.takeLast(VISIBLE_LINES) }
            LaunchedEffect(shown.size) {
                scrollState.scrollTo(scrollState.maxValue)
            }
            Column(
                Modifier.fillMaxSize().verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
            ) {
                shown.forEach { line ->
                    Text(
                        line,
                        color = LineColor,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

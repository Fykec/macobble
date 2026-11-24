package coredevices.ring.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coredevices.ring.battery.RingBatteryEstimator
import coredevices.ring.battery.RingBatteryLifeState
import coredevices.ring.ui.components.SectionHeader
import coredevices.ring.ui.theme.IndexTheme
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import coreapp.ring.generated.resources.Res as RingRes
import coreapp.ring.generated.resources.learning_your_usage
import coreapp.ring.generated.resources.index_01_battery_life_estimate
import coreapp.ring.generated.resources.not_enough_data_yet
import coreapp.ring.generated.resources.use_your_index_and_we_ll

/** "Index 01 Battery Life Estimate"; absent while loading or when no Index 01 is known (Index also runs with just a Pebble). */
@Composable
internal fun BatteryLifeSection(state: RingBatteryLifeState?) {
    if (state == null || state == RingBatteryLifeState.NoRing) return
    Column {
        SectionHeader(
            title = stringResource(RingRes.string.index_01_battery_life_estimate),
            modifier = Modifier.padding(top = 22.dp, bottom = 2.dp, start = 16.dp, end = 16.dp),
        )
        BatteryLifeRows(state)
    }
}

@Composable
private fun BatteryLifeRows(state: RingBatteryLifeState) {
    when (val s = state) {
        RingBatteryLifeState.NoRing -> Unit
        RingBatteryLifeState.NoData -> SettingsRow(
            title = stringResource(RingRes.string.not_enough_data_yet),
            subtitle = stringResource(RingRes.string.use_your_index_and_we_ll),
        )
        is RingBatteryLifeState.Learning -> LearningRow(s)
        is RingBatteryLifeState.Estimated -> SettingsRow(
            title = if (s.replaceSoon) "Battery almost empty" else "About ${formatRemaining(s.daysLeft)} left",
            subtitle = buildString {
                if (s.replaceSoon) {
                    append("Your Index will stop working soon.")
                } else {
                    append("${s.remainingPct}% remaining · ")
                    append("${formatRecordingTime(s.collectionsPerDay)} of recording a day on average")
                }
            },
        )
    }
}

@Composable
private fun LearningRow(s: RingBatteryLifeState.Learning) {
    val colors = IndexTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(stringResource(RingRes.string.learning_your_usage), fontSize = 16.sp, color = colors.onSurface)
        Text(
            "Day ${s.daysCollected} of ${s.daysNeeded} — keep using your Index and an estimate will appear here.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = colors.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { s.daysCollected.toFloat() / s.daysNeeded },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            color = colors.primary,
            trackColor = colors.outlineVariant,
        )
    }
}

/** Recording time, not press count, is what drains the cell. */
internal fun formatRecordingTime(collectionsPerDay: Double): String {
    val seconds = (collectionsPerDay * RingBatteryEstimator.SECONDS_PER_COLLECTION).roundToInt()
    return if (seconds < 60) "$seconds sec" else "${(seconds / 60.0).roundToInt()} min"
}

/** Coarse on purpose: the model isn't precise enough for exact dates. */
internal fun formatRemaining(daysLeft: Int): String = when {
    // Months up to two years: whole years would show 1.9 years as "1 year".
    daysLeft >= 730 -> "${daysLeft / 365} years"
    daysLeft >= 60 -> "${daysLeft / 30} months"
    daysLeft >= 14 -> "${daysLeft / 7} weeks"
    daysLeft == 1 -> "1 day"
    else -> "$daysLeft days"
}

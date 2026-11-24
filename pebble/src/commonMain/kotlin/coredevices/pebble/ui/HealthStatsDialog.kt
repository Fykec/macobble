package coredevices.pebble.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coredevices.ui.M3Dialog
import io.rebble.libpebblecommon.connection.LibPebble
import io.rebble.libpebblecommon.health.HealthDebugStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.compose.resources.stringResource
import coreapp.pebble.generated.resources.Res as PebbleRes
import coreapp.pebble.generated.resources.average
import coreapp.pebble.generated.resources.close
import coreapp.pebble.generated.resources.health_debug_stats
import coreapp.pebble.generated.resources.last_night_s_sleep
import coreapp.pebble.generated.resources.loading_health_statistics
import coreapp.pebble.generated.resources.today_s_steps
import coreapp.pebble.generated.resources.typical_sleep_by_weekday
import coreapp.pebble.generated.resources.typical_steps_by_weekday
import coreapp.pebble.generated.resources.refresh_health_data

private val logger = Logger.withTag("HealthStatsDialog")

@Composable
fun HealthStatsDialog(libPebble: LibPebble, onDismissRequest: () -> Unit) {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf<HealthDebugStats?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    fun syncAndRefresh() {
        scope.launch {
            try {
                isRefreshing = true
                // Pull ALL data from watch to reconcile any missing steps
                val requested = libPebble.requestHealthData(fullSync = true)

                // We don't know how long it will actually take for datalogging to sync health data
                delay(5.seconds)

                // Update stats
                stats = libPebble.getHealthDebugStats()
                // Send updated averages back to watch
                libPebble.sendHealthAveragesToWatch()
            } catch (e: Exception) {
                Logger.e("HealthStatsDialog", e) { "Failed to sync health data" }
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            stats = libPebble.getHealthDebugStats()
        } catch (e: Exception) {
            Logger.e("HealthStatsDialog", e) { "Failed to load health stats" }
        }
    }

    M3Dialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(PebbleRes.string.health_debug_stats))
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp).width(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    IconButton(onClick = { syncAndRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(PebbleRes.string.refresh_health_data))
                    }
                }
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(PebbleRes.string.close)) }
        },
    ) {
        Box(Modifier.heightIn(max = 400.dp)) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                if (stats != null) {
                    val s = stats!!
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Today's Steps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(PebbleRes.string.today_s_steps), style = MaterialTheme.typography.bodyMedium)
                            Text("${s.todaySteps}", style = MaterialTheme.typography.bodyMedium)
                        }

                        // 30d Avg Steps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(PebbleRes.string.average),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "${s.averageStepsPerDay}/day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Last Night's Sleep
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(PebbleRes.string.last_night_s_sleep), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                s.lastNightSleepHours?.let {
                                    "${it.format(1)}h"
                                }
                                    ?: "--",
                                style = MaterialTheme.typography.bodyMedium,
                                color =
                                    if (s.lastNightSleepHours != null) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                            )
                        }

                        // 30d Avg Sleep
                        val avgSleepHrs = s.averageSleepSecondsPerDay / 3600.0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(PebbleRes.string.average),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "${avgSleepHrs.format(1)}h/night",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            stringResource(PebbleRes.string.typical_steps_by_weekday),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        for (wd in DayOfWeek.entries) {
                            val total = s.weekdayTypicalSteps[wd]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    wd.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    total?.toString() ?: "--",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (total != null) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            stringResource(PebbleRes.string.typical_sleep_by_weekday),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        for (wd in DayOfWeek.entries) {
                            val typ = s.weekdayTypicalSleep[wd]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    wd.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    typ?.let {
                                        "${formatHm(it.sleepDurationSeconds)} / ${formatHm(it.deepSleepDurationSeconds)}  ${formatClock(it.fallAsleepSecondsOfDay)}→${formatClock(it.wakeupSecondsOfDay)}"
                                    } ?: "--",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (typ != null) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        stringResource(PebbleRes.string.loading_health_statistics),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

expect fun Double.format(digits: Int): String
fun Float.format(digits: Int): String = toDouble().format(digits)

private fun formatHm(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    return "${h}h${m.toString().padStart(2, '0')}m"
}

private fun formatClock(secondsOfDay: Int): String {
    val h = secondsOfDay / 3600
    val m = (secondsOfDay % 3600) / 60
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}
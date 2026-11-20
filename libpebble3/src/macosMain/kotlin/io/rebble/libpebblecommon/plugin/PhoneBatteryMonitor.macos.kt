package io.rebble.libpebblecommon.plugin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// macOS has no UIDevice; the phone battery level is not surfaced in v1.
actual class PhoneBatteryMonitor {
    actual val batteryLevel: Flow<Int?> = flowOf(null)
}

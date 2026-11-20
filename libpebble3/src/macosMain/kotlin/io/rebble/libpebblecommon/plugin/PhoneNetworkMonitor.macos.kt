package io.rebble.libpebblecommon.plugin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// macOS has no cellular radio; both flows are unknown (null) in v1.
actual class PhoneNetworkMonitor {
    actual val cellGeneration: Flow<String?> = flowOf(null)
    actual val connection: Flow<String?> = flowOf(null)
}

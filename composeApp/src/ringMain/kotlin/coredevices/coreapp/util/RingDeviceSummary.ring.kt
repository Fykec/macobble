package coredevices.coreapp.util

import coredevices.ExperimentalDevices
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Duration.Companion.seconds

private object RingDeviceSummary : KoinComponent {
    val experimentalDevices: ExperimentalDevices by lazy { get() }
}

actual suspend fun generateRingDeviceSummary(): String =
    withTimeoutOrNull(5.seconds) {
        try {
            RingDeviceSummary.experimentalDevices.debugSummary()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            "<RING SUMMARY EXCEPTION: ${e.message}>"
        }
    } ?: "<RING SUMMARY TIMEOUT>"

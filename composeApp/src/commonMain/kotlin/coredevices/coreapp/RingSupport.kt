package coredevices.coreapp

import androidx.compose.runtime.Composable

/**
 * Ring-related hooks whose real implementations live in the `ringMain` source set (Android/iOS).
 * macOS has no Ring support; its actuals are no-ops.
 */
interface RingAppLifecycle {
    suspend fun init()
    fun onBackgroundSync()
}

expect fun ringAppLifecycle(): RingAppLifecycle

@Composable
expect fun RingPromptHost()

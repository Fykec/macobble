package coredevices.coreapp

import androidx.compose.runtime.Composable

actual fun ringAppLifecycle(): RingAppLifecycle = object : RingAppLifecycle {
    override fun init() = Unit
    override fun onBackgroundSync() = Unit
}

@Composable
actual fun RingPromptHost() = Unit

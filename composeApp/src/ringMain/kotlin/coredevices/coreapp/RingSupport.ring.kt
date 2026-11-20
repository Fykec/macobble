package coredevices.coreapp

import androidx.compose.runtime.Composable
import coredevices.ExperimentalDevices
import coredevices.ring.ui.screens.settings.DefaultDestinationPromptHost
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun ringAppLifecycle(): RingAppLifecycle = object : RingAppLifecycle, KoinComponent {
    private val experimentalDevices: ExperimentalDevices by lazy { get() }

    override suspend fun init() {
        experimentalDevices.init()
    }

    override fun onBackgroundSync() {
        experimentalDevices.onBackgroundSync()
    }
}

@Composable
actual fun RingPromptHost() {
    DefaultDestinationPromptHost()
}

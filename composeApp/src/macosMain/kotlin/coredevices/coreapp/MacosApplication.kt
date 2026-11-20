package coredevices.coreapp

import PlatformContext
import androidx.compose.runtime.Composable
import coredevices.coreapp.di.apiModule
import coredevices.coreapp.di.macosDefaultModule
import coredevices.coreapp.di.utilModule
import coredevices.coreapp.ui.App
import coredevices.coreapp.util.initLogging
import coredevices.pebble.watchModule
import coredevices.util.CoreConfigFlow
import io.rebble.libpebblecommon.connection.LibPebble
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

/**
 * Starts Koin and renders the app. The macOS executable entry point (:macosApp) calls this.
 */
@Composable
fun MacosApp() {
    val koin = KoinPlatform.getKoinOrNull() ?: run {
        startKoin {
            modules(
                macosDefaultModule,
                apiModule,
                utilModule,
                watchModule,
            )
        }
        KoinPlatform.getKoin()
    }
    initLogging()
    // Instantiate the watch stack once so the UI has a running LibPebble behind it.
    koin.get<LibPebble>()
    koin.get<CoreConfigFlow>()
    koin.get<PlatformContext>()
    App()
}

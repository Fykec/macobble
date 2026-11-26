package coredevices.coreapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import co.touchlab.kermit.Logger
import coredevices.coreapp.di.apiModule
import coredevices.coreapp.di.macosDefaultModule
import coredevices.coreapp.di.utilModule
import coredevices.coreapp.ui.App
import coredevices.coreapp.ui.DebugLogOverlay
import coredevices.coreapp.util.initLogging
import coredevices.pebble.PebbleAppDelegate
import coredevices.pebble.watchModule
import coredevices.util.CoreConfigHolder
import org.koin.compose.koinInject
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

/**
 * Starts Koin, runs the same app-level startup Android and iOS do, and renders the app.
 * The macOS executable entry point (:macosApp) calls this.
 */
@Composable
fun MacosApp() {
    val koin = remember { bootstrap() }
    remember(koin) { koin.get<CommonAppDelegate>().init() }
    // Android and iOS do this from their app delegate. Without it LibPebble is never set up, so
    // Bluetooth stays reported as disabled and no scan ever starts.
    remember(koin) { koin.get<PebbleAppDelegate>().init() }
    // Debug aid: raw BLE advertisements, unfiltered by the Pebble service UUID.
    DisposableEffect(Unit) {
        startRawBleScan()
        onDispose { }
    }
    // Without an owner, koinViewModel() builds a fresh ViewModel on every recomposition, so state
    // set by a click is lost immediately. Android and iOS get one from their host.
    val viewModelStore = remember { ViewModelStore() }
    val viewModelStoreOwner = remember(viewModelStore) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = viewModelStore
        }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        // Off by default; the log panel is a debugging aid, toggled in Settings > Debug. The log file
        // is written either way (see DebugLogs.startWriting).
        val coreConfigHolder: CoreConfigHolder = koinInject()
        val coreConfig by coreConfigHolder.config.collectAsState()
        Box(Modifier.fillMaxSize()) {
            App()
            if (coreConfig.showDebugLogOverlay) {
                DebugLogOverlay(Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

private fun bootstrap(): Koin {
    val existing = KoinPlatform.getKoinOrNull()
    if (existing != null) return existing
    startKoin {
        modules(
            macosDefaultModule,
            apiModule,
            utilModule,
            watchModule,
        )
    }
    val koin = KoinPlatform.getKoin()
    initLogging()
    DebugLogs.startWriting()
    Logger.addLogWriter(DebugLogWriter())
    return koin
}

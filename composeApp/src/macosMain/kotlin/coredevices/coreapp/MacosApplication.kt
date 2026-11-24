package coredevices.coreapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import coredevices.coreapp.di.apiModule
import coredevices.coreapp.di.macosDefaultModule
import coredevices.coreapp.di.utilModule
import coredevices.coreapp.ui.App
import coredevices.coreapp.util.initLogging
import coredevices.pebble.watchModule
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
    // Without an owner, koinViewModel() builds a fresh ViewModel on every recomposition, so state
    // set by a click is lost immediately. Android and iOS get one from their host.
    val viewModelStore = remember { ViewModelStore() }
    val viewModelStoreOwner = remember(viewModelStore) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = viewModelStore
        }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        App()
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
    return koin
}

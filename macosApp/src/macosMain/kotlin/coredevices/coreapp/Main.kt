@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package coredevices.coreapp

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import io.rebble.libpebblecommon.connection.LibPebble
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.CoreGraphics.CGSizeMake

// Desktop window: wide enough for the tab bar plus a list and its detail pane, which the phone
// shape this started as had no room for.
private const val DEFAULT_WINDOW_WIDTH = 1120
private const val DEFAULT_WINDOW_HEIGHT = 840
private const val MIN_WINDOW_WIDTH = 860.0
private const val MIN_WINDOW_HEIGHT = 640.0

// AppKit keeps the size and position the user last left the window at under this name.
private const val WINDOW_FRAME_AUTOSAVE_NAME = "PebbleMainWindow"

// Records what the watch stack sees, so "no watches" can be told apart from "scan never started".
private fun log(message: String) {
    DebugLogs.add("[CoreApp] $message")
}

private fun CoroutineScope.logCollector(name: String, block: suspend CoroutineScope.() -> Unit) {
    log("collector started: $name")
    launch(block = block)
}

@androidx.compose.runtime.Composable
private fun WatchStackDiagnostics() {
    LaunchedEffect(Unit) {
        log("composition started")
        val koin = KoinPlatform.getKoinOrNull()
        log("koin=$koin")
        if (koin == null) return@LaunchedEffect
        val libPebble = runCatching { koin.get<LibPebble>() }
        log("libPebble=$libPebble")
        val pebble = libPebble.getOrNull() ?: return@LaunchedEffect
        log("bluetoothEnabled=${pebble.bluetoothEnabled.value} isScanningBle=${pebble.isScanningBle.value}")
        logCollector("bluetoothEnabled") {
            pebble.bluetoothEnabled.collect { state -> log("bluetoothEnabled=$state") }
        }
        logCollector("isScanningBle") {
            pebble.isScanningBle.collect { log("isScanningBle=$it") }
        }
        logCollector("watches") {
            pebble.watches.collect { watches ->
                log("watches=${watches.size} ${watches.map { "${it.identifier.asString}/${it.name}" }}")
            }
        }
        logCollector("connectionEvents") {
            pebble.connectionEvents.collect { log("connectionEvent=$it") }
        }
        log("watchesDebugState=${runCatching { pebble.watchesDebugState() }.getOrElse { "error: $it" }}")
    }
}

fun main() {
    log("main() start")
    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    Window(title = "Pebble", size = DpSize(DEFAULT_WINDOW_WIDTH.dp, DEFAULT_WINDOW_HEIGHT.dp)) {
        // Run once: SideEffect re-runs on every recomposition, which kept yanking the window
        // back to the centre whenever the UI updated.
        DisposableEffect(Unit) {
            window.setContentMinSize(CGSizeMake(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT))
            // Restores the frame the user left the window at; first run keeps the size above and
            // gets centred. Setting a fixed frame here fought this and made the window jump.
            val restored = window.setFrameUsingName(WINDOW_FRAME_AUTOSAVE_NAME)
            if (!restored) window.center()
            window.setFrameAutosaveName(WINDOW_FRAME_AUTOSAVE_NAME)
            window.makeKeyAndOrderFront(null)
            app.activateIgnoringOtherApps(true)
            log("window frame set (restored=$restored)")
            onDispose { }
        }
        WatchStackDiagnostics()
        MacosApp()
    }
    log("window created, entering run loop")
    app.run()
}

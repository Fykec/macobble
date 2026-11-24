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
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import org.koin.mp.KoinPlatform
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSHomeDirectory
import kotlin.time.Clock

// Temporary diagnostics for the macOS port: records what the watch stack sees, so "no watches"
// can be told apart from "scan never started". Written to ~/Library/Logs/CoreApp/macos.log so it
// is readable when the app is launched from Finder.
private val diagLogPath = Path("${NSHomeDirectory()}/Library/Logs/CoreApp/macos.log")

private fun log(message: String) {
    val line = "${Clock.System.now()} [CoreApp] $message"
    println(line)
    DebugLogs.add("[CoreApp] $message")
    runCatching {
        diagLogPath.parent?.let { SystemFileSystem.createDirectories(it) }
        SystemFileSystem.sink(diagLogPath, append = true).buffered().use { it.writeString("$line\n") }
    }
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
    Window(title = "CoreApp", size = DpSize(420.dp, 900.dp)) {
        // Run once: SideEffect re-runs on every recomposition, which kept yanking the window
        // back to the centre whenever the UI updated.
        DisposableEffect(Unit) {
            window.setFrame(CGRectMake(0.0, 0.0, 420.0, 900.0), display = true)
            window.center()
            window.makeKeyAndOrderFront(null)
            app.activateIgnoringOtherApps(true)
            log("window frame set")
            onDispose { }
        }
        WatchStackDiagnostics()
        MacosApp()
    }
    log("window created, entering run loop")
    app.run()
}

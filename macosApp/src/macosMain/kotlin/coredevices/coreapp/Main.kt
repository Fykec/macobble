@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package coredevices.coreapp

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.CoreGraphics.CGRectMake

fun main() {
    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    Window(title = "CoreApp", size = DpSize(420.dp, 900.dp)) {
        // The `size` argument doesn't reach the NSWindow, so set the frame explicitly.
        SideEffect {
            window.setFrame(CGRectMake(0.0, 0.0, 420.0, 900.0), display = true)
            window.center()
            window.makeKeyAndOrderFront(null)
            app.activateIgnoringOtherApps(true)
        }
        MacosApp()
    }
    // Window() only builds the scene; the run loop keeps the app alive.
    app.run()
}

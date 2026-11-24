@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package coredevices.coreapp.util

import platform.Foundation.NSHomeDirectory

// Logs live where Console.app and users expect them on macOS.
actual fun getLogsCacheDir(): String = "${NSHomeDirectory()}/Library/Logs/CoreApp"

actual fun generateDeviceSummaryPlatformDetails(): String = "macOS"

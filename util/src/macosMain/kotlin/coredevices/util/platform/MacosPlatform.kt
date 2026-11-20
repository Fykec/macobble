package coredevices.util.platform

import coredevices.util.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AppKit.NSWorkspace
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURL

class MacosPlatform : Platform {
    override val name: String = "macOS " + NSProcessInfo.processInfo.operatingSystemVersionString
    override val deviceModelName: String get() = platform.Foundation.NSHost.currentHost().localizedName ?: "Mac"

    override val previousRunCrashed: Boolean
        get() = false

    override suspend fun openUrl(url: String) {
        withContext(Dispatchers.Main) {
            val nsUrl = NSURL.URLWithString(url) ?: return@withContext
            NSWorkspace.sharedWorkspace.openURL(nsUrl)
        }
    }

    override suspend fun runWithBgTask(name: String, task: suspend () -> Unit) {
        // macOS has no background task assertion; run inline while the app is in the foreground.
        task()
    }
}

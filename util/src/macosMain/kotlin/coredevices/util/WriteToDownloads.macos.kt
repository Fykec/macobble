package coredevices.util

import PlatformUiContext
import kotlinx.io.files.Path
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory

// Copies the file to the user's Downloads folder (no picker UI on macOS v1).
actual suspend fun writeToDownloads(uiContext: PlatformUiContext, path: Path, mimeType: String) {
    val name = path.toString().substringAfterLast('/').ifBlank { "download" }
    val target = "${NSHomeDirectory()}/Downloads/$name"
    NSFileManager.defaultManager.copyItemAtPath(path.toString(), toPath = target, error = null)
}

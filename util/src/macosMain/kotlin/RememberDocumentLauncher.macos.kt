import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import DocumentAttachment
import coredevices.util.toBuffer
import platform.AppKit.NSModalResponseOK
import platform.AppKit.NSOpenPanel
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

// macOS v1: photo picking is not needed for any macOS flow.
@Composable
actual fun rememberOpenPhotoLauncher(onResult: (List<DocumentAttachment>?) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember { { currentOnResult(null) } }
}

@Composable
actual fun rememberOpenDocumentLauncher(
    onResult: (List<DocumentAttachment>?) -> Unit,
): (mimeTypeFilter: List<String>) -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        { _: List<String> ->
            val panel = NSOpenPanel.openPanel()
            panel.setCanChooseFiles(true)
            panel.setCanChooseDirectories(false)
            panel.setAllowsMultipleSelection(true)
            // .pbz has no registered type, so the caller's mime filters cannot narrow this down.
            if (panel.runModal() == NSModalResponseOK) {
                val attachments = panel.URLs
                    .filterIsInstance<NSURL>()
                    .mapNotNull { url -> runCatching { url.toDocumentAttachment() }.getOrNull() }
                currentOnResult(attachments.ifEmpty { null })
            } else {
                currentOnResult(null)
            }
        }
    }
}

private fun NSURL.toDocumentAttachment(): DocumentAttachment {
    val data = NSData.dataWithContentsOfURL(this) ?: error("Failed to read data from URL: $this")
    return DocumentAttachment(
        fileName = this.lastPathComponent ?: "",
        mimeType = null,
        source = data.toBuffer(),
        size = data.length.toLong(),
    )
}

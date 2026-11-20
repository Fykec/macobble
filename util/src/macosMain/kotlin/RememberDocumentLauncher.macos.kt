import androidx.compose.runtime.Composable
import DocumentAttachment

// macOS v1: no document/photo picker; both launchers report a cancelled selection.
@Composable
actual fun rememberOpenDocumentLauncher(onResult: (List<DocumentAttachment>?) -> Unit): (mimeTypeFilter: List<String>) -> Unit =
    { onResult(null) }

@Composable
actual fun rememberOpenPhotoLauncher(onResult: (List<DocumentAttachment>?) -> Unit): () -> Unit =
    { onResult(null) }

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.io.files.Path

// macOS v1: no share sheet; operations are no-ops.
actual class PlatformShareLauncher() {
    actual fun share(text: String?, file: Path) = Unit

    actual fun shareImage(image: ImageBitmap, filename: String) = Unit
}

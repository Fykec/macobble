package coredevices.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// macOS v1: no embedded webviews; the config page is not shown.
@Composable
actual fun PebbleWebview(
    url: String,
    interceptor: PebbleWebviewUrlInterceptor,
    modifier: Modifier,
    onPageFinishedJavaScript: String?,
    onPageError: ((message: String) -> Unit)?,
) = Unit

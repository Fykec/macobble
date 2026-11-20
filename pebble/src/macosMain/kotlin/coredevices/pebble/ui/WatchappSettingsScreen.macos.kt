package coredevices.pebble.ui

import androidx.compose.runtime.Composable
import com.multiplatform.webview.web.NativeWebView
import com.multiplatform.webview.web.PlatformWebViewParams
import com.multiplatform.webview.web.WebViewFactoryParam
import coredevices.pebble.config.ConfigPageBridge
import kotlin.uuid.Uuid

// macOS v1: config pages (embedded webviews) are not supported; all hooks are no-ops.

internal actual fun webViewFactory(
    params: WebViewFactoryParam,
    uuid: Uuid,
    bridge: ConfigPageBridge,
    mayLoad: (String) -> Boolean,
    network: ConfigPageNetwork,
): NativeWebView = NativeWebView()

internal actual class ConfigPageNetwork {
    actual val enforced: Boolean = false
}

internal actual suspend fun configPageNetwork(uuid: Uuid, domains: List<String>?): ConfigPageNetwork =
    ConfigPageNetwork()

internal actual suspend fun restoreLocalStorage(webView: NativeWebView) = Unit

internal actual fun persistLocalStorage(webView: NativeWebView) = Unit

@Composable
internal actual fun rememberPlatformWebViewParams(mayLoad: (String) -> Boolean): PlatformWebViewParams? = null

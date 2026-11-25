package coredevices.pebble.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.NativeWebView
import com.multiplatform.webview.web.PlatformWebViewParams
import com.multiplatform.webview.web.WebViewFactoryParam
import coredevices.pebble.config.ConfigPageBridge
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKContentRuleList
import kotlin.uuid.Uuid

// macOS hosts config pages itself (see ConfigPageSheet); the library has no macOS target, so the
// library-shaped hooks below stay inert.

internal actual class ConfigPageNetwork(val rules: WKContentRuleList?, actual val enforced: Boolean = true)

internal actual suspend fun configPageNetwork(uuid: Uuid, domains: List<String>?): ConfigPageNetwork {
    val granted = domains ?: return ConfigPageNetwork(null)
    val list = compileConfigPageRules(uuid, granted) ?: return ConfigPageNetwork(null, enforced = false)
    return ConfigPageNetwork(list)
}

internal actual fun configPageHostedNatively(): Boolean = true

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun NativeConfigPageHost(
    url: String,
    title: String,
    closeLabel: String,
    network: ConfigPageNetwork,
    bridge: ConfigPageBridge,
    onClosed: (String?) -> Unit,
    onLoadFailed: () -> Unit,
    openStoreAppLink: (String) -> Boolean,
    modifier: Modifier,
) {
    val closed by rememberUpdatedState(onClosed)
    val loadFailed by rememberUpdatedState(onLoadFailed)
    val storeLink by rememberUpdatedState(openStoreAppLink)
    DisposableEffect(url, network) {
        val sheet = ConfigPageSheet(
            url = url,
            title = title,
            closeLabel = closeLabel,
            network = network,
            bridge = bridge,
            onClosed = { closed(it) },
            onLoadFailed = { loadFailed() },
            openStoreAppLink = { storeLink(it) },
        )
        sheet.show()
        onDispose { sheet.dismiss() }
    }
}

internal actual fun webViewFactory(
    params: WebViewFactoryParam,
    uuid: Uuid,
    bridge: ConfigPageBridge,
    mayLoad: (String) -> Boolean,
    network: ConfigPageNetwork,
): NativeWebView = NativeWebView()

internal actual suspend fun restoreLocalStorage(webView: NativeWebView) = Unit

internal actual fun persistLocalStorage(webView: NativeWebView) = Unit

@Composable
internal actual fun rememberPlatformWebViewParams(mayLoad: (String) -> Boolean): PlatformWebViewParams? = null

package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.setting.WebSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class NativeWebView

public class PlatformWebViewParams

public class WebViewFactoryParam

public class WebViewJsBridge

public sealed class WebContent {
    public data class Url(
        val url: String,
        val additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) : WebContent()
}

public sealed class LoadingState {
    public data object Initializing : LoadingState()
    public data class Loading(val progress: Float) : LoadingState()
    public data object Finished : LoadingState()
}

public class WebViewState(webContent: WebContent) {
    public var content: WebContent = webContent
    public var loadingState: LoadingState = LoadingState.Initializing
    public var lastLoadedUrl: String? = null
    public var pageTitle: String? = null
    public var nativeWebView: NativeWebView? = null
    public val webSettings: WebSettings = WebSettings()
    public val isLoading: Boolean
        get() = loadingState !is LoadingState.Finished
}

@Composable
public fun rememberWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap(),
    extraSettings: WebSettings.() -> Unit = {},
): WebViewState = remember {
    WebViewState(WebContent.Url(url, additionalHttpHeaders)).apply {
        extraSettings(webSettings)
    }
}

public class WebViewNavigator internal constructor(
    public val navigationEvents: MutableSharedFlow<Unit> = MutableSharedFlow(),
    public val requestInterceptor: RequestInterceptor? = null,
) {
    public val lastLoadedUrl: StateFlow<String?> = MutableStateFlow(null)
    public fun loadUrl(url: String) = Unit
    public fun loadHtml(html: String) = Unit
    public fun reload() = Unit
    public fun goBack(): Boolean = false
    public fun goForward(): Boolean = false
    public fun stopLoading() = Unit
}

@Composable
public fun rememberWebViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    requestInterceptor: RequestInterceptor? = null,
): WebViewNavigator = remember(coroutineScope) { WebViewNavigator(requestInterceptor = requestInterceptor) }

@Composable
public fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    onCreated: () -> Unit = {},
    onDispose: () -> Unit = {},
    platformWebViewParams: PlatformWebViewParams? = null,
) = Unit

@Composable
public fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    onCreated: (NativeWebView) -> Unit = {},
    onDispose: (NativeWebView) -> Unit = {},
    platformWebViewParams: PlatformWebViewParams? = null,
    factory: ((WebViewFactoryParam) -> NativeWebView)? = null,
) = Unit

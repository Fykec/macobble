package com.multiplatform.webview.request

import com.multiplatform.webview.web.WebViewNavigator

public data class WebRequest(
    val url: String,
    val headers: MutableMap<String, String> = mutableMapOf(),
    val isForMainFrame: Boolean = false,
    val isRedirect: Boolean = false,
    val method: String = "GET",
)

public interface RequestInterceptor {
    public fun onInterceptUrlRequest(request: WebRequest, navigator: WebViewNavigator): WebRequestInterceptResult
}

public sealed interface WebRequestInterceptResult {
    public data object Allow : WebRequestInterceptResult
    public data object Reject : WebRequestInterceptResult
    public class Modify(val request: WebRequest) : WebRequestInterceptResult
}

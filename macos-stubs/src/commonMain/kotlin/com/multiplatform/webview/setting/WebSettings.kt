package com.multiplatform.webview.setting

public class WebSettings {
    public var isJavaScriptEnabled: Boolean = true
    public var userAgentString: String? = null
    public var javaScriptCanOpenWindowsAutomatically: Boolean = false
    public var allowFileAccess: Boolean = false
    public var allowContentAccess: Boolean = false
    public var domStorageEnabled: Boolean = false
    public var supportMultipleWindows: Boolean = false
    public var mediaPlaybackRequiresUserGesture: Boolean = true
    public var allowFileAccessFromFileURLs: Boolean = false
    public var allowUniversalAccessFromFileURLs: Boolean = false
    public var isInspectable: Boolean = false

    public val androidWebSettings: AndroidWebSettings = AndroidWebSettings()

    public val iOSWebSettings: IOSWebSettings = IOSWebSettings()
}

public class AndroidWebSettings {
    public var domStorageEnabled: Boolean = false
    public var javaScriptEnabled: Boolean = true
    public var userAgentString: String? = null
    public var javaScriptCanOpenWindowsAutomatically: Boolean = false
    public var allowFileAccess: Boolean = false
    public var allowContentAccess: Boolean = false
    public var mediaPlaybackRequiresUserGesture: Boolean = true
    public var allowFileAccessFromFileURLs: Boolean = false
    public var allowUniversalAccessFromFileURLs: Boolean = false
    public var supportMultipleWindows: Boolean = false
}

public class IOSWebSettings {
    public var isInspectable: Boolean = false
    public var allowsInlineMediaPlayback: Boolean = false
    public var mediaTypesRequiringUserActionForPlayback: String = ""
    public var allowsBackForwardNavigationGestures: Boolean = false
    public var allowsLinkPreview: Boolean = false
}

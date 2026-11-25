package coredevices.pebble.ui

import co.touchlab.kermit.Logger
import coredevices.pebble.config.CONFIG_NATIVE_NAMESPACE
import coredevices.pebble.config.CONFIG_PAGE_SHIM_JS
import coredevices.pebble.config.ConfigPageBridge
import io.ktor.http.decodeURLPart
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ObjCSignatureOverride
import platform.AppKit.NSApplication
import platform.AppKit.NSBackingStoreBuffered
import platform.AppKit.NSButton
import platform.AppKit.NSProgressIndicator
import platform.AppKit.NSView
import platform.AppKit.NSViewHeightSizable
import platform.AppKit.NSViewMinXMargin
import platform.AppKit.NSViewMinYMargin
import platform.AppKit.NSViewWidthSizable
import platform.AppKit.NSWindow
import platform.AppKit.NSWindowDelegateProtocol
import platform.AppKit.NSWindowStyleMaskClosable
import platform.AppKit.NSWindowStyleMaskResizable
import platform.AppKit.NSWindowStyleMaskTitled
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNotification
import platform.Foundation.NSError
import platform.Foundation.NSTimer
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebsiteDataStore
import platform.darwin.NSObject

private const val DATA_URL_PREFIX = "data:text/html;charset=utf-8,"
private const val CLOSE_URL_PREFIX = "pebblejs://close"
private const val CONTENT_WIDTH = 860.0
private const val CONTENT_HEIGHT = 720.0
// Room for the close button above the page, plus the loading bar at its foot.
private const val BAR_HEIGHT = 38.0
private const val PROGRESS_HEIGHT = 3.0

private const val ESTIMATED_PROGRESS_POLL_SECONDS = 0.1

// Same shapes the in-page interceptor accepts on the other platforms.
private val closeUrlRegex = Regex("""^pebblejs://close(?:#|/\?|/)(.*)$""")

private val noWebRtcScript =
    "['RTCPeerConnection', 'webkitRTCPeerConnection'].forEach(function (k) { try { delete globalThis[k]; } catch (e) {} });"

/**
 * A config page in a sheet over the app window.
 *
 * The webview library has no macOS target, so this drives WKWebView directly: the page gets WebKit's
 * content blocker for its network rules, the `Pebble` API arrives as an injected user script backed
 * by [ConfigPageBridge], and `pebblejs://close` comes back through the navigation delegate.
 */
@OptIn(ExperimentalForeignApi::class)
internal class ConfigPageSheet(
    private val url: String,
    private val title: String,
    private val closeLabel: String,
    private val network: ConfigPageNetwork,
    private val bridge: ConfigPageBridge,
    private val onClosed: (String?) -> Unit,
    private val onLoadFailed: () -> Unit,
    private val openStoreAppLink: (String) -> Boolean,
) : NSObject(), WKNavigationDelegateProtocol, NSWindowDelegateProtocol {
    private val logger = Logger.withTag("ConfigPageSheet")
    private var sheet: NSWindow? = null
    private var parent: NSWindow? = null
    private var webView: WKWebView? = null
    private var progressBar: NSProgressIndicator? = null
    private var progressTimer: NSTimer? = null
    private var closed = false

    fun show() {
        val parentWindow = NSApplication.sharedApplication.keyWindow
            ?: NSApplication.sharedApplication.mainWindow
        if (parentWindow == null) {
            logger.w { "no app window to attach the config page to" }
            failed()
            return
        }
        parent = parentWindow

        val configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = WKWebsiteDataStore.defaultDataStore()
        network.rules?.let { configuration.userContentController.addContentRuleList(it) }
        // A content blocker doesn't cover WebRTC.
        configuration.userContentController.addUserScript(
            WKUserScript(
                source = noWebRtcScript,
                injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
                forMainFrameOnly = false,
            )
        )
        // Installed before the page parses, so a page's own <script> can call Pebble.* at the top
        // level rather than waiting for something injected after load.
        configuration.userContentController.addUserScript(
            WKUserScript(
                source = CONFIG_PAGE_SHIM_JS,
                injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
                forMainFrameOnly = true,
            )
        )
        configuration.userContentController.addScriptMessageHandler(
            ConfigPageMessageHandler(bridge),
            CONFIG_NATIVE_NAMESPACE,
        )

        val webViewHeight = CONTENT_HEIGHT - BAR_HEIGHT - PROGRESS_HEIGHT
        val webView = WKWebView(frame = CGRectMake(0.0, 0.0, CONTENT_WIDTH, webViewHeight), configuration = configuration)
        webView.navigationDelegate = this
        webView.setAutoresizingMask(NSViewWidthSizable or NSViewHeightSizable)
        bridge.attach { js -> webView.evaluateJavaScript(js, null) }
        this.webView = webView

        // A sheet gets no traffic lights, so it carries its own way out.
        val content = NSView(frame = CGRectMake(0.0, 0.0, CONTENT_WIDTH, CONTENT_HEIGHT))
        content.addSubview(webView)

        // Loading progress, WKWebView's own estimate, hidden once the page is up.
        val progressBar = NSProgressIndicator()
        progressBar.setIndeterminate(false)
        progressBar.setMinValue(0.0)
        progressBar.setMaxValue(1.0)
        progressBar.setDoubleValue(0.0)
        progressBar.setFrame(
            CGRectMake(0.0, CONTENT_HEIGHT - BAR_HEIGHT - PROGRESS_HEIGHT, CONTENT_WIDTH, PROGRESS_HEIGHT)
        )
        progressBar.setAutoresizingMask(NSViewWidthSizable or NSViewMinYMargin)
        progressBar.setHidden(true)
        content.addSubview(progressBar)
        this.progressBar = progressBar
        val closeButton = NSButton.buttonWithTitle(
            closeLabel,
            target = this,
            action = NSSelectorFromString("closeTapped:"),
        )
        closeButton.setFrame(CGRectMake(CONTENT_WIDTH - 106.0, CONTENT_HEIGHT - BAR_HEIGHT + 6.0, 94.0, 26.0))
        closeButton.setAutoresizingMask(NSViewMinXMargin or NSViewMinYMargin)
        content.addSubview(closeButton)

        val sheetWindow = NSWindow(
            contentRect = CGRectMake(0.0, 0.0, CONTENT_WIDTH, CONTENT_HEIGHT),
            styleMask = NSWindowStyleMaskTitled or NSWindowStyleMaskClosable or NSWindowStyleMaskResizable,
            backing = NSBackingStoreBuffered,
            defer = false,
        )
        sheetWindow.title = title
        sheetWindow.contentView = content
        sheetWindow.delegate = this
        sheet = sheetWindow

        parentWindow.beginSheet(sheetWindow, completionHandler = null)

        val html = bundledPageHtml(url)
        if (html != null) {
            // A bundled page has no origin to resolve anything against.
            webView.loadHTMLString(html, baseURL = null)
        } else {
            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl == null) {
                logger.w { "config page url couldn't be parsed" }
                failed()
            } else {
                webView.loadRequest(NSURLRequest(nsUrl))
            }
        }
    }

    /** Called when the composable leaves; the page is going away either way. */
    fun dismiss() {
        if (closed) return
        closed = true
        detachSheet()
    }

    /** Dismissing without a `pebblejs://close` payload is a cancel: nothing reaches the watch. */
    @ObjCAction
    fun closeTapped(sender: NSButton?) {
        closedByPage(null)
    }

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val target = decidePolicyForNavigationAction.request.URL?.absoluteString
        if (target == null) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            return
        }
        if (target.startsWith(CLOSE_URL_PREFIX)) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            closedByPage(closeUrlRegex.find(target)?.groupValues?.get(1)?.decodeURLPart().orEmpty())
            return
        }
        if (openStoreAppLink(target)) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            dismiss()
            return
        }
        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?,
    ) {
        progressBar?.let {
            it.setDoubleValue(0.0)
            it.setHidden(false)
        }
        startProgressPolling()
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        stopProgressPolling()
        progressBar?.setHidden(true)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) = failed()

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError,
    ) = failed()

    override fun windowWillClose(notification: NSNotification) {
        closedByPage(null)
    }

    /** WKWebView only exposes its progress through KVO, which Kotlin/Native doesn't bind, so poll it. */
    private fun startProgressPolling() {
        progressTimer?.invalidate()
        progressTimer = NSTimer.scheduledTimerWithTimeInterval(
            ESTIMATED_PROGRESS_POLL_SECONDS,
            repeats = true,
        ) { _ ->
            val loaded = webView?.estimatedProgress ?: 1.0
            progressBar?.let {
                it.setDoubleValue(loaded)
                // It stays at 1.0 afterwards; the bar is only interesting while it climbs.
                it.setHidden(loaded >= 1.0)
            }
            if (loaded >= 1.0) stopProgressPolling()
        }
    }

    private fun stopProgressPolling() {
        progressTimer?.invalidate()
        progressTimer = null
    }

    /** The page handed back its configuration. */
    private fun closedByPage(payload: String?) {
        if (closed) return
        closed = true
        detachSheet()
        onClosed(payload?.takeIf { it.isNotEmpty() })
    }

    private fun failed() {
        if (closed) return
        closed = true
        detachSheet()
        onLoadFailed()
    }

    private fun detachSheet() {
        stopProgressPolling()
        webView = null
        progressBar = null
        val sheetWindow = sheet ?: return
        sheet = null
        sheetWindow.delegate = null
        parent?.endSheet(sheetWindow)
        parent = null
        sheetWindow.orderOut(null)
    }
}

/** `_PebbleConfigNative.call(json)` on the page side; WebKit posts it here. */
private class ConfigPageMessageHandler(
    private val bridge: ConfigPageBridge,
) : NSObject(), WKScriptMessageHandlerProtocol {
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        (didReceiveScriptMessage.body as? String)?.let { bridge.onCall(it) }
    }
}

/** The html of a `data:` url, or null when [url] is a normal address. */
private fun bundledPageHtml(url: String): String? =
    if (url.startsWith(DATA_URL_PREFIX)) url.removePrefix(DATA_URL_PREFIX).decodeURLPart() else null

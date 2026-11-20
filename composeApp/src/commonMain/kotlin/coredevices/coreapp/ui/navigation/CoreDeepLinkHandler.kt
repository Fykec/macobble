package coredevices.coreapp.ui.navigation

import CommonRoutes
import CoreRoute
import androidx.navigation.NavUri
import co.touchlab.kermit.Logger
import com.eygraber.uri.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CoreDeepLinkHandler {
    private val _navigateToDeepLink = MutableSharedFlow<Any>(extraBufferCapacity = 1, replay = 1)
    val navigateToDeepLink = _navigateToDeepLink.asSharedFlow()

    fun handle(uri: Uri): Boolean {
        logger.d { "handle: uri = $uri" }
        val target = routeFor(uri) ?: NavUri(uri.toString())
        return _navigateToDeepLink.tryEmit(target)
    }

    /**
     * Maps a `pebblecore://deep-link/<path>` URI to a typed route, so navigation doesn't depend
     * on a per-route deep link registration. Null for anything else, including a known path
     * with a missing or malformed id.
     */
    internal fun routeFor(uri: Uri): CoreRoute? {
        if (uri.scheme != SCHEME || uri.host != HOST) return null
        val id = uri.getQueryParameter(ID_PARAM)?.takeIf { it.isNotBlank() }
        val path = uri.pathSegments.firstOrNull() ?: return null
        return ringDeepLinkRoute(path, id) ?: when (path) {
            CommonRoutes.SPEECH_MODEL_DOWNLOAD_DEEP_LINK_PATH -> CommonRoutes.SpeechModelDownloadDialog
            else -> null
        }
    }

    fun clearPendingDeepLink() {
        _navigateToDeepLink.resetReplayCache()
    }

    companion object {
        private val logger = Logger.withTag("CoreDeepLinkHandler")
        private const val SCHEME = "pebblecore"
        private val HOST = ringDeepLinkHost
        private val ID_PARAM = ringDeepLinkIdParam
        private const val VIEW_BUG_REPORT_PATH = "view-bug-report"
        private const val CONVERSATION_ID_QUERY_PARAM = "conversationId"

        fun CommonRoutes.ViewBugReportRoute.asUri(): Uri = Uri.Builder()
            .scheme(SCHEME)
            .authority(HOST)
            .appendPath(VIEW_BUG_REPORT_PATH)
            .appendQueryParameter(CONVERSATION_ID_QUERY_PARAM, conversationId)
            .build()
    }
}

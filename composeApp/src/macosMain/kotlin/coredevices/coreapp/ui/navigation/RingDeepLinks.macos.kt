package coredevices.coreapp.ui.navigation

import CoreRoute

internal actual fun ringDeepLinkRoute(path: String, id: String?): CoreRoute? = null

internal actual val ringDeepLinkHost: String
    get() = "deep-link"

internal actual val ringDeepLinkIdParam: String
    get() = "id"

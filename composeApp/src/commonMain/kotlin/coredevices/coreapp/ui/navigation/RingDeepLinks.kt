package coredevices.coreapp.ui.navigation

import CoreRoute

internal expect fun ringDeepLinkRoute(path: String, id: String?): CoreRoute?

internal expect val ringDeepLinkHost: String

internal expect val ringDeepLinkIdParam: String

package coredevices.coreapp.ui.navigation

import CoreRoute
import coredevices.ring.ui.navigation.RingRoutes

internal actual fun ringDeepLinkRoute(path: String, id: String?): CoreRoute? = when (path) {
    RingRoutes.OBJECT_DEEP_LINK_PATH -> id?.let { RingRoutes.ObjectDetails(it) }
    RingRoutes.RECORDING_DEEP_LINK_PATH -> id?.toLongOrNull()?.let { RingRoutes.RecordingDetails(it) }
    RingRoutes.SETTINGS_DEEP_LINK_PATH -> RingRoutes.Settings
    RingRoutes.TIMERS_ALARMS_DEEP_LINK_PATH -> RingRoutes.TimersAndAlarms
    else -> null
}

internal actual val ringDeepLinkHost: String
    get() = RingRoutes.OBJECT_DEEP_LINK_HOST

internal actual val ringDeepLinkIdParam: String
    get() = RingRoutes.OBJECT_DEEP_LINK_ID_PARAM

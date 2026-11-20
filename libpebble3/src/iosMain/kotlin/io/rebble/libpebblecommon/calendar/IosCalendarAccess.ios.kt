package io.rebble.libpebblecommon.calendar

import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStore
import platform.Foundation.NSError
import platform.UIKit.UIDevice

internal actual fun EKEventStore.requestEventsAccess(completion: (Boolean, NSError?) -> Unit) {
    val majorVersion =
        UIDevice.currentDevice.systemVersion.split(".").firstOrNull()?.toIntOrNull() ?: 0
    if (majorVersion >= 17) {
        requestFullAccessToEventsWithCompletion(completion)
    } else {
        requestAccessToEntityType(EKEntityType.EKEntityTypeEvent, completion)
    }
}

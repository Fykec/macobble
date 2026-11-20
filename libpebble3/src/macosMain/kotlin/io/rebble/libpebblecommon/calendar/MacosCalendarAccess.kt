package io.rebble.libpebblecommon.calendar

import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStore
import platform.Foundation.NSError

// macOS EventKit only has the entity-type access API.
internal actual fun EKEventStore.requestEventsAccess(completion: (Boolean, NSError?) -> Unit) {
    requestAccessToEntityType(EKEntityType.EKEntityTypeEvent, completion)
}

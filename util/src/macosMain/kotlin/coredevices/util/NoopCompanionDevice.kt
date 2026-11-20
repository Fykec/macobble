package coredevices.util

import PlatformUiContext
import coredevices.libindex.device.IndexIdentifier
import io.rebble.libpebblecommon.connection.PebbleIdentifier

// macOS has no CompanionDeviceManager; registration is a no-op.
class NoopCompanionDevice : CompanionDevice {
    override suspend fun registerDevice(
        identifier: IndexIdentifier,
        uiContext: PlatformUiContext,
        useClassicAssociation: Boolean,
    ) = Unit

    override suspend fun registerDevice(identifier: PebbleIdentifier, uiContext: PlatformUiContext) = Unit

    override fun hasApprovedDevice(identifier: PebbleIdentifier): Boolean = false

    override fun hasApprovedDevice(identifier: IndexIdentifier): Boolean = false

    override fun canRemoveBond(identifier: IndexIdentifier): Boolean = false

    override fun removeBond(identifier: IndexIdentifier): Boolean = false

    override fun cdmPreviouslyCrashed(): Boolean = false
}

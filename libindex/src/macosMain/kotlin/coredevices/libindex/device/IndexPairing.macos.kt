package coredevices.libindex.device

import io.rebble.libpebblecommon.connection.AppContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// macOS: no ring pairing support in v1; pairing always fails.
actual suspend fun createBond(context: AppContext, identifier: IndexIdentifier): Boolean = false

actual fun getBluetoothDevicePairEvents(
    context: AppContext,
    identifier: IndexIdentifier,
): Flow<BluetoothDevicePairEvent> = emptyFlow()

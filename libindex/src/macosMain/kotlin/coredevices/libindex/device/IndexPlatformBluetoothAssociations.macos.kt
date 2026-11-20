package coredevices.libindex.device

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

actual class IndexPlatformBluetoothAssociations {
    actual val associations: StateFlow<List<IndexAssociation>?> = MutableStateFlow(null)
    actual val bondStateChanges: Flow<IndexBondStateUpdate> = emptyFlow()

    actual fun init(bluetoothPermissionChanged: Flow<Boolean>) = Unit

    actual fun warnIfNoCompanionAssociations(): Unit = Unit

    actual companion object {
        actual val isEnabled: Boolean = false
    }
}

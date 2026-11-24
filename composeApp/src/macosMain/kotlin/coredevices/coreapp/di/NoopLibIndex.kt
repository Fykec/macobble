package coredevices.coreapp.di

import coredevices.libindex.IndexDevices
import coredevices.libindex.LibIndex
import coredevices.libindex.device.IndexDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * macOS does not support the Index ring, but shared screens (e.g. WatchHomeViewModel) still
 * resolve LibIndex, so they get an implementation that never finds anything.
 */
class NoopLibIndex : LibIndex {
    override val isScanning: StateFlow<Boolean> = MutableStateFlow(false)
    override val rings: IndexDevices = MutableStateFlow<List<IndexDevice>>(emptyList())

    override fun init(bluetoothPermissionChanged: Flow<Boolean>) = Unit

    override fun startScan() = Unit

    override fun stopScan() = Unit

    override fun warnIfNoCompanionAssociations() = Unit
}

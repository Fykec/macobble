package io.rebble.libpebblecommon.connection.bt.ble.transport.impl

import com.juul.kable.Advertisement
import com.juul.kable.FilterPredicateBuilder
import com.juul.kable.Identifier
import io.rebble.libpebblecommon.BleConfig
import io.rebble.libpebblecommon.BleConfigFlow
import io.rebble.libpebblecommon.connection.BleScanResult
import io.rebble.libpebblecommon.connection.PebbleBleIdentifier
import io.rebble.libpebblecommon.connection.bt.ble.pebble.LEConstants.UUIDs.PAIRING_SERVICE_UUID
import io.rebble.libpebblecommon.connection.bt.ble.transport.BleScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

expect fun kableBleScanner(bleConfigFlow: BleConfigFlow): BleScanner

/**
 * Must run before anything touches Kable's shared central manager (any scan,
 * [com.juul.kable.Peripheral] creation, or Bluetooth-state collection) — Kable
 * throws if its central has already been initialised.
 */
expect fun configureKableCentral(stateRestoration: Boolean)

internal expect fun createKableAdvertisementsFlow(bleConfig: BleConfig): Flow<Advertisement>

fun FilterPredicateBuilder.applyServiceFilter(bleConfig: BleConfig) {
    if (bleConfig.filterScanResultsByUuid) {
        services = listOf(PAIRING_SERVICE_UUID)
    }
}

class KableBleScanner(
    private val bleConfigFlow: BleConfigFlow,
) : BleScanner {
    override fun scan(): Flow<BleScanResult> {
        return createKableAdvertisementsFlow(bleConfigFlow.value)
            .mapNotNull {
                // The Pebble scan record in the manufacturer data is what identifies a watch;
                // the local name is only used for display and is often missing on macOS.
                val manufacturerData = it.manufacturerData ?: return@mapNotNull null
                BleScanResult(
                    identifier = it.identifier.asPebbleBleIdentifier(),
                    name = it.name ?: UNNAMED_WATCH_NAME,
                    rssi = it.rssi,
                    manufacturerData = manufacturerData
                )
            }
    }
}

private const val UNNAMED_WATCH_NAME = "Pebble Watch"

expect fun Identifier.asPebbleBleIdentifier(): PebbleBleIdentifier

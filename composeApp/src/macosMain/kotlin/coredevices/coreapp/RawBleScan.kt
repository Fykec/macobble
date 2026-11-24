@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package coredevices.coreapp

import platform.CoreBluetooth.CBAdvertisementDataLocalNameKey
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.NSNumber
import platform.darwin.NSObject

private var rawCentral: CBCentralManager? = null
private var rawDelegate: RawScanDelegate? = null
private var fed9Central: CBCentralManager? = null
private var fed9Delegate: Fed9ScanDelegate? = null

private const val PEBBLE_PAIRING_UUID = "0000FED9-0000-1000-8000-00805F9B34FB"

/**
 * Debug aid: scans with no service filter and logs every advertisement, so it is visible whether
 * a watch is reachable at all and which advertisement fields the Pebble scanner needs are missing.
 */
fun startRawBleScan() {
    if (rawCentral != null) return
    val delegate = RawScanDelegate()
    rawDelegate = delegate
    rawCentral = CBCentralManager(delegate = delegate, queue = null)
    val fed9 = Fed9ScanDelegate()
    fed9Delegate = fed9
    fed9Central = CBCentralManager(delegate = fed9, queue = null)
}

private class Fed9ScanDelegate : NSObject(), CBCentralManagerDelegateProtocol {
    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        DebugLogs.add("[BLE] fed9-filter state=${central.state}")
        if (central.state == CBManagerStatePoweredOn) {
            val uuid = CBUUID.UUIDWithString(PEBBLE_PAIRING_UUID)
            central.scanForPeripheralsWithServices(listOf(uuid), null)
            DebugLogs.add("[BLE] fed9-filter scan started")
        }
    }

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber,
    ) {
        val name = didDiscoverPeripheral.name
            ?: advertisementData[CBAdvertisementDataLocalNameKey] as? String
        val manufacturer = (advertisementData[CBAdvertisementDataManufacturerDataKey] as? NSData)?.description
        DebugLogs.add("[BLE] FED9 HIT id=${didDiscoverPeripheral.identifier.UUIDString} name=${name ?: "(nil)"} rssi=$RSSI mfr=${manufacturer ?: "-"}")
    }
}

private class RawScanDelegate : NSObject(), CBCentralManagerDelegateProtocol {
    private val seen = mutableSetOf<String>()

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        DebugLogs.add("[BLE] raw state=${central.state}")
        if (central.state == CBManagerStatePoweredOn) {
            central.scanForPeripheralsWithServices(null, null)
            DebugLogs.add("[BLE] raw scan started, no service filter")
        }
    }

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber,
    ) {
        val identifier = didDiscoverPeripheral.identifier.UUIDString
        if (!seen.add(identifier)) return
        val name = didDiscoverPeripheral.name
            ?: advertisementData[CBAdvertisementDataLocalNameKey] as? String
        val services = (advertisementData[CBAdvertisementDataServiceUUIDsKey] as? List<*>)
            ?.joinToString(",") { it.toString() }
        val manufacturer = (advertisementData[CBAdvertisementDataManufacturerDataKey] as? NSData)?.description
        DebugLogs.add(
            "[BLE] name=${name ?: "(nil)"} rssi=$RSSI services=${services ?: "-"} mfr=${manufacturer ?: "-"}"
        )
    }
}

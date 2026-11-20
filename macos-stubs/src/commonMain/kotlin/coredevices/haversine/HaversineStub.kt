package coredevices.haversine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// macOS stub: no satellite (ring) support on macOS; scanning finds nothing.
// Signatures mirror the real haversine-kmp API so call sites typecheck unchanged.

public class KMPHaversineAdvertisement {
    public companion object {
        public fun parseToStateFingerprint(data: ByteArray): String? = null
    }
}

public fun fingerprintMatchesFailsafe(fingerprint: String): Boolean = false

public fun fingerprintMatchesPTF(fingerprint: String): Boolean = false

public class KMPHaversineSatelliteState {
    public val name: String? = null
    public val timestamp: kotlin.time.Instant = kotlin.time.Instant.fromEpochSeconds(0)
    public val rssi: Float? = null
    public val firmwareVersion: String = ""
    public val serialNumber: String = ""
    public val programmedSerialNumber: String? = null
}

public data class KMPRSSIMeasurement(
    public val rxRSSI: Float = 0f,
    public val phoneRSSI: Float? = null,
)

public class KMPHaversineSatellite {
    public val id: String = ""
    public val name: String? = null
    public val state: StateFlow<KMPHaversineSatelliteState?> = MutableStateFlow(null)
    public val lastAdvertisement: KMPHaversineAdvertisement? = null
    public fun eraseCollections() = Unit
    public fun forceFailsafe() = Unit
    public fun measureRSSI(
        averagingCount: Int,
        connectionTimeoutSeconds: UInt? = null,
    ): KMPRSSIMeasurement = KMPRSSIMeasurement()
    public fun restartPreemptiveTransfer() = Unit
}

public class KMPHaversineSatelliteManager {
    public val lastRing: Flow<KMPHaversineSatellite?> = kotlinx.coroutines.flow.emptyFlow()
    public fun notifyPairedSatelliteChanged() = Unit
    public fun getSatelliteById(satelliteId: String): KMPHaversineSatellite? = null
    public fun ingestAdvertisement(
        satelliteId: String,
        name: String?,
        rssi: Int,
        manufacturerData: ByteArray,
    ): KMPHaversineSatellite = KMPHaversineSatellite()
}

public interface KMPHaversineHacksDelegate {
    public fun wipedCollectionsBeforeTransfer(satellite: KMPHaversineSatellite)
}

public interface CollectionIndexStorage {
    public val lastSuccessfulCollectionIndex: StateFlow<Int?>
    public fun setLastSuccessfulCollectionIndex(index: Int?)
}

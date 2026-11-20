package dev.jordond.compass.geocoder

import dev.jordond.compass.Coordinates
import dev.jordond.compass.Place
import dev.jordond.compass.autocomplete.AutocompleteOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** macOS stub: no mobile geocoding backend on macOS. */
public fun MobileGeocoder(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    locale: String? = null,
): Geocoder = NoopGeocoder

public fun Geocoder.Companion.mobile(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    locale: String? = null,
): Geocoder = NoopGeocoder

private object NoopGeocoder : Geocoder {
    override val platformGeocoder: PlatformGeocoder = object : PlatformGeocoder {
        override fun isAvailable(): Boolean = false
        override suspend fun forward(address: String): List<Coordinates> = emptyList()
        override suspend fun reverse(latitude: Double, longitude: Double): List<Place> = emptyList()
    }

    override fun isAvailable(): Boolean = false

    override suspend fun forward(address: String): GeocoderResult<Coordinates> =
        GeocoderResult.GeocodeFailed("No geocoder on macOS")

    override suspend fun reverse(latitude: Double, longitude: Double): GeocoderResult<Place> =
        GeocoderResult.GeocodeFailed("No geocoder on macOS")
}

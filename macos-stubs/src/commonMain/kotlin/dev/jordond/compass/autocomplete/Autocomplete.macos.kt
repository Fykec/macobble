package dev.jordond.compass.autocomplete

import dev.jordond.compass.Place
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** macOS stub: no mobile autocomplete backend on macOS; always returns empty results. */
public fun Autocomplete.Companion.mobile(
    options: AutocompleteOptions = AutocompleteOptions(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): Autocomplete<Place> = object : Autocomplete<Place> {
    override val options: AutocompleteOptions = options

    override suspend fun search(query: String): AutocompleteResult<Place> =
        AutocompleteResult.Success(emptyList())
}

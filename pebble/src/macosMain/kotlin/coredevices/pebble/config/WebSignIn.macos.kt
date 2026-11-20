package coredevices.pebble.config

import PlatformUiContext

// macOS v1: no embedded web sign-in.
internal actual suspend fun performWebSignIn(
    uiContext: PlatformUiContext,
    request: WebSignInRequest,
): Map<String, String>? = null

internal actual fun webSignInSupported(): Boolean = false

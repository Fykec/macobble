package coredevices.pebble.config

import PlatformUiContext

// macOS v1: no file picker; the picker is treated as cancelled.
internal actual suspend fun pickPluginFile(
    uiContext: PlatformUiContext,
    request: FilePickRequest,
): PickedPluginEntry? = null

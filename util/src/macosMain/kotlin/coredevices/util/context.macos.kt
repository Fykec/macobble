package coredevices.util

import PlatformUiContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun getAndroidActivity(): Any? = null

// Permissions are granted in System Settings on macOS, but the UI still needs a non-null context
// to render the onboarding/permission screens at all.
@Composable
actual fun rememberUiContext(): PlatformUiContext? = remember { PlatformUiContext() }

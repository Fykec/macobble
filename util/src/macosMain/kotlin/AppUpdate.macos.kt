package coredevices.coreapp.util

import PlatformUiContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MacosAppUpdate : AppUpdate {
    override val updateAvailable: StateFlow<AppUpdateState> =
        MutableStateFlow(AppUpdateState.NoUpdateAvailable)

    override fun startUpdateFlow(uiContext: PlatformUiContext, update: AppUpdatePlatformContent) = Unit
}

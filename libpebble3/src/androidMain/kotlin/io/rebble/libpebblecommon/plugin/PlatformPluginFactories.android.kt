package io.rebble.libpebblecommon.plugin

import io.rebble.libpebblecommon.connection.AppContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private object PlatformPluginFactories : KoinComponent {
    val appContext: AppContext by lazy { get<AppContext>() }
}

actual fun createPhoneBatteryMonitor(): PhoneBatteryMonitor =
    PhoneBatteryMonitor(PlatformPluginFactories.appContext)

actual fun createPhoneSoundPlayer(): PhoneSoundPlayer =
    PhoneSoundPlayer(PlatformPluginFactories.appContext)

actual fun createPhoneNetworkMonitor(): PhoneNetworkMonitor =
    PhoneNetworkMonitor(PlatformPluginFactories.appContext)

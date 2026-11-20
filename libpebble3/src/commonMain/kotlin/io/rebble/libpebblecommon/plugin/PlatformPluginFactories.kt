package io.rebble.libpebblecommon.plugin

// Constructor references to expect classes can't be used from intermediate source sets
// (appleMain), so platform instances are created via factory functions in leaf source sets.
expect fun createPhoneBatteryMonitor(): PhoneBatteryMonitor

expect fun createPhoneSoundPlayer(): PhoneSoundPlayer

expect fun createPhoneNetworkMonitor(): PhoneNetworkMonitor

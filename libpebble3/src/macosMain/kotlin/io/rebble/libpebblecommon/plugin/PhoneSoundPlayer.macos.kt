package io.rebble.libpebblecommon.plugin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AppKit.NSSound

// Mac system alert sound, used for "find my phone".
private const val SYSTEM_ALARM_SOUND = "/System/Library/Sounds/Glass.aiff"

actual class PhoneSoundPlayer {
    private val _isRinging = MutableStateFlow(false)
    actual val isRinging: Flow<Boolean> = _isRinging.asStateFlow()

    private var sound: NSSound? = null

    actual fun startRinging() {
        if (_isRinging.value) return
        val newSound = NSSound(contentsOfFile = SYSTEM_ALARM_SOUND, byReference = true)
        if (newSound != null) {
            sound = newSound
            newSound.loops = true
            newSound.play()
        }
        _isRinging.value = true
    }

    actual fun stopRinging() {
        sound?.stop()
        sound = null
        _isRinging.value = false
    }
}

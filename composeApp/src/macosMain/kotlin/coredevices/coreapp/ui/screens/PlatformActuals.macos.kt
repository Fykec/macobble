package coredevices.coreapp.ui.screens

import androidx.compose.ui.text.AnnotatedString
import coredevices.util.Permission

actual fun startForegroundService() = Unit

actual fun notifyState(message: String) = Unit

actual fun stopForegroundService() = Unit

actual fun isThirdPartyTest(): Boolean = false

actual fun getExperimentalDebugInfoDirectory(): String = "/tmp"

actual fun Permission.descriptionOnboarding(): AnnotatedString = AnnotatedString("")

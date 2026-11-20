package coredevices.coreapp.auth

import PlatformUiContext
import coredevices.util.auth.AppleAuthUtil
import coredevices.util.auth.GitHubAuthUtil
import coredevices.util.auth.GoogleAuthUtil
import dev.gitlive.firebase.auth.AuthCredential

// macOS v1: no sign-in providers.

actual class RealAppleAuthUtil : AppleAuthUtil {
    actual override suspend fun signInApple(context: PlatformUiContext): AuthCredential? = null
}

actual class RealGithubAuthUtil : GitHubAuthUtil {
    actual override suspend fun signInGithub(context: PlatformUiContext): AuthCredential? = null
}

actual class RealGoogleAuthUtil : GoogleAuthUtil {
    override suspend fun signInGoogle(context: PlatformUiContext): AuthCredential? = null
}

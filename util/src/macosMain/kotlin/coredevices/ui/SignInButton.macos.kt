package coredevices.ui

import dev.gitlive.firebase.auth.AuthCredential

// macOS v1: no Firebase sign-in.
internal actual suspend fun signInWithCredential(credential: AuthCredential) = Unit

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public val Firebase.auth: FirebaseAuth
    get() = FirebaseAuth()

public fun Firebase.auth(app: FirebaseApp): FirebaseAuth = FirebaseAuth()

public class FirebaseAuth {
    public val currentUser: FirebaseUser? = null
    public val authStateChanged: Flow<FirebaseUser?> = flowOf(null)
    public val idTokenChanged: Flow<FirebaseUser?> = flowOf(null)
    public var languageCode: String = "en"

    public suspend fun signInAnonymously(): AuthResult = AuthResult()
    public suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult = AuthResult()
    public suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult = AuthResult()
    public suspend fun signInWithCredential(authCredential: AuthCredential): AuthResult = AuthResult()
    public suspend fun signInWithCustomToken(token: String): AuthResult = AuthResult()
    public suspend fun signInWithEmailLink(email: String, link: String): AuthResult = AuthResult()
    public suspend fun signOut() = Unit
    public suspend fun updateCurrentUser(user: FirebaseUser) = Unit
    public suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings? = null) = Unit
    public suspend fun fetchSignInMethodsForEmail(email: String): List<String> = emptyList()
    public suspend fun applyActionCode(code: String) = Unit
    public suspend fun confirmPasswordReset(code: String, newPassword: String) = Unit
    public suspend fun verifyPasswordResetCode(code: String): String = ""
    public suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings) = Unit
    public fun isSignInWithEmailLink(link: String): Boolean = false
    public fun useEmulator(host: String, port: Int) = Unit
}

public class FirebaseUser {
    public val uid: String = ""
    public val email: String? = null
    public val emailOrNull: String? = null
    public val displayName: String? = null
    public val phoneNumber: String? = null
    public val photoUrl: String? = null
    public val isAnonymous: Boolean = false
    public val isEmailVerified: Boolean = false
    public val providerId: String = ""
    public val metadata: UserMetadata = UserMetadata()
    public val providerData: List<UserInfo> = emptyList()
    public suspend fun getIdToken(forceRefresh: Boolean): String = ""
    public suspend fun delete() = Unit
    public suspend fun reload() = Unit
    public suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings? = null) = Unit
    public suspend fun updateEmail(email: String) = Unit
    public suspend fun updatePassword(password: String) = Unit
    public suspend fun updateProfile(displayName: String?, photoUrl: String?) = Unit
    public suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult()
    public suspend fun unlink(providerId: String): FirebaseUser = this
    public suspend fun reauthenticate(credential: AuthCredential) = Unit
}

public class UserMetadata {
    public val creationTimestamp: Long = 0L
    public val lastSignInTimestamp: Long = 0L
}

public class UserInfo {
    public val uid: String = ""
    public val providerId: String = ""
    public val displayName: String? = null
    public val photoUrl: String? = null
    public val phoneNumber: String? = null
    public val email: String? = null
}

public class AuthResult {
    public val user: FirebaseUser? = null
    public val credential: AuthCredential? = null
    public val additionalUserInfo: AdditionalUserInfo? = null
}

public class AdditionalUserInfo {
    public val providerId: String? = null
    public val username: String? = null
    public val isNewUser: Boolean = false
    public val profile: Map<String, Any?>? = null
}

public class ActionCodeSettings {
    public var url: String = ""
    public var iOSBundleId: String? = null
    public var androidPackageName: String? = null
    public var androidInstallApp: Boolean = false
    public var androidMinimumVersion: String? = null
    public var handleCodeInApp: Boolean = false
    public var dynamicLinkDomain: String? = null
}

public open class AuthCredential {
    public open val providerId: String = ""
    public open val signInMethod: String? = null
}

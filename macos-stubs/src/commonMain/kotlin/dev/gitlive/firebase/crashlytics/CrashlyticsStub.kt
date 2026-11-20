package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

public val Firebase.crashlytics: FirebaseCrashlytics
    get() = FirebaseCrashlytics()

public fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics = FirebaseCrashlytics()

public class FirebaseCrashlytics {
    public fun recordException(exception: Throwable) = Unit
    public fun recordException(exception: Throwable, customKeys: Map<String, Any>) = Unit
    public fun log(message: String) = Unit
    public fun setCustomKey(key: String, value: String) = Unit
    public fun setCustomKey(key: String, value: Boolean) = Unit
    public fun setCustomKey(key: String, value: Double) = Unit
    public fun setCustomKey(key: String, value: Float) = Unit
    public fun setCustomKey(key: String, value: Int) = Unit
    public fun setCustomKey(key: String, value: Long) = Unit
    public fun setCustomKeys(customKeys: Map<String, Any>) = Unit
    public fun setUserId(userId: String) = Unit
    public fun setCrashlyticsCollectionEnabled(enabled: Boolean) = Unit
    public fun setAnalyticsCollectionEnabled(enabled: Boolean) = Unit
}

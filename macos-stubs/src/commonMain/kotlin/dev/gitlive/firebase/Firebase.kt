package dev.gitlive.firebase

/** macOS stub: Firebase is never initialized on macOS; all operations are no-ops. */
public object Firebase

public class FirebaseApp

public class FirebaseOptions

public open class FirebaseException(message: String, cause: Throwable? = null) : Exception(message, cause)

public sealed interface EncodeDecodeSettings {
    public val serializersModule: kotlinx.serialization.modules.SerializersModule
}

public interface EncodeSettings : EncodeDecodeSettings {
    public val encodeDefaults: Boolean

    public interface Builder {
        public var encodeDefaults: Boolean
        public var serializersModule: kotlinx.serialization.modules.SerializersModule
    }
}

public interface DecodeSettings : EncodeDecodeSettings {
    public interface Builder {
        public var serializersModule: kotlinx.serialization.modules.SerializersModule
    }
}

public interface EncodeDecodeSettingsBuilder : EncodeSettings.Builder, DecodeSettings.Builder

public val Firebase.app: FirebaseApp
    get() = FirebaseApp()

public fun Firebase.app(name: String): FirebaseApp = FirebaseApp()

public fun Firebase.apps(context: Any? = null): List<FirebaseApp> = emptyList()

public fun Firebase.initialize(context: Any? = null): FirebaseApp? = null

public fun Firebase.initialize(context: Any? = null, options: FirebaseOptions): FirebaseApp = FirebaseApp()

public fun Firebase.initialize(context: Any? = null, options: FirebaseOptions, name: String): FirebaseApp =
    FirebaseApp()

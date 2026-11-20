package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

public val Firebase.firestore: FirebaseFirestore
    get() = FirebaseFirestore()

public fun Firebase.firestore(app: FirebaseApp, databaseId: String? = null): FirebaseFirestore =
    FirebaseFirestore()

public class FirebaseFirestore {
    public var settings: FirebaseFirestoreSettings = FirebaseFirestoreSettings()
    public fun collection(collectionPath: String): CollectionReference = CollectionReference(collectionPath)
    public fun collectionGroup(collectionId: String): Query = Query()
    public fun document(documentPath: String): DocumentReference = DocumentReference(documentPath)
    public fun batch(): WriteBatch = WriteBatch()
    public fun setLoggingEnabled(loggingEnabled: Boolean) = Unit
    public suspend fun clearPersistence() = Unit
    public suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T = Transaction().func()
    public fun useEmulator(host: String, port: Int) = Unit
    public fun setSettings(settings: FirebaseFirestoreSettings) = Unit
    public suspend fun disableNetwork() = Unit
    public suspend fun enableNetwork() = Unit
    public suspend fun terminate() = Unit
    public suspend fun waitForPendingWrites() = Unit
}

public class FirebaseFirestoreSettings {
    public val sslEnabled: Boolean = true
    public val host: String = "firestore.googleapis.com"
    public val cacheSettings: LocalCacheSettings = PersistentCacheSettings()

    public class Builder {
        public var sslEnabled: Boolean = true
        public var host: String = "firestore.googleapis.com"
        public var cacheSettings: LocalCacheSettings = PersistentCacheSettings()
        public fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings()
    }

    public companion object {
        public val CACHE_SIZE_UNLIMITED: Long = -1
        public val DEFAULT_CACHE_SIZE_BYTES: Long = 100 * 1024 * 1024
    }
}

public fun firestoreSettings(
    settings: FirebaseFirestoreSettings? = null,
    builder: FirebaseFirestoreSettings.Builder.() -> Unit = {},
): FirebaseFirestoreSettings = settings ?: FirebaseFirestoreSettings.Builder().apply(builder).build()

public open class LocalCacheSettings

public class PersistentCacheSettings : LocalCacheSettings() {
    public class Builder internal constructor() {
        public var sizeBytes: Long = FirebaseFirestoreSettings.DEFAULT_CACHE_SIZE_BYTES
        public fun build(): PersistentCacheSettings = PersistentCacheSettings()
    }

    public companion object {
        public fun newBuilder(): Builder = Builder()
    }
}

public fun persistentCacheSettings(builder: PersistentCacheSettings.Builder.() -> Unit): PersistentCacheSettings =
    PersistentCacheSettings.newBuilder().apply(builder).build()

public class MemoryCacheSettings : LocalCacheSettings()

public class CollectionReference(
    public val id: String = "",
    public val path: String = "",
) : Query() {
    public fun document(documentPath: String = ""): DocumentReference = DocumentReference(documentPath)
    public suspend fun add(data: Any, encodeDefaults: Boolean = false): DocumentReference =
        DocumentReference()
    public suspend fun <T : Any> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = false): DocumentReference =
        DocumentReference()
    public suspend inline fun <reified T : Any> add(data: T, encodeDefaults: Boolean = false): DocumentReference =
        DocumentReference()
}

public open class Query {
    public val snapshots: Flow<QuerySnapshot> = emptyFlow()
    public fun snapshots(includeMetadataChanges: Boolean = false): Flow<QuerySnapshot> = emptyFlow()
    public suspend fun get(source: Source = Source.DEFAULT): QuerySnapshot = QuerySnapshot()
    public suspend fun count(): Long = 0
    public suspend fun sum(field: String): Double = 0.0
    public suspend fun sum(field: FieldPath): Double = 0.0
    public suspend fun average(field: String): Double? = null
    public suspend fun average(field: FieldPath): Double? = null
    public fun where(builder: FilterBuilder.() -> Filter?): Query = this
    public fun orderBy(field: String, direction: Direction = Direction.ASCENDING): Query = this
    public fun orderBy(field: FieldPath, direction: Direction = Direction.ASCENDING): Query = this
    public fun limit(limit: Number): Query = this
    public fun limitToLast(limit: Number): Query = this
    public fun startAfter(document: DocumentSnapshot): Query = this
    public fun startAfter(vararg fieldValues: Any?): Query = this
    public fun startAfter(vararg fieldValues: Any?, buildSettings: EncodeSettings.Builder.() -> Unit): Query = this
    public fun startAt(document: DocumentSnapshot): Query = this
    public fun startAt(vararg fieldValues: Any?): Query = this
    public fun startAt(vararg fieldValues: Any?, buildSettings: EncodeSettings.Builder.() -> Unit): Query = this
    public fun endBefore(document: DocumentSnapshot): Query = this
    public fun endBefore(vararg fieldValues: Any?): Query = this
    public fun endBefore(vararg fieldValues: Any?, buildSettings: EncodeSettings.Builder.() -> Unit): Query = this
    public fun endAt(document: DocumentSnapshot): Query = this
    public fun endAt(vararg fieldValues: Any?): Query = this
    public fun endAt(vararg fieldValues: Any?, buildSettings: EncodeSettings.Builder.() -> Unit): Query = this
    public suspend fun updateFields(builder: FieldsAndValuesUpdateDSL.() -> Unit) = Unit
}

public class DocumentReference(
    public val id: String = "",
    public val path: String = "",
) {
    public val snapshots: Flow<DocumentSnapshot> = emptyFlow()
    public fun snapshots(includeMetadataChanges: Boolean = false): Flow<DocumentSnapshot> = emptyFlow()
    public fun collection(collectionPath: String): CollectionReference = CollectionReference(collectionPath)
    public suspend fun get(source: Source = Source.DEFAULT): DocumentSnapshot = DocumentSnapshot()
    public suspend fun set(data: Any, encodeDefaults: Boolean = false, merge: Boolean = false) = Unit
    public suspend inline fun <reified T : Any> set(data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = Unit
    public suspend inline fun <reified T : Any> set(data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = Unit
    public suspend fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = false, merge: Boolean = false) = Unit
    public suspend inline fun <reified T : Any> set(data: T, encodeDefaults: Boolean = false, merge: Boolean = false) = Unit
    public suspend fun set(data: Any, encodeDefaults: Boolean = false, vararg mergeFields: String) = Unit
    public suspend fun set(data: Any, encodeDefaults: Boolean = false, vararg mergeFieldPaths: FieldPath) = Unit
    public suspend fun update(data: Map<String, Any?>, encodeDefaults: Boolean = false) = Unit
    public suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) = Unit
    public suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) = Unit
    public suspend fun updateFields(builder: FieldsAndValuesUpdateDSL.() -> Unit) = Unit
    public suspend fun delete() = Unit
}

public class QuerySnapshot {
    public val documents: List<DocumentSnapshot> = emptyList()
    public val documentChanges: List<DocumentChange> = emptyList()
    public val metadata: SnapshotMetadata = SnapshotMetadata()
    public val size: Int get() = documents.size
    public fun isEmpty(): Boolean = documents.isEmpty()
}

public enum class ServerTimestampBehavior {
    NONE,
    ESTIMATE,
    PREVIOUS,
}

public class DocumentSnapshot {
    public val id: String = ""
    public val exists: Boolean = false
    public val reference: DocumentReference = DocumentReference()
    public val metadata: SnapshotMetadata = SnapshotMetadata()
    public fun contains(field: String): Boolean = false
    public fun contains(fieldPath: FieldPath): Boolean = false
    public inline fun <reified T> data(
        serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE,
        buildSettings: DecodeSettings.Builder.() -> Unit = {},
    ): T = throw IllegalStateException("Firestore not supported on macOS")
    public inline fun <reified T> get(
        field: String,
        serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE,
        buildSettings: DecodeSettings.Builder.() -> Unit = {},
    ): T = throw IllegalStateException("Firestore not supported on macOS")
}

public class SnapshotMetadata {
    public val hasPendingWrites: Boolean = false
    public val isFromCache: Boolean = false
}

public enum class ChangeType {
    ADDED,
    MODIFIED,
    REMOVED,
}

public class DocumentChange {
    public val document: DocumentSnapshot = DocumentSnapshot()
    public val newIndex: Int = 0
    public val oldIndex: Int = 0
    public val type: ChangeType = ChangeType.ADDED
}

public class FieldValue {
    public companion object {
        public val serverTimestamp: FieldValue = FieldValue()
        public val delete: FieldValue = FieldValue()
        public fun increment(value: Int): FieldValue = FieldValue()
        public fun increment(value: Double): FieldValue = FieldValue()
        public fun arrayUnion(vararg elements: Any): FieldValue = FieldValue()
        public fun arrayRemove(vararg elements: Any): FieldValue = FieldValue()
    }
}

public class FieldPath {
    public constructor(field: String)
    public constructor(vararg segments: String)
    public val encoded: String = ""
    public companion object {
        public fun documentId(): FieldPath = FieldPath("__name__")
    }
}

public enum class Direction {
    ASCENDING,
    DESCENDING,
}

public enum class Source {
    DEFAULT,
    SERVER,
    CACHE,
}

public class NativeTimestamp

public sealed class BaseTimestamp

public class Timestamp : BaseTimestamp {
    public constructor(seconds: Long, nanoseconds: Int) : super() {
        this.seconds = seconds
        this.nanoseconds = nanoseconds
    }

    public var seconds: Long = 0
    public var nanoseconds: Int = 0

    public companion object {
        public fun now(): Timestamp = Timestamp(0, 0)
    }

    public data object ServerTimestamp : BaseTimestamp()
}

public fun Timestamp.Companion.fromDuration(duration: kotlin.time.Duration): Timestamp {
    var seconds = 0L
    var nanoseconds = 0
    duration.toComponents { s, n ->
        seconds = s
        nanoseconds = n
    }
    return Timestamp(seconds, nanoseconds)
}

public fun Timestamp.toDuration(): kotlin.time.Duration =
    seconds.seconds + nanoseconds.nanoseconds

public fun Timestamp.Companion.fromMilliseconds(milliseconds: Double): Timestamp =
    fromDuration(milliseconds.milliseconds)

public fun Timestamp.toMilliseconds(): Double =
    toDuration().toDouble(kotlin.time.DurationUnit.MILLISECONDS)

public class GeoPoint {
    public constructor(latitude: Double, longitude: Double)
    public val latitude: Double = 0.0
    public val longitude: Double = 0.0
}

public class FirebaseFirestoreException(
    message: String = "",
    cause: Throwable? = null,
    public val code: FirestoreExceptionCode = FirestoreExceptionCode.UNKNOWN,
) : FirebaseException(message, cause)

public val FirebaseFirestoreException.code: FirestoreExceptionCode
    get() = FirestoreExceptionCode.UNKNOWN

public enum class FirestoreExceptionCode {
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED,
}

public sealed class Filter {
    public data class Field(val field: String, val value: Any?) : Filter()
    public data class Path(val path: FieldPath, val value: Any?) : Filter()
    public interface WithConstraint
}

public class FilterBuilder : EncodeSettings.Builder {
    public override var encodeDefaults: Boolean = false
    public override var serializersModule: kotlinx.serialization.modules.SerializersModule =
        kotlinx.serialization.modules.EmptySerializersModule()

    public inline infix fun <reified T> String.equalTo(value: T): Filter = Filter.Field(this, value)
    public inline infix fun <reified T> FieldPath.equalTo(value: T): Filter = Filter.Path(this, value)
    public inline infix fun <reified T> String.notEqualTo(value: T): Filter = Filter.Field(this, value)
    public inline infix fun <reified T> FieldPath.notEqualTo(value: T): Filter = Filter.Path(this, value)
    public inline infix fun <reified T : Any> String.lessThan(value: T): Filter = Filter.Field(this, value)
    public inline infix fun <reified T : Any> FieldPath.lessThan(value: T): Filter = Filter.Path(this, value)
    public inline infix fun <reified T : Any> String.greaterThan(value: T): Filter = Filter.Field(this, value)
    public inline infix fun <reified T : Any> FieldPath.greaterThan(value: T): Filter = Filter.Path(this, value)
    public inline infix fun <reified T : Any> String.lessThanOrEqualTo(value: T): Filter = Filter.Field(this, value)
    public inline infix fun <reified T : Any> FieldPath.lessThanOrEqualTo(value: T): Filter = Filter.Path(this, value)
    public inline infix fun <reified T : Any> String.greaterThanOrEqualTo(value: T): Filter = Filter.Field(this, value)
    public inline infix fun <reified T : Any> FieldPath.greaterThanOrEqualTo(value: T): Filter = Filter.Path(this, value)
}

public class FieldsAndValuesUpdateDSL internal constructor() : EncodeSettings.Builder {
    public override var encodeDefaults: Boolean = false
    public override var serializersModule: kotlinx.serialization.modules.SerializersModule =
        kotlinx.serialization.modules.EmptySerializersModule()

    public inline infix fun <reified T> String.to(value: T) = Unit
    public inline infix fun <reified T> FieldPath.to(value: T) = Unit
}

public class WriteBatch {
    public fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = false, merge: Boolean = false): WriteBatch = this
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = false, merge: Boolean = false): WriteBatch = this
    public fun update(documentRef: DocumentReference, data: Map<String, Any?>, encodeDefaults: Boolean = false): WriteBatch = this
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): WriteBatch = this
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): WriteBatch = this
    public fun delete(documentRef: DocumentReference): WriteBatch = this
    public suspend fun commit() = Unit
}

public class Transaction {
    public fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = false, merge: Boolean = false): Transaction = this
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = false, merge: Boolean = false): Transaction = this
    public fun update(documentRef: DocumentReference, data: Map<String, Any?>, encodeDefaults: Boolean = false): Transaction = this
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): Transaction = this
    public fun delete(documentRef: DocumentReference): Transaction = this
    public suspend fun get(documentRef: DocumentReference): DocumentSnapshot = DocumentSnapshot()
}

public fun Query.where(field: String, equalTo: Any?): Query = this
public fun Query.where(path: FieldPath, equalTo: Any?): Query = this
public fun Query.where(field: String, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query = this
public fun Query.where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query = this
public fun Query.where(field: String, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null): Query = this
public fun Query.where(path: FieldPath, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null): Query = this

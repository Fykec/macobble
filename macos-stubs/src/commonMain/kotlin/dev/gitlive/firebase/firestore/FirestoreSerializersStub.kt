package dev.gitlive.firebase.firestore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// macOS stubs: Firestore is never initialized on macOS, so these serializers only need to
// exist with the right names/shapes. BaseTimestampSerializer deliberately throws so callers
// fall through to their non-Firestore decode paths.
public object BaseTimestampSerializer : KSerializer<BaseTimestamp> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BaseTimestampSerializer") {
        element("epochSeconds", kotlin.Long.serializer().descriptor)
        element("nanosecondsOfSecond", kotlin.Int.serializer().descriptor, isOptional = true)
    }

    override fun deserialize(decoder: Decoder): BaseTimestamp =
        throw SerializationException("Firestore not supported on macOS")

    override fun serialize(encoder: Encoder, value: BaseTimestamp) =
        throw SerializationException("Firestore not supported on macOS")
}

public object TimestampSerializer : KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TimestampSerializer") {
        element("epochSeconds", kotlin.Long.serializer().descriptor)
        element("nanosecondsOfSecond", kotlin.Int.serializer().descriptor, isOptional = true)
    }

    override fun deserialize(decoder: Decoder): Timestamp =
        throw SerializationException("Firestore not supported on macOS")

    override fun serialize(encoder: Encoder, value: Timestamp) =
        throw SerializationException("Firestore not supported on macOS")
}

public object ServerTimestampSerializer : KSerializer<Timestamp.ServerTimestamp> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ServerTimestampSerializer") {
    }

    override fun deserialize(decoder: Decoder): Timestamp.ServerTimestamp =
        throw SerializationException("Firestore not supported on macOS")

    override fun serialize(encoder: Encoder, value: Timestamp.ServerTimestamp) =
        throw SerializationException("Firestore not supported on macOS")
}

public object DoubleAsTimestampSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DoubleAsTimestampSerializer") {
    }

    override fun deserialize(decoder: Decoder): Double =
        throw SerializationException("Firestore not supported on macOS")

    override fun serialize(encoder: Encoder, value: Double) =
        throw SerializationException("Firestore not supported on macOS")
}

public object DocumentReferenceSerializer : KSerializer<DocumentReference> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DocumentReferenceSerializer") {
    }

    override fun deserialize(decoder: Decoder): DocumentReference =
        throw SerializationException("Firestore not supported on macOS")

    override fun serialize(encoder: Encoder, value: DocumentReference) =
        throw SerializationException("Firestore not supported on macOS")
}

public object GeoPointSerializer : KSerializer<GeoPoint> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("GeoPointSerializer") {
        element("latitude", kotlin.Double.serializer().descriptor)
        element("longitude", kotlin.Double.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): GeoPoint =
        throw SerializationException("Firestore not supported on macOS")

    override fun serialize(encoder: Encoder, value: GeoPoint) =
        throw SerializationException("Firestore not supported on macOS")
}

public object FieldValueSerializer : KSerializer<FieldValue> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FieldValueSerializer") {
    }

    override fun deserialize(decoder: Decoder): FieldValue =
        throw SerializationException("Firestore not supported on macOS")

    override fun serialize(encoder: Encoder, value: FieldValue) =
        throw SerializationException("Firestore not supported on macOS")
}

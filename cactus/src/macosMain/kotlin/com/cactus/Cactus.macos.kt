package com.cactus

// macOS: no cactus native binaries; every entry point is a no-op. `isCactusSupported()`
// is false so callers take their existing fallback paths.

actual fun isCactusSupported(): Boolean = false

actual fun cactusSetBackend(backend: String): Int = -1

actual fun cactusInit(modelPath: String, corpusDir: String?, cacheIndex: Boolean): Long = 0L

actual fun cactusDestroy(handle: Long) = Unit

actual fun cactusReset(handle: Long) = Unit

actual fun cactusStop(handle: Long) = Unit

actual fun cactusComplete(
    handle: Long,
    messagesJson: String,
    optionsJson: String?,
    toolsJson: String?,
    callback: CactusTokenCallback?,
    pcmData: ByteArray?,
): String = ""

actual fun cactusPrefill(
    handle: Long,
    messagesJson: String,
    optionsJson: String?,
    toolsJson: String?,
    pcmData: ByteArray?,
): String = ""

actual fun cactusTokenize(handle: Long, text: String): IntArray = intArrayOf()

actual fun cactusScoreWindow(handle: Long, tokens: IntArray, start: Long, end: Long, context: Long): String = ""

actual fun cactusTranscribe(
    handle: Long,
    audioPath: String?,
    prompt: String,
    optionsJson: String?,
    callback: CactusTokenCallback?,
    pcmData: ByteArray?,
): String = ""

actual fun cactusStreamTranscribeStart(handle: Long, optionsJson: String?): Long = 0L

actual fun cactusStreamTranscribeProcess(stream: Long, pcmData: ByteArray?): String = ""

actual fun cactusStreamTranscribeStop(stream: Long): String = ""

actual fun cactusEmbed(handle: Long, text: String, normalize: Boolean): FloatArray = floatArrayOf()

actual fun cactusImageEmbed(handle: Long, imagePath: String): FloatArray = floatArrayOf()

actual fun cactusAudioEmbed(handle: Long, audioPath: String): FloatArray = floatArrayOf()

actual fun cactusRagQuery(handle: Long, query: String, topK: Long): String = ""

actual fun cactusIndexInit(indexDir: String, embeddingDim: Long): Long = 0L

actual fun cactusIndexAdd(
    handle: Long,
    ids: IntArray,
    documents: Array<String>,
    metadatas: Array<String>?,
    embeddings: Array<FloatArray>,
    embeddingDim: Long,
): Int = -1

actual fun cactusIndexDelete(handle: Long, ids: IntArray): Int = -1

actual fun cactusIndexGet(handle: Long, ids: IntArray): String = ""

actual fun cactusIndexQuery(handle: Long, embedding: FloatArray, optionsJson: String?): String = ""

actual fun cactusIndexCompact(handle: Long): Int = -1

actual fun cactusIndexDestroy(handle: Long) = Unit

actual fun cactusGetLastError(): String = "cactus not supported on macOS"

actual fun cactusLogSetLevel(level: Int) = Unit

actual fun cactusLogSetCallback(callback: CactusLogCallback?) = Unit

actual fun cactusSetTelemetryEnvironment(framework: String?, cacheLocation: String?, version: String?) = Unit

actual fun cactusSetAppId(appId: String) = Unit

actual fun cactusTelemetryFlush() = Unit

actual fun cactusTelemetryShutdown() = Unit

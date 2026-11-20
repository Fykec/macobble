package coredevices.util.transcription

// macOS has no local models (the Cactus native library ships iOS binaries only).
class NoopCactusModelPathProvider : CactusModelPathProvider {
    override suspend fun getSTTModelPath(modelName: String, version: String): String =
        throw UnsupportedOperationException("Local models are not supported on macOS")

    override suspend fun getLMModelPath(): String =
        throw UnsupportedOperationException("Local models are not supported on macOS")

    override fun isModelDownloaded(modelName: String): Boolean = false

    override fun getDownloadedModels(): List<String> = emptyList()

    override fun getIncompatibleModels(): List<String> = emptyList()

    override fun deleteModel(modelName: String) = Unit

    override fun getModelSizeBytes(modelName: String): Long = 0L

    override fun initTelemetry() = Unit
}

package coredevices.coreapp.ui.screens

import DocumentAttachment
import kotlinx.io.files.Path

internal actual suspend fun ringRebootLogs(): List<Pair<String, String>> = emptyList()

internal actual suspend fun exportRingOutput(id: String): List<DocumentAttachment> = emptyList()

internal actual suspend fun exportRecentRingRecordings(limit: Int): List<DocumentAttachment> = emptyList()

internal actual fun ringBadCollectionsDir(): Path? = null

package coredevices.coreapp.ui.screens

import DocumentAttachment
import kotlinx.io.files.Path

internal expect suspend fun ringRebootLogs(): List<Pair<String, String>>

internal expect suspend fun exportRingOutput(id: String): List<DocumentAttachment>

internal expect suspend fun exportRecentRingRecordings(limit: Int): List<DocumentAttachment>

internal expect fun ringBadCollectionsDir(): Path?

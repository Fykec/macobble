package coredevices.coreapp.ui.screens

import DocumentAttachment
import coredevices.ExperimentalDevices
import kotlinx.io.files.Path
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private object RingBugReportData : KoinComponent {
    val experimentalDevices: ExperimentalDevices by lazy { get() }
}

internal actual suspend fun ringRebootLogs(): List<Pair<String, String>> =
    RingBugReportData.experimentalDevices.rebootLog().map {
        it.timestamp.toString() to it.reason
    }

internal actual suspend fun exportRingOutput(id: String): List<DocumentAttachment> =
    RingBugReportData.experimentalDevices.exportOutput(id)

internal actual suspend fun exportRecentRingRecordings(limit: Int): List<DocumentAttachment> =
    RingBugReportData.experimentalDevices.exportRecentRecordings(limit)

internal actual fun ringBadCollectionsDir(): Path? =
    RingBugReportData.experimentalDevices.badCollectionsDir()

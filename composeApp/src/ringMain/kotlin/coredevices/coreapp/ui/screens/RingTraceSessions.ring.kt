package coredevices.coreapp.ui.screens

import coredevices.ring.util.trace.TraceSessionExporter
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform

internal actual suspend fun exportRingTraceSessionsJson(): ByteArray? {
    val exporter = KoinPlatform.getKoin().getOrNull<TraceSessionExporter>() ?: return null
    val sessions = exporter.exportLastNSessions(20)
    if (sessions.isEmpty()) return null
    return Json.encodeToString(sessions).encodeToByteArray()
}

package coredevices.coreapp.ui.screens

import coredevices.ring.util.trace.TraceSessionExporter
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent

private object RingTraceSessions : KoinComponent {
    fun export(): ByteArray? {
        val exporter = getOrNull<TraceSessionExporter>() ?: return null
        val sessions = exporter.exportLastNSessions(20)
        if (sessions.isEmpty()) return null
        return Json.encodeToString(sessions).encodeToByteArray()
    }
}

internal actual suspend fun exportRingTraceSessionsJson(): ByteArray? = RingTraceSessions.export()

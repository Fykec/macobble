package coredevices.coreapp

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

/**
 * In-memory log ring buffer backing the on-screen log panel. Debug aid for the macOS port;
 * the file log in ~/Library/Logs/CoreApp stays the durable record.
 */
object DebugLogs {
    private const val MAX_LINES = 500

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    fun add(message: String) {
        val timestamp = Clock.System.now().toString().substring(11, 23)
        val entry = "$timestamp $message"
        println("[DBG] $entry")
        _lines.update { current ->
            if (current.size >= MAX_LINES) current.subList(1, current.size) + entry else current + entry
        }
    }

    fun clear() {
        _lines.value = emptyList()
    }
}

class DebugLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        DebugLogs.add("${severity.name.first()} $tag: $message")
    }
}

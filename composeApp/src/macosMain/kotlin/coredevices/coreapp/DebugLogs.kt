package coredevices.coreapp

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import platform.Foundation.NSHomeDirectory
import kotlin.time.Clock

/**
 * Debug aid for the macOS port: a log ring buffer for the on-screen panel, mirrored to
 * ~/Library/Logs/CoreApp/macos.log so it is readable when the app is launched from Finder.
 */
object DebugLogs {
    private const val MAX_LINES = 500
    private const val LOG_FILE = "macos.log"

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val pendingWrites = Channel<String>(Channel.UNLIMITED)
    private var writeLoopStarted = false

    /** Must be called once before [add]; Koin construction alone should not start background work. */
    fun startWriting() {
        if (writeLoopStarted) return
        writeLoopStarted = true
        scope.launch {
            val path = Path("${NSHomeDirectory()}/Library/Logs/CoreApp/$LOG_FILE")
            runCatching {
                path.parent?.let { SystemFileSystem.createDirectories(it) }
                val sink = SystemFileSystem.sink(path, append = true).buffered()
                try {
                    for (line in pendingWrites) {
                        sink.writeString(line)
                        sink.writeString("\n")
                        // Flush once the burst is drained instead of on every line.
                        if (pendingWrites.isEmpty) sink.flush()
                    }
                } finally {
                    sink.close()
                }
            }
        }
    }

    fun add(message: String) {
        val entry = "${Clock.System.now().toString().substring(11, 23)} $message"
        println("[DBG] $entry")
        pendingWrites.trySend(entry)
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

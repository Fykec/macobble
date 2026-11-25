package coredevices.util.transcription

import coredevices.analytics.CoreAnalytics
import kotlinx.coroutines.TimeoutCancellationException

const val TRANSCRIPTION_SUCCESS_EVENT = "transcription.success"
const val TRANSCRIPTION_FAILURE_EVENT = "transcription.failure"

fun CoreAnalytics.logTranscriptionSuccess(service: String) {
    logEvent(TRANSCRIPTION_SUCCESS_EVENT, mapOf("service" to service))
}

fun CoreAnalytics.logTranscriptionFailure(service: String, reason: String, desc: String? = null) {
    logEvent(TRANSCRIPTION_FAILURE_EVENT, mapOf("service" to service, "reason" to reason, "desc" to (desc?.take(128) ?: "<none>")))
}

fun transcriptionFailureReason(e: Throwable): String = when (e) {
    is TranscriptionException.NotEnoughMemory -> "not_enough_memory" // not-localized
    is TranscriptionException.TranscriptionServiceUnavailable -> "service_unavailable" // not-localized
    is TranscriptionException.TranscriptionNetworkError -> "network_error" // not-localized
    is TranscriptionException.TranscriptionRequiresDownload -> "requires_download" // not-localized
    is TranscriptionException.NoSupportedLanguage -> "no_supported_language" // not-localized
    is TranscriptionException.NoSpeechDetected -> "no_speech_${e.type}" // not-localized
    is TranscriptionException.TranscriptionServiceError -> "service_error" // not-localized
    is TimeoutCancellationException -> "timeout"
    else -> e::class.simpleName ?: "unknown"
}
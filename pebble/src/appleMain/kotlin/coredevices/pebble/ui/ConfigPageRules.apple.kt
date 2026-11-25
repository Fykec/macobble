package coredevices.pebble.ui

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import platform.WebKit.WKContentRuleList
import platform.WebKit.WKContentRuleListStore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.uuid.Uuid

/**
 * WebKit content blocker: every http(s) and ws(s) load is blocked except to [domains] and their
 * subdomains. WKWebView can't intercept http(s) itself, so this is what keeps a config page to the
 * hosts the app was granted.
 *
 * Returns null when WebKit refused to compile the rules; the caller must then not open the page.
 */
@OptIn(ExperimentalForeignApi::class)
internal suspend fun compileConfigPageRules(uuid: Uuid, domains: List<String>): WKContentRuleList? {
    val rules = buildJsonArray {
        listOf("^https?://", "^wss?://").forEach { scheme ->
            add(configPageRule(scheme, "block"))
            domains.forEach { domain ->
                add(configPageRule(configPageUrlFilter(scheme, domain), "ignore-previous-rules"))
            }
        }
    }
    val store = WKContentRuleListStore.defaultStore() ?: return null
    return suspendCoroutine { cont ->
        store.compileContentRuleListForIdentifier("config-$uuid", rules.toString()) { list, error ->
            if (error != null) {
                Logger.withTag("configPageRules").w { "Couldn't compile rules: ${error.localizedDescription}" }
            }
            cont.resume(list)
        }
    }
}

private fun configPageRule(urlFilter: String, action: String) = buildJsonObject {
    putJsonObject("trigger") { put("url-filter", urlFilter) }
    putJsonObject("action") { put("type", action) }
}

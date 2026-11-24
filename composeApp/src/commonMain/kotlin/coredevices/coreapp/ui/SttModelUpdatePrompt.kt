package coredevices.coreapp.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mmk.kmpnotifier.notification.NotifierManager
import com.russhwolf.settings.Settings
import coredevices.coreapp.STT_MODE_BEFORE_UPDATE_KEY
import coredevices.coreapp.STT_UPDATE_NOTIFICATION_ID
import coredevices.coreapp.STT_UPDATE_WATCH_KEY
import coredevices.ui.M3Dialog
import coredevices.util.CommonBuildKonfig
import coredevices.util.CoreConfigHolder
import coredevices.util.WatchNotifier
import coredevices.util.models.CactusSTTMode
import coredevices.util.models.ModelDownloadStatus
import coredevices.util.models.ModelManager
import coredevices.util.transcription.CactusModelPathProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import coreapp.composeapp.generated.resources.Res as AppRes
import coreapp.composeapp.generated.resources.cancel
import coreapp.composeapp.generated.resources.downloading_the_updated_model_this_may
import coreapp.composeapp.generated.resources.later
import coreapp.composeapp.generated.resources.the_download_didn_t_finish_check
import coreapp.composeapp.generated.resources.we_ve_improved_on_device_voice

@Composable
fun SttModelUpdatePrompt() {
    val modelProvider: CactusModelPathProvider = koinInject()
    val modelManager: ModelManager = koinInject()
    val watchNotifier: WatchNotifier = koinInject()
    val configHolder: CoreConfigHolder = koinInject()
    val settings: Settings = koinInject()
    val scope = rememberCoroutineScope()
    val sttModel = CommonBuildKonfig.CACTUS_STT_MODEL

    var needsUpdate by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val downloadStatus by modelManager.modelDownloadStatus.collectAsState()

    LaunchedEffect(Unit) {
        needsUpdate = withContext(Dispatchers.IO) { sttModel in modelProvider.getIncompatibleModels() }
    }

    LaunchedEffect(downloadStatus) {
        if (!downloading) return@LaunchedEffect
        when (downloadStatus) {
            is ModelDownloadStatus.Idle -> {
                val stillStale = withContext(Dispatchers.IO) { sttModel in modelProvider.getIncompatibleModels() }
                if (stillStale) {
                    failed = true
                } else {
                    val restored = CactusSTTMode.fromId(
                        settings.getInt(STT_MODE_BEFORE_UPDATE_KEY, CactusSTTMode.RemoteOnly.id)
                    )
                    configHolder.update(
                        configHolder.config.value.copy(
                            sttConfig = configHolder.config.value.sttConfig.copy(
                                mode = restored,
                                modelName = sttModel,
                            )
                        )
                    )
                    settings.remove(STT_MODE_BEFORE_UPDATE_KEY)
                    needsUpdate = false
                }
                downloading = false
            }
            is ModelDownloadStatus.Failed -> {
                failed = true
                downloading = false
            }
            else -> {}
        }
    }

    if (!needsUpdate) return

    fun startDownload() {
        failed = false
        runCatching {
            NotifierManager.getLocalNotifier().remove(STT_UPDATE_NOTIFICATION_ID)
        }
        watchNotifier.remove(STT_UPDATE_WATCH_KEY)
        scope.launch {
            val info = modelManager.getAvailableSTTModels().firstOrNull { it.slug == sttModel }
            if (info != null && modelManager.downloadSTTModel(info, allowMetered = true)) {
                downloading = true
            } else {
                failed = true
            }
        }
    }

    M3Dialog(
        onDismissRequest = { if (!downloading) needsUpdate = false },
        properties = DialogProperties(
            dismissOnBackPress = !downloading,
            dismissOnClickOutside = !downloading,
        ),
        icon = { Icon(Icons.Outlined.CloudDownload, contentDescription = null) },
        title = {
            Text(
                when {
                    downloading -> "Updating voice model"
                    failed -> "Update failed"
                    else -> "Voice model update"
                }
            )
        },
        buttons = {
            if (downloading) {
                TextButton(onClick = {
                    modelManager.cancelDownload()
                    downloading = false
                }) { Text(stringResource(AppRes.string.cancel)) }
            } else {
                TextButton(onClick = { needsUpdate = false }) { Text(stringResource(AppRes.string.later)) }
                TextButton(onClick = { startDownload() }) { Text(if (failed) "Retry" else "Update") }
            }
        },
    ) {
        when {
            downloading -> {
                Text(stringResource(AppRes.string.downloading_the_updated_model_this_may))
                Spacer(Modifier.height(24.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            failed -> Text(stringResource(AppRes.string.the_download_didn_t_finish_check))
            else -> Text(stringResource(AppRes.string.we_ve_improved_on_device_voice))
        }
    }
}

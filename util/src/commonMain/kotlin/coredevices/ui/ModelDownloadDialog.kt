package coredevices.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import co.touchlab.kermit.Logger
import coredevices.util.transcription.CactusModelPathProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import coreapp.util.generated.resources.Res
import coreapp.util.generated.resources.models_download_explanation
import coreapp.util.generated.resources.cancel
import coreapp.util.generated.resources.download
import coreapp.util.generated.resources.download_required
import coreapp.util.generated.resources.downloading_models
import coreapp.util.generated.resources.this_might_take_a_few_minutes

sealed interface ModelType {
    val modelName: String
    data class STT(override val modelName: String) : ModelType
    data class Agent(override val modelName: String) : ModelType
}

@Composable
fun ModelDownloadDialog(
    onDismissRequest: (success: Boolean) -> Unit,
    models: Set<ModelType>
) {
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    val modelProvider: CactusModelPathProvider = koinInject()

    fun onCancel() {
        job?.cancel()
        onDismissRequest(false)
    }

    fun onDownload() {
        if (job?.isActive == true) return
        job = scope.launch(Dispatchers.IO) {
            try {
                models.forEach { model ->
                    when (model) {
                        is ModelType.STT -> modelProvider.getSTTModelPath(model.modelName)
                        is ModelType.Agent -> modelProvider.getLMModelPath()
                    }
                }
                onDismissRequest(true)
            } catch (e: Exception) {
                Logger.e("ModelDownloadDialog", e) { "Error downloading models" }
                onDismissRequest(false)
            }
        }
    }
    M3Dialog(
        modifier = Modifier.animateContentSize(),
        properties = DialogProperties(
            dismissOnBackPress = job == null,
            dismissOnClickOutside = job == null
        ),
        onDismissRequest = ::onCancel,
        icon = {
            Icon(Icons.Outlined.CloudDownload, contentDescription = null)
        },
        title = {
            if (job == null) {
                Text(stringResource(Res.string.download_required))
            } else {
                Text(stringResource(Res.string.downloading_models))
            }
        },
        buttons = if (job == null) {
            {
                TextButton(
                    onClick = ::onCancel
                ) {
                    Text(stringResource(Res.string.cancel))
                }
                TextButton(
                    onClick = ::onDownload
                ) {
                    Text(stringResource(Res.string.download))
                }
            }
        } else {
            {
                TextButton(
                    onClick = ::onCancel
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        }
    ) {
        if (job == null) {
            Text(stringResource(Res.string.models_download_explanation))
        } else {
            Text(stringResource(Res.string.this_might_take_a_few_minutes))
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}
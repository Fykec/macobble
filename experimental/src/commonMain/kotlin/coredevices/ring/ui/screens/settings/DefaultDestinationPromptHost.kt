package coredevices.ring.ui.screens.settings

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coredevices.ring.agent.integrations.providers.DefaultDestinationPrompter
import coredevices.ui.M3Dialog
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import coreapp.ring.generated.resources.Res as RingRes
import coreapp.ring.generated.resources.not_now
import coreapp.ring.generated.resources.set_default
import coreapp.ring.generated.resources.set_default_destination
import coreapp.ring.generated.resources.set_default_destination_prompt

/** Shows the default-destination question wherever the app is when a plugin's config page closes. */
@Composable
fun DefaultDestinationPromptHost(prompter: DefaultDestinationPrompter = koinInject()) {
    val prompt by prompter.prompt.collectAsState()
    val current = prompt ?: return
    M3Dialog(
        onDismissRequest = prompter::dismiss,
        title = { Text(stringResource(RingRes.string.set_default_destination)) },
        buttons = {
            TextButton(onClick = prompter::dismiss) { Text(stringResource(RingRes.string.not_now)) }
            TextButton(onClick = prompter::accept) { Text(stringResource(RingRes.string.set_default)) }
        },
    ) {
        Text(stringResource(RingRes.string.set_default_destination_prompt, current.provider.title, current.kindsLabel))
    }
}

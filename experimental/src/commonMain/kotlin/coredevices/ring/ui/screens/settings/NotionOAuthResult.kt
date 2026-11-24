package coredevices.ring.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import coreapp.ring.generated.resources.Res as RingRes
import coreapp.ring.generated.resources.continue_label
import coreapp.ring.generated.resources.notion_link_failed
import coreapp.ring.generated.resources.notion_linked

@Composable
fun NotionOAuthResult(success: Boolean, onContinue: () -> Unit) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(100.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (success) {
                Text(stringResource(RingRes.string.notion_linked), style = MaterialTheme.typography.headlineMedium)
            } else {
                Text(stringResource(RingRes.string.notion_link_failed), style = MaterialTheme.typography.headlineMedium)
            }
            Button(onClick = onContinue) {
                Text(stringResource(RingRes.string.continue_label))
            }
        }
    }
}
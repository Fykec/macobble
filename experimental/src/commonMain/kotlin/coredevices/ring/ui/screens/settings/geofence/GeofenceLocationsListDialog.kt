package coredevices.ring.ui.screens.settings.geofence

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coredevices.ring.ui.viewmodel.IndexActionsViewModel
import coredevices.ui.M3Dialog
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.stringResource
import coreapp.ring.generated.resources.Res as RingRes
import coreapp.ring.generated.resources.add
import coreapp.ring.generated.resources.done
import coreapp.ring.generated.resources.no_saved_places_yet_add_one
import coreapp.ring.generated.resources.saved_places

@Composable
fun GeofenceLocationsListDialog(
    viewModel: IndexActionsViewModel = koinViewModel(),
    onAdd: () -> Unit,
    onEdit: (uuid: Uuid) -> Unit,
    onDismissRequest: () -> Unit
) {
    val locations = viewModel.geofenceLocations.collectAsState()
    val emptyLocations by derivedStateOf { locations.value.isEmpty() }
    M3Dialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(RingRes.string.saved_places)) },
        buttons = {
            TextButton(onClick = onAdd) {
                Text(stringResource(RingRes.string.add))
            }
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(RingRes.string.done))
            }
        },
    ) {
        if (emptyLocations) {
            Text(
                stringResource(RingRes.string.no_saved_places_yet_add_one),
                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
            )
        } else {
            GeofenceLocationsList(
                modifier = Modifier.fillMaxWidth(),
                viewModel = viewModel,
                onEdit = { uuid ->
                    onEdit(uuid)
                }
            )
        }
    }
}
package coredevices.ring.ui.components.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import coreapp.ring.generated.resources.Res as RingRes
import coreapp.ring.generated.resources.bread
import coreapp.ring.generated.resources.shopping_list_2

@Composable
fun ListsGrid(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(10) {
            ListsGridItem(
                title = stringResource(RingRes.string.shopping_list_2),
                trailing = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
                content = {
                    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                        Row { Checkbox(false, onCheckedChange = null); Text(stringResource(RingRes.string.bread)) }
                    }
                }
            )
        }
    }
}
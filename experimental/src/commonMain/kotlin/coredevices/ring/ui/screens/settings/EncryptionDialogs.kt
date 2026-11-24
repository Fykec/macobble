package coredevices.ring.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coredevices.ring.encryption.EncryptionSetupState
import coredevices.ring.ui.components.QrCodeImage
import coredevices.ring.ui.viewmodel.EncryptionViewModel
import coredevices.ui.dismissKeyboardOnTapOutside
import coredevices.util.rememberUiContext
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import coreapp.ring.generated.resources.Res as RingRes
import coreapp.ring.generated.resources.cancel
import coreapp.ring.generated.resources.copy_key
import coreapp.ring.generated.resources.done
import coreapp.ring.generated.resources.encryption_key
import coreapp.ring.generated.resources.encryption_key_backup
import coreapp.ring.generated.resources.encryption_key_not_backed_up
import coreapp.ring.generated.resources.generate_key
import coreapp.ring.generated.resources.generating_your_key_and_saving_it
import coreapp.ring.generated.resources.i_confirm_my_key_is_saved
import coreapp.ring.generated.resources.import_qr_code_from_photos
import coreapp.ring.generated.resources.looking_for_your_key_in_your
import coreapp.ring.generated.resources.restore
import coreapp.ring.generated.resources.retry
import coreapp.ring.generated.resources.save_this_key_securely_you_will
import coreapp.ring.generated.resources.set_up_backup_encryption

/**
 * The "key not backed up" warning and generated-key QR/backup sheet,
 * driven by [EncryptionViewModel] so any host of the key flow can render them.
 */
@Composable
fun EncryptionKeyResultDialogs(viewModel: EncryptionViewModel = koinViewModel()) {
    val showKeyNotBackedUpDialog by viewModel.showKeyNotBackedUpDialog.collectAsState()
    val generatedKey by viewModel.generatedKey.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    if (showKeyNotBackedUpDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissKeyNotBackedUpDialog() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            title = { Text(stringResource(RingRes.string.encryption_key_not_backed_up)) },
            text = {
                Text(
                    buildAnnotatedString {
                        append("You're attempting to enable backup encryption without a confirmed keychain backup of your key, ")
                        append("please ensure you've saved your key either via QR code or copied it to your clipboard and saved it elsewhere.\n\n")
                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                            append("If you lose this key, you will not be able to sync or restore anything from backups and will have to start fresh.")
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmEnableEncryption() }) {
                    Text(stringResource(RingRes.string.i_confirm_my_key_is_saved))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissKeyNotBackedUpDialog() }) {
                    Text(stringResource(RingRes.string.cancel))
                }
            }
        )
    }
    if (generatedKey != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearGeneratedKey() },
            title = { Text(stringResource(RingRes.string.encryption_key_backup)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(RingRes.string.save_this_key_securely_you_will),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        QrCodeImage(
                            data = generatedKey!!,
                            size = 220.dp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(generatedKey!!))
                }) {
                    Text(stringResource(RingRes.string.copy_key))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearGeneratedKey() }) {
                    Text(stringResource(RingRes.string.done))
                }
            }
        )
    }
}

/**
 * Guided "turn on encryption" dialog, driven by
 * [EncryptionViewModel.encryptionSetup] through generate or restore before
 * encryption is enabled, so it's reusable wherever the switch lives.
 */
@Composable
fun EncryptionSetupDialog(viewModel: EncryptionViewModel = koinViewModel()) {
    val current by viewModel.encryptionSetup.collectAsState()
    if (current is EncryptionSetupState.Hidden) return

    val uiContext = rememberUiContext()
    val clipboardManager = LocalClipboardManager.current
    var pasted by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { viewModel.cancelEncryptionSetup() },
        modifier = Modifier.dismissKeyboardOnTapOutside(),
        title = { Text(stringResource(RingRes.string.set_up_backup_encryption)) },
        text = {
            when (val s = current) {
                EncryptionSetupState.Hidden -> {}
                EncryptionSetupState.PromptGenerate -> Text(
                    "Generate an encryption key so only you can read your backups. " +
                        "You'll save a copy so you can restore on another device."
                )

                EncryptionSetupState.Generating -> Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(RingRes.string.generating_your_key_and_saving_it))
                }

                is EncryptionSetupState.ShowKey -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        buildString {
                            append("Saved to your password manager. ")
                            if (s.qrSavedToPhotos) {
                                append("A QR code of your key was also saved to your photos. ")
                            }
                            append(
                                "Save this key somewhere safe too — you'll need it to " +
                                    "restore your backups and it can't be recovered."
                            )
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        QrCodeImage(data = s.keyBase64, size = 220.dp)
                    }
                }

                EncryptionSetupState.Restoring -> Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(RingRes.string.looking_for_your_key_in_your))
                }

                is EncryptionSetupState.PasteKey -> Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val focusManager = LocalFocusManager.current
                    Text(
                        "Your key was generated on another device and isn't in this " +
                            "password manager. Paste your backup key, or import the " +
                            "QR code saved in your photos."
                    )
                    OutlinedTextField(
                        value = pasted,
                        onValueChange = { pasted = it },
                        singleLine = true,
                        label = { Text(stringResource(RingRes.string.encryption_key)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        enabled = uiContext != null,
                        onClick = { uiContext?.let { viewModel.restoreFromQrPhoto(it) } }
                    ) { Text(stringResource(RingRes.string.import_qr_code_from_photos)) }
                    s.error?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                is EncryptionSetupState.Failed -> Text(
                    s.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            when (val s = current) {
                EncryptionSetupState.PromptGenerate -> TextButton(
                    enabled = uiContext != null,
                    onClick = { uiContext?.let { viewModel.generateKeyForSetup(it) } }
                ) { Text(stringResource(RingRes.string.generate_key)) }

                is EncryptionSetupState.ShowKey -> TextButton(
                    onClick = { viewModel.completeEncryptionSetup() }
                ) { Text(stringResource(RingRes.string.done)) }

                is EncryptionSetupState.PasteKey -> TextButton(
                    enabled = pasted.isNotBlank(),
                    onClick = { viewModel.restoreFromPastedKey(pasted) }
                ) { Text(stringResource(RingRes.string.restore)) }

                is EncryptionSetupState.Failed -> TextButton(
                    enabled = uiContext != null,
                    onClick = { uiContext?.let { viewModel.generateKeyForSetup(it) } }
                ) { Text(stringResource(RingRes.string.retry)) }

                else -> {}
            }
        },
        dismissButton = {
            when (val s = current) {
                is EncryptionSetupState.ShowKey -> TextButton(
                    onClick = { clipboardManager.setText(AnnotatedString(s.keyBase64)) }
                ) { Text(stringResource(RingRes.string.copy_key)) }

                EncryptionSetupState.Generating,
                EncryptionSetupState.Restoring,
                EncryptionSetupState.Hidden -> {}

                else -> TextButton(
                    onClick = { viewModel.cancelEncryptionSetup() }
                ) { Text(stringResource(RingRes.string.cancel)) }
            }
        }
    )
}

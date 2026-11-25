package coredevices.pebble.ui

import CommonRoutes
import NextBugReportContext
import PlatformShareLauncher
import PlatformUiContext
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import coreapp.pebble.generated.resources.Res
import coreapp.pebble.generated.resources.state_connected
import coreapp.pebble.generated.resources.state_connected_factory
import coreapp.pebble.generated.resources.state_connecting
import coreapp.pebble.generated.resources.state_disconnected
import coreapp.pebble.generated.resources.state_disconnecting
import coreapp.pebble.generated.resources.state_downloading_language_pack
import coreapp.pebble.generated.resources.state_error_starting_update
import coreapp.pebble.generated.resources.state_installing_language_pack
import coreapp.pebble.generated.resources.state_negotiating
import coreapp.pebble.generated.resources.state_rebooting_after_update
import coreapp.pebble.generated.resources.state_rebooting_into_recovery_to_install
import coreapp.pebble.generated.resources.state_rebooting_into_recovery_to_update
import coreapp.pebble.generated.resources.state_rebooting_to_finish_update
import coreapp.pebble.generated.resources.state_reverse_ppog
import coreapp.pebble.generated.resources.state_updating_to_pebbleos
import coreapp.pebble.generated.resources.state_updating_to_pebbleos_brief
import coreapp.pebble.generated.resources.state_waiting
import coreapp.pebble.generated.resources.battery
import org.jetbrains.compose.resources.StringResource
import coreapp.pebble.generated.resources.scan_action
import coreapp.pebble.generated.resources.remove_device_title
import coreapp.pebble.generated.resources.about_x
import coreapp.pebble.generated.resources.fab_add_classic_watch_body
import coreapp.pebble.generated.resources.fab_add_classic_watch_title
import coreapp.pebble.generated.resources.fab_add_watch_body
import coreapp.pebble.generated.resources.fab_add_watch_title
import coreapp.pebble.generated.resources.Res as PebbleRes
import coreapp.pebble.generated.resources.other_pebble_apps_warning
import coreapp.pebble.generated.resources.connect_crash_hint
import coreapp.pebble.generated.resources.other_pebbleos_apps_warning
import coreapp.pebble.generated.resources.devices
import coredevices.analytics.CoreAnalytics
import coredevices.firestore.UsersDao
import coredevices.libindex.IndexDevices
import coredevices.libindex.LibIndex
import coredevices.libindex.device.DiscoveredIndexDevice
import coredevices.libindex.device.IndexDevice
import coredevices.libindex.device.IndexIdentifier
import coredevices.libindex.device.IndexImage
import coredevices.libindex.device.IndexPairingResult
import coredevices.libindex.device.IndexPairingState
import coredevices.libindex.device.InterviewedIndexDevice
import coredevices.libindex.device.KnownIndexDevice
import coredevices.libindex.device.PairableIndexDevice
import coredevices.libindex.device.PairingRequestResult
import coredevices.libindex.device.RSSIMeasurement
import coredevices.libindex.device.RepairableIndexDevice
import coredevices.libindex.ui.components.Press
import coredevices.libindex.ui.components.PressPatternDot
import coredevices.pebble.PebbleDeepLinkHandler
import coredevices.pebble.PebbleFeatures
import coredevices.pebble.account.PebbleAccount
import coredevices.pebble.firmware.FirmwareUpdateUiTracker
import coredevices.pebble.firmware.isCoreDevice
import coredevices.pebble.rememberLibPebble
import coredevices.pebble.services.LanguagePackRepository
import coredevices.pebble.services.displayName
import coredevices.ui.ConfirmDialog
import coredevices.ui.CoreLinearProgressIndicator
import coredevices.ui.M3Dialog
import coredevices.ui.PebbleElevatedButton
import coredevices.util.CompanionDevice
import coredevices.util.CoreConfig
import coredevices.util.CoreConfigFlow
import coredevices.util.Permission
import coredevices.util.PermissionRequester
import coredevices.util.PermissionResult
import coredevices.util.Platform
import coredevices.util.isIOS
import coredevices.util.rememberUiContext
import io.rebble.libpebblecommon.connection.ActiveDevice
import io.rebble.libpebblecommon.connection.AppContext
import io.rebble.libpebblecommon.connection.BleDiscoveredPebbleDevice
import io.rebble.libpebblecommon.connection.CommonConnectedDevice
import io.rebble.libpebblecommon.connection.ConnectedPebble
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.ConnectedPebbleDeviceInRecovery
import io.rebble.libpebblecommon.connection.ConnectingPebbleDevice
import io.rebble.libpebblecommon.connection.ConnectionFailureReason
import io.rebble.libpebblecommon.connection.PERIPHERAL_NOT_FOUND_MAX_ATTEMPTS
import io.rebble.libpebblecommon.connection.DisconnectingPebbleDevice
import io.rebble.libpebblecommon.connection.DiscoveredPebbleDevice
import io.rebble.libpebblecommon.connection.FirmwareUpdateCheckResult
import io.rebble.libpebblecommon.connection.KnownPebbleDevice
import io.rebble.libpebblecommon.connection.PebbleDevice
import io.rebble.libpebblecommon.connection.bt.BluetoothState
import io.rebble.libpebblecommon.connection.bt.ble.pebble.ReversePpogVersion
import io.rebble.libpebblecommon.connection.color
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdateErrorStarting
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdateTrigger
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdater
import io.rebble.libpebblecommon.connection.endpointmanager.LanguagePackInstallState
import io.rebble.libpebblecommon.connection.endpointmanager.installing
import io.rebble.libpebblecommon.database.entity.buildTimelineNotification
import io.rebble.libpebblecommon.packets.blobdb.TimelineIcon
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.TimelineActionResult
import io.rebble.libpebblecommon.timeline.TimelineColor
import io.rebble.libpebblecommon.timeline.toPebbleColor
import io.rebble.libpebblecommon.util.getTempFilePath
import io.rebble.libpebblecommon.metadata.WatchColor
import io.rebble.libpebblecommon.metadata.WatchType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.uuid.Uuid
import coreapp.pebble.generated.resources.add_index_01
import coreapp.pebble.generated.resources.add_watch
import coreapp.pebble.generated.resources.after_unpairing_reset_the_ring_by
import coreapp.pebble.generated.resources.an_index_01_is_already_paired
import coreapp.pebble.generated.resources.are_you_sure
import coreapp.pebble.generated.resources.back
import coreapp.pebble.generated.resources.battery_life
import coreapp.pebble.generated.resources.bluetooth_sync_disabled
import coreapp.pebble.generated.resources.body
import coreapp.pebble.generated.resources.cancel
import coreapp.pebble.generated.resources.check_for_updates
import coreapp.pebble.generated.resources.checking_for_updates
import coreapp.pebble.generated.resources.close
import coreapp.pebble.generated.resources.create_a_core_dump
import coreapp.pebble.generated.resources.debug
import coreapp.pebble.generated.resources.dev_connection
import coreapp.pebble.generated.resources.device_already_paired
import coreapp.pebble.generated.resources.enable_background_access
import coreapp.pebble.generated.resources.factory_reset
import coreapp.pebble.generated.resources.firmware_update_debug
import coreapp.pebble.generated.resources.index_01_already_paired
import coreapp.pebble.generated.resources.install
import coreapp.pebble.generated.resources.need_more_help_view_detailed_instructions
import coreapp.pebble.generated.resources.no_watches_found
import coreapp.pebble.generated.resources.ok
import coreapp.pebble.generated.resources.open_bluetooth_settings
import coreapp.pebble.generated.resources.pair
import coreapp.pebble.generated.resources.pairing
import coreapp.pebble.generated.resources.pairing_issue_detected
import coreapp.pebble.generated.resources.ping_watch
import coreapp.pebble.generated.resources.please_reset_the_ring_by_pressing
import coreapp.pebble.generated.resources.remember_to_unpair_any_other_phones
import coreapp.pebble.generated.resources.remove
import coreapp.pebble.generated.resources.rename
import coreapp.pebble.generated.resources.reset_into_prf
import coreapp.pebble.generated.resources.restore_firmware
import coreapp.pebble.generated.resources.restoring_please_wait
import coreapp.pebble.generated.resources.s_1_go_to_settings_bluetooth
import coreapp.pebble.generated.resources.s_2_find_e_g_pebble_index
import coreapp.pebble.generated.resources.s_3_if_it_s_there_tap
import coreapp.pebble.generated.resources.s_4_reset_the_ring_by_pressing
import coreapp.pebble.generated.resources.save
import coreapp.pebble.generated.resources.scan_again
import coreapp.pebble.generated.resources.screenshot
import coreapp.pebble.generated.resources.send
import coreapp.pebble.generated.resources.send_notification
import coreapp.pebble.generated.resources.short_3_times_long_1_second
import coreapp.pebble.generated.resources.this_device_is_already_paired_to
import coreapp.pebble.generated.resources.this_device_is_having_trouble_pairing
import coreapp.pebble.generated.resources.title
import coreapp.pebble.generated.resources.to_unpair_find_the_index_01
import coreapp.pebble.generated.resources.to_unpair_first_remove_it_from
import coreapp.pebble.generated.resources.write_notification
import coreapp.pebble.generated.resources.you_ll_see_the_light_flash
import coreapp.pebble.generated.resources.add_a_pebble
import coreapp.pebble.generated.resources.add_classic_watch
import coreapp.pebble.generated.resources.bug_report
import coreapp.pebble.generated.resources.can_t_find_your_index_01
import coreapp.pebble.generated.resources.checkupdaterotation
import coreapp.pebble.generated.resources.classic_watch
import coreapp.pebble.generated.resources.connect
import coreapp.pebble.generated.resources.connected_to_cloudpebble
import coreapp.pebble.generated.resources.create_a_core_dump_2
import coreapp.pebble.generated.resources.developer_connection_active
import coreapp.pebble.generated.resources.disconnect
import coreapp.pebble.generated.resources.enable_bluetooth_to_connect_to_your
import coreapp.pebble.generated.resources.error
import coreapp.pebble.generated.resources.factory_reset_2
import coreapp.pebble.generated.resources.limited_background_access
import coreapp.pebble.generated.resources.more_options
import coreapp.pebble.generated.resources.pairing_failure
import coreapp.pebble.generated.resources.press_the_button_on_your_index
import coreapp.pebble.generated.resources.remove_nickname
import coreapp.pebble.generated.resources.reset_into_prf_2
import coreapp.pebble.generated.resources.rotate
import coreapp.pebble.generated.resources.scanning_for_devices
import coreapp.pebble.generated.resources.share
import coreapp.pebble.generated.resources.this_will_capture_the_current_state
import coreapp.pebble.generated.resources.this_will_reset_the_watch_into
import coreapp.pebble.generated.resources.this_will_wipe_the_watch_completely
import coreapp.pebble.generated.resources.update_pebbleos
import coreapp.pebble.generated.resources.watch
import coreapp.pebble.generated.resources.pebbleos_version
import coreapp.pebble.generated.resources.dev_connection_failed
import coreapp.pebble.generated.resources.ipv4_address
import coreapp.pebble.generated.resources.ipv6_address
import coreapp.pebble.generated.resources.serial_number
import coreapp.pebble.generated.resources.install_pebbleos_version
import coreapp.pebble.generated.resources.watch_language_label
import coreapp.pebble.generated.resources.watch_language_label_value

expect fun scanPermission(): Permission?

expect fun getIPAddress(): Pair<String?, String?>

private val logger = Logger.withTag("WatchesScreen")

sealed interface ScanningStatus {
    object NotScanning : ScanningStatus
    sealed interface ScanningWatch : ScanningStatus
    object ScanningWatchClassic : ScanningWatch
    object ScanningWatchBle : ScanningWatch
    object ScanningRing : ScanningStatus
}

@Composable
fun WatchesScreen(navBarNav: NavBarNav, topBarParams: TopBarParams) {
    val libPebble = rememberLibPebble()
    val libIndex = koinInject<LibIndex>()
    val pebbleFeatures = koinInject<PebbleFeatures>()
    val scanningStatus by combine(
        libPebble.isScanningBle,
        libPebble.isScanningClassic,
        libIndex.isScanning,
    ) { scanningBle, scanningClassic, scanningIndex ->
        when {
            scanningBle -> ScanningStatus.ScanningWatchBle
            scanningClassic -> ScanningStatus.ScanningWatchClassic
            scanningIndex -> ScanningStatus.ScanningRing
            else -> ScanningStatus.NotScanning
        }
    }.collectAsState(ScanningStatus.NotScanning)
    val scope = rememberCoroutineScope()
    val permissionRequester: PermissionRequester = koinInject()
    val requiredScanPermission = remember { scanPermission() }

    val companionDevice = koinInject<CompanionDevice>()
    val deepLinkHandler = koinInject<PebbleDeepLinkHandler>()
    val screenUiContext = rememberUiContext()
    val requestIndexCompanion by deepLinkHandler.requestIndexCompanion.collectAsState()

    val bluetoothEnabled by libPebble.bluetoothEnabled.collectAsState()
    var addFabExpanded by remember { mutableStateOf(false) }
    var showIndexAlreadyPairedDialog by remember { mutableStateOf(false) }
    var fabInfoDialog by remember { mutableStateOf<FabInfo?>(null) }
    var showRingWakeHint by remember { mutableStateOf(false) }
    var discoveryScan by remember { mutableStateOf<DiscoveryScan?>(null) }
    LaunchedEffect(scanningStatus) {
        showRingWakeHint = false
        if (scanningStatus == ScanningStatus.ScanningRing) {
            delay(15_000)
            showRingWakeHint = true
        }
    }

    suspend fun requestCDMForInactiveIndex(uiContext: PlatformUiContext, identifier: IndexIdentifier) {
        // Use classic as it won't scan and some phones have had trouble with LE CDM scan on the random addr
        companionDevice.registerDevice(identifier, uiContext, true)
    }

    LaunchedEffect(requestIndexCompanion) {
        if (!requestIndexCompanion) return@LaunchedEffect
        val pairedRing = libIndex.rings.value.firstOrNull { it is KnownIndexDevice }
        if (screenUiContext != null && pairedRing != null) {
            requestCDMForInactiveIndex(screenUiContext, (pairedRing as KnownIndexDevice).identifier)
        }
        deepLinkHandler.consumeRequestIndexCompanion()
    }

    suspend fun ensureScanPermission(uiContext: PlatformUiContext): Boolean {
        if (requiredScanPermission != null && permissionRequester.missingPermissions.value.contains(
                requiredScanPermission
            )
        ) {
            val result = permissionRequester.requestPermission(requiredScanPermission, uiContext)
            if (result != PermissionResult.Granted) {
                logger.w { "Failed to grant scan permission" }
                return false
            }
        }
        return true
    }

    fun scan(uiContext: PlatformUiContext) {
        scope.launch {
            if (!ensureScanPermission(uiContext)) return@launch
            discoveryScan = DiscoveryScan.Ble
            libPebble.startBleScan()
        }
    }

    fun scanClassic(uiContext: PlatformUiContext) {
        scope.launch {
            if (!ensureScanPermission(uiContext)) return@launch
            discoveryScan = DiscoveryScan.Classic
            libPebble.startClassicScan()
        }
    }

    Scaffold(
        floatingActionButton = {
            if (!bluetoothEnabled.enabled()) {
                FloatingActionButton(
                    onClick = { topBarParams.showSnackbar("Enable Bluetooth to connect a Pebble") }
                ) {
                    Icon(Icons.Filled.BluetoothDisabled, "Bluetooth is disabled")
                }
            } else if (scanningStatus != ScanningStatus.NotScanning) {
                FloatingActionButton(
                    onClick = {
                        libPebble.stopBleScan()
                        libPebble.stopClassicScan()
                        libIndex.stopScan()
                    }
                ) {
                    Icon(Icons.Filled.Stop, "Stop Scanning")
                }
            } else {
                FloatingActionButtonMenu(
//                    modifier = Modifier.padding(16.dp),
                    expanded = addFabExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            checked = addFabExpanded,
                            onCheckedChange = { addFabExpanded = !addFabExpanded },
                        ) {
                            Icon(
                                imageVector = if (addFabExpanded) Icons.Filled.Close else Icons.Filled.Add,
                                contentDescription = stringResource(Res.string.add_a_pebble),
                                tint = Color.White,
                            )
                        }
                    },
                ) {
                    val uiContext = rememberUiContext()
                    FloatingActionButtonMenuItem(
                        onClick = {
                            addFabExpanded = false
                            if (uiContext != null) {
                                scan(uiContext)
                            }
                        },
                        icon = { Icon(Icons.Default.Watch, contentDescription = stringResource(Res.string.watch)) },
                        text = {
                            FabMenuItemLabel(
                                text = stringResource(Res.string.add_watch),
                                onInfoClick = { fabInfoDialog = FabInfo.Watch },
                            )
                        },
                    )
                    FloatingActionButtonMenuItem(
                        onClick = {
                            addFabExpanded = false
                            uiContext?.let { ctx ->
                                scope.launch {
                                    // Ask for the scan permission before trusting the paired
                                    // state: reconciling it against the platform bond list
                                    // needs that permission.
                                    if (!ensureScanPermission(ctx)) return@launch
                                    if (libIndex.rings.value.any { it is KnownIndexDevice }) {
                                        showIndexAlreadyPairedDialog = true
                                    } else {
                                        libIndex.startScan()
                                    }
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = stringResource(Res.string.scan_action)) },
                        text = { Text(stringResource(Res.string.add_index_01)) },
                    )
                    if (pebbleFeatures.supportsBtClassic()) {
                        FloatingActionButtonMenuItem(
                            onClick = {
                                addFabExpanded = false
                                if (uiContext != null) {
                                    scanClassic(uiContext)
                                }
                            },
                            icon = { Icon(Icons.Default.Watch, contentDescription = stringResource(Res.string.classic_watch)) },
                            text = {
                                FabMenuItemLabel(
                                    text = stringResource(Res.string.add_classic_watch),
                                    onInfoClick = { fabInfoDialog = FabInfo.ClassicWatch },
                                )
                            },
                        )
                    }
                }
            }
        },
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            val firmwareUpdateUiTracker = koinInject<FirmwareUpdateUiTracker>()
            val otherPebbleAppsInstalledFlow =
                remember { libPebble.otherPebbleCompanionAppsInstalled() }
            val otherPebbleAppsInstalled by otherPebbleAppsInstalledFlow.collectAsState()
            val coreConfigFlow = koinInject<CoreConfigFlow>()
            val coreConfig by coreConfigFlow.flow.collectAsState()
            val showOtherPebbleAppsWarningAndPreventConnection =
                otherPebbleAppsInstalled.isNotEmpty() && !coreConfig.ignoreOtherPebbleApps
            val companionDevice: CompanionDevice = koinInject()
            val companionDevicePreviouslyCrashed =
                remember { companionDevice.cdmPreviouslyCrashed() }

            val title = stringResource(Res.string.devices)
            val listState = rememberLazyListState()

            LaunchedEffect(Unit) {
                topBarParams.searchAvailable(null)
                topBarParams.title(title)
                topBarParams.actions {}
                launch {
                    topBarParams.scrollToTop.collect {
                        listState.animateScrollToItem(0)
                    }
                }
                if (libPebble.watches.value.none { it.isFullyConnected() }) {
                    addFabExpanded = true
                }

                if (firmwareUpdateUiTracker.shouldUiUpdateCheck()) {
                    firmwareUpdateUiTracker.didFirmwareUpdateCheckFromUi()
                    libPebble.checkForFirmwareUpdates(false)
                }
            }

            val watchesFlow = remember {
                libPebble.watches
                    .map { it.sortedWith(PebbleDeviceComparator) }
            }
            val watches by watchesFlow.collectAsState(
                initial = libPebble.watches.value.sortedWith(
                    PebbleDeviceComparator
                )
            )
            val rings by libIndex.rings.collectAsState()
            val ringPairing by remember {
                derivedStateOf {
                    rings.any { (it as? PairableIndexDevice)?.pairingState == IndexPairingState.Pairing }
                }
            }
            val entriesFlow = remember {
                combine(watchesFlow, libIndex.rings) { sortedWatches, rings ->
                    rings.map { DeviceListEntry.Ring(it) } +
                    sortedWatches.filter { it !is DiscoveredPebbleDevice }.map { DeviceListEntry.Watch(it) }
                }
            }
            val entries by entriesFlow.collectAsState(initial = emptyList())

            Column {
                if (!bluetoothEnabled.enabled()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = stringResource(Res.string.enable_bluetooth_to_connect_to_your),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(15.dp)
                        )
                    }
                }
                if (pebbleFeatures.supportsDetectingOtherPebbleApps() && showOtherPebbleAppsWarningAndPreventConnection) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        val otherAppNames = otherPebbleAppsInstalled.joinToString { it.name }
                        Text(
                            text = stringResource(Res.string.other_pebbleos_apps_warning, otherAppNames),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(15.dp)
                        )
                    }
                }
                if (companionDevicePreviouslyCrashed) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = stringResource(Res.string.connect_crash_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(15.dp)
                        )
                    }
                }
                if (scanningStatus == ScanningStatus.ScanningRing && !ringPairing) {
                    Text(
                        text = stringResource(Res.string.scanning_for_devices),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(5.dp)
                    )
                    Text(
                        text = stringResource(Res.string.press_the_button_on_your_index),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 16.dp)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(5.dp)
                    )
                    if (showRingWakeHint && rings.isEmpty()) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            )
                        ) {
                            Text(
                                text = stringResource(Res.string.can_t_find_your_index_01),
                                modifier = Modifier.padding(15.dp).align(Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    if (watches.isEmpty() && rings.isEmpty()) {
                        if (!pebbleFeatures.supportsDetectingOtherPebbleApps()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = stringResource(Res.string.other_pebble_apps_warning),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(15.dp)
                                )
                            }
                        }
                    }
                }
                val scope = rememberCoroutineScope()
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Reserve space so the last device rows can scroll clear of the floating
                    // add-device FAB (Scaffold does not reserve content padding for the FAB).
                    contentPadding = PaddingValues(bottom = 88.dp),
                    state = listState,
                ) {
                    itemsIndexed(
                        items = entries,
                        key = { _, entry -> entry.key }
                    ) { index, entry ->
                        when (entry) {
                            is DeviceListEntry.Watch -> WatchItem(
                                watch = entry.device,
                                bluetoothState = bluetoothEnabled,
                                allowedToConnect = !showOtherPebbleAppsWarningAndPreventConnection,
                                navBarNav = navBarNav,
                            )
                            is DeviceListEntry.Ring -> RingItem(
                                ring = entry.device,
                                scope = scope,
                                onRequestCompanion = { identifier ->
                                    screenUiContext?.let { requestCDMForInactiveIndex(it, identifier) }
                                },
                            )
                        }
                        if (index < entries.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
            discoveryScan?.let { scanType ->
                val uiContext = rememberUiContext()
                fun close() {
                    discoveryScan = null
                    libPebble.stopBleScan()
                    libPebble.stopClassicScan()
                }
                DiscoveredWatchesDialog(
                    watches = watches.filterIsInstance<DiscoveredPebbleDevice>(),
                    scanning = scanningStatus == ScanningStatus.ScanningWatchBle ||
                            scanningStatus == ScanningStatus.ScanningWatchClassic,
                    connectEnabled = bluetoothEnabled.enabled() && !showOtherPebbleAppsWarningAndPreventConnection,
                    onConnect = { watch ->
                        close()
                        if (uiContext != null) {
                            scope.launch {
                                companionDevice.registerDevice(watch.identifier, uiContext)
                                watch.connect()
                            }
                        }
                    },
                    onScanAgain = {
                        if (uiContext != null) {
                            when (scanType) {
                                DiscoveryScan.Ble -> scan(uiContext)
                                DiscoveryScan.Classic -> scanClassic(uiContext)
                            }
                        }
                    },
                    onDismiss = ::close,
                )
            }
        }
    }

    val platform = koinInject<Platform>()
    if (showIndexAlreadyPairedDialog) {
        AlertDialog(
            onDismissRequest = { showIndexAlreadyPairedDialog = false },
            title = { Text(stringResource(Res.string.index_01_already_paired)) },
            text = {
                Column {
                    Text(stringResource(Res.string.an_index_01_is_already_paired))
                    if (platform.isIOS) {
                        Text(stringResource(Res.string.to_unpair_first_remove_it_from))
                        Text(stringResource(Res.string.after_unpairing_reset_the_ring_by))
                    } else {
                        Text(stringResource(Res.string.to_unpair_find_the_index_01))
                        Text(stringResource(Res.string.after_unpairing_reset_the_ring_by))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PressPatternDot(
                        Press.SOS,
                        size = 30.dp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        stringResource(Res.string.short_3_times_long_1_second),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    )
                    Text(
                        stringResource(Res.string.you_ll_see_the_light_flash),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    )
                    val uriHandler = LocalUriHandler.current
                    TextButton(
                        onClick = { uriHandler.openUri("https://help.repebble.com/en/articles/15724430-hard-resets-how-to-fix-most-problems-on-index-01") },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(stringResource(Res.string.need_more_help_view_detailed_instructions))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIndexAlreadyPairedDialog = false }) { Text(stringResource(Res.string.ok)) }
            },
        )
    }
    fabInfoDialog?.let { info ->
        M3Dialog(
            onDismissRequest = { fabInfoDialog = null },
            title = { Text(stringResource(info.title)) },
            buttons = {
                TextButton(onClick = { fabInfoDialog = null }) { Text(stringResource(Res.string.close)) }
            },
        ) {
            Text(stringResource(info.body))
        }
    }
}

private enum class DiscoveryScan { Ble, Classic }

@Composable
private fun DiscoveredWatchesDialog(
    watches: List<DiscoveredPebbleDevice>,
    scanning: Boolean,
    connectEnabled: Boolean,
    onConnect: (DiscoveredPebbleDevice) -> Unit,
    onScanAgain: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.add_watch)) },
        text = {
            Column {
                Text(
                    stringResource(Res.string.remember_to_unpair_any_other_phones),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(16.dp))
                if (scanning) {
                    CoreLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else if (watches.isEmpty()) {
                    Column(Modifier.padding(vertical = 16.dp)) {
                        Text(stringResource(Res.string.no_watches_found))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "A watch that is connected to another phone stops advertising, so it " +
                                "will not appear here. Unpair it from that phone, or factory reset the " +
                                "watch, then tap Scan again.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                LazyColumn {
                    items(watches, key = { it.identifier.asString }) { watch ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = connectEnabled) { onConnect(watch) }
                                .padding(vertical = 12.dp),
                        ) {
                            WatchColorIndicator(watch.color())
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(watch.displayName(), style = MaterialTheme.typography.bodyLarge)
                                watch.color()?.let {
                                    Text(
                                        it.uiDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!scanning) {
                TextButton(onClick = onScanAgain) { Text(stringResource(Res.string.scan_again)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
    )
}

private enum class FabInfo(val title: StringResource, val body: StringResource) {
    Watch(
        title = PebbleRes.string.fab_add_watch_title,
        body = PebbleRes.string.fab_add_watch_body,
    ),
    ClassicWatch(
        title = PebbleRes.string.fab_add_classic_watch_title,
        body = PebbleRes.string.fab_add_classic_watch_body,
    ),
}

@Composable
private fun FabMenuItemLabel(text: String, onInfoClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text)
        IconButton(onClick = onInfoClick) {
            Icon(Icons.Outlined.Info, contentDescription = stringResource(Res.string.about_x, text))
        }
    }
}

@Preview
@Composable
fun WatchesPreview() {
    PreviewWrapper(
        module {
            single<LibIndex> {
                object : LibIndex {
                    override val rings: IndexDevices = MutableStateFlow(
                        listOf(
                            object : PairableIndexDevice {
                                override val identifier = IndexIdentifier("1234")
                                override val name = "Index 01"
                                override val rssi = -50
                                override val currentImage: IndexImage = IndexImage.Primary
                                override val pairingState: IndexPairingState =
                                    IndexPairingState.NotPaired

                                override suspend fun pair(): IndexPairingResult {
                                    TODO("Not yet implemented")
                                }
                            },
                            object : InterviewedIndexDevice {
                                override val identifier = IndexIdentifier("5678")
                                override val name = "Index 01 02"
                                override val firmwareVersion = "1.0.0"
                                override val serialNumber = "SN12345678"
                                override val updating: Boolean = false
                                override val mac: String = "00:11:22:33:44:55"
                                override fun remove() {
                                    TODO("Not yet implemented")
                                }

                                override suspend fun measureRSSI(connectionTimeout: Duration): RSSIMeasurement {
                                    TODO()
                                }
                            }
                        )
                    )

                    override fun warnIfNoCompanionAssociations() {

                    }

                    override fun init(bluetoothPermissionChanged: Flow<Boolean>) {
                        TODO("Not yet implemented")
                    }

                    override val isScanning: StateFlow<Boolean> = MutableStateFlow(false)

                    override fun startScan() {
                        TODO("Not yet implemented")
                    }

                    override fun stopScan() {
                        TODO("Not yet implemented")
                    }
                }
            }
        }
    ) {
        WatchesScreen(
            navBarNav = NoOpNavBarNav,
            topBarParams = WrapperTopBarParams,
        )
    }
}

sealed interface DeviceListEntry {
    val key: String

    data class Watch(val device: PebbleDevice) : DeviceListEntry {
        override val key get() = "watch-${device.identifier::class.simpleName}-${device.identifier.asString}"
    }

    data class Ring(val device: IndexDevice) : DeviceListEntry {
        override val key get() = "ring-${device.identifier.asString}"
    }
}

@Composable
fun RingItem(
    ring: IndexDevice,
    scope: CoroutineScope,
    onRequestCompanion: suspend (IndexIdentifier) -> Unit = {},
) {
    val coreAnalytics = koinInject<CoreAnalytics>()
    val platform = koinInject<Platform>()
    val companionDevice = koinInject<CompanionDevice>()
    val libIndex = koinInject<LibIndex>()
    val coreConfig by koinInject<CoreConfigFlow>().flow.collectAsState()
    val uiContext = rememberUiContext()
    var showRingAlreadyPairedDialog by remember { mutableStateOf(false) }
    var showSyncDisabledDialog by remember { mutableStateOf(false) }
    var companionApproved by remember(ring.identifier) {
        mutableStateOf(companionDevice.hasApprovedDevice(ring.identifier))
    }
    ListItem(
        headlineContent = {
            Text(
                text = ring.name,
                fontSize = 23.sp,
                fontWeight = if (ring is DiscoveredIndexDevice) FontWeight.Normal else FontWeight.Bold,
            )
        },
        supportingContent = {
            val stateText = when {
                ring is DiscoveredIndexDevice -> when (ring.currentImage) {
                    IndexImage.Failsafe -> "Failsafe mode"
                    IndexImage.ProductionTest -> "Production test mode"
                    IndexImage.Primary -> "Available to pair"
                }
                ring is InterviewedIndexDevice && ring.updating -> "Updating..."
                coreConfig.disableRingBluetoothSync -> "Bluetooth Sync Disabled"
                else -> "Ready"
            }
            Column {
                Text(
                    text = stateText,
                    fontSize = 16.sp,
                    fontWeight = if (ring is DiscoveredIndexDevice) FontWeight.Normal else FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 3.dp),
                )
                // Re-check once the system association dialog resolves so the
                // warning clears if the user approved it.
                when (ring) {
                    is PairableIndexDevice -> {
                        when (ring.pairingState) {
                            is IndexPairingState.Error -> Text(
                                text = stringResource(Res.string.pairing_failure),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 3.dp),
                            )

                            IndexPairingState.Pairing -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 5.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(Res.string.pairing))
                                }
                            }

                            IndexPairingState.NotPaired -> {
                            }
                        }
                        if (ring.pairingState is IndexPairingState.Error || ring.pairingState is IndexPairingState.NotPaired) {
                            Button(
                                onClick = {
                                    if (coreConfig.disableRingBluetoothSync) {
                                        showSyncDisabledDialog = true
                                        return@Button
                                    }
                                    scope.launch {
                                        uiContext?.let { companionDevice.registerDevice(ring.identifier, it, false) }
                                        companionApproved = companionDevice.hasApprovedDevice(ring.identifier)
                                        val result = try {
                                            ring.pair()
                                        } catch (e: Exception) {
                                            null
                                        }
                                        when (result) {
                                            is IndexPairingResult.Success -> {
                                                coreAnalytics.logEvent("ring.pair_success")
                                            }

                                            is IndexPairingResult.PairingFailure -> {
                                                coreAnalytics.logEvent(
                                                    "ring.pair_failed",
                                                    mapOf("reason" to "bonding_error")
                                                )
                                                if (
                                                    result.cause is PairingRequestResult.RingAlreadyPaired ||
                                                    (platform.isIOS && result.cause is PairingRequestResult.CreateBondFailed)
                                                ) {
                                                    showRingAlreadyPairedDialog = true
                                                }
                                            }

                                            is IndexPairingResult.EraseFailed -> {
                                                coreAnalytics.logEvent(
                                                    "ring.pair_failed",
                                                    mapOf("reason" to "erase_failed")
                                                )
                                            }

                                            null -> {
                                                coreAnalytics.logEvent(
                                                    "ring.pair_failed",
                                                    mapOf("reason" to "bonding_error")
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(top = 5.dp)
                            ) {
                                Text(stringResource(Res.string.pair))
                            }
                        }
                    }

                    is RepairableIndexDevice -> {
                        var buttonEnabled by remember { mutableStateOf(true) }
                        Button(
                            enabled = buttonEnabled,
                            onClick = {
                                buttonEnabled = false
                                scope.launch {
                                    try {
                                        ring.forceFailsafe()
                                        // The ring reboots into failsafe under a new address; rescan
                                        // so it is listed and the recovery scan loop picks it up.
                                        libIndex.startScan()
                                    } catch (e: Exception) {
                                        logger.e(e) { "Failed to force failsafe: ${e.message}" }
                                    }
                                    buttonEnabled = true
                                }
                            },
                            modifier = Modifier.padding(top = 5.dp)
                        ) {
                            Text(stringResource(Res.string.restore_firmware))
                        }
                    }

                    is InterviewedIndexDevice if ring.updating -> {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )
                    }

                    is DiscoveredIndexDevice if ring.currentImage == IndexImage.Failsafe -> {
                        Text(stringResource(Res.string.restoring_please_wait))
                    }
                    else -> {}
                }
                if (ring is KnownIndexDevice && !companionApproved && !coreConfig.disableRingBluetoothSync) {
                    Text(
                        text = stringResource(Res.string.limited_background_access),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                onRequestCompanion(ring.identifier)
                                // Re-check once the system association dialog resolves so the
                                // warning clears if the user approved it.
                                companionApproved = companionDevice.hasApprovedDevice(ring.identifier)
                            }
                        },
                        modifier = Modifier.padding(top = 5.dp),
                    ) {
                        Text(stringResource(Res.string.enable_background_access))
                    }
                }
            }
        },
        trailingContent = {
            if (ring is KnownIndexDevice) {
                RingMenu(ring)
            }
        },
    )

    if (showSyncDisabledDialog) {
        M3Dialog(
            onDismissRequest = { showSyncDisabledDialog = false },
            icon = { Icon(Icons.Default.SwitchAccount, null) },
            title = { Text(stringResource(Res.string.bluetooth_sync_disabled)) },
            buttons = {
                TextButton(onClick = { showSyncDisabledDialog = false }) { Text(stringResource(Res.string.ok)) }
            },
        ) {
            Text(
                "Bluetooth sync is disabled for this profile, so an Index 01 can't be paired here.\n\n" +
                        "Switch to your primary profile to pair, or if you're not using multiple profiles " +
                        "on this phone, turn off \"Disable Bluetooth Sync\" in Index settings."
            )
        }
    }

    if (showRingAlreadyPairedDialog) {
        val platform = koinInject<Platform>()
        AlertDialog(
            onDismissRequest = { showRingAlreadyPairedDialog = false },
            title = {
                if (platform.isIOS) {
                    Text(stringResource(Res.string.pairing_issue_detected))
                } else {
                    Text(stringResource(Res.string.device_already_paired))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = stringResource(Res.string.error),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.Start,
                ) {
                    if (platform.isIOS) {
                        Text(stringResource(Res.string.this_device_is_having_trouble_pairing))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(Res.string.s_1_go_to_settings_bluetooth))
                        Text(stringResource(Res.string.s_2_find_e_g_pebble_index))
                        Text(stringResource(Res.string.s_3_if_it_s_there_tap))
                        Text(stringResource(Res.string.s_4_reset_the_ring_by_pressing))
                    } else {
                        Text(stringResource(Res.string.this_device_is_already_paired_to))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(Res.string.please_reset_the_ring_by_pressing))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PressPatternDot(
                        Press.SOS,
                        size = 30.dp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(stringResource(Res.string.short_3_times_long_1_second), modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp) )
                    Text(stringResource(Res.string.you_ll_see_the_light_flash), modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp) )
                    val uriHandler = LocalUriHandler.current
                    TextButton(
                        onClick = { uriHandler.openUri("https://help.repebble.com/en/articles/15724430-hard-resets-how-to-fix-most-problems-on-index-01") },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(stringResource(Res.string.need_more_help_view_detailed_instructions))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRingAlreadyPairedDialog = false }) { Text(stringResource(Res.string.ok)) }
            },
        )
    }
}

@Composable
expect fun RemovePairingMenuItem(ring: KnownIndexDevice, onShowRemoveDialog: () -> Unit, onHideMenu: () -> Unit)

@Composable
private fun RingMenu(ring: KnownIndexDevice) {
    var showMenu by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMenu = !showMenu }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.more_options))
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            RemovePairingMenuItem(
                ring = ring,
                onShowRemoveDialog = {
                    showRemoveDialog = true
                    showMenu = false
                },
                onHideMenu = { showMenu = false }
            )
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text(stringResource(Res.string.remove_device_title, ring.name)) },
            text = { Text(stringResource(Res.string.are_you_sure)) },
            confirmButton = {
                TextButton(onClick = {
                    ring.remove()
                    showRemoveDialog = false
                }) { Text(stringResource(Res.string.remove)) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }
}

@Composable
private fun NicknameDialog(watch: KnownPebbleDevice, onDismissRequest: () -> Unit) {
    var nickname by remember { mutableStateOf(watch.nickname) }
    val focusRequester = remember { FocusRequester() }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(nickname ?: watch.name))
    }

    // 1. Initial "Select All" logic when dialog opens
    LaunchedEffect(Unit) {
        textFieldValue = textFieldValue.copy(
            selection = TextRange(0, textFieldValue.text.length)
        )
        focusRequester.requestFocus()
    }

    // 2. Focus logic when nickname is cleared (via the button)
    LaunchedEffect(nickname) {
        // Only request focus if we aren't at the very start (Unit handle that)
        // This ensures the keyboard pops back up if the user hits "Cancel"
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.rename)) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = textFieldValue,
                        onValueChange = {
                            if (it.text.length <= 25) {
                                textFieldValue = it
                                nickname = if (it.text == watch.name) null else it.text
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester), // Bind focus requester
                        singleLine = true,
                    )
                    if (nickname != null) {
                        IconButton(
                            onClick = {
                                nickname = null
                                textFieldValue = TextFieldValue(watch.name)
                            },
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = stringResource(Res.string.remove_nickname))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    watch.setNickname(nickname)
                    onDismissRequest()
                },
                enabled = nickname != watch.nickname
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.back))
            }
        }
    )
}

object PebbleDeviceComparator : Comparator<PebbleDevice> {
    private fun getStateRank(device: PebbleDevice): Int {
        return when (device) {
            is ConnectedPebbleDevice -> 0
            is ConnectedPebbleDeviceInRecovery -> 1
            is ConnectingPebbleDevice -> 3
            is DisconnectingPebbleDevice -> 2
            is KnownPebbleDevice -> 4
            is DiscoveredPebbleDevice -> 5
            else -> Int.MAX_VALUE
        }
    }

    override fun compare(a: PebbleDevice, b: PebbleDevice): Int {
        val rankA = getStateRank(a)
        val rankB = getStateRank(b)
        return if (rankA != rankB) rankA.compareTo(rankB) else {
            // Sort by last connected if available
            val lastConnectedA =
                (a as? KnownPebbleDevice)?.lastConnected?.epochSeconds ?: Long.MIN_VALUE
            val lastConnectedB =
                (b as? KnownPebbleDevice)?.lastConnected?.epochSeconds ?: Long.MIN_VALUE
            if (lastConnectedA != lastConnectedB) {
                lastConnectedB.compareTo(lastConnectedA)
            } else {
                a.name.compareTo(b.name)
            }
        }
    }
}

fun FirmwareUpdateErrorStarting.message(): String = when (this) {
    FirmwareUpdateErrorStarting.ErrorDownloading -> "Failed to download firmware" // not-localized
    FirmwareUpdateErrorStarting.ErrorParsingPbz -> "Failed to parse manifest" // not-localized
}

@Composable
fun PebbleDevice.stateText(
    firmwareUpdateState: FirmwareUpdater.FirmwareUpdateStatus,
    languagePackInstallState: LanguagePackInstallState,
    coreConfig: CoreConfig,
): String {
    val installingState = when (firmwareUpdateState) {
        is FirmwareUpdater.FirmwareUpdateStatus.InProgress -> {
            val progress by firmwareUpdateState.progress.collectAsState()
            stringResource(
                Res.string.state_updating_to_pebbleos,
                firmwareUpdateState.update.version.stringVersion,
                "${(progress * 100).toInt()}%",
            )
        }

        is FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.ErrorStarting -> stringResource(Res.string.state_error_starting_update, firmwareUpdateState.error.message())
        is FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle -> when (languagePackInstallState) {
            is LanguagePackInstallState.Installing -> stringResource(Res.string.state_installing_language_pack, languagePackInstallState.language)
            is LanguagePackInstallState.Idle -> ""
            is LanguagePackInstallState.Downloading -> stringResource(Res.string.state_downloading_language_pack, languagePackInstallState.language)
        }
        is FirmwareUpdater.FirmwareUpdateStatus.WaitingForReboot -> if (firmwareUpdateState.viaPrf) {
            stringResource(
                Res.string.state_rebooting_into_recovery_to_install,
                firmwareUpdateState.update.version.stringVersion,
            )
        } else {
            stringResource(
                Res.string.state_rebooting_to_finish_update,
                firmwareUpdateState.update.version.stringVersion,
            )
        }
        is FirmwareUpdater.FirmwareUpdateStatus.WaitingToStart -> stringResource(Res.string.state_updating_to_pebbleos_brief, firmwareUpdateState.update.version.stringVersion)
    }
    val reversePpogState = when {
        !coreConfig.showWatchConnectionDebugInfo -> ""
        reversePpogVersion() != null -> stringResource(Res.string.state_reverse_ppog, reversePpogVersion().toString())
        else -> ""
    }
    val stateText = when (this) {
        is ConnectedPebbleDevice ->
            stringResource(Res.string.state_connected) + reversePpogState + installingState
        is ConnectedPebbleDeviceInRecovery ->
            stringResource(Res.string.state_connected_factory) + reversePpogState + installingState
        is ConnectingPebbleDevice -> {
            val reboot = rebootingForFirmwareUpdate
            val connectingState =
                when {
                reboot != null -> {
                    val rebooting = stringResource(
                        if (reboot.viaPrf) {
                            Res.string.state_rebooting_into_recovery_to_update
                        } else {
                            Res.string.state_rebooting_after_update
                        }
                    )
                    rebooting + " - " + stringResource(
                        if (negotiating) Res.string.state_negotiating else Res.string.state_waiting
                    )
                }

                negotiating -> stringResource(Res.string.state_negotiating)
                else -> stringResource(Res.string.state_connecting)
            }
            "$connectingState$reversePpogState"
        }

        is KnownPebbleDevice, is DiscoveredPebbleDevice -> stringResource(Res.string.state_disconnected)
        is DisconnectingPebbleDevice -> stringResource(Res.string.state_disconnecting)
    }
    return stateText
}

private fun PebbleDevice.reversePpogVersion(): ReversePpogVersion? =
    when (this) {
        is ConnectedPebbleDevice -> reversePpogVersion
        is ConnectedPebbleDeviceInRecovery -> reversePpogVersion
        is ConnectingPebbleDevice -> reversePpogVersion
        else -> null
    }

private fun PebbleDevice.isActive(): Boolean = when (this) {
    is ConnectedPebbleDevice, is ConnectingPebbleDevice, is ConnectedPebbleDeviceInRecovery -> true
    else -> false
}

expect fun postTestNotification(appContext: AppContext)

expect fun openSystemBluetoothSettings(appContext: AppContext)

expect fun ImageBitmap.toPngBytes(): ByteArray

@Composable
fun WatchItem(
    watch: PebbleDevice,
    bluetoothState: BluetoothState,
    allowedToConnect: Boolean,
    navBarNav: NavBarNav,
) {
    ListItem(
        headlineContent = {
            WatchHeader(watch)
        },
        supportingContent = {
            Column {
                WatchDetails(
                    watch = watch,
                    bluetoothState = bluetoothState,
                    allowedToConnect = allowedToConnect,
                    navBarNav = navBarNav,
                )
            }
        },
    )
}

@Composable
fun WatchMenu(watch: PebbleDevice, navBarNav: NavBarNav) {
    var showMenu by remember { mutableStateOf(false) }
    var debugMenuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showScreenshotDialog by remember { mutableStateOf(false) }
    var showForgetDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    val showConfirmCoreDumpDialog = remember { mutableStateOf(false) }
    val showConfirmResetIntoPrfDialog = remember { mutableStateOf(false) }
    val showConfirmFactoryResetDialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box {
        IconButton(onClick = { showMenu = !showMenu }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.more_options))
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.widthIn(min = 250.dp),
        ) {
            val firmwareVersion = when {
                watch is KnownPebbleDevice -> watch.runningFwVersion
                watch is BleDiscoveredPebbleDevice -> {
                    val extInfo = watch.pebbleScanRecord.extendedInfo
                    if (extInfo != null) {
                        "v${extInfo.major}.${extInfo.minor}.${extInfo.patch}"
                    } else null
                }

                else -> null
            }
            if (firmwareVersion != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(PebbleRes.string.pebbleos_version, firmwareVersion),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            if (watch is ConnectedPebble.Firmware) {
                if (watch.firmwareUpdateAvailable.checkingForUpdates) {
                    val infiniteTransition =
                        rememberInfiniteTransition(label = stringResource(Res.string.checkupdaterotation))
                    val rotate by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = stringResource(Res.string.rotate)
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.checking_for_updates)) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Autorenew,
                                contentDescription = null,
                                modifier = Modifier.graphicsLayer { rotationZ = rotate },
                            )
                        },
                        enabled = false,
                        onClick = {}
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.check_for_updates)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Autorenew, contentDescription = null)
                        },
                        onClick = {
                            watch.checkforFirmwareUpdate(true)
                        }
                    )
                }
            }

            HorizontalDivider()

            if (watch is ConnectedPebbleDevice) {
                val languagePackRepository: LanguagePackRepository = koinInject()
                val languagePackInstalled = watch.languagePackInstalled(languagePackRepository)
                val languageText = when  {
                    languagePackInstalled.isNullOrBlank() -> stringResource(PebbleRes.string.watch_language_label)
                    else -> stringResource(PebbleRes.string.watch_language_label_value, languagePackInstalled)
                }
                DropdownMenuItem(
                    text = { Text(languageText) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Language,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        showLanguageDialog = true
                    }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.screenshot)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Image,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        showScreenshotDialog = true
                    }
                )

                if (watch.watchInfo.platform.isCoreDevice()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.battery_life)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.BatteryFull,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            navBarNav.navigateTo(PebbleNavBarRoutes.BatterySettingsRoute)
                        }
                    )
                }
                HorizontalDivider()
            }

            if (watch is KnownPebbleDevice) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.rename)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.DriveFileRenameOutline,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        showRenameDialog = true
                    }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.remove)) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        showForgetDialog = true
                    }
                )

                HorizontalDivider()
            }

            val libPebble = rememberLibPebble()
            if (watch is ConnectedPebble.DevConnection) {
                val config by libPebble.config.collectAsState()
                val usersDao: UsersDao = koinInject()
                val user by usersDao.user.collectAsState(null)
                val active by watch.devConnectionActive.collectAsState()
                val canUseDevConnection = user?.isAnonymousUser == false || config.watchConfig.lanDevConnection
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.dev_connection)) },
                    leadingIcon = { Icon(Icons.Outlined.DeveloperBoard, contentDescription = null) },
                    trailingIcon = { Switch(
                        checked = active,
                        onCheckedChange = null,
                    ) },
                    enabled = canUseDevConnection,
                    onClick = {
                        scope.launch {
                            if (active) {
                                watch.stopDevConnection()
                            } else {
                                watch.startDevConnection()
                            }
                        }
                    }
                )

                val error by watch.devConnectionError.collectAsState()
                if (!active && error != null) {
                    Text(
                        text = stringResource(PebbleRes.string.dev_connection_failed, "$error"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                }

                if (active) {
                    if (config.watchConfig.lanDevConnection) {
                        val (v4, v6) = remember { getIPAddress() }
                        v4?.let {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(PebbleRes.string.ipv4_address, v4),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                        v6?.let {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(PebbleRes.string.ipv6_address, v6),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(Res.string.connected_to_cloudpebble),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
                HorizontalDivider()
            }

            val settings: Settings = koinInject()
            val showDebugOptions = settings.showDebugOptions()
            if (showDebugOptions && watch is CommonConnectedDevice) {
                Box {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.debug)) },
                        leadingIcon = { Icon(Icons.Default.Terminal, null) },
                        trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
                        onClick = { debugMenuExpanded = true }
                    )

                    val debugMenuTheme = MaterialTheme.colorScheme.copy(
                        surface = MaterialTheme.colorScheme.primary,
                        onSurface = MaterialTheme.colorScheme.onPrimary
                    )
                    MaterialTheme(colorScheme = debugMenuTheme) {
                        DropdownMenu(
                            expanded = debugMenuExpanded,
                            onDismissRequest = { debugMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.firmware_update_debug)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.SystemUpdateAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    debugMenuExpanded = false
                                    navBarNav.navigateTo(
                                        PebbleRoutes.FirmwareSideloadRoute(
                                            watch.identifier.asString,
                                        )
                                    )
                                }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f))

                            if (watch is ConnectedPebbleDevice) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.ping_watch)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.NetworkPing,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        debugMenuExpanded = false
                                        scope.launch { watch.sendPing(1234u) }
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.write_notification)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.NotificationAdd,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        debugMenuExpanded = false
                                        scope.launch { showNotificationDialog = true }
                                    }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f))

                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.create_a_core_dump)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        debugMenuExpanded = false
                                        showConfirmCoreDumpDialog.value = true
                                    }
                                )

                                if (watch.watchInfo.recoveryFwVersion != null) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.reset_into_prf)) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            debugMenuExpanded = false
                                            showConfirmResetIntoPrfDialog.value = true
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.factory_reset)) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            debugMenuExpanded = false
                                            showConfirmFactoryResetDialog.value = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()
            }

            val serial = when (watch) {
                is KnownPebbleDevice -> watch.serial
                is BleDiscoveredPebbleDevice -> watch.pebbleScanRecord.serialNumber
                else -> null
            }
            if (serial != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(PebbleRes.string.serial_number, serial),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }

    if (showRenameDialog && watch is KnownPebbleDevice) {
        NicknameDialog(watch, onDismissRequest = { showRenameDialog = false })
    }
    if (showScreenshotDialog && watch is ConnectedPebble.Screenshot) {
        ScreenshotDialog(watch, onDismissRequest = { showScreenshotDialog = false }, navBarNav)
    }
    if (showLanguageDialog && watch is ConnectedPebbleDevice) {
        LanguageDialog(watch, onDismissRequest = { showLanguageDialog = false })
    }
    if (showNotificationDialog && watch is ConnectedPebbleDevice) {
        NotificationDialog(
            onDismiss = { showNotificationDialog = false },
        )
    }
    if (watch is ConnectedPebbleDevice) {
        ConfirmDialog(
            show = showConfirmCoreDumpDialog,
            title = stringResource(Res.string.create_a_core_dump_2),
            text = stringResource(Res.string.this_will_capture_the_current_state),
            onConfirm = {
                watch.createCoreDump()
            },
            confirmText = "OK",
        )
        ConfirmDialog(
            show = showConfirmResetIntoPrfDialog,
            title = stringResource(Res.string.reset_into_prf_2),
            text = stringResource(Res.string.this_will_reset_the_watch_into),
            onConfirm = {
                if (watch.watchInfo.recoveryFwVersion != null) {
                    watch.resetIntoPrf()
                }
            },
            confirmText = "OK",
        )
        ConfirmDialog(
            show = showConfirmFactoryResetDialog,
            title = stringResource(Res.string.factory_reset_2),
            text = stringResource(Res.string.this_will_wipe_the_watch_completely),
            onConfirm = {
                if (watch.watchInfo.recoveryFwVersion != null) {
                    watch.factoryReset()
                }
            },
            confirmText = "OK",
        )
    }
    if (showForgetDialog && watch is KnownPebbleDevice) {
        AlertDialog(
            onDismissRequest = { showForgetDialog = false },
            title = { Text(stringResource(PebbleRes.string.remove_device_title, watch.displayName())) },
            text = { Text(stringResource(Res.string.are_you_sure)) },
            confirmButton = {
                TextButton(onClick = {
                    logger.d { "forget: ${watch.identifier}" }
                    watch.forget()
                    showForgetDialog = false
                }) { Text(stringResource(Res.string.remove)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgetDialog = false
                }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }
}

@Serializable
data class TestNotificationContent(
    val title: String = "Test Notification",
    val body: String = "This is a test notification",
    val icon: TimelineIcon? = TimelineIcon.GenericSms,
    val color: TimelineColor? = TimelineColor.Orange,
)

@Composable
fun ConnectedPebbleDevice.languagePackInstalled(repository: LanguagePackRepository): String? {
    val manifest by repository.manifest.collectAsState()
    val languagePackInstalledAtConnectionTime = installedLanguagePack
    val installedName = remember(languagePackInstalledAtConnectionTime, manifest) {
        languagePackInstalledAtConnectionTime?.let { repository.displayNameForInstalled(it) }
    }
    val languagePackRecentlyInstalledSinceConnection = (languagePackInstallState as? LanguagePackInstallState.Idle)?.activeLanguage
    return languagePackRecentlyInstalledSinceConnection ?: installedName
}

private const val CUSTOM_NOTIFICATION_CONTENT_KEY = "custom_notification_content"

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
@Composable
private fun NotificationDialog(
    onDismiss: () -> Unit,
) {
    val settings: Settings = koinInject()
    var content by remember { mutableStateOf(settings.decodeValue(CUSTOM_NOTIFICATION_CONTENT_KEY, TestNotificationContent())) }
    val scope = rememberCoroutineScope()
    val libPebble = rememberLibPebble()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.send_notification)) },
        text = {
            Column {
                TextField(
                    value = content.title,
                    onValueChange = { content = content.copy(title = it) },
                    label = { Text(stringResource(Res.string.title)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = content.body,
                    onValueChange = { content = content.copy(body = it) },
                    label = { Text(stringResource(Res.string.body)) },
                    modifier = Modifier.fillMaxWidth()
                )
                SelectColorOrNone(
                    currentColorName = content.color?.name,
                    onChangeColor = { color ->
                        content = content.copy(color = color)
                    },
                )
                SelectIconOrNone(
                    currentIcon = TimelineIcon.fromCode(content.icon?.code),
                    onChangeIcon = { icon ->
                        content = content.copy(icon = icon)
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                settings.encodeValue(CUSTOM_NOTIFICATION_CONTENT_KEY, content)
                onDismiss()
                scope.launch {
                    val testActionId: UByte = 0u
                    val notif = buildTimelineNotification(
                        timestamp = Clock.System.now(),
                        parentId = Uuid.NIL,
                    ) {
                        layout = TimelineItem.Layout.GenericNotification
                        attributes {
                            title { content.title }
                            body { content.body }
                            content.icon?.let { tinyIcon { it } }
                            content.color?.let { backgroundColor { it.toPebbleColor() } }
                        }
                        actions {
                            action(TimelineItem.Action.Type.Generic) {
                                attributes {
                                    title { "Test" }
                                }
                            }
                        }
                    }
                    libPebble.sendNotification(
                        notif, mapOf(
                            testActionId to { _ ->
                                TimelineActionResult(
                                    success = true,
                                    icon = TimelineIcon.GenericConfirmation,
                                    "Test Successtitle = "
                                )
                            }
                        ))
                }
            }) {
                Text(stringResource(Res.string.send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Composable
fun ScreenshotDialog(watch: ConnectedPebble.Screenshot, onDismissRequest: () -> Unit, navBarNav: NavBarNav) {
    var screenshot by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()
    val nextBugReportContext: NextBugReportContext = koinInject()
    val appContext: AppContext = koinInject()

    suspend fun takeScreenshot() {
        screenshot = null
        screenshot = watch.takeScreenshot()
        logger.v { "screenshot = $screenshot" }
    }

    LaunchedEffect(Unit) {
        takeScreenshot()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.screenshot)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val screenshot = screenshot
                if (screenshot == null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    IconButton(onClick = { scope.launch { takeScreenshot() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }

                    ElevatedCard(
                        shape = CutCornerShape(0.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp,
                        ),
                        modifier = Modifier.background(Color.Transparent).padding(bottom = 8.dp),
                    ) {
                        val height = 140.dp
                        val width = height / screenshot.height * screenshot.width
                        Image(
                            bitmap = screenshot,
                            contentDescription = stringResource(Res.string.screenshot),
                            modifier = Modifier.height(height).width(width),
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        PebbleElevatedButton(
                            text = stringResource(Res.string.bug_report),
                            onClick = {
                                nextBugReportContext.nextContext = null
                                scope.launch {
                                    val tempScreenshotFile = withContext(Dispatchers.Default) {
                                        val file = getTempFilePath(
                                            appContext,
                                            "watch-screenshot.png"
                                        )
                                        SystemFileSystem.sink(file, append = false).buffered().use { sink ->
                                            sink.write(screenshot.toPngBytes())
                                        }
                                        file
                                    }
                                    navBarNav.navigateTo(
                                        CommonRoutes.BugReport(
                                            pebble = true,
                                            screenshotPath = tempScreenshotFile.toString(),
                                        )
                                    )
                                }
                            },
                            icon = Icons.Default.BugReport,
                            primaryColor = false,
                            modifier = Modifier.padding(5.dp),
                        )

                        val platformShareLauncher = koinInject<PlatformShareLauncher>()
                        PebbleElevatedButton(
                            text = stringResource(Res.string.share),
                            onClick = {
                                platformShareLauncher.shareImage(
                                    screenshot,
                                    "pebble_screenshot.png"
                                )
                            },
                            icon = Icons.Default.Share,
                            primaryColor = false,
                            modifier = Modifier.padding(5.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.back))
            }
        },
    )
}


@Composable
private fun WatchColorIndicator(color: WatchColor?) {
    val round = color?.platform == WatchType.CHALK || color?.platform == WatchType.GABBRO
    val shape = if (round) CircleShape else RoundedCornerShape(6.dp)
    val modifier = if (round) Modifier.size(16.dp) else Modifier.size(width = 13.dp, height = 17.dp)
    val accent = color?.accentColor
    Box(
        modifier
            .background(color?.color ?: MaterialTheme.colorScheme.surfaceVariant, shape)
            .border(
                if (accent != null) 2.5.dp else 1.dp,
                accent ?: MaterialTheme.colorScheme.outline,
                shape,
            )
    )
}

@Composable
fun WatchHeader(watch: PebbleDevice) {
    Row {
        Text(
            text = watch.displayName(),
            fontSize = 23.sp,
            fontWeight = when {
                watch.isActive() -> FontWeight.Bold
                else -> FontWeight.Normal
            },
        )
    }
}

@Composable
fun WatchDetails(
    watch: PebbleDevice,
    bluetoothState: BluetoothState,
    allowedToConnect: Boolean,
    navBarNav: NavBarNav,
) {
    val appContext: AppContext = koinInject()
    val pebbleFeatures = koinInject<PebbleFeatures>()
    val coreConfigFlow = koinInject<CoreConfigFlow>()
    val coreConfig by coreConfigFlow.flow.collectAsState()
    val firmwareUpdateState = remember(watch) {
        if (watch is ConnectedPebble.Firmware) {
            watch.firmwareUpdateState
        } else {
            FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle()
        }
    }
    val firmwareUpdateInProgress =
        firmwareUpdateState !is FirmwareUpdater.FirmwareUpdateStatus.NotInProgress
    val languagePackInstallState = (watch as? ConnectedPebble.LanguageState)?.languagePackInstallState ?: LanguagePackInstallState.Idle()
    Row(verticalAlignment = Alignment.CenterVertically) {
        WatchColorIndicator(watch.color())
        Spacer(Modifier.width(8.dp))
        Text(
            text = watch.stateText(firmwareUpdateState, languagePackInstallState, coreConfig),
            fontWeight = when {
                watch.isActive() -> FontWeight.Bold
                else -> FontWeight.Normal
            },
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 3.dp),
        )
    }
    if (firmwareUpdateInProgress) {
        if (firmwareUpdateState is FirmwareUpdater.FirmwareUpdateStatus.InProgress) {
            val progress by firmwareUpdateState.progress.collectAsState()
            CoreLinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
            )
        }
    } else if (languagePackInstallState is LanguagePackInstallState.Installing) {
        val progress by languagePackInstallState.progress.collectAsState()
        CoreLinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        )
    }
    val watchColorText = remember(watch) {
        watch.color()?.uiDescription
    }
    FlowRow(verticalArrangement = Arrangement.Center) {
        if (watch is ConnectedPebble.DevConnection) {
            val devConnectionActive by watch.devConnectionActive.collectAsState()
            if (devConnectionActive) {
                Icon(
                    imageVector = Icons.Outlined.DeveloperBoard,
                    contentDescription = stringResource(Res.string.developer_connection_active),
                    modifier = Modifier.size(18.dp).padding(end = 5.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (watchColorText != null) {
            Text(
                text = watchColorText,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(end = 5.dp).align(Alignment.CenterVertically),
            )
        }
        if (watch is ConnectedPebble.Battery) {
            val batteryLevel = watch.batteryLevel
            if (batteryLevel != null) {
                // Use a Row to keep Icon and Text together,
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 5.dp)
                ) {
                    Icon(
                        imageVector = batteryLevel.batteryIcon(),
                        contentDescription = stringResource(PebbleRes.string.battery),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$batteryLevel%",
                        fontSize = 11.sp,
                        lineHeight = 12.sp,
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(5.dp))
    val firmwareUpdateAvailable =
        (watch as? ConnectedPebble.Firmware)?.firmwareUpdateAvailable?.result
    val firmwareUpdater = watch as? ConnectedPebble.Firmware
    val scope = rememberCoroutineScope()
    val companionDevice: CompanionDevice = koinInject()
    val pebbleAccount = koinInject<PebbleAccount>()
    val loggedIn by pebbleAccount.loggedIn.collectAsState()
    val uriHandler = LocalUriHandler.current

    if (firmwareUpdateAvailable is FirmwareUpdateCheckResult.FoundUpdate && firmwareUpdater != null && !firmwareUpdateInProgress) {
        var showFirmwareUpdateConfirmDialog by remember { mutableStateOf(false) }
        if (showFirmwareUpdateConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showFirmwareUpdateConfirmDialog = false },
                title = { Text(stringResource(PebbleRes.string.install_pebbleos_version, firmwareUpdateAvailable.version.stringVersion)) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text(firmwareUpdateAvailable.notes)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showFirmwareUpdateConfirmDialog = false
                        firmwareUpdater.updateFirmware(firmwareUpdateAvailable, FirmwareUpdateTrigger.Ui)
                    }) { Text(stringResource(Res.string.install)) }
                },
                dismissButton = { TextButton(onClick = { showFirmwareUpdateConfirmDialog = false }) { Text(stringResource(Res.string.cancel)) } }
            )
        }
        PebbleElevatedButton(
            text = stringResource(Res.string.update_pebbleos),
            onClick = {
                logger.d { "Starting firmware update from watches screen" }
                showFirmwareUpdateConfirmDialog = true
            },
            enabled = bluetoothState.enabled(),
            icon = Icons.Default.SystemUpdateAlt,
            contentDescription = stringResource(Res.string.update_pebbleos),
            primaryColor = true,
            modifier = Modifier.padding(vertical = 5.dp),
        )
    } else if (firmwareUpdateAvailable is FirmwareUpdateCheckResult.UpdateCheckFailed) {
        Card(
            modifier = Modifier
//                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = firmwareUpdateAvailable.error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(15.dp)
            )
        }
    }
    var companionApproved by remember(watch) { mutableStateOf(companionDevice.hasApprovedDevice(watch.identifier)) }
    if (watch is KnownPebbleDevice && !companionApproved && !coreConfig.disableCompanionDeviceManager) {
        val uiContext = rememberUiContext()
        PebbleElevatedButton(
            text = stringResource(Res.string.enable_background_access),
            onClick = {
                if (uiContext == null) return@PebbleElevatedButton
                scope.launch {
                    // CDM can only find the watch while it's advertising, i.e. not connected
                    val wasActive = watch is ActiveDevice
                    if (watch is ActiveDevice) watch.disconnect()
                    try {
                        companionDevice.registerDevice(watch.identifier, uiContext)
                    } finally {
                        if (wasActive) watch.connect()
                    }
                    companionApproved = companionDevice.hasApprovedDevice(watch.identifier)
                }
            },
            enabled = bluetoothState.enabled(),
            primaryColor = true,
            modifier = Modifier.padding(vertical = 5.dp),
        )
    }
    val failureInfo = watch.connectionFailureInfo
    if (watch !is CommonConnectedDevice && failureInfo?.reason == ConnectionFailureReason.CreateBondFailed) {
        ConnectionFailureGuidanceButton(
            appContext = appContext,
            pebbleFeatures = pebbleFeatures,
            buttonText = "Error Pairing",
            dialogTitle = "Error Pairing",
            dialogText = "Please go to system bluetooth settings, and unpair this device.",
        )
    }
    if (watch !is CommonConnectedDevice && failureInfo?.reason == ConnectionFailureReason.ClassicConnectionFailed && failureInfo.times >= 5) {
        ConnectionFailureGuidanceButton(
            appContext = appContext,
            pebbleFeatures = pebbleFeatures,
            buttonText = "Connection failing",
            dialogTitle = "Can't connect to watch",
            dialogText = "Make sure your watch is powered on and nearby. " +
                    "If it still won't connect, open Bluetooth settings, " +
                    "remove (\"Forget\") this watch, and also unpair the phone " +
                    "in watch settings (Settings > Bluetooth), then accept the new pairing request.",
        )
    }
    if (watch !is CommonConnectedDevice && failureInfo?.reason == ConnectionFailureReason.PeripheralNotFound && failureInfo.times >= PERIPHERAL_NOT_FOUND_MAX_ATTEMPTS) {
        ConnectionFailureGuidanceButton(
            appContext = appContext,
            pebbleFeatures = pebbleFeatures,
            buttonText = "Can't find watch",
            dialogTitle = "Can't find watch",
            dialogText = "Your phone can't find this watch. Forget it in this app, remove (\"Forget\") it " +
                    "in your phone's Bluetooth settings, then pair it again.",
        )
    }
    if (watch !is CommonConnectedDevice && failureInfo?.reason == ConnectionFailureReason.MalformedConnectivityStatus) {
        ConnectionFailureGuidanceButton(
            appContext = appContext,
            pebbleFeatures = pebbleFeatures,
            buttonText = "Error Connecting - Restart Your Watch",
            dialogTitle = "Restart your watch",
            dialogText = "Your watch needs to be restarted before it can connect. " +
                    "Press and hold the back button (top-left) for about 15 seconds " +
                    "until the watch restarts, then try connecting again.",
            showBluetoothSettingsLink = false,
        )
    }
    Row {
        Box(modifier = Modifier.weight(1f)) {
            if (watch is ActiveDevice) {
                PebbleElevatedButton(
                    text = stringResource(Res.string.disconnect),
                    onClick = { watch.disconnect() },
                    enabled = bluetoothState.enabled() && !firmwareUpdateInProgress,
                    primaryColor = false,
                    modifier = Modifier.padding(vertical = 5.dp),
                )
            } else if (watch !is DisconnectingPebbleDevice) {
                val uiContext = rememberUiContext()
                if (uiContext != null) {
                    PebbleElevatedButton(
                        text = stringResource(Res.string.connect),
                        onClick = {
                            scope.launch {
                                companionDevice.registerDevice(watch.identifier, uiContext)
                                watch.connect()
                            }
                        },
                        enabled = bluetoothState.enabled() && allowedToConnect,
                        primaryColor = false,
                        modifier = Modifier.padding(vertical = 5.dp),
                    )
                }
            }
        }
        WatchMenu(watch, navBarNav)
    }
}

@Composable
private fun ConnectionFailureGuidanceButton(
    appContext: AppContext,
    pebbleFeatures: PebbleFeatures,
    buttonText: String,
    dialogTitle: String,
    dialogText: String,
    showBluetoothSettingsLink: Boolean = true,
) {
    var showDialog by remember { mutableStateOf(false) }
    val canDeepLink = showBluetoothSettingsLink && pebbleFeatures.supportsLinkingToOsBtSettings()
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogText) },
            confirmButton = {
                if (canDeepLink) {
                    TextButton(onClick = {
                        openSystemBluetoothSettings(appContext)
                        showDialog = false
                    }) { Text(stringResource(Res.string.open_bluetooth_settings)) }
                } else {
                    TextButton(onClick = { showDialog = false }) { Text(stringResource(Res.string.ok)) }
                }
            },
            dismissButton = if (canDeepLink) {
                { TextButton(onClick = { showDialog = false }) { Text(stringResource(Res.string.cancel)) } }
            } else null,
        )
    }
    PebbleElevatedButton(
        text = buttonText,
        icon = Icons.Default.Error,
        onClick = { showDialog = true },
        primaryColor = true,
        modifier = Modifier.padding(vertical = 5.dp),
    )
}

fun Int.batteryIcon() = when {
    this >= 98 -> Icons.Default.BatteryFull
    this >= 82 -> Icons.Default.Battery6Bar
    this >= 67 -> Icons.Default.Battery5Bar
    this >= 51 -> Icons.Default.Battery4Bar
    this >= 36 -> Icons.Default.Battery3Bar
    this >= 20 -> Icons.Default.Battery2Bar
    this >= 5 -> Icons.Default.Battery1Bar
    else -> Icons.Default.Battery0Bar
}

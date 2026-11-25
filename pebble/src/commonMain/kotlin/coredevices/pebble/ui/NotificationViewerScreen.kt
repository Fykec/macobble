package coredevices.pebble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coredevices.pebble.Platform
import coredevices.pebble.rememberLibPebble
import io.rebble.libpebblecommon.connection.NotificationApps
import io.rebble.libpebblecommon.notification.NotificationDecision
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import coreapp.pebble.generated.resources.notif_decision_sent
import coreapp.pebble.generated.resources.notif_decision_local_only
import coreapp.pebble.generated.resources.notif_decision_group_summary
import coreapp.pebble.generated.resources.notif_decision_app_muted
import coreapp.pebble.generated.resources.notif_decision_channel_muted
import coreapp.pebble.generated.resources.notif_decision_duplicate
import coreapp.pebble.generated.resources.notif_decision_contact_muted
import coreapp.pebble.generated.resources.notif_decision_screen_on
import coreapp.pebble.generated.resources.notif_decision_rule_filtered
import coreapp.pebble.generated.resources.notif_decision_empty
import org.jetbrains.compose.resources.stringResource
import coreapp.pebble.generated.resources.Res as PebbleRes

@Composable
fun AppNotificationViewerScreen(
    topBarParams: TopBarParams,
    nav: NavBarNav,
    packageName: String,
    channelId: String?,
) {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        LaunchedEffect(Unit) {
            topBarParams.searchAvailable(null)
            topBarParams.actions {}
        }
        val libPebble = rememberLibPebble()
        val appWrapperFlow = remember(packageName) {
            libPebble.notificationApps()
                .map { it.firstOrNull { it.app.packageName == packageName } }
        }
        val appWrapper by appWrapperFlow.collectAsState(null)
        val channel =
            appWrapper?.app?.channelGroups?.flatMap { it.channels }
                ?.firstOrNull { it.id == channelId }
        appWrapper?.let { appWrapper ->
            val bootConfig = rememberBootConfig()
            val platform = koinInject<Platform>()
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIconImage(
                        entry = appWrapper,
                        modifier = Modifier.width(100.dp).height(100.dp).padding(15.dp),
                        notificationApps = libPebble,
                        bootConfig = bootConfig,
                        platform = platform,
                        showBadge = false,
                    )
                    val text = if (channelId == null) {
                        "Recent notifications for ${appWrapper.app.name}"
                    } else {
                        "Recent notifications for ${appWrapper.app.name} in channel: ${channel?.name}"
                    }
                    Text(text, modifier = Modifier.padding(8.dp))
                }
                NotificationHistoryList(
                    packageName = packageName,
                    channelId = channelId,
                    contactId = null,
                    limit = 25,
                    showAppIcon = false,
                )
            }
        }
    }
}

@Composable
fun NotificationHistoryList(
    packageName: String?,
    channelId: String?,
    contactId: String?,
    limit: Int,
    showAppIcon: Boolean,
) {
    val libPebble = rememberLibPebble()
    val notificationsFlow = remember(packageName, channelId) {
        libPebble.mostRecentNotificationsFor(
            pkg = packageName,
            channelId = channelId,
            contactId = contactId,
            limit = limit,
        ).map { it.filter { it.decision != NotificationDecision.NotSentGroupSummary } }
    }
    val bootConfig = rememberBootConfig()
    val notifications by notificationsFlow.collectAsState(emptyList())
    val notificationApi: NotificationApps = koinInject()
    val platform = koinInject<Platform>()

    LazyColumn {
        items(notifications, key = { it.id }) { notification ->
            val appWrapperFlow = remember(packageName) {
                notificationApi.notificationApps()
                    .map { it.firstOrNull { it.app.packageName == notification.pkg } }
            }
            val appWrapper by appWrapperFlow.collectAsState(null)
            val app = appWrapper
            ListItem(
                headlineContent = {
                    Text(
                        text = notification.title ?: "<Empty>",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                supportingContent = {
                    Text(
                        text = notification.body?.take(100)?.replace("\n", " ")
                            ?: "<Empty>",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                },
                leadingContent = {
                    if (showAppIcon && app != null) {
                        AppIconImage(
                            entry = app,
                            modifier = Modifier.width(HEIGHT),
                            notificationApps = notificationApi,
                            bootConfig = bootConfig,
                            platform = platform,
                            showBadge = false,
                        )
                    }
                },
                trailingContent = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            notification.timestamp.instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                .format(FORMAT),
                        )
                        val sentText =
                            if (notification.decision == NotificationDecision.SendToWatch) {
                                "Displayed"
                            } else {
                                "Not displayed - ${notification.decision.displayName()}"
                            }
                        Text(sentText, fontSize = 10.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun NotificationDecision.displayName(): String = when (this) {
    NotificationDecision.SendToWatch -> stringResource(PebbleRes.string.notif_decision_sent)
    NotificationDecision.NotSentLocalOnly -> stringResource(PebbleRes.string.notif_decision_local_only)
    NotificationDecision.NotSentGroupSummary -> stringResource(PebbleRes.string.notif_decision_group_summary)
    NotificationDecision.NotSentAppMuted -> stringResource(PebbleRes.string.notif_decision_app_muted)
    NotificationDecision.NotSendChannelMuted -> stringResource(PebbleRes.string.notif_decision_channel_muted)
    NotificationDecision.NotSentDuplicate -> stringResource(PebbleRes.string.notif_decision_duplicate)
    NotificationDecision.NotSendContactMuted -> stringResource(PebbleRes.string.notif_decision_contact_muted)
    NotificationDecision.NotSentScreenOn -> stringResource(PebbleRes.string.notif_decision_screen_on)
    NotificationDecision.NotSentRuleFiltered -> stringResource(PebbleRes.string.notif_decision_rule_filtered)
    NotificationDecision.NotSentEmpty -> stringResource(PebbleRes.string.notif_decision_empty)
}

private val FORMAT = LocalDateTime.Format {
    date(LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); dayOfMonth(); chars(
        " "
    );
    })
    chars(" - ")
    time(LocalTime.Format { hour(); char(':'); minute() })
}

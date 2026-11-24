package coredevices.coreapp.ui.navigation

import CommonRoutes
import CoreNav
import CoreRoute
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import coredevices.coreapp.ui.SpeechModelDownloadDialog
import coredevices.coreapp.ui.screens.BugReportScreen
import coredevices.coreapp.ui.screens.BugReportsListScreen
import coredevices.coreapp.ui.screens.OnboardingScreen
import coredevices.coreapp.ui.screens.ViewBugReportScreen
import coredevices.coreapp.ui.screens.WatchOnboardingScreen
import coredevices.pebble.PebbleDeepLinkHandler
import coredevices.pebble.ui.PebbleRoutes
import coredevices.pebble.ui.addPebbleRoutes
import coredevices.pebble.ui.toNavBarRoute
import coredevices.ui.GenericWebViewScreen
import coredevices.util.BugReportContext
import coredevices.util.CommonBuildKonfig
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import coreapp.composeapp.generated.resources.Res as AppRes
import coreapp.composeapp.generated.resources.alpha_test_instructions
import coreapp.composeapp.generated.resources.getting_started_troubleshooting
import coreapp.composeapp.generated.resources.what_s_new_in_pebbleos
import coreapp.composeapp.generated.resources.what_s_new_in_the_app

private val logger = Logger.withTag("AppNavHost")

@Composable
fun AppNavHost(navController: NavHostController, startDestination: Any) {
    val deepLinks: CoreDeepLinkHandler = koinInject()
    val pebbleDeepLinks: PebbleDeepLinkHandler = koinInject()
    val scope = rememberCoroutineScope()
    LaunchedEffect(navController) { // React to NavController changes
        // This ensures the graph is available before doing any deep link
        snapshotFlow { navController.graph }
            .filterNotNull() // Wait until the graph is not null
            .first()
            .let {
                // Now that we know the graph is set up, start collecting deep links
                scope.launch {
                    deepLinks.navigateToDeepLink.collect { route ->
                        logger.d { "navigateToDeepLink $route" }
                        try {
                            if (route is NavUri) {
                                navController.navigate(route)
                            } else {
                                navController.navigate(route)
                            }
                        } catch (e: IllegalArgumentException) {
                            logger.w(e) { "Failed to navigate to $route" }
                        }
                        deepLinks.clearPendingDeepLink()
                    }
                }
                scope.launch {
                    pebbleDeepLinks.navigateToPebbleDeepLink.collect { route ->
                        if (route == null || route.consumed) {
                            return@collect
                        }
                        val isOnWatchHome = navController.currentBackStackEntry?.destination?.hasRoute<PebbleRoutes.WatchHomeRoute>() == true
                        // Pebble deep links need us to navigate to WatchHomeScreen - but only if we
                        // aren't already there.
                        if (!isOnWatchHome) {
                            logger.d { "Navigating to WatchHomeRoute for pebble deep link" }
                            navController.navigate(PebbleRoutes.WatchHomeRoute)
                        }
                    }
                }
            }
    }
    val coreNav = remember {
        object : CoreNav {
            override fun navigateTo(route: CoreRoute) {
                if (route is CommonRoutes.LockerAppRoute) {
                    pebbleDeepLinks.navigateToTab(route.toNavBarRoute())
                    return
                }
                if (route is CommonRoutes.LockerTabRoute) {
                    pebbleDeepLinks.navigateToLockerTab(route.initialTab)
                    return
                }
                if (route == PebbleRoutes.WatchHomeRoute) {
                    navController.popBackStack(CommonRoutes.OnboardingRoute, true)
                }
                navController.navigate(route)
            }

            override fun goBack() {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }

            override fun goBackToPebble() {
                navController.popBackStack(PebbleRoutes.WatchHomeRoute, inclusive = false)
            }

            override fun replaceWith(route: CoreRoute) {
                navController.popBackStack()
                navController.navigate(route)
            }
        }
    }
    NavHost(navController, startDestination = startDestination) {
        ringNavGraph(coreNav)
        addPebbleRoutes(
            coreNav,
            indexScreen = ringIndexScreen(coreNav),
            addExperimentalRoutes = { scopedCoreNav ->
                ringNavGraph(scopedCoreNav)
            },
            isInnerScopedRoute = { isRingRoute(it) },
        )
        dialog<CommonRoutes.SpeechModelDownloadDialog> {
            SpeechModelDownloadDialog(onDismiss = { coreNav.goBack() })
        }
        if (CommonBuildKonfig.QA) {
            composable<CommonRoutes.BugReport>(
                deepLinks = listOf(
                    NavDeepLink("pebblecore://deep-link/bug-report?pebble={pebble}&context={context}&text={text}")
                )
            ) {
                val route: CommonRoutes.BugReport = it.toRoute()
                BugReportScreen(
                    coreNav = coreNav,
                    pebble = route.pebble,
                    recordingPath = route.recordingPath,
                    screenshotPath = route.screenshotPath,
                    screenContext = BugReportContext.decodeBugReportContext(route.context),
                    initialText = route.text,
                )
            }
            composable<CommonRoutes.AlphaTestInstructionsRoute> {
                GenericWebViewScreen(
                    coreNav = coreNav,
                    title = stringResource(AppRes.string.alpha_test_instructions),
                    url = "https://ndocs.repebble.com/alpha-tester-guide",
                )
            }
            composable<CommonRoutes.ViewMyBugReportsRoute> {
                BugReportsListScreen(
                    coreNav = coreNav,
                )
            }
            composable<CommonRoutes.ViewBugReportRoute>(
                deepLinks = listOf(
                    NavDeepLink("pebblecore://deep-link/view-bug-report?conversationId={conversationId}")
                )
            ) {
                val route: CommonRoutes.ViewBugReportRoute = it.toRoute()
                ViewBugReportScreen(
                    coreNav = coreNav,
                    conversationId = route.conversationId,
                )
            }
            composable<CommonRoutes.RoadmapChangelogRoute> {
                GenericWebViewScreen(
                    coreNav = coreNav,
                    title = stringResource(AppRes.string.what_s_new_in_the_app),
                    url = "https://ndocs.repebble.com/changelog",
                )
            }
            composable<CommonRoutes.PebbleOsChangelogRoute> {
                GenericWebViewScreen(
                    coreNav = coreNav,
                    title = stringResource(AppRes.string.what_s_new_in_pebbleos),
                    url = "https://ndocs.repebble.com/pebbleos-changelog",
                )
            }
            composable<CommonRoutes.TroubleshootingRoute> {
                GenericWebViewScreen(
                    coreNav = coreNav,
                    title = stringResource(AppRes.string.getting_started_troubleshooting),
                    url = "https://pbl.zip/in-app-getting-started-and-troubleshooting",
                )
            }
            composable<CommonRoutes.OnboardingRoute> {
                OnboardingScreen(
                    coreNav = coreNav,
                )
            }
            composable<CommonRoutes.WatchOnboardingRoute> {
                WatchOnboardingScreen(
                    coreNav = coreNav,
                )
            }
            ringOnboardingRoute(coreNav)
        }
    }
}
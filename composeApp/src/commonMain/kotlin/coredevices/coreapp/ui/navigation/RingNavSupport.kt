package coredevices.coreapp.ui.navigation

import CoreNav
import CoreRoute
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import coredevices.pebble.ui.NavBarNav
import coredevices.pebble.ui.TopBarParams

/** Ring navigation hooks; real implementations live in `ringMain` (Android/iOS). macOS has none. */
expect fun NavGraphBuilder.ringNavGraph(coreNav: CoreNav)

expect fun ringIndexScreen(coreNav: CoreNav): @Composable (TopBarParams, NavBarNav, CoreNav) -> Unit

expect fun isRingRoute(route: CoreRoute): Boolean

expect fun NavGraphBuilder.ringOnboardingRoute(coreNav: CoreNav)

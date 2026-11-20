package coredevices.coreapp.ui.navigation

import CoreNav
import CoreRoute
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import coredevices.pebble.ui.NavBarNav
import coredevices.pebble.ui.TopBarParams

actual fun NavGraphBuilder.ringNavGraph(coreNav: CoreNav) = Unit

actual fun ringIndexScreen(coreNav: CoreNav): @Composable (TopBarParams, NavBarNav, CoreNav) -> Unit =
    { _, _, _ -> }

actual fun isRingRoute(route: CoreRoute): Boolean = false

actual fun NavGraphBuilder.ringOnboardingRoute(coreNav: CoreNav) = Unit

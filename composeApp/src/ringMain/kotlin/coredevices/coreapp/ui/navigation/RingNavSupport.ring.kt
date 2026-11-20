package coredevices.coreapp.ui.navigation

import CommonRoutes
import CoreNav
import CoreRoute
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coredevices.pebble.ui.NavBarNav
import coredevices.pebble.ui.TopBarParams
import coredevices.ring.ui.navigation.RingRoute
import coredevices.ring.ui.navigation.addRingRoutes
import coredevices.ring.ui.screens.IndexScreen
import coredevices.ring.ui.theme.IndexThemeHost
import coredevices.coreapp.ui.screens.ringonboarding.RingOnboardingScreen

actual fun NavGraphBuilder.ringNavGraph(coreNav: CoreNav) {
    addRingRoutes(coreNav)
}

actual fun ringIndexScreen(coreNav: CoreNav): @Composable (TopBarParams, NavBarNav, CoreNav) -> Unit =
    { topBarParams, _, scopedCoreNav ->
        IndexScreen(scopedCoreNav, topBarParams)
    }

actual fun isRingRoute(route: CoreRoute): Boolean = route is RingRoute

actual fun NavGraphBuilder.ringOnboardingRoute(coreNav: CoreNav) {
    composable<CommonRoutes.RingOnboardingRoute> {
        IndexThemeHost {
            RingOnboardingScreen(
                coreNav = coreNav,
            )
        }
    }
}

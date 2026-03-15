package idv.fan.iisigroup.android.test.feature.flight

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object FlightNav

fun NavGraphBuilder.flightNavigation() {
    composable<FlightNav> {
        FlightRoute()
    }
}

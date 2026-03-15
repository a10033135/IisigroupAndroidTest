package idv.fan.iisigroup.android.test.feature.exchangeRate

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ExchangeRateNav

fun NavGraphBuilder.exchangeRateNavigation() {
    composable<ExchangeRateNav> {
        ExchangeRateRoute()
    }
}

package idv.fan.iisigroup.android.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import idv.fan.iisigroup.android.test.feature.exchangeRate.ExchangeRateNav
import idv.fan.iisigroup.android.test.feature.exchangeRate.exchangeRateNavigation
import idv.fan.iisigroup.android.test.feature.flight.FlightNav
import idv.fan.iisigroup.android.test.feature.flight.flightNavigation
import idv.fan.iisigroup.android.test.feature.setting.SettingNav
import idv.fan.iisigroup.android.test.feature.setting.settingNavigation
import idv.fan.iisigroup.android.test.ui.theme.IisigroupAndroidTestTheme
import kotlin.reflect.KClass

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IisigroupAndroidTestTheme {
                IisigroupAndroidTestApp()
            }
        }
    }
}

@Composable
fun IisigroupAndroidTestApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                val selected = currentDestination?.hasRoute(destination.routeClass) == true
                item(
                    icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelResId)) },
                    label = { Text(stringResource(destination.labelResId)) },
                    selected = selected,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = FlightNav,
            modifier = Modifier.fillMaxSize(),
        ) {
            flightNavigation()
            exchangeRateNavigation()
            settingNavigation()
        }
    }
}

enum class AppDestinations(
    @StringRes val labelResId: Int,
    val icon: ImageVector,
    val route: Any,
    val routeClass: KClass<*>,
) {
    FLIGHT(R.string.tab_flight, Icons.Default.List, FlightNav, FlightNav::class),
    EXCHANGE_RATE(R.string.tab_exchange_rate, Icons.Default.Star, ExchangeRateNav, ExchangeRateNav::class),
    SETTING(R.string.tab_setting, Icons.Default.Settings, SettingNav, SettingNav::class),
}

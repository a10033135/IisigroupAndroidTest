package idv.fan.iisigroup.android.test.feature.setting

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object SettingNav

fun NavGraphBuilder.settingNavigation() {
    composable<SettingNav> {
        SettingRoute()
    }
}

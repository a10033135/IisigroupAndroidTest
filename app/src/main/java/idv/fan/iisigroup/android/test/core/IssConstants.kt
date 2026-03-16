package idv.fan.iisigroup.android.test.core

import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.SyncInterval

object IssConstants {

    object DataStore {
        const val USER_PREFERENCES_NAME = "user_preferences"
    }

    object UserPreferences {
        const val DEFAULT_DARK_THEME = false
        const val DEFAULT_AUTO_SYNC_ENABLED = true
        val DEFAULT_SYNC_INTERVAL: SyncInterval = SyncInterval.TEN_SECONDS
        val DEFAULT_CURRENCY: Currency = Currency.USD
    }

    object ExchangeRate {
        const val DEFAULT_CALCULATOR_AMOUNT = 1.0
    }
}

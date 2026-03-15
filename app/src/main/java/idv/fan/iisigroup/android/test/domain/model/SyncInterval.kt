package idv.fan.iisigroup.android.test.domain.model

enum class SyncInterval(val ms: Long) {
    TEN_SECONDS(10_000L),
    ONE_MINUTE(60_000L),
    FIVE_MINUTES(300_000L),
    ;

    companion object {
        val default = TEN_SECONDS
        fun fromMs(ms: Long): SyncInterval = entries.find { it.ms == ms } ?: default
    }
}

package idv.fan.iisigroup.android.test.domain.model

enum class SyncInterval(val label: String, val ms: Long) {
    TEN_SECONDS("10 秒", 10_000L),
    ONE_MINUTE("1 分鐘", 60_000L),
    FIVE_MINUTES("5 分鐘", 300_000L),
    ;

    companion object {
        val default = TEN_SECONDS
        fun fromMs(ms: Long): SyncInterval = entries.find { it.ms == ms } ?: default
    }
}

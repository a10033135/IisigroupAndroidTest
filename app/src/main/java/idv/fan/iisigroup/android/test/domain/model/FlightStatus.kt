package idv.fan.iisigroup.android.test.domain.model

enum class FlightStatus {
    ARRIVED, DELAYED, CANCELLED, DEPARTED, UNKNOWN;

    companion object {
        fun fromString(value: String?): FlightStatus = when (value) {
            "抵達" -> ARRIVED
            "延誤" -> DELAYED
            "取消" -> CANCELLED
            "起飛" -> DEPARTED
            else -> UNKNOWN
        }
    }
}

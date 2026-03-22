package idv.fan.iisigroup.android.test.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FlightStatusTest {

    // -------------------------------------------------------------------------
    // 1. fromString "抵達" returns ARRIVED
    // -------------------------------------------------------------------------
    @Test
    fun `fromString 抵達 returns ARRIVED`() {
        assertEquals(FlightStatus.ARRIVED, FlightStatus.fromString("抵達"))
    }

    // -------------------------------------------------------------------------
    // 2. fromString "延誤" returns DELAYED
    // -------------------------------------------------------------------------
    @Test
    fun `fromString 延誤 returns DELAYED`() {
        assertEquals(FlightStatus.DELAYED, FlightStatus.fromString("延誤"))
    }

    // -------------------------------------------------------------------------
    // 3. fromString "取消" returns CANCELLED
    // -------------------------------------------------------------------------
    @Test
    fun `fromString 取消 returns CANCELLED`() {
        assertEquals(FlightStatus.CANCELLED, FlightStatus.fromString("取消"))
    }

    // -------------------------------------------------------------------------
    // 4. fromString "起飛" returns DEPARTED
    // -------------------------------------------------------------------------
    @Test
    fun `fromString 起飛 returns DEPARTED`() {
        assertEquals(FlightStatus.DEPARTED, FlightStatus.fromString("起飛"))
    }

    // -------------------------------------------------------------------------
    // 5. fromString null returns UNKNOWN
    // -------------------------------------------------------------------------
    @Test
    fun `fromString null returns UNKNOWN`() {
        assertEquals(FlightStatus.UNKNOWN, FlightStatus.fromString(null))
    }

    // -------------------------------------------------------------------------
    // 6. fromString unknown string returns UNKNOWN
    // -------------------------------------------------------------------------
    @Test
    fun `fromString unknown string returns UNKNOWN`() {
        assertEquals(FlightStatus.UNKNOWN, FlightStatus.fromString("SomeUnknownStatus"))
    }
}

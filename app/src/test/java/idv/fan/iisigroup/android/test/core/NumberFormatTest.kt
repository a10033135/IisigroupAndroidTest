package idv.fan.iisigroup.android.test.core

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormatTest {

    // -------------------------------------------------------------------------
    // 1. formatNumber 1.0 returns "1"
    // -------------------------------------------------------------------------
    @Test
    fun `formatNumber 1_0 returns 1`() {
        assertEquals("1", formatNumber(1.0))
    }

    // -------------------------------------------------------------------------
    // 2. formatNumber 3.14 returns "3.14"
    // -------------------------------------------------------------------------
    @Test
    fun `formatNumber 3_14 returns 3_14`() {
        assertEquals("3.14", formatNumber(3.14))
    }

    // -------------------------------------------------------------------------
    // 3. formatNumber 3.1400 returns "3.14"
    // -------------------------------------------------------------------------
    @Test
    fun `formatNumber 3_1400 returns 3_14`() {
        assertEquals("3.14", formatNumber(3.1400))
    }

    // -------------------------------------------------------------------------
    // 4. formatNumber 0.0001 returns "0.0001"
    // -------------------------------------------------------------------------
    @Test
    fun `formatNumber 0_0001 returns 0_0001`() {
        assertEquals("0.0001", formatNumber(0.0001))
    }

    // -------------------------------------------------------------------------
    // 5. formatNumber 100.0 returns "100"
    // -------------------------------------------------------------------------
    @Test
    fun `formatNumber 100_0 returns 100`() {
        assertEquals("100", formatNumber(100.0))
    }
}

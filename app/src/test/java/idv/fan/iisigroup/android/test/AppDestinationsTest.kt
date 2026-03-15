package idv.fan.iisigroup.android.test

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationsTest {

    @Test
    fun `AppDestinations contains exactly four entries`() {
        assertEquals(4, AppDestinations.entries.size)
    }

    @Test
    fun `HOME has correct label`() {
        assertEquals("Home", AppDestinations.HOME.label)
    }

    @Test
    fun `FAVORITES has correct label`() {
        assertEquals("Favorites", AppDestinations.FAVORITES.label)
    }

    @Test
    fun `PROFILE has correct label`() {
        assertEquals("Profile", AppDestinations.PROFILE.label)
    }

    @Test
    fun `FLIGHT has correct label`() {
        assertEquals("航班", AppDestinations.FLIGHT.label)
    }

    @Test
    fun `first entry is HOME`() {
        assertEquals(AppDestinations.HOME, AppDestinations.entries.first())
    }

    @Test
    fun `last entry is FLIGHT`() {
        assertEquals(AppDestinations.FLIGHT, AppDestinations.entries.last())
    }

    @Test
    fun `entries order is HOME FAVORITES PROFILE FLIGHT`() {
        val expected = listOf(
            AppDestinations.HOME,
            AppDestinations.FAVORITES,
            AppDestinations.PROFILE,
            AppDestinations.FLIGHT,
        )
        assertEquals(expected, AppDestinations.entries.toList())
    }
}

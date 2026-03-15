package idv.fan.iisigroup.android.test

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationsTest {

    @Test
    fun `AppDestinations contains exactly three entries`() {
        assertEquals(3, AppDestinations.entries.size)
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
    fun `first entry is HOME`() {
        assertEquals(AppDestinations.HOME, AppDestinations.entries.first())
    }

    @Test
    fun `last entry is PROFILE`() {
        assertEquals(AppDestinations.PROFILE, AppDestinations.entries.last())
    }

    @Test
    fun `entries order is HOME FAVORITES PROFILE`() {
        val expected = listOf(
            AppDestinations.HOME,
            AppDestinations.FAVORITES,
            AppDestinations.PROFILE
        )
        assertEquals(expected, AppDestinations.entries.toList())
    }
}

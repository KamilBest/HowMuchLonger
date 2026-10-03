package com.icyapps.howmuchlonger.ui.screen.eventlist

import org.junit.Assert.assertEquals
import org.junit.Test

class CountryFlagTest {
    @Test
    fun `country code is converted to flag emoji`() {
        assertEquals("🇵🇱", countryFlag("PL"))
        assertEquals("🇺🇸", countryFlag("us"))
    }

    @Test
    fun `invalid country code has no flag`() {
        assertEquals("", countryFlag("POL"))
        assertEquals("", countryFlag("1A"))
        assertEquals("", countryFlag(""))
    }
}

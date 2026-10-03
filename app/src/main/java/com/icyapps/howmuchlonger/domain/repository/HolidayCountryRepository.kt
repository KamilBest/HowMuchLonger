package com.icyapps.howmuchlonger.domain.repository

import com.icyapps.howmuchlonger.domain.model.HolidayCountry

interface HolidayCountryRepository {
    /** Countries supported by the holiday API, or an empty list when they cannot be loaded. */
    suspend fun getAvailableCountries(): List<HolidayCountry>

    /** Country codes chosen by the user, or null when nothing has been chosen yet. */
    fun getSelectedCountryCodes(): Set<String>?
    fun setSelectedCountryCodes(countryCodes: Set<String>)
}

package com.icyapps.howmuchlonger.data.source

import com.icyapps.howmuchlonger.data.model.AvailableCountryDto
import com.icyapps.howmuchlonger.data.model.PublicHolidayDto
import com.icyapps.howmuchlonger.data.model.PublicHolidayApi

interface PublicHolidayDataSource {
    suspend fun getPublicHolidays(year: Int, countryCode: String): List<PublicHolidayDto>
    suspend fun getAvailableCountries(): List<AvailableCountryDto>
}

class PublicHolidayApiDataSource(private val api: PublicHolidayApi) : PublicHolidayDataSource {
    override suspend fun getPublicHolidays(year: Int, countryCode: String): List<PublicHolidayDto> {
        return api.getPublicHolidays(year, countryCode)
    }

    override suspend fun getAvailableCountries(): List<AvailableCountryDto> {
        return api.getAvailableCountries()
    }
} 
package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.HolidayCountry
import com.icyapps.howmuchlonger.domain.repository.HolidayCountryRepository
import javax.inject.Inject

class GetAvailableHolidayCountriesUseCase @Inject constructor(
    private val repository: HolidayCountryRepository
) {
    suspend operator fun invoke(): List<HolidayCountry> = repository.getAvailableCountries()
}

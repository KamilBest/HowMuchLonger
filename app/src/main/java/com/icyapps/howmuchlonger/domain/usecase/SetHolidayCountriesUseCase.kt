package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.repository.HolidayCountryRepository
import javax.inject.Inject

class SetHolidayCountriesUseCase @Inject constructor(
    private val repository: HolidayCountryRepository
) {
    operator fun invoke(countryCodes: Set<String>) {
        require(countryCodes.isNotEmpty()) { "At least one holiday country must be selected" }
        repository.setSelectedCountryCodes(countryCodes)
    }
}

package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.HolidayCountry
import com.icyapps.howmuchlonger.domain.repository.HolidayCountryRepository
import com.icyapps.howmuchlonger.domain.repository.LanguageCountryProvider
import javax.inject.Inject

/** Country codes whose holidays should be shown: the user's selection, or a default based on the device language. */
class GetHolidayCountriesUseCase @Inject constructor(
    private val repository: HolidayCountryRepository,
    private val languageCountryProvider: LanguageCountryProvider
) {
    suspend operator fun invoke(): List<String> {
        repository.getSelectedCountryCodes()
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it.sorted() }
        return listOf(defaultCountryCode())
    }

    private suspend fun defaultCountryCode(): String {
        val languageCountry = languageCountryProvider.languageCountryCode()
            ?: return HolidayCountry.DEFAULT_CODE
        val available = repository.getAvailableCountries()
        return when {
            // Offline the language country cannot be verified, so try it; the user can still change it.
            available.isEmpty() -> languageCountry
            available.any { it.code == languageCountry } -> languageCountry
            else -> HolidayCountry.DEFAULT_CODE
        }
    }
}

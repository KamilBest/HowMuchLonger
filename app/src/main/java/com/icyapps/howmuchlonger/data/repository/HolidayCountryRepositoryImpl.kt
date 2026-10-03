package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.source.PublicHolidayDataSource
import com.icyapps.howmuchlonger.data.store.HolidayCountryStore
import com.icyapps.howmuchlonger.domain.model.HolidayCountry
import com.icyapps.howmuchlonger.domain.repository.HolidayCountryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class HolidayCountryRepositoryImpl(
    private val publicHolidayDataSource: PublicHolidayDataSource,
    private val holidayCountryStore: HolidayCountryStore
) : HolidayCountryRepository {

    private val loadLock = Mutex()
    private var cachedCountries: List<HolidayCountry>? = null

    override suspend fun getAvailableCountries(): List<HolidayCountry> = loadLock.withLock {
        cachedCountries ?: try {
            withContext(Dispatchers.IO) { publicHolidayDataSource.getAvailableCountries() }
                .map { HolidayCountry(code = it.countryCode.uppercase(), name = it.name) }
                .also { cachedCountries = it }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            // Do not cache failures so the list can be loaded once the device is back online.
            emptyList()
        }
    }

    override fun getSelectedCountryCodes(): Set<String>? = holidayCountryStore.getSelectedCountryCodes()

    override fun setSelectedCountryCodes(countryCodes: Set<String>) {
        holidayCountryStore.setSelectedCountryCodes(countryCodes.mapTo(mutableSetOf()) { it.uppercase() })
    }
}

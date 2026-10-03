package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.HolidayCountry
import com.icyapps.howmuchlonger.domain.repository.HolidayCountryRepository
import com.icyapps.howmuchlonger.domain.repository.LanguageCountryProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetHolidayCountriesUseCaseTest {
    private lateinit var repository: HolidayCountryRepository
    private lateinit var languageCountryProvider: LanguageCountryProvider
    private lateinit var useCase: GetHolidayCountriesUseCase

    private val available = listOf(
        HolidayCountry("PL", "Poland"),
        HolidayCountry("US", "United States"),
        HolidayCountry("DE", "Germany")
    )

    @Before
    fun setup() {
        repository = mockk()
        languageCountryProvider = mockk()
        every { repository.getSelectedCountryCodes() } returns null
        coEvery { repository.getAvailableCountries() } returns available
        useCase = GetHolidayCountriesUseCase(repository, languageCountryProvider)
    }

    @Test
    fun `selected countries take precedence over device language`() = runTest {
        every { repository.getSelectedCountryCodes() } returns setOf("PL", "DE")
        every { languageCountryProvider.languageCountryCode() } returns "US"

        assertEquals(listOf("DE", "PL"), useCase())
        coVerify(exactly = 0) { repository.getAvailableCountries() }
    }

    @Test
    fun `empty selection falls back to default country`() = runTest {
        every { repository.getSelectedCountryCodes() } returns emptySet()
        every { languageCountryProvider.languageCountryCode() } returns "PL"

        assertEquals(listOf("PL"), useCase())
    }

    @Test
    fun `language country is used when it is supported`() = runTest {
        every { languageCountryProvider.languageCountryCode() } returns "PL"

        assertEquals(listOf("PL"), useCase())
    }

    @Test
    fun `unsupported language country falls back to US`() = runTest {
        every { languageCountryProvider.languageCountryCode() } returns "IN"

        assertEquals(listOf(HolidayCountry.DEFAULT_CODE), useCase())
    }

    @Test
    fun `unknown language country falls back to US`() = runTest {
        every { languageCountryProvider.languageCountryCode() } returns null

        assertEquals(listOf(HolidayCountry.DEFAULT_CODE), useCase())
    }

    @Test
    fun `language country is used when supported countries cannot be loaded`() = runTest {
        every { languageCountryProvider.languageCountryCode() } returns "IN"
        coEvery { repository.getAvailableCountries() } returns emptyList()

        assertEquals(listOf("IN"), useCase())
    }
}

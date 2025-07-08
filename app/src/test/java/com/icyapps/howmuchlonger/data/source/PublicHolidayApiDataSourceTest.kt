package com.icyapps.howmuchlonger.data.source

import com.icyapps.howmuchlonger.data.model.PublicHolidayApi
import com.icyapps.howmuchlonger.data.model.PublicHolidayDto
import com.icyapps.howmuchlonger.data.source.PublicHolidayApiDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class PublicHolidayApiDataSourceTest {
    private lateinit var api: PublicHolidayApi
    private lateinit var dataSource: PublicHolidayApiDataSource
    private val holidays = listOf(
        PublicHolidayDto(
            date = "2023-01-01",
            localName = "New Year",
            name = "New Year's Day",
            countryCode = "PL",
            fixed = true,
            global = true,
            counties = null,
            launchYear = 1950,
            types = listOf("Public")
        )
    )

    @Before
    fun setup() {
        api = mockk()
        dataSource = PublicHolidayApiDataSource(api)
    }

    @Test
    fun `getPublicHolidays calls api and returns result`() = runTest {
        coEvery { api.getPublicHolidays(2023, "PL") } returns holidays
        val result = dataSource.getPublicHolidays(2023, "PL")
        assertEquals(holidays, result)
    }

    @Test
    fun `getPublicHolidays throws exception on API error`() = runTest {
        coEvery { api.getPublicHolidays(any(), any()) } throws RuntimeException("Network error")
        assertThrows(RuntimeException::class.java) {
            runTest { dataSource.getPublicHolidays(2023, "PL") }
        }
    }

    @Test
    fun `getPublicHolidays returns empty list`() = runTest {
        coEvery { api.getPublicHolidays(any(), any()) } returns emptyList()
        val result = dataSource.getPublicHolidays(2023, "PL")
        assertEquals(emptyList<PublicHolidayDto>(), result)
    }
} 
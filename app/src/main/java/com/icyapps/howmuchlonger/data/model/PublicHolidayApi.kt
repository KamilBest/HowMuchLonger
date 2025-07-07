package com.icyapps.howmuchlonger.data.model

import retrofit2.http.GET
import retrofit2.http.Path

interface PublicHolidayApi {
    @GET("publicholidays/{year}/{countryCode}")
    suspend fun getPublicHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): List<PublicHolidayDto>
} 
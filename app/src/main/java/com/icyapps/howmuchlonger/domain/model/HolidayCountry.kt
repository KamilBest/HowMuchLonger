package com.icyapps.howmuchlonger.domain.model

data class HolidayCountry(
    val code: String,
    val name: String
) {
    companion object {
        const val DEFAULT_CODE = "US"
    }
}

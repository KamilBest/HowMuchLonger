package com.icyapps.howmuchlonger.domain.repository

interface LanguageCountryProvider {
    /** The country most associated with the device language (e.g. "en" -> "US", "pl" -> "PL"). */
    fun languageCountryCode(): String?
}

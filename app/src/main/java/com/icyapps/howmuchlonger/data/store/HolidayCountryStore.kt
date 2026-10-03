package com.icyapps.howmuchlonger.data.store

import android.content.Context
import androidx.core.content.edit

interface HolidayCountryStore {
    fun getSelectedCountryCodes(): Set<String>?
    fun setSelectedCountryCodes(countryCodes: Set<String>)
}

class HolidayCountryPreferencesStore(context: Context) : HolidayCountryStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getSelectedCountryCodes(): Set<String>? =
        preferences.getStringSet(KEY_SELECTED_COUNTRIES, null)?.toSet()

    override fun setSelectedCountryCodes(countryCodes: Set<String>) {
        preferences.edit { putStringSet(KEY_SELECTED_COUNTRIES, countryCodes) }
    }

    private companion object {
        const val PREFERENCES_NAME = "holiday_settings"
        const val KEY_SELECTED_COUNTRIES = "selected_countries"
    }
}

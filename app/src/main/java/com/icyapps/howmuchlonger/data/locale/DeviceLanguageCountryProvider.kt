package com.icyapps.howmuchlonger.data.locale

import android.icu.util.ULocale
import com.icyapps.howmuchlonger.domain.repository.LanguageCountryProvider
import java.util.Locale

/**
 * Resolves the country from the device language only, ignoring the region,
 * so "en-PL" resolves to "US" and "pl-PL" to "PL".
 */
class DeviceLanguageCountryProvider : LanguageCountryProvider {
    override fun languageCountryCode(): String? {
        val language = Locale.getDefault().language.ifBlank { return null }
        return ULocale.addLikelySubtags(ULocale(language)).country
            .uppercase()
            .takeIf { it.length == 2 }
    }
}

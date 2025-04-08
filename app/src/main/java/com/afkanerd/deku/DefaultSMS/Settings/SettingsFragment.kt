package com.afkanerd.deku.DefaultSMS.Settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: android.os.Bundle?,
        rootKey: kotlin.String?
    ) {
        setPreferencesFromResource(com.afkanerd.deku.DefaultSMS.R.xml.settings_preferences, rootKey)

        val languagePreference =
            findPreference<androidx.preference.ListPreference?>(getString(com.afkanerd.deku.DefaultSMS.R.string.settings_locale))

        languagePreference!!.onPreferenceChangeListener = object :
            androidx.preference.Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(
                preference: androidx.preference.Preference,
                newValue: kotlin.Any?
            ): kotlin.Boolean {
                val languageLocale = newValue as kotlin.String
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context?.getSystemService<android.app.LocaleManager?>(
                        android.app.LocaleManager::class.java
                    )?.applicationLocales =
                        android.os.LocaleList(java.util.Locale.forLanguageTag(languageLocale))
                } else {
                    val appLocale =
                        androidx.core.os.LocaleListCompat.forLanguageTags(languageLocale)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
                return true
            }
        }
    }
}
package com.afkanerd.deku.DefaultSMS.Settings

import android.content.Intent
import android.preference.Preference
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import com.afkanerd.deku.DefaultSMS.Models.DevMode
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.MainActivity

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: android.os.Bundle?,
        rootKey: kotlin.String?
    ) {
        setPreferencesFromResource(com.afkanerd.deku.DefaultSMS.R.xml.settings_preferences, rootKey)

        val languagePreference =
            findPreference<androidx.preference.ListPreference?>(getString(com.afkanerd.deku.DefaultSMS.R.string.settings_locale))

        val devModeLogCatPreference =
            findPreference<androidx.preference.Preference?>("dev_mode")

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

        devModeLogCatPreference!!.onPreferenceClickListener = object:
            androidx.preference.Preference.OnPreferenceClickListener {
            override fun onPreferenceClick(preference: androidx.preference.Preference): Boolean {
                startActivity(
                    Intent(requireContext(), MainActivity::class.java).apply {
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        action = Intent.ACTION_VIEW
                        putExtra("view", DevMode.viewLogCat)
                    }
                )
                requireActivity().finish()
                return true
            }
        }
    }
}
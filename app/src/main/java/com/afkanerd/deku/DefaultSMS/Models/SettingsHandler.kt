package com.afkanerd.deku.DefaultSMS.Models

import android.content.Context
import androidx.preference.PreferenceManager

object SettingsHandler {
    fun alertNotEncryptedCommunicationDisabled(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getBoolean("encryption_disable", false)
    }

    fun canSwipe(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getBoolean("swipe_actions", true)
    }
}

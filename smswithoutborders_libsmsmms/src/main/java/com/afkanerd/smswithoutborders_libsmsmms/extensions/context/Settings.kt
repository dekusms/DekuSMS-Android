package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.content.Context
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences

fun Context.alertNotEncryptedCommunicationDisabled(): Boolean {
    val sharedPreferences = getDefaultSharedPreferences(this)
    return sharedPreferences.getBoolean("encryption_disable", false)
}

object Settings {
    const val FILENAME: String = "com.afkanerd.deku.settings"
    const val SETTINGS_CAN_SWIPE = "SETTINGS_CAN_SWIPE"
}

fun Context.settingsCanSwipe(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(Settings.SETTINGS_CAN_SWIPE, false)
}

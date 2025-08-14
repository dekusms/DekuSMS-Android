package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.content.Context
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences

fun Context.alertNotEncryptedCommunicationDisabled(): Boolean {
    val sharedPreferences = getDefaultSharedPreferences(this)
    return sharedPreferences.getBoolean("encryption_disable", false)
}

fun canSwipe(context: Context): Boolean {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    return sharedPreferences.getBoolean("swipe_actions", true)
}

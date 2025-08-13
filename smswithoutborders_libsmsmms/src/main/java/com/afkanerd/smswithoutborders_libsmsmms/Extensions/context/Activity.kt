package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.core.content.edit
import com.afkanerd.smswithoutborders_libsmsmms.R

object ActivitiesConstant {
    const val ACTIVITIES_FILENAMES = "activitiesFilenames"
    const val NATIVES_LOADED = "nativesLoaded"
}

fun Context.getNativesLoaded(): Boolean {
    val sharedPreferences = getSharedPreferences(
        ActivitiesConstant.ACTIVITIES_FILENAMES, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(ActivitiesConstant.NATIVES_LOADED, false)
}

fun Context.setNativesLoaded(load: Boolean) {
    val sharedPreferences = getSharedPreferences(
        ActivitiesConstant.ACTIVITIES_FILENAMES, Context.MODE_PRIVATE)
    return sharedPreferences.edit {
        putBoolean(ActivitiesConstant.NATIVES_LOADED, load)
    }
}
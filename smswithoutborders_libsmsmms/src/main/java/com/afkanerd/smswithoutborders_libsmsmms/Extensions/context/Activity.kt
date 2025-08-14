package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.app.role.RoleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.provider.Telephony
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

fun Context.isDefault(): Boolean {
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        (getSystemService(Context.ROLE_SERVICE) as RoleManager)
            .isRoleHeld(RoleManager.ROLE_SMS)
    } else {
        Telephony.Sms.getDefaultSmsPackage(this) == packageName
    }
}

package com.afkanerd.deku.DefaultSMS.extensions.context

import android.content.Context
import androidx.core.content.edit
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.ActivitiesConstant

const val dbV2Migration = "dbV2Migration"

fun Context.getMigratedV2(): Boolean {
    val sharedPreferences = getSharedPreferences(
        ActivitiesConstant.ACTIVITIES_FILENAMES, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(dbV2Migration, false)
}

fun Context.setMigratedV2(load: Boolean) {
    val sharedPreferences = getSharedPreferences(
        ActivitiesConstant.ACTIVITIES_FILENAMES, Context.MODE_PRIVATE)
    return sharedPreferences.edit {
        putBoolean(dbV2Migration, load)
    }
}

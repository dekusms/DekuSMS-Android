package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.app.role.RoleManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.provider.Telephony
import android.widget.Toast
import androidx.core.content.edit
import com.afkanerd.smswithoutborders_libsmsmms.BuildConfig
import com.afkanerd.smswithoutborders_libsmsmms.R
import kotlin.jvm.java

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

fun Context.copyItemToClipboard(text: String) {
    val clip = ClipData.newPlainText(text, text)
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(clip)

    Toast.makeText( this, getString(R.string.conversation_copied),
        Toast.LENGTH_SHORT
    ).show()
}

fun Context.shareItem(text: String) {
    val sendIntent = Intent().apply {
        setAction(Intent.ACTION_SEND)
        putExtra(Intent.EXTRA_TEXT, text)
        setType("text/plain")
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    // Only use for components you have control over
    TODO("Implement shared items")
//    val excludedComponentNames = arrayOf(
//        ComponentName(
//            BuildConfig.APPLICATION_ID,
//            MainActivity::class.java.name
//        )
//    )
//    shareIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponentNames)
//    startActivity(shareIntent)
}

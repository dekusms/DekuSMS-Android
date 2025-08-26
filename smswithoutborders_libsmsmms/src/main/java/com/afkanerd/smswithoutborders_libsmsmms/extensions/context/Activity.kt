package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.app.role.RoleManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.afkanerd.smswithoutborders_libsmsmms.BuildConfig
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.activities.ShareItem
import java.util.Locale


object ActivitiesConstant {
    const val ACTIVITIES_FILENAMES = "com.afkanerd.deku.activities"
    const val NATIVES_LOADED = "nativesLoaded"
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
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

fun Context.shareItem(uri: Uri, mimeType: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = mimeType
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    val excludedComponentNames = arrayOf(
        ComponentName(
            BuildConfig.LIBRARY_PACKAGE_NAME,
            ShareItem::class.java.name
        )
    )
    shareIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponentNames)
    startActivity(shareIntent)
}

fun Context.shareItem(text: String) {
    val sendIntent = Intent().apply {
        setAction(Intent.ACTION_SEND)
        putExtra(Intent.EXTRA_TEXT, text)
        setType("text/plain")
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    val excludedComponentNames = arrayOf(
        ComponentName(
            BuildConfig.LIBRARY_PACKAGE_NAME,
            ShareItem::class.java.name
        )
    )
    shareIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponentNames)
    startActivity(shareIntent)
}

fun Context.getUriForDrawable(drawableId: Int): Uri? {
    return Uri.parse("android.resource://${packageName}/$drawableId")
}

fun Context.getBytesFromUri(uri: Uri): ByteArray? {
    return try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Context.getFileNameFromUri(uri: Uri): String? {
    var fileName: String? = null
    val cursor = contentResolver.query(
        uri,
        null,
        null,
        null,
        null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) { // Ensure the column exists
                fileName = it.getString(nameIndex)
            }
        }
    }
    return fileName
}

fun Context.getMimeTypeFromUri(uri: Uri): String? {
    val contentResolver = getContentResolver()
    val mimeType = contentResolver.getType(uri)
    return mimeType
}

fun Context.getCurrentLocale(): Locale? {
    val resources = getResources()
    val configuration: Configuration = resources.configuration
    return ConfigurationCompat.getLocales(configuration).get(0)
}

fun Context.setLocale(languageLocale: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSystemService( android.app.LocaleManager::class.java )
            ?.applicationLocales =
            android.os.LocaleList(Locale.forLanguageTag(languageLocale))
    } else {
        val appLocale =
            androidx.core.os.LocaleListCompat.forLanguageTags(languageLocale)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}

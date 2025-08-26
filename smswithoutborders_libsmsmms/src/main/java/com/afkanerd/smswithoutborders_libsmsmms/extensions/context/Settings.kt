package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.content.Context
import androidx.core.content.edit

object Settings {
    const val FILENAME: String = "com.afkanerd.deku.settings"
    const val SETTINGS_CAN_SWIPE = "SETTINGS_CAN_SWIPE"
    const val SETTINGS_STORE_IN_TELEPHONY = "SETTINGS_STORE_IN_TELEPHONY"
    const val SETTINGS_GET_DELIVERY_REPORTS = "SETTINGS_GET_DELIVERY_REPORTS"
    const val SETTINGS_KEEP_MESSAGES_ARCHIVED = "SETTINGS_KEEP_MESSAGES_ARCHIVED"
    const val SETTINGS_ENABLE_CONTEXT_REPLIES = "SETTINGS_ENABLE_CONTEXT_REPLIES"
    const val SETTINGS_ENABLE_24_HOUR_FORMAT = "SETTINGS_ENABLE_24_HOUR_FORMAT"
}

val Context.settingsGetEnableContextReplies get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(Settings.SETTINGS_ENABLE_CONTEXT_REPLIES, true)
}

val Context.settingsGetEnableSwipeBehaviour get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(Settings.SETTINGS_CAN_SWIPE, false)
}

val Context.settingsGetStoreTelephonyDb get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(Settings.SETTINGS_STORE_IN_TELEPHONY, true)
}

val Context.settingsGetGetDeliveryReports get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(Settings.SETTINGS_GET_DELIVERY_REPORTS, true)
}

val Context.settingsGetKeepMessagesArchived get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(Settings.SETTINGS_KEEP_MESSAGES_ARCHIVED, true)
}

val Context.settingsGetEnable24HourFormat get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(Settings.SETTINGS_ENABLE_24_HOUR_FORMAT, false)
}

fun Context.settingsSetKeepMessagesArchived(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_KEEP_MESSAGES_ARCHIVED, state)
        apply()
    }
}

fun Context.settingsSetCanSwipeBehaviour(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_CAN_SWIPE, state)
        apply()
    }
}

fun Context.settingsSetStoreTelephonyDb(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_STORE_IN_TELEPHONY, state)
        apply()
    }
}

fun Context.settingsSetGetDeliveryReports(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_GET_DELIVERY_REPORTS, state)
        apply()
    }
}

fun Context.settingsSetEnableContextReplies(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_ENABLE_CONTEXT_REPLIES, state)
        apply()
    }
}

fun Context.settingsSetEnable24HourFormat(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_ENABLE_24_HOUR_FORMAT, state)
        apply()
    }
}
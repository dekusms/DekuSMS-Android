package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.provider.Telephony
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.startActivity
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

fun Context.getDefaultRegion(): String {
    var countryCode: String? = null

    // Check if network information is available
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE)
    if (cm != null) {
        // Get the TelephonyManager to access network-related information
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (tm != null) {
            // Get the ISO country code from the network
            countryCode = tm.networkCountryIso.uppercase()
        }
    }
    return PhoneNumberUtil.getInstance()
        .getCountryCodeForRegion(countryCode).toString()
}

fun Context.makeE16PhoneNumber(address: String): String {
    var address = address
    val defaultRegion = getDefaultRegion()
    if(address.startsWith("0"))
        address = address.substring(1)

    address = address.replace("%2B".toRegex(), "+")
        .replace("-".toRegex(), "")
        .replace("%20".toRegex(), "")
        .replace(" ".toRegex(), "")

    if (address.length < 5) return address
    val phoneNumberUtil = PhoneNumberUtil.getInstance()

    var outputNumber = address
    try {
        val phoneNumber = phoneNumberUtil.parse(address, defaultRegion)
        val nationalNumber = phoneNumber.nationalNumber
        val countryCode = phoneNumber.countryCode.toLong()

        return "+$countryCode$nationalNumber"
    } catch (e: NumberParseException) {
        if (e.errorType == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
            address = outputNumber
                .replace("sms[to]*:".toRegex(), "")
                .replaceFirst("^0+".toRegex(), "")
            outputNumber = if (address.startsWith(defaultRegion)) {
                "+$address"
            } else {
                "+$defaultRegion$address"
            }
            return outputNumber
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return address
}

fun Context.getThreadId(address: String): Long{
    return Telephony.Threads.getOrCreateThreadId(this, address);
}

fun Context.blockContact(address: String) {
    val contentValues = ContentValues();
    contentValues.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, address);
    val uri = contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI,
        contentValues);

    Toast.makeText(this,
        getString(this, R.string.conversations_menu_block_toast),
        Toast.LENGTH_SHORT).show();
    val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    startActivity(this,
        telecomManager.createManageBlockedNumbersIntent(), null);
}

fun Context.retrieveContactName(phoneNumber: String): String? {
    val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )
    val cursor = contentResolver.query(
        uri,
        arrayOf<String>(ContactsContract.PhoneLookup.DISPLAY_NAME),
        null,
        null, null
    )

    if (cursor == null) return null

    try {
        if (cursor.moveToFirst()) {
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
            return cursor.getString(displayNameIndex)
        }
    } finally {
        cursor.close()
    }

    return null
}

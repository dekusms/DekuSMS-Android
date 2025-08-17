package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.provider.Telephony
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.regex.Pattern

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

fun Context.getThreadId(address: String): Int{
    return Telephony.Threads.getOrCreateThreadId(this, address).toInt();
}

fun Context.blockContact(address: String) {
    val contentValues = ContentValues();
    contentValues.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, address);
    contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, contentValues);

    Toast.makeText(this,
        getString(this, R.string.conversations_menu_block_toast),
        Toast.LENGTH_SHORT).show();
    val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    startActivity(this,
        telecomManager.createManageBlockedNumbersIntent(), null);
}

fun Context.unblockContact(address: String) {
    BlockedNumberContract.unblock(this, address)
}

fun Context.getBlocked(): Cursor? {
    return contentResolver.query(
        BlockedNumberContract.BlockedNumbers.CONTENT_URI,
        arrayOf<String>(
            BlockedNumberContract.BlockedNumbers.COLUMN_ID,
            BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
            BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER
        ),
        null, null, null
    )
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

fun Context.retrieveContactPhoto(phoneNumber: String?): String? {
    val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )
    val cursor = contentResolver.query(
        uri,
        arrayOf<String>(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI),
        null,
        null, null
    )

    var contactPhotoThumbUri: String? = ""
    if (cursor == null) return null

    try {
        if (cursor.moveToFirst()) {
            val displayContactPhoto =
                cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
            contactPhotoThumbUri = cursor.getString(displayContactPhoto).toString()
        }
    } finally {
        cursor.close()
    }
    return contactPhotoThumbUri
}

fun Context.call(address: String) {
    val callIntent = Intent(Intent.ACTION_DIAL).apply {
        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        setData("tel:$address".toUri());
    }
    startActivity(callIntent);
}

fun Context.getSimCardInformation(): MutableList<SubscriptionInfo>? {
    val subscriptionManager =
        getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return subscriptionManager.getActiveSubscriptionInfoList()
    }
    return null
}

fun Context.isDualSim(): Boolean {
    val manager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        manager.activeModemCount > 1
    } else manager.getPhoneCount() > 1
}


fun Context.getDefaultSimSubscription(): Long? {
    // TODO: check if there's even a simcard and handle it accordingly
    val subId = SubscriptionManager.getDefaultSmsSubscriptionId()
    if (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID)  //            return getSimCardInformation(context).get(0).getSubscriptionId();
        return null
    return subId.toLong()
}

fun Context.getSubscriptionName(subscriptionId: Long): String {
    val subscriptionId = subscriptionId.toInt()
    val subscriptionInfos = getSimCardInformation()
    for (subscriptionInfo in subscriptionInfos!!)
        if (subscriptionInfo.subscriptionId == subscriptionId) {
            if (subscriptionInfo.carrierName != null) {
                return subscriptionInfo.displayName.toString()
            }
        }
    return ""
}

fun Context.getSubscriptionBitmap(subscriptionId: Int): Bitmap? {
    val subscriptionInfos = getSimCardInformation()

    for (subscriptionInfo in subscriptionInfos!!)
        if (subscriptionInfo.subscriptionId == subscriptionId) {
            return subscriptionInfo.createIconBitmap(this)
    }
    return null
}

fun Context.updateSmsToLocalDb(
    conversations: Conversations
) {
    val contentValues = ContentValues()
    contentValues.put(Telephony.Sms._ID, conversations.sms?._id)
    contentValues.put(Telephony.TextBasedSmsColumns.TYPE, conversations.sms?.type)
    contentValues.put(Telephony.TextBasedSmsColumns.STATUS, conversations.sms?.status)

    try {
        contentResolver.update(
            Telephony.Sms.CONTENT_URI,
            contentValues,
            "${Telephony.Sms._ID} = ?",
            arrayOf(conversations.sms?._id.toString())
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.registerSmsToLocalDb(
    messageId: String,
    address: String,
    body: String,
    subscriptionId: Long,
    date: Long,
    dateSent: Long,
    type: Int,
) {
    val contentValues = ContentValues()
    contentValues.put(Telephony.Sms._ID, messageId)
    contentValues.put(Telephony.TextBasedSmsColumns.ADDRESS, address)
    contentValues.put(Telephony.TextBasedSmsColumns.BODY, body)
    contentValues.put(Telephony.TextBasedSmsColumns.SUBSCRIPTION_ID, subscriptionId)
    contentValues.put(Telephony.TextBasedSmsColumns.DATE, date)
    contentValues.put(Telephony.TextBasedSmsColumns.DATE_SENT, dateSent)
    contentValues.put( Telephony.TextBasedSmsColumns.TYPE, type)

    try {
         contentResolver.insert(
            Telephony.Sms.CONTENT_URI,
            contentValues
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun isShortCode(address: String): Boolean {
    if (address.length < 4) return true
    val pattern = Pattern.compile("[a-zA-Z]")
    val matcher = pattern.matcher(address)
    return !PhoneNumberUtils.isWellFormedSmsAddress(address) || matcher.find()
}

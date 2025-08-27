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

@Throws
fun Context.blockContact(addresses: List<String>) {
    try {
        addresses.forEach { address ->
            val contentValues = ContentValues();
            contentValues.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, address);
            contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, contentValues);

        }

        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        startActivity(this,
            telecomManager.createManageBlockedNumbersIntent(), null);
    } catch(e: Exception) {
        throw e
    }
}

@Throws
fun Context.unblockContact(addresses: List<String>) {
    try {
        addresses.forEach { address ->
            BlockedNumberContract.unblock(this, address)
        }
    } catch(e: Exception) {
        throw e
    }
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


fun isShortCode(address: String): Boolean {
    if (address.length < 4) return true
    val pattern = Pattern.compile("[a-zA-Z]")
    val matcher = pattern.matcher(address)
    return !PhoneNumberUtils.isWellFormedSmsAddress(address) || matcher.find()
}

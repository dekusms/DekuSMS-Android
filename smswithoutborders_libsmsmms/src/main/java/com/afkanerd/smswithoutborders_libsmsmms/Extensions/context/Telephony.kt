package com.afkanerd.smswithoutborders_libsmsmms.Extensions.context

import android.content.Context
import android.telephony.TelephonyManager
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
    return com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance()
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

package com.afkanerd.deku.DefaultSMS.Commons

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.OpenableColumns
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.format.DateUtils
import android.util.Base64
import androidx.compose.ui.graphics.vector.path
import com.afkanerd.deku.DefaultSMS.R
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object Helpers {
    fun isShortCode(address: String): Boolean {
        if (address.length < 4) return true
        val pattern = Pattern.compile("[a-zA-Z]")
        val matcher = pattern.matcher(address)
        return !PhoneNumberUtils.isWellFormedSmsAddress(address) || matcher.find()
    }

    fun getFormatCompleteNumber(data: String, defaultRegion: String): String {
        var data = data
        if(data.startsWith("0"))
            data = data.substring(1)

        data = data.replace("%2B".toRegex(), "+")
            .replace("-".toRegex(), "")
            .replace("%20".toRegex(), "")
            .replace(" ".toRegex(), "")

        if (data.length < 5) return data
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        var outputNumber = data
        try {
            val phoneNumber = phoneNumberUtil.parse(data, defaultRegion)
            val nationalNumber = phoneNumber.nationalNumber
            val countryCode = phoneNumber.countryCode.toLong()

            return "+$countryCode$nationalNumber"
        } catch (e: NumberParseException) {
            if (e.errorType == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
                data = outputNumber
                    .replace("sms[to]*:".toRegex(), "")
                    .replaceFirst("^0+".toRegex(), "")
                outputNumber = if (data.startsWith(defaultRegion)) {
                    "+$data"
                } else {
                    "+$defaultRegion$data"
                }
                return outputNumber
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    fun getCountryNationalAndCountryCode(data: String?): Array<String> {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val phoneNumber = phoneNumberUtil.parse(data, "")
        val nationNumber = phoneNumber.getNationalNumber()
        val countryCode = phoneNumber.getCountryCode().toLong()

        return arrayOf<String>(countryCode.toString(), nationNumber.toString())
    }

    fun getFormatForTransmission(data: String, defaultRegion: String): String {
        var data = data
        data = data.replace("%2B".toRegex(), "+")
            .replace("%20".toRegex(), "")
            .replace("sms[to]*:".toRegex(), "")

        // Remove any non-digit characters except the plus sign at the beginning of the string
        var strippedNumber = data.replace("[^0-9+;]".toRegex(), "")
        if (strippedNumber.length > 6) {
            // If the stripped number starts with a plus sign followed by one or more digits, return it as is
            if (!strippedNumber.matches("^\\+\\d+".toRegex())) {
                strippedNumber = "+" + defaultRegion + strippedNumber
            }
            return strippedNumber
        }

        // If the stripped number is not a valid phone number, return an empty string
        return data
    }

    fun getFormatNationalNumber(data: String, defaultRegion: String): String? {
        var data = data
        data = data.replace("%2B".toRegex(), "+")
            .replace("%20".toRegex(), "")
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        var outputNumber = data
        try {
            try {
                val phoneNumber = phoneNumberUtil.parse(data, defaultRegion)

                return phoneNumber.getNationalNumber().toString()
            } catch (e: NumberParseException) {
                if (e.getErrorType() == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
                    data = data.replace("sms[to]*:".toRegex(), "")
                    if (data.startsWith(defaultRegion)) {
                        outputNumber = "+" + data
                    } else {
                        outputNumber = "+" + defaultRegion + data
                    }
                    return getFormatNationalNumber(outputNumber, defaultRegion)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    fun formatDateExtended(context: Context, epochTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - epochTime

        val currentDate = Date(currentTime)
        val targetDate = Date(epochTime)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val fullDayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val shortDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val shortMonthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        if (isYesterday(currentDate, targetDate)) { // yesterday
            return context.getString(R.string.single_message_thread_yesterday) + " • " + timeFormat.format(
                targetDate
            )
        } else if (diff < DateUtils.DAY_IN_MILLIS) { // today
            return timeFormat.format(targetDate)
        } else if (isSameWeek(currentDate, targetDate)) { // within the same week
            return fullDayFormat.format(targetDate) + " • " + timeFormat.format(targetDate)
        } else { // greater than 1 week
            return (shortDayFormat.format(targetDate) + ", " + shortMonthDayFormat.format(targetDate)
                    + " • " + timeFormat.format(targetDate))
        }
    }

    private fun isYesterday(date1: Date, date2: Date): Boolean {
        val dayFormat = SimpleDateFormat("yyyyDDD", Locale.getDefault())
        val day1 = dayFormat.format(date1)
        val day2 = dayFormat.format(date2)

        val dayOfYear1 = day1.substring(4).toInt()
        val dayOfYear2 = day2.substring(4).toInt()
        val year1 = day1.substring(0, 4).toInt()
        val year2 = day2.substring(0, 4).toInt()

        return (year1 == year2 && dayOfYear1 - dayOfYear2 == 1)
                || (year1 - year2 == 1 && dayOfYear1 == 1 && dayOfYear2 == 365)
    }

    private fun isSameWeek(date1: Date, date2: Date): Boolean {
        val weekFormat = SimpleDateFormat("yyyyww", Locale.getDefault())
        val week1 = weekFormat.format(date1)
        val week2 = weekFormat.format(date2)
        return week1 == week2
    }


    fun isBase64Encoded(input: String): Boolean {
        try {
            val decodedBytes = Base64.decode(input, Base64.DEFAULT)

            //            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            val reencodedString = Base64.encodeToString(decodedBytes, Base64.DEFAULT)
                .replace("\\n".toRegex(), "")

            return input.replace("\\n".toRegex(), "") == reencodedString
        } catch (e: Exception) {
            return false
        }
    }

    fun isSameMinute(date1: Long, date2: Long?): Boolean {
        val date = java.util.Date(date1)
        val currentCalendar = Calendar.getInstance()
        currentCalendar.setTime(date)

        val previousDateString = date2.toString()
        val previousDate = java.util.Date(previousDateString.toLong())
        val prevCalendar = Calendar.getInstance()
        prevCalendar.setTime(previousDate)

        return !((prevCalendar.get(Calendar.HOUR_OF_DAY) != currentCalendar.get(Calendar.HOUR_OF_DAY) || (prevCalendar.get(
            Calendar.MINUTE
        ) != currentCalendar.get(Calendar.MINUTE))
                || (prevCalendar.get(Calendar.DATE) != currentCalendar.get(Calendar.DATE))))
    }

    fun isSameHour(date1: Long, date2: Long?): Boolean {
        val date = java.util.Date(date1)
        val currentCalendar = Calendar.getInstance()
        currentCalendar.setTime(date)

        val previousDateString = date2.toString()
        val previousDate = java.util.Date(previousDateString.toLong())
        val prevCalendar = Calendar.getInstance()
        prevCalendar.setTime(previousDate)

        return !((prevCalendar.get(Calendar.HOUR_OF_DAY) != currentCalendar.get(Calendar.HOUR_OF_DAY)
                || (prevCalendar.get(Calendar.DATE) != currentCalendar.get(Calendar.DATE))))
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        // Try to get the file name from the content resolver
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
        // If the content resolver fails, try to get the file name from the path
        if (fileName == null) {
            fileName = uri.path?.substringAfterLast('/')
        }
        return fileName
    }
}

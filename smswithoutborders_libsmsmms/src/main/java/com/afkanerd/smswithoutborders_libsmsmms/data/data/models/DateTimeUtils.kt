package com.afkanerd.smswithoutborders_libsmsmms.data.data.models

import android.content.Context
import android.text.format.DateUtils
import com.afkanerd.smswithoutborders_libsmsmms.R
import java.util.Calendar

object DateTimeUtils {
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

    fun formatDate(context: Context, epochTime: Long): String? {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - epochTime

        val now = Calendar.getInstance()
        now.setTimeInMillis(currentTime)
        val dateCal = Calendar.getInstance()
        dateCal.setTimeInMillis(epochTime)

        // Check if the date is today
        if (dateCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            dateCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
        ) {
            // Use relative time or time if less than a day
            if (diff < DateUtils.HOUR_IN_MILLIS) {
                return DateUtils.getRelativeTimeSpanString(
                    epochTime,
                    currentTime,
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } else if (diff < DateUtils.DAY_IN_MILLIS) {
                return DateUtils.formatDateTime(
                    context, epochTime, DateUtils.FORMAT_SHOW_TIME)
            }
        } else if (dateCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1) {
            // Show "yesterday" if the date is yesterday
            return context.getString(R.string.thread_conversation_timestamp_yesterday)
        } else {
            // Use standard formatting for other dates
            return DateUtils.formatDateTime(
                context,
                epochTime,
                DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_DATE
            )
        }
        return null
    }

}
package com.afkanerd.smswithoutborders_libsmsmms.data.data.models

import android.content.Context
import android.text.format.DateUtils
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsGetEnable24HourFormat
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                return when(context.settingsGetEnable24HourFormat) {
                    true -> SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(epochTime))

                    false -> return DateUtils.formatDateTime(
                        context,
                        epochTime,
                        DateUtils.FORMAT_SHOW_TIME
                    )
                }

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

    fun formatDateExtended(context: Context, epochTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - epochTime

        val currentDate = Date(currentTime)
        val targetDate = Date(epochTime)

        val timeFormat = when(context.settingsGetEnable24HourFormat) {
            true -> SimpleDateFormat("HH:mm", Locale.getDefault())
            false -> SimpleDateFormat("h:mm a", Locale.getDefault())
        }
        val fullDayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val shortDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val shortMonthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        if (isYesterday(currentDate, targetDate)) { // yesterday
            return context.getString(R.string.single_message_thread_yesterday) +
                    " • " + timeFormat.format( targetDate )
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

}
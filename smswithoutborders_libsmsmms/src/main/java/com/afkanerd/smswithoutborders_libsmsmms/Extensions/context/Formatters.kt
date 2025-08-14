package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.content.Context
import android.text.format.DateUtils
import com.afkanerd.smswithoutborders_libsmsmms.R
import java.util.Calendar

fun Context.formatDate(epochTime: Long): String? {
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
                this, epochTime, DateUtils.FORMAT_SHOW_TIME)
        }
    } else if (dateCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1) {
        // Show "yesterday" if the date is yesterday
        return getString(R.string.thread_conversation_timestamp_yesterday)
    } else {
        // Use standard formatting for other dates
        return DateUtils.formatDateTime(
            this,
            epochTime,
            DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_DATE
        )
    }
    return null
}


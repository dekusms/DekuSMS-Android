package com.afkanerd.deku.DefaultSMS.extensions

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.util.Locale
import java.util.concurrent.TimeUnit

fun ByteArray.isValidTimestampWithinHours(maxHours: Long): Boolean {
    // 1. Validate exact byte length (must be 8 bytes for a Long)
    if (this.size != Long.SIZE_BYTES) {
        return false
    }

    // 2. Decode Big-Endian bytes back into epoch milliseconds
    val timestampMillis = ByteBuffer.wrap(this)
        .order(ByteOrder.BIG_ENDIAN)
        .long

    // 3. Reject clearly invalid timestamps (e.g., negative or zeroed buffers)
    if (timestampMillis <= 0L) {
        return false
    }

    // 4. Check if the timestamp is in the past and within maxHours
    val nowMillis = System.currentTimeMillis()
    val diffMillis = nowMillis - timestampMillis

    if (diffMillis < 0L) {
        return false // Timestamp is in the future
    }

    val maxMillis = TimeUnit.HOURS.toMillis(maxHours)
    return diffMillis < maxMillis
}

package com.afkanerd.smswithoutborders_libsmsmms.Extensions.context

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.afkanerd.smswithoutborders_libsmsmms.data.DatabaseImpl

fun Context.getDatabase(): DatabaseImpl {
    return DatabaseImpl.getDatabaseImpl(this)
}
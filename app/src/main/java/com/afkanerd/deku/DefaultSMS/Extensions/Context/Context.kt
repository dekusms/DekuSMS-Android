package com.afkanerd.deku.DefaultSMS.Extensions.Context

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.afkanerd.deku.DefaultSMS.BroadcastReceivers.SmsMmsActionsImpl
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.MmsHandler
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.MainActivity
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream


fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}


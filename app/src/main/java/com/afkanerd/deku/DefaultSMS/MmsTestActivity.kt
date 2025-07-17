package com.afkanerd.deku.DefaultSMS

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction

class MmsTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mmsInternalInjectionTest()
    }

    fun mmsInternalInjectionTest() {
        val settings = Settings()
        settings.useSystemSending = true
        settings.deliveryReports = true
        settings.subscriptionId = SIMHandler.getDefaultSimSubscription(applicationContext)
        settings.mmsc = "http://mms.du.ae"
        settings.proxy = "10.19.18.4"
        settings.port = "8080"
        settings.group = true
        settings.sendLongAsMms = true
//        settings.sendLongAsMmsAfter = 3
//        settings.group = false

        val transaction = Transaction(this, settings)

        val message = Message("Hello world 2", "+971582306355")
        message.setImage(
            AppCompatResources.getDrawable(applicationContext,
                R.drawable.ic_launcher_foreground)?.toBitmap()
        )

//        transaction.checkMMS(message)
//        transaction.setExplicitBroadcastForSentMms(
//            Intent(
//                applicationContext,
//                MMSReceiverBroadcastReceiver::class.java
//            )
//        )

        transaction.sendNewMessage(message, Transaction.NO_THREAD_ID)
//        transaction.sendNewMessage(message, 1)
    }

    data class MmsMessageInfo(
        val id: Long,
        val threadId: Long,
        val messageId: String?, // M-ID
        val messageBox: Int,    // e.g., Telephony.Mms.MESSAGE_BOX_INBOX, Telephony.Mms.MESSAGE_BOX_SENT
        val subject: String?,
        val date: Long
        // Add other fields you need from Telephony.Mms columns
    )

    fun getAllMmsMessages(context: Context): List<MmsMessageInfo> {
        val mmsMessages = mutableListOf<MmsMessageInfo>()
        val contentResolver: ContentResolver = context.contentResolver

        // Projection: Define which columns you want to retrieve from the MMS table
        val projection = arrayOf(
            Telephony.Mms._ID,
            Telephony.Mms.THREAD_ID,
            Telephony.Mms.MESSAGE_ID, // M-ID (often null for drafts or outgoing messages not yet sent)
            Telephony.Mms.MESSAGE_BOX,
            Telephony.Mms.SUBJECT,
            Telephony.Mms.DATE
            // Add other Telephony.Mms columns here, e.g., Telephony.Mms.CONTENT_LOCATION
        )

        // No specific selection for "queued" in this basic example, as MMS queueing is complex.
        // You might filter by Telephony.Mms.MESSAGE_BOX (e.g., Telephony.Mms.MESSAGE_BOX_OUTBOX)
        val selection: String? = "${Telephony.Mms.MESSAGE_BOX} = ?}" // Or, e.g., "${Telephony.Mms.MESSAGE_BOX} = ?"
        val selectionArgs: Array<String>? = arrayOf(Telephony.Mms.MESSAGE_BOX_OUTBOX.toString()) // Or, e.g., arrayOf(Telephony.Mms.MESSAGE_BOX_OUTBOX.toString())

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                Telephony.Mms.CONTENT_URI, // Use Telephony.Mms.CONTENT_URI
                projection,
                selection,
                selectionArgs,
                Telephony.Mms.DEFAULT_SORT_ORDER // Or any other sort order
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val idColumn = it.getColumnIndexOrThrow(Telephony.Mms._ID)
                    val threadIdColumn = it.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID)
                    val messageIdColumn = it.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_ID)
                    val messageBoxColumn = it.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX)
                    val subjectColumn = it.getColumnIndexOrThrow(Telephony.Mms.SUBJECT)
                    val dateColumn = it.getColumnIndexOrThrow(Telephony.Mms.DATE)

                    do {
                        val id = it.getLong(idColumn)
                        val threadId = it.getLong(threadIdColumn)
                        val messageId = it.getString(messageIdColumn)
                        val messageBox = it.getInt(messageBoxColumn)
                        val subject = it.getString(subjectColumn)
                        val date = it.getLong(dateColumn)

                        mmsMessages.add(
                            MmsMessageInfo(id, threadId, messageId, messageBox, subject, date)
                        )
                    } while (it.moveToNext())
                } else {
                    Log.d("MmsQuery", "No MMS messages found.")
                }
            }
        } catch (e: Exception) {
            Log.e("MmsQuery", "Error querying MMS database", e)
            // Handle exceptions
        } finally {
            cursor?.close()
        }

        return mmsMessages
    }

    fun clearMmsOutboxMessages(context: Context): Int {
        // Crucially, verify if this app is the default SMS app.
        val myPackageName = context.packageName
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context) // Checks for default SMS handler

        if (myPackageName != defaultSmsPackage) {
            Log.w("MmsOutboxClear", "Cannot clear MMS outbox: App is not the default SMS app.")
            return 0 // Indicate no messages were deleted
        }

        val contentResolver: ContentResolver = context.contentResolver
        val selection = "${Telephony.Mms.MESSAGE_BOX} = ?"
        val selectionArgs = arrayOf(Telephony.Mms.MESSAGE_BOX_OUTBOX.toString())
        var rowsDeleted = 0

        try {
            // This attempts to delete the main entries of MMS messages in the outbox.
            // It does NOT automatically guarantee that all associated parts (images, videos)
            // are also deleted from their storage, though the system often handles this cleanup.
            rowsDeleted = contentResolver.delete(
                Telephony.Mms.CONTENT_URI, // URI for the main MMS table
                selection,
                selectionArgs
            )
            Log.d("MmsOutboxClear", "Successfully deleted $rowsDeleted MMS messages from the outbox.")

            // Potentially, you might also need to delete parts associated with these messages.
            // However, this is more complex as you'd first need to query the IDs of the
            // messages in the outbox and then delete their parts from Telephony.Mms.Part.
            // Often, the system handles part cleanup when the main MMS entry is deleted by the default app.
            // For simplicity, this example only deletes from the main MMS table.

        } catch (e: SecurityException) {
            Log.e("MmsOutboxClear", "SecurityException: Failed to delete MMS from outbox. Ensure app is default SMS app.", e)
        } catch (e: Exception) {
            Log.e("MmsOutboxClear", "Error deleting MMS messages from outbox", e)
        }
        return rowsDeleted
    }



}
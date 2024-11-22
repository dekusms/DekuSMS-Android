package com.afkanerd.deku.DefaultSMS.ui.Components

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.startActivity
import com.afkanerd.deku.Datastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.afkanerd.deku.DefaultSMS.R

object ConvenientMethods {

    fun blockContact(context: Context, threadId: String, address: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val datastore = Datastore.getDatastore(context)
            val threadedConversations = datastore.threadedConversationsDao().get(threadId);
            threadedConversations.isIs_blocked = true;
            datastore.threadedConversationsDao().update(context, threadedConversations);
        }

        val contentValues = ContentValues();
        contentValues.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, address);
        val uri = context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI,
            contentValues);

        Toast.makeText(context, getString(context, R.string.conversations_menu_block_toast),
            Toast.LENGTH_SHORT).show();
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        startActivity(context, telecomManager.createManageBlockedNumbersIntent(), null);
    }

}
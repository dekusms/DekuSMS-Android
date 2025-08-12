package com.afkanerd.deku.DefaultSMS.Models.Conversations;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import com.afkanerd.deku.DefaultSMS.Commons.Helpers;
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB;

public class ThreadedConversationsHandler {

//    public static ThreadedConversations get(Context context, String address) {
//        final String defaultUserCountry = Helpers.INSTANCE.getUserCountry(context);
//        long threadId = Telephony.Threads.getOrCreateThreadId(context, address);
//        ThreadedConversations threadedConversations = new ThreadedConversations();
//        threadedConversations.setAddress(Helpers.INSTANCE.getFormatCompleteNumber(address, defaultUserCountry));
//        threadedConversations.setThread_id(String.valueOf(threadId));
//        return threadedConversations;
//    }
//
//    public static void call(Context context, String address) {
//        Intent callIntent = new Intent(Intent.ACTION_DIAL);
//        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        callIntent.setData(Uri.parse("tel:" + address));
//
//        context.startActivity(callIntent);
//    }
}

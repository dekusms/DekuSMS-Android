package com.afkanerd.deku.DefaultSMS.Models;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;

import com.afkanerd.deku.DefaultSMS.BroadcastReceivers.SmsMmsActionsImpl;
import com.afkanerd.deku.DefaultSMS.Commons.Helpers;
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation;
import com.afkanerd.deku.DefaultSMS.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsHandler {

    public static void sendIncomingTextMessageNotification(
            Context context,
            Conversation conversation
    ) {
        NotificationCompat.MessagingStyle messagingStyle = getMessagingStyle(context, conversation, null);

        Intent replyIntent = getReplyIntent(context, conversation);
//        PendingIntent pendingIntent = getPendingIntent(context, conversation);

//        NotificationCompat.Builder builder = getNotificationBuilder(
//                context,
//                replyIntent,
//                conversation,
//                pendingIntent
//        );
//        builder.setStyle(messagingStyle);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
//        notificationManagerCompat.notify(Integer.parseInt(conversation.getThread_id()), builder.build());
    }



    public static Person getPerson(Context context, Conversation conversation) {
        String contactName = Contacts.retrieveContactName(context, conversation.getAddress());
        try {
            Bitmap bitmap = Contacts.getContactBitmapPhoto(context, conversation.getAddress());
            IconCompat icon = bitmap == null ? null : IconCompat.createWithBitmap(bitmap);

            if(icon == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                icon = IconCompat.createWithResource(context, R.drawable.baseline_account_circle_24);
            }
        } catch(Exception e) {

        }

        Person.Builder personBuilder = new Person.Builder()
//                .setIcon(icon)
                .setName(contactName == null ? conversation.getAddress() : contactName)
                .setKey(contactName == null ? conversation.getAddress() : contactName);
        return personBuilder.build();
    }

    public static Intent getReplyIntent(Context context, Conversation conversation) {
        if(conversation != null && !Helpers.INSTANCE.isShortCode(conversation.getAddress())) {
            Intent replyBroadcastIntent = new Intent(context, SmsMmsActionsImpl.class);

//            replyBroadcastIntent.putExtra(IncomingTextSMSReplyMuteActionBroadcastReceiver.Companion
//                            .getREPLY_BROADCAST_INTENT(), conversation.getAddress());
//
//            replyBroadcastIntent.putExtra(IncomingTextSMSReplyMuteActionBroadcastReceiver.Companion
//                            .getREPLY_THREAD_ID(), conversation.getThread_id());
//
//            replyBroadcastIntent.putExtra(IncomingTextSMSReplyMuteActionBroadcastReceiver.Companion
//                            .getREPLY_SUBSCRIPTION_ID(), conversation.getSubscription_id());
//
//            replyBroadcastIntent.setAction(IncomingTextSMSReplyMuteActionBroadcastReceiver.
//                    Companion.getREPLY_BROADCAST_INTENT());
            return replyBroadcastIntent;
        }
        return null;
    }

    private static class MessageTrackers {
        String title;
        StringBuilder message = new StringBuilder();
        Person person;
    }

    public static NotificationCompat.MessagingStyle getMessagingStyle(Context context,
                                                                      Conversation conversation,
                                                                      String reply) {
        Person person = getPerson(context, conversation);

        Bitmap bitmap = Contacts.getContactBitmapPhoto(context, conversation.getAddress());
        IconCompat icon = bitmap == null ? null : IconCompat.createWithBitmap(bitmap);

        if(icon == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            icon = IconCompat.createWithResource(context, R.drawable.baseline_account_circle_24);
        }

        Person.Builder personBuilder = new Person.Builder()
                .setIcon(icon)
                .setName(context.getString(R.string.notification_title_reply_you))
                .setKey(context.getString(R.string.notification_title_reply_you));
        Person replyPerson = personBuilder.build();
        String contactName = Contacts.retrieveContactName(context, conversation.getAddress());

        NotificationCompat.MessagingStyle messagingStyle =
                new NotificationCompat.MessagingStyle(contactName == null ?
                        conversation.getAddress() : contactName);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        List<StatusBarNotification> notifications = notificationManager.getActiveNotifications();
        List<MessageTrackers> listMessages = new ArrayList<>();
        for(StatusBarNotification notification : notifications) {
            if (notification.getId() == Integer.parseInt(conversation.getThread_id())) {
                Bundle extras = notification.getNotification().extras;
                String prevMessage = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
                String prevTitle = extras.getCharSequence(Notification.EXTRA_TITLE).toString();

                MessageTrackers messageTrackers = new MessageTrackers();
                messageTrackers.message.append(prevMessage);
                messageTrackers.title = prevTitle;
                messageTrackers.person = prevTitle.equals(contactName == null ?
                        conversation.getAddress() : contactName) ?
                        person : replyPerson;
                listMessages.add(messageTrackers);
            }
        }
        MessageTrackers messageTrackers = new MessageTrackers();
        messageTrackers.message.append(conversation.isIs_key() ?
                context.getString(R.string.notification_title_new_key) :
                reply == null ? conversation.getText() : reply);
        messageTrackers.title = reply == null ?
                (contactName == null ? conversation.getAddress() : contactName) : context.getString(R.string.notification_title_reply_you);
        messageTrackers.person = reply == null ? person : replyPerson;
        listMessages.add(messageTrackers);

        StringBuilder personConversations = new StringBuilder();
        StringBuilder replyConversations = new StringBuilder();

        List<MessageTrackers> newTrackers = new ArrayList<>();
        for(MessageTrackers messageTracker : listMessages) {
            if(messageTracker.title.equals(contactName == null ? conversation.getAddress(): contactName)) {
                if(personConversations.length() > 0)
                    personConversations.append("\n\n");
                personConversations.append(messageTracker.message);
                if(replyConversations.length() > 0) {
                    MessageTrackers messageTrackers1 = new MessageTrackers();
                    messageTrackers1.person = replyPerson;
                    messageTrackers1.message = replyConversations;
                    newTrackers.add(messageTrackers1);
                    replyConversations = new StringBuilder();
                }
            }
            else {
                if(replyConversations.length() > 0)
                    replyConversations.append("\n\n");
                replyConversations.append(messageTracker.message);
                if(personConversations.length() > 0) {
                    MessageTrackers messageTrackers1 = new MessageTrackers();
                    messageTrackers1.person = person;
                    messageTrackers1.message = personConversations;
                    newTrackers.add(messageTrackers1);
                    personConversations = new StringBuilder();
                }
            }
        }

        if(personConversations.length() > 0) {
            MessageTrackers messageTrackers1 = new MessageTrackers();
            messageTrackers1.person = person;
            messageTrackers1.message = personConversations;
            newTrackers.add(messageTrackers1);
        }

        if(replyConversations.length() > 0) {
            MessageTrackers messageTrackers1 = new MessageTrackers();
            messageTrackers1.person = replyPerson;
            messageTrackers1.message = replyConversations;
            newTrackers.add(messageTrackers1);
        }

        for(MessageTrackers messageTracker : newTrackers) {
            messagingStyle.addMessage(new NotificationCompat.MessagingStyle.Message(
                    messageTracker.message, System.currentTimeMillis(),messageTracker.person));
        }

//        messagingStyle.addMessage(new NotificationCompat.MessagingStyle.Message(
//                conversation.getText(), System.currentTimeMillis(), person));

        return messagingStyle;
    }

    public static NotificationCompat.Builder
    getNotificationBuilder(Context context, Intent replyBroadcastIntent, Conversation conversation,
                           PendingIntent pendingIntent){

//        String shortcutInfo = getShortcutInfo(context, conversation);

        String contactName = Contacts.retrieveContactName(context, conversation.getAddress());
        NotificationCompat.BubbleMetadata bubbleMetadata = new NotificationCompat.BubbleMetadata
                .Builder(contactName == null ? conversation.getAddress() : contactName)
                .setDesiredHeight(400)
                .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, context.getString(R.string.incoming_messages_channel_id))
                .setContentTitle(contactName == null ? conversation.getAddress() : contactName)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setAllowSystemGeneratedContextualActions(true)
                .setPriority(Notification.PRIORITY_MAX)
//                .setShortcutId(shortcutInfo)
                .setBubbleMetadata(bubbleMetadata)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);


        String markAsReadLabel = context.getResources().getString(R.string.notifications_mark_as_read_label);

        Intent markAsReadIntent = new Intent(context, SmsMmsActionsImpl.class);
        markAsReadIntent.putExtra(Conversation.THREAD_ID, conversation.getThread_id());
        markAsReadIntent.putExtra(Conversation.ID, conversation.getMessage_id());
//        markAsReadIntent.setAction(IncomingTextSMSReplyMuteActionBroadcastReceiver.Companion
//                .getMARK_AS_READ_BROADCAST_INTENT());

        PendingIntent markAsReadPendingIntent =
                PendingIntent.getBroadcast(context, Integer.parseInt(conversation.getThread_id()),
                        markAsReadIntent,
                        PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Action markAsReadAction = new NotificationCompat.Action.Builder(null,
                markAsReadLabel, markAsReadPendingIntent)
                .build();

        builder.addAction(markAsReadAction);

        if(replyBroadcastIntent != null) {
            PendingIntent replyPendingIntent =
                    PendingIntent.getBroadcast(context, Integer.parseInt(conversation.getThread_id()),
                            replyBroadcastIntent,
                            PendingIntent.FLAG_MUTABLE);

            String replyLabel = context.getResources().getString(R.string.notifications_reply_label);
//            RemoteInput remoteInput = new RemoteInput.Builder(
//                    IncomingTextSMSReplyMuteActionBroadcastReceiver.KEY_TEXT_REPLY)
//                    .setLabel(replyLabel)
//                    .build();
//
//            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(null,
//                    replyLabel, replyPendingIntent)
//                    .addRemoteInput(remoteInput)
//                    .build();
//
//            builder.addAction(replyAction);
        }
        else if(conversation.getThread_id() != null){
            Intent muteIntent = new Intent(context, SmsMmsActionsImpl.class);
            muteIntent.putExtra(Conversation.ADDRESS, conversation.getAddress());
            muteIntent.putExtra(Conversation.ID, conversation.getMessage_id());
            muteIntent.putExtra(Conversation.THREAD_ID, conversation.getThread_id());
//            muteIntent.setAction(IncomingTextSMSReplyMuteActionBroadcastReceiver.Companion
//                    .getMUTE_BROADCAST_INTENT());

            PendingIntent mutePendingIntent =
                    PendingIntent.getBroadcast(context, Integer.parseInt(conversation.getThread_id()),
                            muteIntent, PendingIntent.FLAG_MUTABLE);

            NotificationCompat.Action muteAction = new NotificationCompat.Action.Builder(null,
                    context.getString(R.string.conversation_menu_mute), mutePendingIntent)
                    .build();

            builder.addAction(muteAction);
        }

        return builder;
    }


}

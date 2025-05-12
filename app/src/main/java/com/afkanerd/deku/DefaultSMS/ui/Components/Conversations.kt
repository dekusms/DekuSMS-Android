package com.afkanerd.deku.DefaultSMS.ui.Components

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.SMSHandler.sendTextMessage
import com.afkanerd.deku.DefaultSMS.ui.Conversations
import com.example.compose.AppTheme
import sh.calvin.autolinktext.rememberAutoLinkText

enum class ConversationPositionTypes(val value: Int) {
    NORMAL(0),
    START(1),
    MIDDLE(2),
    END(3),
    START_TIMESTAMP(4),
    NORMAL_TIMESTAMP(5),
}

enum class ConversationStatusTypes(val value: Int) {
    STATUS_NONE(-1),
    STATUS_COMPLETE(0),
    STATUS_PENDING(32),
    STATUS_FAILED(64);

    companion object {
        fun fromInt(value: Int): ConversationStatusTypes? {
            return ConversationStatusTypes.entries.find { it.value == value }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationIsKey(isReceived: Boolean = false) {
    Column(
        modifier = Modifier
        .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(isReceived) {
            Text(
                text=stringResource(R.string.secure_communications_request_received),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp),
                color = colorResource(R.color.md_theme_secondary)
            )
        } else {
            Text(
                text=stringResource(R.string.secure_communication_requested),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp),
                color = colorResource(R.color.md_theme_secondary)
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationReceived(
    text: AnnotatedString,
    date: String,
    position: ConversationPositionTypes = ConversationPositionTypes.START_TIMESTAMP,
    showDate: Boolean = true,
    isSelected: Boolean = false,
    onClickCallback: (() -> Unit)? = null,
    onLongClickCallback: (() -> Unit)? = null,
    color: Color = colorResource(R.color.md_theme_onBackground)
) {
    val receivedShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
    val receivedStartShape = RoundedCornerShape(28.dp, 28.dp, 28.dp, 1.dp)
    val receivedMiddleShape = RoundedCornerShape(1.dp, 28.dp, 28.dp, 1.dp)
    val receivedEndShape = RoundedCornerShape(1.dp, 28.dp, 28.dp, 28.dp)

    val shape = when(position) {
        ConversationPositionTypes.NORMAL, ConversationPositionTypes.NORMAL_TIMESTAMP ->
            receivedShape
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP ->
            receivedStartShape
        ConversationPositionTypes.MIDDLE -> receivedMiddleShape
        ConversationPositionTypes.END -> receivedEndShape
    }

    val modifier = when(position) {
        ConversationPositionTypes.NORMAL, ConversationPositionTypes.NORMAL_TIMESTAMP ->
            Modifier.padding(end=32.dp, top=16.dp, bottom=16.dp)
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP ->
            Modifier.padding(end=32.dp, top=16.dp)
        ConversationPositionTypes.MIDDLE -> Modifier.padding(end=32.dp, top=1.dp)
        ConversationPositionTypes.END -> Modifier.padding(end=32.dp, top=1.dp, bottom=16.dp)
    }

    Row(modifier = modifier
        .fillMaxWidth()
    ) {
        Column {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if(isSelected) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .clip(shape=shape)
                    .combinedClickable(
                        onClick = { onClickCallback?.let{ it() }},
                        onLongClick = { onLongClickCallback?.let{ it() } }
                    )
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = color
                )
            }

            if(showDate) {
                Text(
                    text= date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationSent(
    text: AnnotatedString,
    position: ConversationPositionTypes = ConversationPositionTypes.START_TIMESTAMP,
    status: ConversationStatusTypes = ConversationStatusTypes.STATUS_FAILED,
    date: String = "yesterday",
    isSelected: Boolean = false,
    showDate: Boolean = true,
    onClickCallback: (() -> Unit)? = null,
    onLongClickCallback: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.onPrimary
) {
    val sentShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
    val sentStartShape = RoundedCornerShape(28.dp, 28.dp, 1.dp, 28.dp)
    val sentMiddleShape = RoundedCornerShape(28.dp, 1.dp, 1.dp, 28.dp)
    val sentEndShape = RoundedCornerShape(28.dp, 1.dp, 28.dp, 28.dp)

    val shape = when(position) {
        ConversationPositionTypes.NORMAL, ConversationPositionTypes.NORMAL_TIMESTAMP -> sentShape
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP -> sentStartShape
        ConversationPositionTypes.MIDDLE -> sentMiddleShape
        ConversationPositionTypes.END -> sentEndShape
    }

    val modifier = when(position) {
        ConversationPositionTypes.NORMAL, ConversationPositionTypes.NORMAL_TIMESTAMP ->
            Modifier.padding(start=32.dp, top=16.dp, bottom=16.dp)
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP ->
            Modifier.padding(start=32.dp, top=16.dp)
        ConversationPositionTypes.MIDDLE -> Modifier.padding(start=32.dp, top=1.dp)
        ConversationPositionTypes.END -> Modifier.padding(start=32.dp, top=1.dp, bottom=16.dp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if(isSelected) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .clip(shape=shape)
                    .align(alignment = Alignment.End)
                    .combinedClickable(
                        onClick = {
                            onClickCallback?.let { it() }
                        },
                        onLongClick ={
                            onLongClickCallback?.let { it() }
                        }
                    )
            ) {
                Text(
                    text= text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else color
                )
            }

            if(showDate) {
                Text(
                    text= if(status == ConversationStatusTypes.STATUS_PENDING)
                        stringResource(R.string.sms_status_sending)
                    else if(status == ConversationStatusTypes.STATUS_COMPLETE)
                        "$date " + stringResource(R.string.sms_status_delivered)
                    else if(status == ConversationStatusTypes.STATUS_FAILED)
                        stringResource(R.string.sms_status_failed)
                    else "$date " + stringResource(R.string.sms_status_sent),
                    style = MaterialTheme.typography.labelSmall,
                    color = if(status == ConversationStatusTypes.STATUS_FAILED)
                        colorResource(R.color.md_theme_error)
                    else colorResource(R.color.md_theme_outlineVariant),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom=4.dp)
                )
            }
        }

        if(status == ConversationStatusTypes.STATUS_FAILED) {
            Column(modifier = Modifier
                .align(Alignment.CenterVertically)) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Info, "Message failed icon",
                        tint=colorResource(R.color.md_theme_error))
                }
            }
        }
    }
}

@Composable
fun ConversationsCard(
    text: AnnotatedString,
    timestamp: String,
    date: String,
    type: Int = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT,
    showDate: Boolean = true,
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL_TIMESTAMP,
    status: ConversationStatusTypes = ConversationStatusTypes.STATUS_FAILED,
    isSelected: Boolean = false,
    isKey: Boolean = false,
    onClickCallback: (() -> Unit)? = null,
    onLongClickCallback: (() -> Unit)? = null,
) {
    Column {
        if(position == ConversationPositionTypes.START_TIMESTAMP ||
            position == ConversationPositionTypes.NORMAL_TIMESTAMP) {
            Text(
                text=timestamp,
                style= MaterialTheme.typography.labelSmall,
                color = colorResource(R.color.md_theme_outlineVariant),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        Box(modifier = Modifier.padding(start=8.dp, end=8.dp)) {
            when(type)  {
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_ALL -> TODO()
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX -> {
                    if(isKey) {
                        ConversationIsKey(isReceived = true)
                    } else {
                        ConversationReceived(
                            text =text,
                            position =position,
                            date =date,
                            showDate = showDate,
                            isSelected = isSelected,
                            onClickCallback = onClickCallback,
                            onLongClickCallback = onLongClickCallback,
                        )
                    }
                }
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT,
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED,
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX, -> {
                    if(isKey) {
                        ConversationIsKey(isReceived = false)
                    } else {
                        ConversationSent(
                            text =text,
                            position =position,
                            date =date,
                            status =status,
                            isSelected = isSelected,
                            showDate = showDate,
                            onClickCallback = onClickCallback,
                            onLongClickCallback = onLongClickCallback,
                        )
                    }
                }
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, -> TODO()
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_QUEUED, -> TODO()
            }
        }
    }
}

@Composable
fun ConversationsMainDropDownMenu(
    expanded: Boolean = true,
    searchCallback: (() -> Unit)? = null,
    blockCallback: (() -> Unit)? = null,
    deleteCallback: (() -> Unit)? = null,
    archiveCallback: (() -> Unit)? = null,
    muteCallback: (() -> Unit)? = null,
    secureCallback: (() -> Unit)? = null,
    isMute: Boolean = false,
    isBlocked: Boolean = false,
    isArchived: Boolean = false,
    isSecure: Boolean = false,
    dismissCallback: ((Boolean) -> Unit)? = null,
) {
    var expanded = expanded
    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.TopEnd)
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { dismissCallback?.let{ it(false) }},
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.conversations_menu_search_title),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    searchCallback?.let{
                        dismissCallback?.let { it(false) }
                        it()
                    }
                }
            )

            if(isSecure)
                DropdownMenuItem(
                    text = {
                        Text(
                            text=stringResource(R.string.conversations_menu_secure_title),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        secureCallback?.let{
                            dismissCallback?.let { it(false) }
                            it()
                        }
                    }
                )

            DropdownMenuItem(
                text = {
                    Text(
                        text=if(isBlocked) stringResource(R.string.conversations_menu_unblock)
                        else stringResource(R.string.conversation_menu_block),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    blockCallback?.let {
                        dismissCallback?.let { it(false) }
                        it()
                    }
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text=if(isArchived) stringResource(R.string.conversation_menu_unarchive)
                        else stringResource(R.string.archive),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    archiveCallback?.let {
                        dismissCallback?.let { it(false) }
                        it()
                    }
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.conversation_menu_delete),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    deleteCallback?.let {
                        dismissCallback?.let { it(false) }
                        it()
                    }
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text= if(isMute) stringResource(R.string.conversation_menu_unmute)
                        else stringResource(R.string.conversation_menu_mute),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    muteCallback?.let {
                        dismissCallback?.let { it(false) }
                        it()
                    }
                }
            )
        }
    }
}

private fun getPredefinedType(type: Int) : PredefinedTypes? {
    when(type) {
        Telephony.Sms.MESSAGE_TYPE_OUTBOX,
        Telephony.Sms.MESSAGE_TYPE_QUEUED,
        Telephony.Sms.MESSAGE_TYPE_SENT -> {
            return PredefinedTypes.OUTGOING
        }
        Telephony.Sms.MESSAGE_TYPE_INBOX -> {
            return PredefinedTypes.INCOMING
        }
    }
    return null
}

fun getConversationType(
    index: Int,
    conversation: Conversation,
    conversations: List<Conversation>
): ConversationPositionTypes {
    if(conversations.size < 2) {
        return ConversationPositionTypes.NORMAL_TIMESTAMP
    }
    if(index == 0) {
        if(getPredefinedType(conversation.type) == getPredefinedType(conversations[1].type)) {
            if(Helpers.isSameMinute(conversation.date!!.toLong(), conversations[1].date!!.toLong())) {
                return ConversationPositionTypes.END
            }
        }
        if(!Helpers.isSameHour(conversation.date!!.toLong(), conversations[1].date!!.toLong())) {
            return ConversationPositionTypes.NORMAL_TIMESTAMP
        }
    }
    if(index == conversations.size - 1) {
        if(getPredefinedType(conversation.type) == getPredefinedType(conversations.last().type) &&
            Helpers.isSameMinute(
                conversation.date!!.toLong(),
                conversations[index -1].date!!.toLong())
        ) {
            return ConversationPositionTypes.START_TIMESTAMP
        }
        return ConversationPositionTypes.NORMAL_TIMESTAMP
    }

    if(index + 1 < conversations.size && index - 1 > -1 ) {
        if(getPredefinedType(conversation.type) == getPredefinedType(conversations[index - 1].type)
            &&
            getPredefinedType(conversation.type) == getPredefinedType(conversations[index + 1].type)
        ) {
            if(Helpers.isSameHour(conversation.date!!.toLong(), conversations[index -1].date!!.toLong())) {
                if(Helpers.isSameMinute(conversation.date!!.toLong(),
                        conversations[index -1].date!!.toLong()) &&
                    Helpers.isSameMinute(conversation.date!!.toLong(), conversations[index +1].date!!.toLong())) {
                    return ConversationPositionTypes.MIDDLE
                }
                if(Helpers.isSameMinute(conversation.date!!.toLong(), conversations[index -1].date!!.toLong())) {
                    return ConversationPositionTypes.START
                }
            }
        }

        if(getPredefinedType(conversation.type) == getPredefinedType(conversations[index + 1].type))
        {
            if(Helpers.isSameHour(
                    conversation.date!!.toLong(), conversations[index +1].date!!.toLong())
            ) {
                if(Helpers.isSameMinute(
                        conversation.date!!.toLong(), conversations[index +1].date!!.toLong())
                ) {
                    return ConversationPositionTypes.END
                }
            }
            return ConversationPositionTypes.NORMAL_TIMESTAMP
        }

        if(getPredefinedType(conversation.type) == getPredefinedType(conversations[index - 1].type))
        {
            if(Helpers.isSameMinute(
                    conversation.date!!.toLong(), conversations[index -1].date!!.toLong())
            ) {
                if(Helpers.isSameHour(
                        conversation.date!!.toLong(), conversations[index +1].date!!.toLong())
                ) {
                    return ConversationPositionTypes.START_TIMESTAMP
                }
                return ConversationPositionTypes.START
            }
        }

    }
    return ConversationPositionTypes.NORMAL
}

fun sendSMS(
    context: Context,
    text: String,
    messageId: String,
    threadId: String,
    address: String,
    conversationsViewModel: ConversationsViewModel,
    onCompleteCallback: () -> Unit
) {
    val conversation = Conversation()
    conversation.text = text
    conversation.message_id = messageId
    conversation.thread_id = threadId
    conversation.subscription_id = conversationsViewModel.subscriptionId
    conversation.type = Telephony.Sms.MESSAGE_TYPE_OUTBOX
    conversation.date = System.currentTimeMillis().toString()
    conversation.address = address
    conversation.status = Telephony.Sms.STATUS_PENDING
    conversation.isRead = true

    sendTextMessage(
        context = context,
        text = text,
        address = address,
        conversation = conversation,
        conversationsViewModel = conversationsViewModel,
        messageId = null,
        onCompleteCallback = onCompleteCallback
    )
}

enum class PredefinedTypes {
    OUTGOING,
    INCOMING
}

private fun call(context: Context, address: String) {
    val callIntent = Intent(Intent.ACTION_DIAL).apply {
        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        setData("tel:$address".toUri());
    }
    context.startActivity(callIntent);
}

@Preview
@Composable
fun PreviewConversationsReceived() {
    AppTheme(darkTheme = true) {
        Surface(Modifier.safeDrawingPadding()) {
            ConversationReceived(
                text = AnnotatedString("Hello world"),
                date = "yesterday",
            )
        }
    }
}

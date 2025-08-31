package com.afkanerd.smswithoutborders_libsmsmms.ui.components

import android.net.Uri
import android.provider.Telephony
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.DateTimeUtils
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.ui.MmsContentView
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.CustomsConversationsViewModel

enum class ConversationPositionTypes(val value: Int) {
    NORMAL(0),
    START(1),
    MIDDLE(2),
    END(3),
    START_TIMESTAMP(4),
    NORMAL_TIMESTAMP(5),
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
//                color = colorResource(R.color.md_theme_secondary)
            )
        } else {
            Text(
                text=stringResource(R.string.secure_communication_requested),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp),
//                color = colorResource(R.color.md_theme_secondary)
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
    isSelected: Boolean = false,
    onClickCallback: (() -> Unit)? = null,
    onLongClickCallback: (() -> Unit)? = null,
//    color: Color = colorResource(R.color.md_theme_onBackground)
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
            Modifier.padding(end=32.dp, top=16.dp, bottom=8.dp)
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP ->
            Modifier.padding(end=32.dp, top=16.dp)
        ConversationPositionTypes.MIDDLE -> Modifier.padding(end=32.dp, top=1.dp)
        ConversationPositionTypes.END -> Modifier.padding(end=32.dp, top=1.dp, bottom=8.dp)
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
                    .clip(shape = shape)
                    .combinedClickable(
                        onClick = { onClickCallback?.let { it() } },
                        onLongClick = { onLongClickCallback?.let { it() } }
                    )
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
//                    color = color
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
    type: Int,
    isSelected: Boolean = false,
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
            Modifier.padding(start=32.dp, top=16.dp, bottom=8.dp)
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP ->
            Modifier.padding(start=32.dp, top=16.dp)
        ConversationPositionTypes.MIDDLE -> Modifier.padding(start=32.dp, top=1.dp)
        ConversationPositionTypes.END -> Modifier.padding(start=32.dp, top=1.dp, bottom=8.dp)
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
                    .clip(shape = shape)
                    .align(alignment = Alignment.End)
                    .combinedClickable(
                        onClick = {
                            onClickCallback?.let { it() }
                        },
                        onLongClick = {
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

        }

        if(type == Telephony.Sms.MESSAGE_TYPE_FAILED) {
            Column(modifier = Modifier
                .align(Alignment.CenterVertically)) {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Info,
                        "Message failed icon",
//                        tint= colorResource(R.color.design_default_color_error)
                        tint= MaterialTheme.colorScheme.error
                    )
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
    type: Int,
    showDate: Boolean = true,
    position: ConversationPositionTypes,
    status: Int,
    isSelected: Boolean = false,
    mmsContentUri: Uri? = null,
    mmsMimeType: String? = null,
    mmsFilename: String? = null,
    onClickCallback: (() -> Unit)? = null,
    onLongClickCallback: (() -> Unit)? = null,
) {
    val inPreview = LocalInspectionMode.current
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if(position == ConversationPositionTypes.START_TIMESTAMP ||
            position == ConversationPositionTypes.NORMAL_TIMESTAMP || inPreview) {
            Text(
                text=timestamp,
                style= MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp)
        ) {
            when(type) {
                SmsMmsNatives.MMS_MESSAGE_TYPES_M_RETRIEVE_CONF,
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX -> {
                    Column {
                        if(mmsContentUri != null && mmsMimeType != null) {
                            MmsContentView(
                                mmsContentUri,
                                mmsMimeType,
                                mmsFilename,
                                type = type,
                                isSelected = isSelected,
                                onClickCallback = onClickCallback,
                                onLongClickCallback = onLongClickCallback,
                            )
                        }
                        if(text.isNotEmpty()) {
                            ConversationReceived(
                                text =text,
                                position =position,
                                date =date,
                                isSelected = isSelected,
                                onClickCallback = onClickCallback,
                                onLongClickCallback = onLongClickCallback,
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

                SmsMmsNatives.MMS_MESSAGE_TYPES_M_SEND_REQ,
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT,
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_QUEUED,
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED,
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX -> {
                    Column(modifier = Modifier
                        .fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        if(mmsContentUri != null && mmsMimeType != null) {
                            MmsContentView(
                                mmsContentUri,
                                mmsMimeType,
                                mmsFilename,
                                isSelected = isSelected,
                                isSending = true,
                                type = type,
                                onClickCallback = onClickCallback,
                                onLongClickCallback = onLongClickCallback,
                            )
                        }
                        if(text.isNotEmpty()) {
                            ConversationSent(
                                text =text,
                                position =position,
                                type =type,
                                isSelected = isSelected,
                                onClickCallback = onClickCallback,
                                onLongClickCallback = onLongClickCallback,
                            )
                        }

                        if(showDate || inPreview) {
                            Text(
                                text= when(type) {
                                    Telephony.Sms.MESSAGE_TYPE_SENT -> {
                                        if(status == Telephony.Sms.STATUS_COMPLETE)
                                            "$date ${stringResource(
                                                R.string.sms_status_delivered)}"
                                        else
                                            "$date " + stringResource(R.string.sms_status_sent)
                                    }

                                    Telephony.Sms.MESSAGE_TYPE_FAILED ->
                                        stringResource(R.string.sms_status_failed)

                                    Telephony.Sms.MESSAGE_TYPE_QUEUED ->
                                        stringResource(R.string.waiting_to_send)

                                    else ->
                                        stringResource(R.string.sms_status_sending)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if(type == Telephony.Sms.MESSAGE_TYPE_FAILED)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.outlineVariant,

                                modifier = Modifier
                                    .padding(bottom=4.dp)
                            )
                        }
                    }
                }
                else -> {}
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
    isMute: Boolean = false,
    isBlocked: Boolean = false,
    isArchived: Boolean = false,
    customMenuCallbacks: Map<String, () -> Unit>? = null,
    dismissCallback: ((Boolean) -> Unit)? = null,
) {
    val expanded = expanded
    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.TopEnd)
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { dismissCallback?.invoke( false )},
        ) {
            customMenuCallbacks?.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text=item.key,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        dismissCallback?.invoke( false )
                        item.value()
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.conversations_menu_search_title),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    searchCallback?.let{
                        dismissCallback?.invoke( false )
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
                        dismissCallback?.invoke( false )
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
                        dismissCallback?.invoke( false )
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
                        dismissCallback?.invoke( false )
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
                        dismissCallback?.invoke( false )
                        it()
                    }
                }
            )
        }
    }
}

private fun getPredefinedType(type: Int) : ConversationsPredefinedTypes? {
    when(type) {
        Telephony.Sms.MESSAGE_TYPE_OUTBOX,
        Telephony.Sms.MESSAGE_TYPE_QUEUED,
        Telephony.Sms.MESSAGE_TYPE_SENT -> {
            return ConversationsPredefinedTypes.OUTGOING
        }
        Telephony.Sms.MESSAGE_TYPE_INBOX -> {
            return ConversationsPredefinedTypes.INCOMING
        }
    }
    return null
}

fun getConversationType(
    index: Int,
    conversation: Conversations,
    conversations: List<Conversations>
): ConversationPositionTypes {
    if(conversations.size < 2) {
        return ConversationPositionTypes.NORMAL_TIMESTAMP
    }
    if(index == 0) {
        if(getPredefinedType(
                conversation.sms?.type!!) ==
            getPredefinedType(conversations[1].sms?.type!!)) {
            if(DateTimeUtils.isSameMinute(
                    conversation.sms?.date!!.toLong(),
                    conversations[1].sms?.date!!.toLong())) {
                return ConversationPositionTypes.END
            }
        }
        if(!DateTimeUtils.isSameHour( conversation.sms?.date!!.toLong(),
                conversations[1].sms?.date!!.toLong())) {
            return ConversationPositionTypes.NORMAL_TIMESTAMP
        }
    }

    if(index == conversations.size - 1) {
        if(getPredefinedType(conversation.sms?.type!!) ==
            getPredefinedType(conversations.last().sms?.type!!) &&
            DateTimeUtils.isSameMinute(
                conversation.sms?.date!!.toLong(),
                conversations[index -1].sms?.date!!.toLong())
        ) {
            return ConversationPositionTypes.START_TIMESTAMP
        }
        return ConversationPositionTypes.NORMAL_TIMESTAMP
    }

    if(index + 1 < conversations.size && index - 1 > -1 ) {
        if(getPredefinedType(conversation.sms?.type!!) ==
            getPredefinedType(conversations[index - 1].sms?.type!!) &&
            getPredefinedType(conversation.sms?.type!!) ==
            getPredefinedType(conversations[index + 1].sms?.type!!)
        ) {
            if(DateTimeUtils.isSameHour(conversation.sms?.date!!.toLong(),
                    conversations[index -1].sms?.date!!.toLong())) {
                if(DateTimeUtils.isSameMinute(conversation.sms?.date!!.toLong(),
                        conversations[index -1].sms?.date!!.toLong()) &&
                    DateTimeUtils.isSameMinute(conversation.sms?.date!!.toLong(),
                        conversations[index +1].sms?.date!!.toLong())) {
                    return ConversationPositionTypes.MIDDLE
                }
                if(DateTimeUtils.isSameMinute(conversation.sms?.date!!.toLong(),
                        conversations[index -1].sms?.date!!.toLong())) {
                    return ConversationPositionTypes.START
                }
            }
        }

        if(getPredefinedType(conversation.sms?.type!!) ==
            getPredefinedType(conversations[index + 1].sms?.type!!)) {
            if(DateTimeUtils.isSameHour(
                    conversation.sms?.date!!.toLong(),
                    conversations[index +1].sms?.date!!.toLong())
            ) {
                if(DateTimeUtils.isSameMinute(
                        conversation.sms?.date!!.toLong(),
                        conversations[index +1].sms?.date!!.toLong())
                ) {
                    return ConversationPositionTypes.END
                }
            }
            return ConversationPositionTypes.NORMAL_TIMESTAMP
        }

        if(getPredefinedType(conversation.sms?.type!!) ==
            getPredefinedType(conversations[index - 1].sms?.type!!)) {
            if(DateTimeUtils.isSameMinute(
                    conversation.sms?.date!!.toLong(),
                    conversations[index -1].sms?.date!!.toLong())
            ) {
                if(DateTimeUtils.isSameHour(
                        conversation.sms?.date!!.toLong(),
                        conversations[index +1].sms?.date!!.toLong())
                ) {
                    return ConversationPositionTypes.START_TIMESTAMP
                }
                return ConversationPositionTypes.START
            }
        }

    }
    return ConversationPositionTypes.NORMAL
}


enum class ConversationsPredefinedTypes {
    OUTGOING,
    INCOMING
}

@Composable
fun ConversationContactName(
    contactName: String,
    isSecured: Boolean = false
) {
    Column {
        Row {
            Text(
                text= contactName,
                maxLines =1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end=8.dp),
            )
            if(isSecured || LocalInspectionMode.current) {
                Icon(Icons.Default.Security,
                    stringResource(R.string.conversation_is_secured)
                )
            }
        }
        if(isSecured || LocalInspectionMode.current) {
            Text(
                stringResource(R.string.secured),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Preview
@Composable
fun PreviewConversations_Card() {
    Surface(Modifier.safeDrawingPadding()) {
        Column {
            ConversationsCard(
                text = AnnotatedString("Hello world"),
                timestamp = "Yesterday",
                date = "Yesterday",
                type = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
                position = ConversationPositionTypes.NORMAL,
                status = Telephony.Sms.STATUS_PENDING,
                isSelected = false,
            )
        }
    }
}

@Preview
@Composable
fun PreviewConversationsReceived() {
    Surface(Modifier.safeDrawingPadding()) {
        Column {
            ConversationReceived(
                text = AnnotatedString("Hello world"),
                date = "yesterday",
            )
            ConversationSent(
                text = AnnotatedString("Hello world"),
                type = Telephony.Mms.MESSAGE_BOX_FAILED
            )
        }
    }
}


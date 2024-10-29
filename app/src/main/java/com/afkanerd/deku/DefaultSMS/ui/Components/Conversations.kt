package com.afkanerd.deku.DefaultSMS.ui.Components

import android.provider.Telephony
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.room.util.TableInfo
import com.google.android.material.card.MaterialCardView

enum class ConversationMessageTypes(val value: Int) {
    MESSAGE_TYPE_ALL(0),
    MESSAGE_TYPE_INBOX(1),
    MESSAGE_TYPE_SENT(2),
    MESSAGE_TYPE_DRAFT(3),
    MESSAGE_TYPE_OUTBOX(4),
    MESSAGE_TYPE_FAILED(5),
    MESSAGE_TYPE_QUEUED(6);

    companion object {
        fun fromInt(value: Int): ConversationMessageTypes? {
            return ConversationMessageTypes.entries.find { it.value == value }
        }
    }
}

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
private fun ConversationReceived(
    text: String = stringResource(R.string.settings_add_gateway_server_protocol_meta_description),
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL,
    date: String = "yesterday"
) {
    val receivedShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
    val receivedStartShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp)
    val receivedMiddleShape = RoundedCornerShape(5.dp, 18.dp, 18.dp, 5.dp)
    val receivedEndShape = RoundedCornerShape(5.dp, 18.dp, 18.dp, 18.dp)

    val shape = when(position) {
        ConversationPositionTypes.NORMAL, ConversationPositionTypes.NORMAL_TIMESTAMP ->
            receivedShape
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP ->
            receivedStartShape
        ConversationPositionTypes.MIDDLE -> receivedMiddleShape
        ConversationPositionTypes.END -> receivedEndShape
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(end=32.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(shape=shape)
                    .background(colorResource(R.color.md_theme_outline))
                    .padding(16.dp)
            ) {
                Text(
                    text=text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.md_theme_outlineVariant)
                )
            }

            Text(
                text= date,
                style = MaterialTheme.typography.labelSmall,
                color = colorResource(R.color.md_theme_outlineVariant),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationSent(
    text: String = stringResource(R.string.settings_add_gateway_server_protocol_meta_description),
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL,
    status: ConversationStatusTypes = ConversationStatusTypes.STATUS_FAILED,
    date: String = "yesterday",
) {
    val sentShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
    val sentStartShape = RoundedCornerShape(18.dp, 18.dp, 5.dp, 18.dp)
    val sentMiddleShape = RoundedCornerShape(18.dp, 5.dp, 5.dp, 18.dp)
    val sentEndShape = RoundedCornerShape(18.dp, 5.dp, 18.dp, 18.dp)

    val shape = when(position) {
        ConversationPositionTypes.NORMAL, ConversationPositionTypes.NORMAL_TIMESTAMP -> sentShape
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP -> sentStartShape
        ConversationPositionTypes.MIDDLE -> sentMiddleShape
        ConversationPositionTypes.END -> sentEndShape
    }

    Row(
        modifier = Modifier
            .padding(start=32.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(shape=shape)
                    .background(colorResource(R.color.md_theme_primaryContainer))
                    .padding(16.dp)
            ) {
                Text(
                    text=text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.md_theme_onPrimaryContainer)
                )
            }
            Text(
                text= if(status == ConversationStatusTypes.STATUS_PENDING)
                    stringResource(R.string.sms_status_sending)
                else if(status == ConversationStatusTypes.STATUS_COMPLETE)
                    stringResource(R.string.sms_status_delivered)
                else if(status == ConversationStatusTypes.STATUS_FAILED)
                    stringResource(R.string.sms_status_failed_only)
                else stringResource(R.string.sms_status_sent),
                style = MaterialTheme.typography.labelSmall,
                color = if(status == ConversationStatusTypes.STATUS_FAILED)
                    colorResource(R.color.md_theme_error)
                else colorResource(R.color.md_theme_outlineVariant),
                modifier = Modifier.align(Alignment.End)
            )
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

@Preview(showBackground = true)
@Composable
fun ConversationsCard(
    text: String = "Hello world",
    timestamp: String = "Yesterday",
    date: String = "yesterday",
    type: ConversationMessageTypes = ConversationMessageTypes.MESSAGE_TYPE_SENT,
    position: ConversationPositionTypes = ConversationPositionTypes.START_TIMESTAMP,
    status: ConversationStatusTypes = ConversationStatusTypes.STATUS_FAILED,
) {
    Column(modifier = Modifier.padding(start = 8.dp, end=8.dp)) {
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
        when(type)  {
            ConversationMessageTypes.MESSAGE_TYPE_ALL -> TODO()
            ConversationMessageTypes.MESSAGE_TYPE_INBOX -> ConversationReceived(
                text=text, position=position, date=date)
            ConversationMessageTypes.MESSAGE_TYPE_SENT,
            ConversationMessageTypes.MESSAGE_TYPE_FAILED,
            ConversationMessageTypes.MESSAGE_TYPE_OUTBOX -> ConversationSent(
                text=text, position=position, date=date, status=status)
            ConversationMessageTypes.MESSAGE_TYPE_DRAFT -> TODO()
            ConversationMessageTypes.MESSAGE_TYPE_QUEUED -> TODO()
        }
    }
}
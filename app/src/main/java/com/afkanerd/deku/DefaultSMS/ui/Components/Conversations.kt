package com.afkanerd.deku.DefaultSMS.ui.Components

import android.provider.Telephony
import android.text.Layout
import androidx.compose.foundation.CombinedClickableNode
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
import androidx.compose.material3.ElevatedCard
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
import androidx.core.content.ContextCompat.getString
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.room.util.TableInfo
import com.google.android.material.card.MaterialCardView

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


@Preview(showBackground = true)
@Composable
private fun ConversationReceived(
    text: String = stringResource(R.string.settings_add_gateway_server_protocol_meta_description),
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL,
    date: String = "yesterday",
    showDate: Boolean = true,
    isSelected: Boolean = false,
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

    val modifier = when(position) {
        ConversationPositionTypes.NORMAL, ConversationPositionTypes.NORMAL_TIMESTAMP ->
            Modifier.padding(end=32.dp, top=16.dp, bottom=16.dp)
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP ->
            Modifier.padding(end=32.dp, top=16.dp)
        ConversationPositionTypes.MIDDLE -> Modifier.padding(end=32.dp, top=4.dp)
        ConversationPositionTypes.END -> Modifier.padding(end=32.dp, top=4.dp, bottom=16.dp)
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
            ) {
                Text(
                    text=text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = colorResource(R.color.md_theme_onBackground)
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

@Preview(showBackground = true)
@Composable
private fun ConversationSent(
    text: String = stringResource(R.string.settings_add_gateway_server_protocol_meta_description),
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL,
    status: ConversationStatusTypes = ConversationStatusTypes.STATUS_FAILED,
    date: String = "yesterday",
    isSelected: Boolean = false,
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

    val modifier = when(position) {
        ConversationPositionTypes.NORMAL, ConversationPositionTypes.NORMAL_TIMESTAMP ->
            Modifier.padding(start=32.dp, top=16.dp, bottom=16.dp)
        ConversationPositionTypes.START, ConversationPositionTypes.START_TIMESTAMP ->
            Modifier.padding(start=32.dp, top=16.dp)
        ConversationPositionTypes.MIDDLE -> Modifier.padding(start=32.dp, top=4.dp)
        ConversationPositionTypes.END -> Modifier.padding(start=32.dp, top=4.dp, bottom=16.dp)
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
            ) {
                Text(
                    text=text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onPrimary
                )
            }
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
    type: Int = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT,
    position: ConversationPositionTypes = ConversationPositionTypes.START_TIMESTAMP,
    status: ConversationStatusTypes = ConversationStatusTypes.STATUS_FAILED,
    showDate: Boolean = false,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isKey: Boolean = false,
) {
    Column(modifier = modifier) {
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
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_ALL -> TODO()
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX -> {
                if(isKey) {
                    ConversationIsKey(isReceived = true)
                } else {
                    ConversationReceived(
                        text=text,
                        position=position,
                        date=date,
                        showDate = showDate,
                        isSelected = isSelected
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
                        text=text,
                        position=position,
                        date=date,
                        status=status,
                        isSelected = isSelected
                    )
                }
            }
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, -> TODO()
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_QUEUED, -> TODO()
        }
    }
}
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
}


@Preview(showBackground = true)
@Composable
private fun ConversationReceived(
    text: String = stringResource(R.string.settings_add_gateway_server_protocol_meta_description),
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL) {
    val receivedShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
    val receivedStartShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp)
    val receivedMiddleShape = RoundedCornerShape(5.dp, 18.dp, 18.dp, 5.dp)
    val receivedEndShape = RoundedCornerShape(5.dp, 18.dp, 18.dp, 18.dp)

    val shape = when(position) {
        ConversationPositionTypes.NORMAL -> receivedShape
        ConversationPositionTypes.START -> receivedStartShape
        ConversationPositionTypes.MIDDLE -> receivedMiddleShape
        ConversationPositionTypes.END -> receivedEndShape
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(end=32.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(shape=shape)
                .background(colorResource(R.color.md_theme_inverseSurface_highContrast))
                .padding(16.dp)
        ) {
            Text(
                text=text,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.md_theme_inverseOnSurface_highContrast)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationSent(
    text: String = stringResource(R.string.settings_add_gateway_server_protocol_meta_description),
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL) {
    val sentShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
    val sentStartShape = RoundedCornerShape(18.dp, 18.dp, 5.dp, 18.dp)
    val sentMiddleShape = RoundedCornerShape(18.dp, 5.dp, 5.dp, 18.dp)
    val sentEndShape = RoundedCornerShape(18.dp, 5.dp, 18.dp, 18.dp)

    val shape = when(position) {
        ConversationPositionTypes.NORMAL -> sentShape
        ConversationPositionTypes.START -> sentStartShape
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
                text="sent",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.md_theme_onPrimaryContainer),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationsCard(
    text: String = "Hello world",
    timestamp: String = "Yesterday",
    type: ConversationMessageTypes = ConversationMessageTypes.MESSAGE_TYPE_SENT,
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL
) {
    Column(modifier = Modifier.padding(start = 8.dp, end=8.dp)) {
        Text(
            text=timestamp,
            style= MaterialTheme.typography.labelSmall,
            color = colorResource(R.color.md_theme_secondary),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        when(type)  {
            ConversationMessageTypes.MESSAGE_TYPE_ALL -> TODO()
            ConversationMessageTypes.MESSAGE_TYPE_INBOX -> ConversationReceived(text, position)
            ConversationMessageTypes.MESSAGE_TYPE_SENT -> ConversationSent(text, position)
            ConversationMessageTypes.MESSAGE_TYPE_DRAFT -> TODO()
            ConversationMessageTypes.MESSAGE_TYPE_OUTBOX -> TODO()
            ConversationMessageTypes.MESSAGE_TYPE_FAILED -> TODO()
            ConversationMessageTypes.MESSAGE_TYPE_QUEUED -> TODO()
        }
    }
}
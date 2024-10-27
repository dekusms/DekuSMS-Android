package com.afkanerd.deku.DefaultSMS.ui.Components

import android.provider.Telephony
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.google.android.material.card.MaterialCardView

enum class ConversationMessageTypes(val value: Int) {
    MESSAGE_TYPE_ALL(0),
    MESSAGE_TYPE_INBOX(1),
    MESSAGE_TYPE_SENT(2),
    MESSAGE_TYPE_DRAFT(3),
    MESSAGE_TYPE_OUTBOX(4),
    MESSAGE_TYPE_FAILED(5),
    MESSAGE_TYPE_QUEUED(6)
}

enum class ConversationPositionTypes(val value: Int) {
    NORMAL(0),
    START(1),
    MIDDLE(2),
    END(3),
}


@Preview
@Composable
private fun ConversationReceived(
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
    Box(
        modifier = Modifier
            .clip(shape=shape)
            .background(Color.Blue)
            .size(100.dp)
    ) {
    }
}

@Preview
@Composable
private fun ConversationSent(
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
    Box(
        modifier = Modifier
            .clip(shape=shape)
            .background(Color.Blue)
            .size(100.dp)
    ) {
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationsCard(
    type: ConversationMessageTypes = ConversationMessageTypes.MESSAGE_TYPE_SENT,
    position: ConversationPositionTypes = ConversationPositionTypes.NORMAL
) {

    when(type)  {
        ConversationMessageTypes.MESSAGE_TYPE_ALL -> TODO()
        ConversationMessageTypes.MESSAGE_TYPE_INBOX -> TODO()
        ConversationMessageTypes.MESSAGE_TYPE_SENT -> {
            ConversationSent(position)
        }
        ConversationMessageTypes.MESSAGE_TYPE_DRAFT -> TODO()
        ConversationMessageTypes.MESSAGE_TYPE_OUTBOX -> TODO()
        ConversationMessageTypes.MESSAGE_TYPE_FAILED -> TODO()
        ConversationMessageTypes.MESSAGE_TYPE_QUEUED -> TODO()
    }
}
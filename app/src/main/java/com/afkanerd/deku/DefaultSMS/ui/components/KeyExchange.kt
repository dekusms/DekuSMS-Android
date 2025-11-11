package com.afkanerd.deku.DefaultSMS.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.EncryptionController
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.example.compose.AppTheme

@Composable
fun KeyExchangeType(conversation: Conversations) {
    val color = MaterialTheme.colorScheme.secondary

    val type = if(LocalInspectionMode.current) EncryptionController.MessageRequestType.TYPE_REQUEST
    else EncryptionController.MessageRequestType.fromMessage(conversation.sms_data!!)

    val text = when(type) {
        EncryptionController.MessageRequestType.TYPE_REQUEST ->
            stringResource(R.string.secure_message_requested)
        EncryptionController.MessageRequestType.TYPE_ACCEPT ->
            stringResource(R.string.secure_message_accepted)
        else -> {
            String()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(Modifier
            .padding()
            .weight(1f),
            color = color
        )

        Text(text, Modifier
            .padding(start = 8.dp)
            .weight(1f),
            color = color,
            fontStyle = FontStyle.Italic
        )

        HorizontalDivider(Modifier
            .padding()
            .weight(1f),
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KeyExchangeType_Preview() {
    AppTheme {
        KeyExchangeType(Conversations())
    }
}
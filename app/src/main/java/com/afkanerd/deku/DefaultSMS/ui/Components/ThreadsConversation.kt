package com.afkanerd.deku.DefaultSMS.ui.Components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.Extensions.toHslColor

@Composable
private fun ThreadConversationsAvatar(id: String, firstName: String, lastName: String) {
    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        val color = remember(id, firstName, lastName) {
            Color("$id / $firstName".toHslColor())
        }
        val initials = (firstName.take(1) + lastName.take(1)).uppercase()
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(color))
        }
        Text(text=initials, style= MaterialTheme.typography.titleSmall, color= Color.White)
    }
}

@Composable
private fun ThreadConversationsContents(firstName: String, lastName: String, content: String) {
    Column {
        Text(
            text = "$firstName $lastName",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = content,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview
@Composable
fun ThreadConversationCard(
    id: String = "id",
    firstName: String = "Jane",
    lastName: String = "Doe",
    content: String = "Text Template"
) {
    Row(Modifier
        .fillMaxWidth()
        .padding(all = 8.dp)
    ) {
        ThreadConversationsAvatar(id=id, firstName=firstName, lastName=lastName)
        Spacer(Modifier.padding(start = 16.dp))
        ThreadConversationsContents(firstName=firstName, lastName=lastName, content=content)
    }
}

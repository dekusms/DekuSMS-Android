package com.afkanerd.deku.DefaultSMS.ui.Components

import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.Extensions.toHslColor
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import java.nio.file.WatchEvent

@Composable
private fun ThreadConversationsAvatar(
    id: String,
    firstName: String,
    lastName: String,
    isContact: Boolean = true) {
    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        if(isContact) {
            val color = remember(id, firstName, lastName) {
                Color("$id / $firstName".toHslColor())
            }
            val initials = (firstName.take(1) + lastName.take(1)).uppercase()
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(SolidColor(color))
            }
            Text(text = initials, style = MaterialTheme.typography.titleSmall, color = Color.White)
        }
        else {
//            Image(
//                painter = painterResource(R.drawable.baseline_account_circle_24ne_account_circle_24),
//                contentDescription = "Avatar image",
//                modifier = Modifier.fillMaxSize()
//            )
            Icon(
                Icons.Filled.Person,
                contentDescription = "",
                Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
private fun ThreadConversationsContents(
    firstName: String,
    lastName: String,
    content: String,
    date: String,
    isRead: Boolean = false,
    unreadCount: Int = 1
) {

    var color = MaterialTheme.colorScheme.onBackground
    var weight = FontWeight.Bold

    if(isRead) {
        MaterialTheme.colorScheme.secondary
        weight = FontWeight.Normal
    }
    Row {
        Column(Modifier.weight(1f)) {
            Text(
                text = "$firstName $lastName",
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = weight
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = content,
                color = color,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = weight,
                maxLines = if(isRead) 1 else 3
            )
        }
//        Spacer(modifier = Modifier.weight(1f))
        Column {
            if(unreadCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(top=16.dp)
                        ) {
                            Text(unreadCount.toString())
                        }
                    },
                ) {
                    Text(
                        text = date,
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = weight
                    )
                }
            } else {
                Text(
                    text = date,
                    color = color,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = weight
                )
            }
        }
    }
    
}

@Preview(showBackground = true)
@Composable
fun ThreadConversationCard(
    id: String = "id",
    firstName: String = "Jane",
    lastName: String = "",
    content: String = "Text Template",
    date: String = "Tues",
    isRead: Boolean = false,
    isContact: Boolean = true,
    unreadCount: Int = 1,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    var color = MaterialTheme.colorScheme.onBackground
    var weight = FontWeight.Bold

    if(isRead) {
        MaterialTheme.colorScheme.secondary
        weight = FontWeight.Normal
    }
    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = if(isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.background
        ),
        headlineContent = {
            Text(
                text = "$firstName $lastName",
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = weight
            )
        },
        supportingContent = {
            Text(
                text = content,
                color = color,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = weight,
                maxLines = if(isRead) 1 else 3
            )
        },
        trailingContent = {
            if(unreadCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(bottom=16.dp)
                        ) {
                            Text(unreadCount.toString())
                        }
                    },
                ) {
                    Text(
                        text = date,
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = weight
                    )
                }
            } else {
                Text(
                    text = date,
                    color = color,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = weight
                )
            }
        },
        leadingContent = {
            ThreadConversationsAvatar(id, firstName, lastName, isContact)
        }
    )
}

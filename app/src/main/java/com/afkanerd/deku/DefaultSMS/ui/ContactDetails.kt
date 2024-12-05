package com.afkanerd.deku.DefaultSMS.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.afkanerd.deku.DefaultSMS.Extensions.toHslColor
import com.afkanerd.deku.DefaultSMS.R
import kotlin.text.take
import kotlin.text.uppercase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetails (
    phoneNumber: String,
    contactPhotoUri: String? = null,
    isContact: Boolean = false,
    isEncryptionEnabled: Boolean = false,
    firstName: String,
    lastName: String,
    id: String? = null,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {Text("")},
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isContact) {
                    if (contactPhotoUri != null && contactPhotoUri != "null") {
                        AsyncImage(
                            model = contactPhotoUri,
                            contentDescription = "Contact Photo",
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .background(
                                    remember(id, firstName, lastName) {
                                        Color("$id / $firstName".toHslColor())
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (firstName.take(1) + lastName.take(1)).uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 24.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Default Avatar",
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }

                if (isContact) {
                    Text(
                        text = "$firstName $lastName",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                IconButton(onClick = { /* Handle call action */ }) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.LightGray, CircleShape)
                            .clip(CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row {
                    IconButton(onClick = { /* Handle edit action */ }) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.LightGray, CircleShape)
                                .clip(CircleShape)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Call",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { /* Handle action */ }) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.LightGray, CircleShape)
                            .clip(CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "About",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { /* Handle search action */ }) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.LightGray, CircleShape)
                            .clip(CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    TextButton(onClick = { /* Handle notifications click */ }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notification"
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(R.string.notifications),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    TextButton(onClick = { /* Handle block & report spam click */ }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Block,
                                contentDescription = "Block and Report",
                                tint = Color.Red
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(R.string.block_report_spam),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = stringResource(R.string.end_to_end_encryption),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(R.string.end_to_end_encrypt))
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEncryptionEnabled) Color.Green else Color.Red
                                    )
                                ) {
                                    append(if (isEncryptionEnabled) "On" else "Off")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.end_to_end_encryption_isn_t_available_in_this_conversation),
                            style = MaterialTheme.typography.bodySmall
                        )

                    }
                }

            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = phoneNumber,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = { /* Handle copy phone number action */ }) {
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = "Copy Phone Number"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactDetailsPreview() {
    ContactDetails(
        phoneNumber = "+1-555-123-4567",
        isContact = true,
        isEncryptionEnabled = true,
        firstName = "John",
        lastName = "Doe",
        id = "12345",
        onBackClick = { }
    )
}
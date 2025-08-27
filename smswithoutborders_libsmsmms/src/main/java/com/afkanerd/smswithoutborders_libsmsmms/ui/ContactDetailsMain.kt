package com.afkanerd.smswithoutborders_libsmsmms.ui

import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PersonAdd
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.Uri
import coil3.compose.AsyncImage
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.blockContact
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.copyItemToClipboard
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultRegion
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isShortCode
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactName
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactPhoto
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactUri
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.unblockContact
import com.afkanerd.smswithoutborders_libsmsmms.extensions.toHslColor
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.SearchScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.SearchViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ThreadsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetails (
    address: String,
    navController: NavController,
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current

    val contactUri = context.retrieveContactUri(address)
    val isContact by remember { mutableStateOf( contactUri != null) }

    val contactPhotoUri by remember {
        mutableStateOf(context.retrieveContactPhoto(address) )
    }
    val isEncryptionEnabled by remember { mutableStateOf(false) }

    var isDefault by remember{ mutableStateOf( inPreviewMode || context.isDefault()) }

    val contactName by remember{ mutableStateOf(
        if(isDefault) {
            context.retrieveContactName(address) ?: address
        } else address.replace(Regex("[\\s-]"), "")
    )}

    val isShortCode = isShortCode(address)
    var isBlocked by remember { mutableStateOf(
        if(!inPreviewMode)
            ConversationsViewModel().contactIsBlocked(context, address)
        else false
    ) }

    var isMute by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back")
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
                                    remember(contactName) {
                                        Color(contactName.toHslColor())
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contactName[0].uppercase(),
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
                        text = contactName,
                        style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                if (!isShortCode) {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_CALL,
                            "tel:$address".toUri())
                        context.startActivity(intent)
                    }) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
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
                }

                Row {
                    if (isContact) {
                        IconButton(onClick = {
                            try {
                                if (contactUri != null) {
                                    val editIntent = Intent(Intent.ACTION_EDIT)
                                    editIntent.setDataAndType(contactUri,
                                        ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                                    editIntent.putExtra("finishActivityOnSaveCompleted", true)
                                    context.startActivity(editIntent)
                                } else {
                                    Toast.makeText(context, "Contact not found",
                                        Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context,
                                    "Failed to open editor: ${e.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
                                    .clip(CircleShape)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    else if(!isShortCode) {
                        IconButton(onClick = {
                            val addContactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
                            addContactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
                            addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, address)
                            context.startActivity(addContactIntent)
                        }) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
                                    .clip(CircleShape)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PersonAdd,
                                    contentDescription = "Add Contact",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = {
                    navController.navigate(SearchScreenNav(address = address))
                }) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
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
                    TextButton(onClick = {
                        TODO("Implement mute")
                    }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isMute)
                                    Icons.Outlined.NotificationsOff
                                else Icons.Outlined.Notifications, // Change icon based on isMute
                                contentDescription = "Notification"
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(R.string.notifications),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    TextButton(onClick = {
                        if(isBlocked) {
                            ThreadsViewModel().setIsBlocked(
                                context, listOf(address), false) {
                                context.unblockContact(listOf(address))
                            }
                            isBlocked = false
                        }
                        else {
                            ThreadsViewModel().setIsBlocked(context,
                                listOf(address), true) {
                                context.blockContact(listOf(address))
                            }
                            isBlocked = true
                        }
                    }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Block,
                                contentDescription = "Block",
                                tint = Color.Red
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (isBlocked) stringResource(R.string.unblock) else stringResource(R.string.block),
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
                                    append(if (isEncryptionEnabled) stringResource(R.string.on) else stringResource(
                                        R.string.off
                                    )
                                    )
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!isEncryptionEnabled) {
                            Text(
                                text = stringResource(R.string.end_to_end_encryption_isn_t_available_in_this_conversation),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.end_to_end_encryption_is_available_in_this_conversation),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }


                    }
                }

            }

            if (!isShortCode) {
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
                                text = address,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            IconButton(onClick = {
                                context.copyItemToClipboard(address)
                            }) {
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
}

@Preview(showBackground = true)
@Composable
fun ContactDetailsPreview() {
    ContactDetails(
        address = "+123456789",
        navController = rememberNavController()
    )
}

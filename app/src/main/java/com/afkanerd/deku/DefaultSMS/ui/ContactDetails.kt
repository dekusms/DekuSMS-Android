package com.afkanerd.deku.DefaultSMS.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ContactsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.SearchViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Extensions.toHslColor
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.E2EEHandler
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.SearchThreadScreen
import com.afkanerd.deku.DefaultSMS.ui.Components.ConvenientMethods
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetails (
    conversationViewModel: ConversationsViewModel,
    searchViewModel: SearchViewModel,
    threadConversationsViewModel: ThreadedConversationsViewModel = ThreadedConversationsViewModel(),
    navController: NavController,
) {

    val context = LocalContext.current
    val phoneNumber by remember { mutableStateOf(conversationViewModel.address) }

    val isContact by remember { mutableStateOf(Contacts.retrieveContactUri(context, phoneNumber) != null) }
    val contactPhotoUri by remember { mutableStateOf(Contacts
        .retrieveContactPhoto(context, conversationViewModel.address) )}
    val isEncryptionEnabled by remember { mutableStateOf(E2EEHandler.isSecured(context,
        conversationViewModel.address) )}
    val contactName by remember { mutableStateOf(conversationViewModel.contactName) }
    val isShortCode by remember { mutableStateOf(Helpers.isShortCode(conversationViewModel.address)) }
    val isBlocked by remember { mutableStateOf( BlockedNumberContract.isBlocked(context, conversationViewModel.address)) }


    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                    Log.d("ContactDetails", "is contact: $isContact")
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
                    Log.d("ContactDetails", "Default Avatar")
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
                if (!isShortCode) {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
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
                                val contactUri = Contacts.retrieveContactUri(context, phoneNumber)

                                if (contactUri != null) {
                                    val editIntent = Intent(Intent.ACTION_EDIT)
                                    editIntent.setDataAndType(contactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                                    editIntent.putExtra("finishActivityOnSaveCompleted", true)
                                    context.startActivity(editIntent)
                                } else {
                                    Toast.makeText(context, "Contact not found", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to open editor: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("ContactDetails", "Failed to open editor", e)
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
                    } else if(!isShortCode) {
                        IconButton(onClick = {
                            val addContactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
                            addContactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
                            addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
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
                    searchViewModel.threadId = conversationViewModel.threadId
                    navController.navigate(SearchThreadScreen)
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
                        TODO()
                    }) {
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

                    TextButton(onClick = {
                        if(isBlocked) {
                            TODO()
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
                                text = stringResource(R.string.conversation_menu_block),
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
                                text = phoneNumber,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            IconButton(onClick = {
                                val clipData = ClipData.newPlainText("plain text", phoneNumber)
                                val clipEntry = ClipEntry(clipData)
                                clipboardManager.setClip(clipEntry)
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
    val conversationViewModel = ConversationsViewModel()
    ContactDetails(
        conversationViewModel = conversationViewModel,
        searchViewModel = SearchViewModel(),
        navController = rememberNavController()
    )
}

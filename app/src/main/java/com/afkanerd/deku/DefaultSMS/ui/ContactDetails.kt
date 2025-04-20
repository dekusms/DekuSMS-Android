package com.afkanerd.deku.DefaultSMS.ui

import android.content.ClipData
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.SearchViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Extensions.toHslColor
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.E2EEHandler
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.ui.Components.ConvenientMethods
import com.afkanerd.deku.SearchThreadScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetails (
    conversationsViewModel: ConversationsViewModel,
    searchViewModel: SearchViewModel,
    navController: NavController,
) {

    val context = LocalContext.current
    val phoneNumber by remember { mutableStateOf(conversationsViewModel.address) }

    val isContact by remember {
        mutableStateOf(Contacts.retrieveContactUri(context, phoneNumber) != null)
    }
    val contactPhotoUri by remember { mutableStateOf(Contacts
        .retrieveContactPhoto(context, conversationsViewModel.address) )}
    val isEncryptionEnabled by remember { mutableStateOf(E2EEHandler.isSecured(context,
        conversationsViewModel.address) )}

    val defaultRegion = if(LocalInspectionMode.current) "cm" else Helpers.getUserCountry( context )
    val contactName by remember{ mutableStateOf(
        Contacts.retrieveContactName(
            context,
            Helpers.getFormatCompleteNumber(conversationsViewModel.address, defaultRegion)
        ) ?: conversationsViewModel.address.run {
            conversationsViewModel.address.replace(Regex("[\\s-]"), "")
        }
    )}

    val isShortCode by remember { mutableStateOf(Helpers.isShortCode(conversationsViewModel.address)) }
    val inPreviewMode = LocalInspectionMode.current
    var isBlocked by remember { mutableStateOf(
        if(!inPreviewMode)
            BlockedNumberContract .isBlocked(context, phoneNumber)
        else false
    ) }


    val clipboardManager = LocalClipboardManager.current
    var isMute by remember { mutableStateOf(false) }
    val coroutineScope = remember { CoroutineScope(Dispatchers.Default) }

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
                    searchViewModel.threadId = conversationsViewModel.threadId
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
                        coroutineScope.launch {
                            if (isMute) conversationsViewModel.unMute(context)
                            else conversationsViewModel.mute(context)
                            isMute = conversationsViewModel.isMuted(context)
                        }
                    }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isMute) Icons.Outlined.NotificationsOff else Icons.Outlined.Notifications, // Change icon based on isMute
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
                        if (isBlocked) {
                            conversationsViewModel.unblock(context)
                        } else {
                            ConvenientMethods.blockContact(context, phoneNumber)
                        }
                        isBlocked = BlockedNumberContract.isBlocked(context, conversationsViewModel.address)
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
        conversationsViewModel = conversationViewModel,
        searchViewModel = SearchViewModel(),
        navController = rememberNavController()
    )
}

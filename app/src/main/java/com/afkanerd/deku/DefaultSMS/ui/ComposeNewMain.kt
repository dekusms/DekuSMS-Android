package com.afkanerd.deku.DefaultSMS.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ContactsViewModel
import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.runtime.setValue
import com.afkanerd.deku.DefaultSMS.Models.Contacts

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Extensions.toHslColor
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversationsHandler
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler

@Preview
@Composable
fun ContactAvatar(
    id: String = "",
    name: String = "Template User",
    phoneNumber: String = "+237123456789",
) {
    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        val color = remember(id, name, phoneNumber) {
            Color("$id / $name".toHslColor())
        }
        val initials = name.take(1).uppercase()
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(color))
        }
        Text(text = initials, style = MaterialTheme.typography.titleSmall, color = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeNewMessage(
    navController: NavController,
    conversationsViewModel: ConversationsViewModel = ConversationsViewModel(),
    _items: List<Contacts>? = null
) {
    val context = LocalContext.current
    val viewModel = ContactsViewModel()

    val items: List<Contacts> by viewModel
        .getContacts(context).observeAsState(emptyList())

    val listState = rememberLazyListState()
    var userInput by remember { mutableStateOf("") }

    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text= stringResource(R.string.compose_new_message_title),
                                maxLines =1,
                                overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(R.string.go_back))
                        }
                    },
                )
                TextField(
                    value=userInput,
                    onValueChange = {
                        userInput = it
                        if(!userInput.isBlank())
                            viewModel.filterContact(context, userInput)
                    },
                    placeholder = {
                        Text(stringResource(R.string.type_names_or_phone_numbers))
                    },
                    prefix = {
                        Text(
                            "${stringResource(R.string.compose_new_message_to)}:",
                            modifier = Modifier.padding(end=16.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(innerPadding),
            state = listState,
        ){
            items(if(_items == null) items else _items) { contact ->
                Card(
                    onClick = {
                        conversationsViewModel.address = contact.number
                        conversationsViewModel.threadId = ThreadedConversationsHandler.get(context,
                            conversationsViewModel.address).thread_id

                        navigateToConversation(
                            conversationsViewModel = conversationsViewModel,
                            address = conversationsViewModel.address,
                            threadId = conversationsViewModel.threadId,
                            subscriptionId =
                            SIMHandler.getDefaultSimSubscription(context),
                            navController = navController,
                        )
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(Color.Transparent),
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        ContactAvatar(
                            id=contact.id.toString(),
                            name=contact.contactName,
                            phoneNumber = contact.number,
                        )
                        Spacer(Modifier.padding(start = 16.dp))

                        Row {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = contact.contactName,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 16.sp
                                )

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = contact.number,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodySmall,
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
fun PreviewComposeMessage() {
    var contacts: MutableList<Contacts> =
        remember { mutableListOf( ) }
    for(i in 0..10) {
        val contact = Contacts()
        contact.number = "$i$i$i$i$i$i$i$i$i$i"
        contact.contactName = "Jane Doe ($i)"
        contact.id = i.toLong()
        contacts.add(contact)
    }
    ComposeNewMessage(navController = rememberNavController(), _items = contacts)
}

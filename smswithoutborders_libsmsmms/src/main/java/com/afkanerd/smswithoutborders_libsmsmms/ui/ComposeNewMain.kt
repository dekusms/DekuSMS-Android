package com.afkanerd.smswithoutborders_libsmsmms.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.Contacts
import com.afkanerd.smswithoutborders_libsmsmms.extensions.toHslColor
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.HomeScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ContactsViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel

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
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(color))
        }
        Text(text = initials, style = MaterialTheme.typography.titleSmall, color = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeNewMessage(
    navController: NavController,
    _items: List<Contacts>? = null
) {
    val context = LocalContext.current

    val viewModel: ContactsViewModel = viewModel()
    val items: List<Contacts> by viewModel.getContacts(context).observeAsState(emptyList())

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
            items(_items ?: items) { contact ->
                Card(
                    onClick = {
                        navController.navigate(ConversationsScreenNav(
                            address = contact.address
                        )) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(Color.Transparent),
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        ContactAvatar(
                            id= contact.id.toString(),
                            name= contact.displayName,
                            phoneNumber = contact.address,
                        )
                        Spacer(Modifier.padding(start = 16.dp))

                        Row {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = contact.displayName,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 16.sp
                                )

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = contact.address,
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
        val contact = Contacts(
            i.toLong(),
            "$i$i$i$i$i$i$i$i$i$i",
            "Jane Doe ($i)"
        )
        contacts.add(contact)
    }
    ComposeNewMessage(
        navController = rememberNavController(),
        _items = contacts
    )
}

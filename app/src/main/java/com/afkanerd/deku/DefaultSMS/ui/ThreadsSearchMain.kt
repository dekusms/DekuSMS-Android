package com.afkanerd.deku.DefaultSMS.ui

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.SearchViewModel
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun SearchThreadsMain(
    viewModel: SearchViewModel = SearchViewModel(),
    conversationsViewModel: ConversationsViewModel = ConversationsViewModel(),
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current

    var expanded by rememberSaveable { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    val items: List<Conversation> by viewModel.get().observeAsState(emptyList())

    BackHandler {
        viewModel.liveData = MutableLiveData()
        navController.popBackStack()
    }

    Scaffold(
        modifier = Modifier.padding(8.dp),
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query= searchInput,
                        onQueryChange = {
                            searchInput = it
                            if(it.length > 1)
                                viewModel.search(context, searchInput)
                        },
                        onSearch = {
                            expanded = false

                            if(items.isNotEmpty()) {
                                val message = items.first()
                                if(viewModel.threadId != null) {
                                    viewModel.liveData = MutableLiveData()
                                    viewModel.threadId = null
                                    navigateToConversation(
                                        conversationsViewModel = conversationsViewModel,
                                        address = message.address!!,
                                        threadId = message.thread_id!!,
                                        navController = navController,
                                        subscriptionId = message.subscription_id,
                                        searchQuery = searchInput
                                    )
                                }
                            }
                        },
                        expanded = expanded,
                        onExpandedChange = { /* expanded = it */ },
                        placeholder = {
                            Text(stringResource(R.string.search_messages_text))
                        },
                        leadingIcon = {
                            IconButton(onClick = {
                                if(searchInput.isNotBlank()) {
                                    searchInput = ""
                                    viewModel.search(context, searchInput)
                                } else {
                                    navController.popBackStack()
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                searchInput = ""
                                viewModel.search(context, searchInput)
                            }) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                            }
                        },
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it},
                modifier = Modifier
                    .fillMaxWidth()
            ) {

            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            state = listState
        )  {
            items(
                items = items,
                key = { it.hashCode() }
            ) { message ->
                message.address?.let {
                    val contactName: String? by remember { mutableStateOf(
                        Contacts.retrieveContactName(context, message.address))
                    }
                    var firstName = message.address!!
                    var lastName = ""
                    if (!contactName.isNullOrEmpty()) {
                        contactName!!.split(" ").let {
                            firstName = it[0]
                            if (it.size > 1)
                                lastName = it[1]
                        }
                    }

                    ThreadConversationCard(
                        id = message.thread_id!!,
                        firstName = firstName,
                        lastName = lastName,
                        content = if(message.isData)
                            stringResource(R.string.conversation_threads_secured_content)
                        else message.text!!,
                        date =
                        if(!message.date.isNullOrBlank())
                            Helpers.formatDate(context, message.date!!.toLong())
                        else "Tues",
                        isRead = message.isRead,
                        isContact = !contactName.isNullOrBlank(),
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                viewModel.liveData = MutableLiveData()
                                navigateToConversation(
                                    conversationsViewModel = conversationsViewModel,
                                    address = message.address!!,
                                    threadId = message.thread_id!!,
                                    navController = navController,
                                    subscriptionId = message.subscription_id,
                                    searchQuery = searchInput
                                )
                            },
                        ),
                    )
                }
            }
        }
    }

}


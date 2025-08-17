package com.afkanerd.deku.DefaultSMS.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.DateTimeUtils
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.makeE16PhoneNumber
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactName
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.ThreadConversationCard
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.SearchViewModel
import kotlinx.coroutines.flow.first
import kotlin.collections.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchThreadsMain(
    address: String? = null,
    searchViewModel: SearchViewModel,
    navController: NavController = rememberNavController()
) {
    val inPreviewMode = LocalInspectionMode.current
    val context = LocalContext.current

    var expanded by rememberSaveable { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    val items = searchViewModel.search(context)

    val inboxMessagesItems = items.collectAsLazyPagingItems()

    var isDefault by remember{ mutableStateOf(inPreviewMode || context.isDefault()) }

    BackHandler {
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
                                searchViewModel.searchQuery.value = it
                        },
                        onSearch = {
                            expanded = false
                            inboxMessagesItems.refresh()
                        },
                        expanded = expanded,
                        onExpandedChange = { /* expanded = it */ },
                        placeholder = {
                            Text(stringResource(R.string.search_messages_text))
                        },
                        leadingIcon = {
                            IconButton(onClick = {
                                if(searchInput.isNotBlank()) {
                                    searchViewModel.searchQuery.value = ""
                                    inboxMessagesItems.refresh()
                                } else {
                                    navController.popBackStack()
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                searchViewModel.searchQuery.value = ""
                                inboxMessagesItems.refresh()
                            }) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                            }
                        },
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it},
                modifier = Modifier.fillMaxWidth()
            ) {

            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            state = listState
        )  {
            items(
                count = inboxMessagesItems.itemCount,
                key = { inboxMessagesItems.itemKey { it.threadId }}
            ) { index ->
                val message = inboxMessagesItems[index]

                message?.let {
                    val contactName = if(isDefault)
                        context.retrieveContactName(message.address)
                    else address

                    var firstName = message.address
                    var lastName = ""
                    if (!contactName.isNullOrEmpty()) {
                        contactName.split(" ").let {
                            firstName = it[0]
                            if (it.size > 1)
                                lastName = it[1]
                        }
                    }

                    val date = if (!inPreviewMode)
                        DateTimeUtils.formatDate(
                            context, message.date) ?: ""
                    else "Tues"

                    ThreadConversationCard(
                        phoneNumber = message.address,
                        id = message.threadId,
                        firstName = firstName,
                        lastName = lastName,
                        content = message.snippet,
                        date = date,
                        isRead = !message.unread,
                        isContact = !contactName.isNullOrBlank(),
                        modifier = Modifier.combinedClickable(
                            onClick = { navController
                                .navigate( ConversationsScreenNav(
                                    message.address,
                                    query = searchInput,
                                ))
                            },
                        ),
                        type = message.type,
                    )
                }
            }
        }
    }
}


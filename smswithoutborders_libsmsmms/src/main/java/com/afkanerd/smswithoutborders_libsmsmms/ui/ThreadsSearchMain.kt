package com.afkanerd.smswithoutborders_libsmsmms.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.room.util.TableInfo
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.DateTimeUtils
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactName
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.ThreadConversationCard
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.SearchViewModel

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

    val listState = rememberLazyListState()

    val inboxMessagesItems = searchViewModel.threads.collectAsLazyPagingItems()

    var isDefault by remember{ mutableStateOf(inPreviewMode || context.isDefault()) }

    var searchQuery by remember { mutableStateOf("") }

    BackHandler {
        if(!searchViewModel.searchQuery.value.isEmpty())
            searchViewModel.setSearchQuery("")
        else navController.popBackStack()
    }

    Scaffold(
        modifier = Modifier.padding(8.dp),
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query= searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            searchViewModel.setSearchQuery(it)
                            if(it.length > 1) {
                                searchViewModel.setSearchQuery(it)
                            }
                            else searchViewModel.setSearchQuery("")
                        },
                        onSearch = {
                            expanded = false
                        },
                        expanded = expanded,
                        onExpandedChange = { /* expanded = it */ },
                        placeholder = {
                            Text(stringResource(R.string.search_messages_text))
                        },
                        leadingIcon = {
                            IconButton(onClick = {
                                if(searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                    searchViewModel.setSearchQuery(searchQuery)
                                }
                                else navController.popBackStack()
                            }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                if(searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                    searchViewModel.setSearchQuery(searchQuery)
                                }
                            }) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                            }
                        },
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it},
                modifier = Modifier.fillMaxWidth()
            ) { }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = listState
        )  {
            items(
                count = inboxMessagesItems.itemCount,
                key = inboxMessagesItems.itemKey { it.threadId }
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
                            onClick = {
//                                navController.navigate( ConversationsScreenNav(
//                                    message.address,
//                                    query = searchViewModel.searchQuery.value,
//                                ))
                            },
                        ),
                        type = message.type,
                    )
                }
            }
        }
    }
}


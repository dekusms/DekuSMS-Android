package com.afkanerd.deku.DefaultSMS.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.ConversationsScreen
import com.afkanerd.deku.DefaultSMS.Extensions.isScrollingUp
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.example.compose.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadConversationLayout(
    viewModel: ThreadedConversationsViewModel = ThreadedConversationsViewModel(),
    conversationsViewModel: ConversationsViewModel = ConversationsViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    val items: List<ThreadedConversations> by viewModel
        .getAllLiveData(context).observeAsState(emptyList())
    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Inboxes", modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item") },
                    selected = false,
                    onClick = { }
                )
            }
        }
    ) {
        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text= stringResource(R.string.app_name),
                            maxLines =1,
                            overflow = TextOverflow.Ellipsis)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if(isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.open_side_menu)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            TODO("Implement search functions")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.search_messages)
                            )
                        }
                        IconButton(onClick = {
                            TODO("Implement menu functionality")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.open_menu)
                            )
                        }
                    },
                    scrollBehavior = scrollBehaviour
                )
            },
            bottomBar = {

            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { TODO("Implement compose new message method") },
                    icon = { Icon( Icons.Default.ChatBubbleOutline, "Compose new message" ) },
                    text = { Text(text = "Compose") },
                    expanded = listState.isScrollingUp()
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                state = listState
            )  {
                items(
                    items = items,
                    key = { threadConversation ->
                        threadConversation.thread_id
                    }
                ) { message ->
                    message.address?.let {
                        var firstName = message.address
                        var lastName = ""
                        val isContact = !message.contact_name.isNullOrBlank()
                        if(!message.contact_name.isNullOrBlank()) {
                            message.contact_name.split(" ").let {
                                firstName = it[0]
                                if(it.size > 1)
                                    lastName = it[1]
                            }
                        }

                        ThreadConversationCard(
                            id = message.thread_id,
                            firstName = firstName,
                            lastName = lastName,
                            content = if(message.snippet.isNullOrBlank()) "<b>Secure Content</b>"
                                else message.snippet,
                            date =
                            if(!message.date.isNullOrBlank())
                                Helpers.formatDate(context, message.date.toLong())
                            else "Tues",
                            isRead = message.isIs_read,
                            isContact = isContact,
                            onItemClick = { selectedItem ->
                                conversationsViewModel.address = message.address
                                conversationsViewModel.threadId = message.thread_id
                                navController.navigate(ConversationsScreen)
                            }
                        )
                    }
                }
            }

        }

    }

}

@Preview
@Composable
fun PreviewMessageCard() {
    AppTheme(darkTheme = true) {
        Surface(Modifier.safeDrawingPadding()) {
            var messages: MutableList<ThreadedConversations> =
                remember { mutableListOf( ) }
            for(i in 0..10) {
                val thread = ThreadedConversations()
                thread.thread_id = i.toString()
                thread.address = "$i"
                thread.contact_name = "Jane $i"
                thread.snippet = "Hello world: $i"
                thread.date = ""
                messages.add(thread)
            }
            ThreadConversationLayout(navController = rememberNavController())
        }
    }
}


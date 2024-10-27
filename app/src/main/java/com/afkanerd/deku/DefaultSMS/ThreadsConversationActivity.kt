package com.afkanerd.deku.DefaultSMS

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.TableInfo
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.example.compose.AppTheme
import com.google.android.material.button.MaterialButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import kotlin.concurrent.thread

class ThreadsConversationActivity : AppCompatActivity() {
    val viewModel: ThreadedConversationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(Modifier.safeDrawingPadding()) {
                    ThreadConversationLayout(getThreads())
                }
            }
        }
    }

    @Composable
    private fun getThreads() : List<ThreadedConversations>{
        val items: List<ThreadedConversations> by viewModel
            .getAllLiveData(applicationContext).observeAsState(emptyList())
        return items
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ThreadConversationLayout(items: List<ThreadedConversations>) {
        val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text= stringResource(R.string.app_name),
                            maxLines =1,
                            overflow = TextOverflow.Ellipsis)
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
                FloatingActionButton(onClick = {
                    TODO("Implement compose new message method")
                }) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.compose_new_message)
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.padding(innerPadding))  {
                items(
                    items = items,
                    key = { threadConversation ->
                        threadConversation.thread_id
                    }
                ) { message ->
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
                        content = message.snippet,
                        date =
                        if(!message.date.isNullOrBlank())
                            Helpers.formatDate(applicationContext, message.date.toLong())
                        else "Tues",
                        isRead = message.isIs_read,
                        isContact = isContact
                    )
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
                ThreadConversationLayout(messages)
            }
        }
    }

}
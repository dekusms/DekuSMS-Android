package com.afkanerd.deku.DefaultSMS

import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Extensions.isScrollingUp
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.example.compose.AppTheme
import kotlinx.coroutines.launch
import kotlin.getValue

class ConversationsActivity : AppCompatActivity() {
    private lateinit var threadId: String

    val viewModel: ConversationsViewModel by viewModels()

    enum class EXPECTED_INTENTS(val value: String) {
        THREAD_ID("THREAD_ID")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if(!intent.hasExtra(EXPECTED_INTENTS.THREAD_ID.value) ||
            intent.getStringExtra(EXPECTED_INTENTS.THREAD_ID.value).isNullOrBlank()) {
            Toast.makeText(applicationContext, "Error: No ThreadID found!", Toast.LENGTH_LONG)
                .show()
            finish()
        } else {
            threadId = intent.getStringExtra(EXPECTED_INTENTS.THREAD_ID.value)!!
            setContent {
                AppTheme {
                    Surface(Modifier.safeDrawingPadding()) {
                        Conversations(getConversation())
                    }
                }
            }
        }

    }

    @Composable
    private fun getConversation() : List<Conversation> {
        val items: List<Conversation> by viewModel
            .getLiveData(applicationContext, threadId).observeAsState(emptyList())
        return items
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Conversations(items: List<Conversation>) {
        val listState = rememberLazyListState()
        val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text= "Contact Name",
                            maxLines =1,
                            overflow = TextOverflow.Ellipsis)
                    },
                    actions = {
                        IconButton(onClick = {
                            TODO("Implement call function")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = stringResource(R.string.call)
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
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                state = listState
            )  {
                items(
                    items = items,
                    key = { conversation -> conversation.id }
                ) { conversation ->
                }
            }

        }

    }

    @Preview
    @Composable
    fun PreviewConversations() {
        AppTheme {
            Surface(Modifier.safeDrawingPadding()) {
                var conversations: MutableList<Conversation> =
                    remember { mutableListOf( ) }
                for(i in 0..10) {
                    val conversation = Conversation()
                    conversation.id = i.toLong()
                    conversation.thread_id = i.toString()
                    conversation.text = stringResource(R.string
                        .settings_add_gateway_server_protocol_meta_description)
                }
                Conversations(conversations)
            }
        }
    }
}
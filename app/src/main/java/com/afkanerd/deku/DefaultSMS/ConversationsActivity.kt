package com.afkanerd.deku.DefaultSMS

import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.TelephonyCallback
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Extensions.isScrollingUp
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.ui.Components.ChatCompose
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationMessageTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationsCard
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.example.compose.AppTheme
import kotlinx.coroutines.launch
import kotlin.getValue

class ConversationsActivity : CustomAppCompactActivity(){
//    private lateinit var threadId: String

    val viewModel: ConversationsViewModel by viewModels()

    enum class EXPECTED_INTENTS(val value: String) {
        THREAD_ID("THREAD_ID")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TODO: remove this, temp used for now
        conversationsViewModel = viewModel

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

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Preview(showBackground = true)
    @Composable
    fun ChatCompose() {
        val interactionsSource = remember { MutableInteractionSource() }
        var userInput by remember { mutableStateOf("") }
        Row(modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(top = 4.dp, bottom = 4.dp)
        ) {
            Column(modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .weight(1f)
                .fillMaxSize()) {
                BasicTextField(
                    value = userInput,
                    onValueChange = {
                        userInput = it
                    },
                    maxLines = 7,
                    singleLine = false,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp, 24.dp, 24.dp, 24.dp))
                        .background(MaterialTheme.colorScheme.outline)
                        .fillMaxWidth()
                ) {
                    TextFieldDefaults.DecorationBox(
                        value = userInput,
                        visualTransformation = VisualTransformation.None,
                        innerTextField = it,
                        singleLine = false,
                        enabled = true,
                        interactionSource = interactionsSource,
                        placeholder = {
                            Text(text= stringResource(R.string.text_message))
                        },
                        shape = RoundedCornerShape(24.dp, 24.dp, 24.dp, 24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                    )
                }

            }

            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom
            ) {
                IconButton(onClick = {
                    val subscriptionId = SIMHandler.getDefaultSimSubscription(applicationContext)
                    sendTextMessage(userInput, subscriptionId)
                },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.AutoMirrored.Default.Send, stringResource(R.string.send_message))
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Conversations(items: List<Conversation>) {
        val listState = rememberLazyListState()
        val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())


        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            topBar = {
                TopAppBar(
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
            bottomBar = {
                ChatCompose()
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(innerPadding),
                state = listState,
                reverseLayout = true,
            ) {
                items(
                    items = items,
                    key = { conversation -> conversation.id }
                ) { conversation ->
                    ConversationsCard(
                        text= conversation.text!!,
                        timestamp =
                        if(!conversation.date.isNullOrBlank())
                            Helpers.formatDateExtended(applicationContext,
                                conversation.date!!.toLong())
                        else "1730062120",
                        type= ConversationMessageTypes.fromInt(conversation.type)!!
                    )
                }
            }
        }

    }

    @Preview
    @Composable
    fun PreviewConversations() {
        AppTheme(darkTheme = true) {
            Surface(Modifier.safeDrawingPadding()) {
                var conversations: MutableList<Conversation> =
                    remember { mutableListOf( ) }
                var isSend = false
                for(i in 0..1) {
                    val conversation = Conversation()
                    conversation.id = i.toLong()
                    conversation.thread_id = i.toString()
                    conversation.text = stringResource(R.string
                        .settings_add_gateway_server_protocol_meta_description)
                    conversation.type = if(!isSend) ConversationMessageTypes.MESSAGE_TYPE_INBOX.value
                    else ConversationMessageTypes.MESSAGE_TYPE_SENT.value
                    conversations.add(conversation)
                    isSend = !isSend
                }
                Conversations(conversations)
            }
        }
    }
}
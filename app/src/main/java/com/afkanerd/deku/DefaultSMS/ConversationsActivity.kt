package com.afkanerd.deku.DefaultSMS

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.TelephonyCallback
import android.widget.Toast
import android.window.OnBackInvokedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.TurnRight
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.ui.Components.ChatCompose
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationMessageTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationPositionTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationStatusTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationsCard
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.example.compose.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.getValue

class ConversationsActivity : CustomAppCompactActivity(){
    private lateinit var threadId: String
    private lateinit var address: String

    val viewModel: ConversationsViewModel by viewModels()

    enum class EXPECTED_INTENTS(val value: String) {
        THREAD_ID("THREAD_ID"),
        ADDRESS("ADDRESS")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if(!intent.hasExtra(EXPECTED_INTENTS.THREAD_ID.value) ||
            intent.getStringExtra(EXPECTED_INTENTS.THREAD_ID.value).isNullOrBlank()) {
            Toast.makeText(applicationContext, "Error: No ThreadID found!", Toast.LENGTH_LONG)
                .show()
            finish()
        }
        if(!intent.hasExtra(EXPECTED_INTENTS.ADDRESS.value) ||
            intent.getStringExtra(EXPECTED_INTENTS.ADDRESS.value).isNullOrBlank()) {
            Toast.makeText(applicationContext, "Error: No Address found!", Toast.LENGTH_LONG)
                .show()
            finish()
        }
        else {
            threadId = intent.getStringExtra(EXPECTED_INTENTS.THREAD_ID.value)!!
            address = intent.getStringExtra(EXPECTED_INTENTS.ADDRESS.value)!!
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

    private fun copyItem(text: String) {
        val clip = ClipData.newPlainText(text, text)
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(clip)

        Toast.makeText(
            applicationContext, getString(R.string.conversation_copied),
            Toast.LENGTH_SHORT
        ).show()
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
                    sendTextMessage(
                        text=userInput,
                        address=address,
                        subscriptionId= subscriptionId,
                        threadId=threadId,
                        conversationsViewModel = viewModel)
                    userInput = ""
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

    private fun getContentType(index: Int, conversation: Conversation, conversations: List<Conversation>):
            ConversationPositionTypes {
        if(conversations.size < 2)
            return ConversationPositionTypes.NORMAL_TIMESTAMP
        if(index == 0) {
            if(Helpers.isSameHour(conversation.date!!.toLong(),
                    conversations[index + 1].date!!.toLong())) {
                if(conversation.type == conversations[index + 1].type) {
                    if(Helpers.isSameMinute(conversation.date!!.toLong(),
                            conversations[index + 1].date!!.toLong())) {
                        return ConversationPositionTypes.END
                    }
                }
                return ConversationPositionTypes.NORMAL
            }
        } else if(index == conversations.size - 1) {
            if(conversation.type == conversations[index - 1].type) {
                if(Helpers.isSameMinute(conversation.date!!.toLong(),
                        conversations[index - 1].date!!.toLong())) {
                    return ConversationPositionTypes.START_TIMESTAMP
                }
            }
            return ConversationPositionTypes.NORMAL_TIMESTAMP
        } else {
            if(Helpers.isSameHour(conversation.date!!.toLong(),
                    conversations[index + 1].date!!.toLong())) {
                if(conversation.type == conversations[index - 1].type) {
                    if(Helpers.isSameMinute(conversation.date!!.toLong(),
                            conversations[index - 1].date!!.toLong())) {
                        if(Helpers.isSameMinute(conversation.date!!.toLong(),
                                conversations[index + 1].date!!.toLong())) {
                            return ConversationPositionTypes.MIDDLE
                        }
                        return ConversationPositionTypes.START
                    } else {
                        if(Helpers.isSameMinute(conversation.date!!.toLong(),
                                conversations[index + 1].date!!.toLong())) {
                            return ConversationPositionTypes.END
                        }
                        return ConversationPositionTypes.NORMAL
                    }
                }
            } else {
                if(conversation.type == conversations[index + 1].type) {
                    if(Helpers.isSameMinute(conversation.date!!.toLong(),
                            conversations[index - 1].date!!.toLong())) {
                        return ConversationPositionTypes.START_TIMESTAMP
                    }
                }
            }
        }
        return ConversationPositionTypes.NORMAL_TIMESTAMP
    }

    @Preview
    @Composable
    private fun ConversationCrudBottomBar(
        items: List<Conversation> = emptyList<Conversation>(),
        onCompleted: (() -> Unit)? = null
    ) {
        BottomAppBar (
            actions = {
                if(items.size < 2) {
                    IconButton(onClick = {
                        copyItem(items.first().text!!)
                        onCompleted?.let { it() }
                    }) {
                        Icon(Icons.Filled.ContentCopy, stringResource(R.string.copy_message))
                    }

                    IconButton(onClick = {
                        TODO("Implement forward message")
                    }) {
                        Icon(painter= painterResource(id=R.drawable.rounded_forward_24),
                            stringResource(R.string.forward_message)
                        )
                    }

                    IconButton(onClick = {
                        shareItem(items.first().text!!)
                        onCompleted?.let { it() }
                    }) {
                        Icon(Icons.Filled.Share, stringResource(R.string.share_message))
                    }
                }

                IconButton(onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        viewModel.deleteItems(applicationContext, items)
                        onCompleted?.let { it() }
                    }
                }) {
                    Icon(Icons.Filled.Delete, stringResource(R.string.delete_message))
                }
            }
        )
    }


    private fun shareItem(text: String) {
        val sendIntent = Intent().apply {
            setAction(Intent.ACTION_SEND)
            putExtra(Intent.EXTRA_TEXT, text)
            setType("text/plain")
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        // Only use for components you have control over
        val excludedComponentNames = arrayOf(
            ComponentName(
                BuildConfig.APPLICATION_ID,
                ThreadedConversationsActivity::class.java.name
            )
        )
        shareIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponentNames)
        startActivity(shareIntent)
    }

    private fun call(address: String) {
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            setData(Uri.parse("tel:" + address));
        }
        startActivity(callIntent);
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun Conversations(items: List<Conversation>) {
        var contactName by remember { mutableStateOf("Template Contact")}
        var selectedItems = remember { mutableStateListOf<Conversation>() }

        LaunchedEffect("contact_name"){
            val defaultRegion = Helpers.getUserCountry( applicationContext )
            contactName = Contacts.retrieveContactName( applicationContext,
                Helpers.getFormatCompleteNumber(address, defaultRegion) )
            if(contactName.isNullOrBlank())
                contactName = address
        }

        val listState = rememberLazyListState()
        val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        LaunchedEffect(items){
            if(items.isNotEmpty()) listState.animateScrollToItem(0)
        }

        val backHandler = BackHandler{}

        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text= contactName,
                            maxLines =1,
                            overflow = TextOverflow.Ellipsis)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            backHandler.run {
                                if(selectedItems.isEmpty()) finish()
                                else selectedItems.clear()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            call(address)
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
                if(selectedItems.isEmpty()) ChatCompose()
                else ConversationCrudBottomBar(selectedItems) {
                    selectedItems.clear()
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(innerPadding),
                state = listState,
                reverseLayout = true,
            ) {
                itemsIndexed(
                    items = items,
                    key = { index, conversation -> conversation.id }
                ) { index, conversation ->
                    ConversationsCard(
                        text= conversation.text!!,
                        timestamp =
                        if(!conversation.date.isNullOrBlank())
                            Helpers.formatDateExtended(applicationContext,
                                conversation.date!!.toLong())
                        else "1730062120",
                        type= ConversationMessageTypes.fromInt(conversation.type)!!,
                        status = ConversationStatusTypes.fromInt(conversation.status)!!,
                        position = getContentType(index, conversation, items),
                        date =
                        if(!conversation.date.isNullOrBlank()) deriveMetaDate(conversation)
                        else "1730062120",
                        showDate = index == 0,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .combinedClickable(
                                onLongClick = {
                                    selectedItems.add(conversation)
                                },
                                onClick = {
                                    if (!selectedItems.isEmpty()) {
                                        if (selectedItems.contains(conversation))
                                            selectedItems.remove(conversation)
                                        else
                                            selectedItems.add(conversation)
                                    }
                                }
                            ),
                        isSelected = selectedItems.contains(conversation)
                    )
                }
            }
        }

    }

    private fun deriveMetaDate(conversation: Conversation): String{
        val dateFormat: DateFormat = SimpleDateFormat("h:mm a");
        return dateFormat.format(Date(conversation.date!!.toLong()));
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
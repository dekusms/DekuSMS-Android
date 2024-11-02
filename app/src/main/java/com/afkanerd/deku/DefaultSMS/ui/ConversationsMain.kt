package com.afkanerd.deku.DefaultSMS.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.E2EEHandler
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.Models.SMSHandler.sendTextMessage
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.Deprecated.ThreadedConversationsActivity
import com.afkanerd.deku.DefaultSMS.HomeScreen
import com.afkanerd.deku.DefaultSMS.Models.SMSHandler.sendDataMessage
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationMessageTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationPositionTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationStatusTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationsCard
import com.example.compose.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

private var contactName: String = ""


private fun copyItem(context: Context, text: String) {
    val clip = ClipData.newPlainText(text, text)
    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(clip)

    Toast.makeText(
        context, context.getString(R.string.conversation_copied),
        Toast.LENGTH_SHORT
    ).show()
}

private fun sendSMS(
    context: Context,
    text: String,
    messageId: String,
    threadId: String,
    address: String,
    conversationsViewModel: ConversationsViewModel
) {
    val subscriptionId = SIMHandler.getDefaultSimSubscription(context)

    val conversation = Conversation()
    conversation.text = text
    conversation.message_id = messageId
    conversation.thread_id = threadId
    conversation.subscription_id = subscriptionId
    conversation.type = Telephony.Sms.MESSAGE_TYPE_OUTBOX
    conversation.date = System.currentTimeMillis().toString()
    conversation.address = address
    conversation.status = Telephony.Sms.STATUS_PENDING

    sendTextMessage(
        context = context,
        text = text,
        address = address,
        conversation = conversation,
        conversationsViewModel = conversationsViewModel,
        messageId = null,
    )
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ChatCompose(
    context: Context? = null,
    address: String = "",
    threadId: String = "",
    viewModel: ConversationsViewModel = ConversationsViewModel()
) {
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
                sendSMS(
                    context=context!!,
                    text=userInput,
                    threadId=threadId,
                    messageId = System.currentTimeMillis().toString(),
                    address=address,
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
    context: Context?=null,
    viewModel: ConversationsViewModel = ConversationsViewModel(),
    items: List<Conversation> = emptyList<Conversation>(),
    onCompleted: (() -> Unit)? = null
) {
    BottomAppBar (
        actions = {
            if(items.size < 2) {
                IconButton(onClick = {
                    copyItem(context!!, items.first().text!!)
                    onCompleted?.let { it() }
                }) {
                    Icon(Icons.Filled.ContentCopy, stringResource(R.string.copy_message))
                }

                IconButton(onClick = {
                    TODO("Implement forward message")
                }) {
                    Icon(painter= painterResource(id= R.drawable.rounded_forward_24),
                        stringResource(R.string.forward_message)
                    )
                }

                IconButton(onClick = {
                    shareItem(context!!, items.first().text!!)
                    onCompleted?.let { it() }
                }) {
                    Icon(Icons.Filled.Share, stringResource(R.string.share_message))
                }
            }

            IconButton(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    viewModel.deleteItems(context!!, items)
                    onCompleted?.let { it() }
                }
            }) {
                Icon(Icons.Filled.Delete, stringResource(R.string.delete_message))
            }
        }
    )
}


private fun shareItem(context: Context, text: String) {
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
    context.startActivity(shareIntent)
}

private fun call(context: Context, address: String) {
    val callIntent = Intent(Intent.ACTION_DIAL).apply {
        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        setData(Uri.parse("tel:$address"));
    }
    context.startActivity(callIntent);
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SecureRequestModal(
    context: Context? = null,
    viewModel: ConversationsViewModel = ConversationsViewModel(),
    dismissCallback: (() -> Unit)? = null
) {
    var showSecureRequestModal by remember { mutableStateOf(true) }
    val state = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )

    ModalBottomSheet(
        onDismissRequest = {
            dismissCallback?.let { it() }
            showSecureRequestModal = false },
        sheetState = state,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(
                    R.string
                        .conversation_secure_popup_request_menu_description),
                fontSize = 16.sp
            )
            Text(
                text = stringResource(
                    R.string
                        .conversation_secure_popup_request_menu_description_subtext),
                modifier = Modifier.padding(16.dp)
            )

            Button(onClick = {
                E2EEHandler.clear(context!!, viewModel.address!!)
                val publicKey = E2EEHandler.generateKey(context, viewModel.address!!)
                val txPublicKey = E2EEHandler.formatRequestPublicKey(publicKey,
                    E2EEHandler.MagicNumber.REQUEST)
                sendDataMessage(
                    context=context,
                    viewModel=viewModel,
                    data=txPublicKey
                )
            }) {
                Text(stringResource(R.string.request))
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Conversations(
    context: Context? = null,
    viewModel: ConversationsViewModel = ConversationsViewModel(),
    navController: NavController,
    showIsSecured: Boolean? = false
) {
    var getContactName by remember { mutableStateOf("")}
    var selectedItems = remember { mutableStateListOf<Conversation>() }

    LaunchedEffect("contact_name"){
        val defaultRegion = Helpers.getUserCountry( context )

        contactName = Contacts.retrieveContactName( context,
            Helpers.getFormatCompleteNumber(viewModel.address, defaultRegion) )
        if(contactName.isNullOrBlank())
            contactName = viewModel.address!!
        getContactName = contactName
    }

    BackHandler {
        if(selectedItems.isEmpty()) {
            navController.popBackStack()
        }
        else selectedItems.clear()
    }

    val items: List<Conversation> by viewModel
        .getLiveData(context!!).observeAsState(emptyList())

    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(items){
        listState.animateScrollToItem(0)
    }

    var showSecureRequestModal by remember { mutableStateOf(false) }

    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text= getContactName,
                            maxLines =1,
                            overflow = TextOverflow.Ellipsis)

                        if(showIsSecured != null && showIsSecured) {
                            Text(
                                text= stringResource(R.string.secured),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines =1,
                                overflow = TextOverflow.Ellipsis)
                        }
                        else if(E2EEHandler.isSecured(context, viewModel.address!!)) {
                            Text(
                                text= stringResource(R.string.secured),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines =1,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if(selectedItems.isEmpty()) {
                            navController.popBackStack()
                        }
                        else selectedItems.clear()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        call(context, viewModel.address!!)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = stringResource(R.string.call)
                        )
                    }

                    IconButton(onClick = {
                        showSecureRequestModal = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.EnhancedEncryption,
                            contentDescription = stringResource(
                                R.string
                                    .request_secure_communication)
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
            if(selectedItems.isEmpty()) ChatCompose(
                context,
                viewModel.address!!,
                viewModel.threadId!!,
                viewModel
            )
            else ConversationCrudBottomBar(
                context,
                viewModel,
                selectedItems
            ) {
                selectedItems.clear()
            }
        }
    ) { innerPadding ->

        if(showSecureRequestModal) {
            SecureRequestModal(
                context=context,
                viewModel=viewModel,
            ){
                showSecureRequestModal = false
            }
        }

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
                    text= if(conversation.text.isNullOrBlank()) ""
                    else conversation.text!!,
                    timestamp =
                    if(!conversation.date.isNullOrBlank())
                        Helpers.formatDateExtended(context,
                            conversation.date!!.toLong())
                    else "1730062120",
                    type= ConversationMessageTypes.fromInt(conversation.type)!!,
                    status = ConversationStatusTypes.fromInt(conversation.status)!!,
                    position = getContentType(index, conversation, items),
                    date =
                    if(!conversation.date.isNullOrBlank()) deriveMetaDate(conversation)
                    else "1730062120",
                    showDate = index == 0,
                    modifier =
                    if(conversation.isIs_key) Modifier
                    else Modifier
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
                    isSelected = selectedItems.contains(conversation),
                    isKey = conversation.isIs_key,
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
            val address = "+123456789"
            val threadId = "1"
            for(i in 0..1) {
                val conversation = Conversation()
                conversation.id = i.toLong()
                conversation.text = stringResource(
                    R.string
                        .settings_add_gateway_server_protocol_meta_description)
                conversation.type = if(!isSend) ConversationMessageTypes.MESSAGE_TYPE_INBOX.value
                else ConversationMessageTypes.MESSAGE_TYPE_SENT.value
                conversations.add(conversation)
                isSend = !isSend
            }
            Conversations(navController = rememberNavController(), showIsSecured = true)
        }
    }
}

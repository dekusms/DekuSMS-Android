package com.afkanerd.deku.DefaultSMS.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.BlockedNumberContract
import android.provider.Telephony
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Colors
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.ContactDetailsScreen
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.SearchViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.E2EEHandler
import com.afkanerd.deku.DefaultSMS.Models.Notifications
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.Models.SMSHandler.sendTextMessage
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.ui.Components.ChatCompose
import com.afkanerd.deku.DefaultSMS.ui.Components.ConvenientMethods
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationCrudBottomBar
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationPositionTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationStatusTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationsCard
import com.afkanerd.deku.DefaultSMS.ui.Components.DeleteConfirmationAlert
import com.afkanerd.deku.DefaultSMS.ui.Components.FailedMessageOptionsModal
import com.afkanerd.deku.DefaultSMS.ui.Components.SearchCounterCompose
import com.afkanerd.deku.DefaultSMS.ui.Components.SearchTopAppBarText
import com.afkanerd.deku.DefaultSMS.ui.Components.SecureRequestAcceptModal
import com.afkanerd.deku.DefaultSMS.ui.Components.ShortCodeAlert
import com.afkanerd.deku.DefaultSMS.ui.Components.SimChooser
import com.afkanerd.deku.Modules.Subroutines
import com.afkanerd.deku.SearchThreadScreen
import com.example.compose.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import androidx.core.net.toUri
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversationsHandler.call
import com.afkanerd.deku.DefaultSMS.ui.Components.ConvenientMethods.deriveMetaDate
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationsMainDropDownMenu
import com.afkanerd.deku.DefaultSMS.ui.Components.MessageInfoAlert
import com.afkanerd.deku.DefaultSMS.ui.Components.getConversationType
import com.afkanerd.deku.DefaultSMS.ui.Components.sendSMS
import kotlinx.coroutines.withContext
import sh.calvin.autolinktext.rememberAutoLinkText


fun backHandler(
    context: Context,
    viewModel: ConversationsViewModel,
    navController: NavController
) {
    if(viewModel.text.isNotBlank()) {
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.insertDraft(context)
            viewModel.text = ""
        }
    }

    if(!viewModel.selectedItems.isEmpty()) {
        viewModel.selectedItems.clear()
    }
    else navController.popBackStack()
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Conversations(
    viewModel: ConversationsViewModel = ConversationsViewModel(),
    searchViewModel: SearchViewModel = SearchViewModel(),
    navController: NavController,
    _items: List<Conversation>? = null
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current
    val dualSim = if(inPreviewMode) true else SIMHandler.isDualSim(context)

    val scope = rememberCoroutineScope()
    val coroutineScope = remember { CoroutineScope(Dispatchers.Default) }

    var isDefault by remember{ mutableStateOf(inPreviewMode || Subroutines.isDefault(context)) }

    var isSecured by remember {
        mutableStateOf(
            if(viewModel.address.isBlank()) false
            else E2EEHandler.isSecured(context, viewModel.address)
        )
    }

    var showSecureRequestModal by rememberSaveable { mutableStateOf(false) }
    var showSecureAgreeModal by rememberSaveable {
        mutableStateOf(
            if(viewModel.address.isBlank()) false
            else E2EEHandler.hasPendingApproval(context, viewModel.address)
        )
    }
    var showFailedRetryModal by rememberSaveable { mutableStateOf(false) }


    val messages = viewModel.getConversationLivePaging(context)
    val inboxMessagesItems = messages.collectAsLazyPagingItems()

    val selectedItems = remember { viewModel.selectedItems }

    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var rememberMenuExpanded by remember { mutableStateOf( false) }
    var openSimCardChooser by remember { mutableStateOf(inPreviewMode) }
    val searchIndexes = remember { mutableStateListOf<Int>() }

    var searchQuery by remember { mutableStateOf(viewModel.searchQuery) }
    var searchIndex by remember { mutableIntStateOf(0) }

    var isMute by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }

    var isBlocked by remember { mutableStateOf(
        if(!inPreviewMode && isDefault)
            BlockedNumberContract .isBlocked(context, viewModel.address)
        else false
    ) }

    var openAlertDialog by remember { mutableStateOf(false)}

    val isShortCode = if(inPreviewMode) false else Helpers.isShortCode(viewModel.address)
    val defaultRegion = if(inPreviewMode) "cm" else Helpers.getUserCountry( context )

    var shouldPulse by remember { mutableStateOf(false) }
    val pulseRateMs by remember { mutableLongStateOf(3000L) }

    var rememberDeleteAlert by remember { mutableStateOf(false) }
    var openInfoAlert by remember { mutableStateOf(false) }

    LaunchedEffect(inboxMessagesItems.loadState) {
        println("Checking search...")
        if(searchQuery.isNotBlank() && inboxMessagesItems.loadState.isIdle) {
            coroutineScope.launch {
                viewModel.getThread(context).let { items ->
                    items.forEachIndexed { index, it ->
                        it.text?.let { text ->
                            if(it.text!!.contains(other=searchQuery, ignoreCase=true)
                                && !searchIndexes.contains(index))
                                searchIndexes.add(index)
                        }
                    }

                    if(searchIndexes.isNotEmpty() && searchIndex == 0) {
                        if(inboxMessagesItems.itemCount > searchIndexes.first()) {
                            inboxMessagesItems[searchIndexes.first()]
                            scope.launch {
                                listState.animateScrollToItem(searchIndexes.first())
                            }
                        }
                        else {
                            println("Refreshing search... ${inboxMessagesItems.itemCount} - ${searchIndexes.first()}")
                            inboxMessagesItems.refresh()
                        }
                    }
                }
            }
        }

        coroutineScope.launch {
            if(viewModel.fetchDraft(context) == null && searchQuery.isEmpty()) {
                scope.launch{
                    listState.animateScrollToItem(0)
                }
            }
        }

        Notifications.cancel(context, viewModel.threadId.toInt())
    }

    val contactName by remember{ mutableStateOf(
        if(isDefault) {
            Contacts.retrieveContactName(
                context,
                Helpers.getFormatCompleteNumber(viewModel.address, defaultRegion)
            ) ?: viewModel.address.run {
                viewModel.address.replace(Regex("[\\s-]"), "")
            }
        } else viewModel.address.replace(Regex("[\\s-]"), "")
    )}

    LaunchedEffect(viewModel.address){
        coroutineScope.launch {
            if(viewModel.text.isEmpty()) {
                viewModel.fetchDraft(context)?.let {
                    viewModel.clearDraft(context)
                    viewModel.text = it.text!!
                }
            }
            viewModel.updateToRead(context)
            isMute = viewModel.isMuted(context)
            isArchived = viewModel.isArchived(context)
        }
    }

    if(isSecured) {
        LaunchedEffect(viewModel.text) {
            if(viewModel.text.isBlank()) {
                viewModel.encryptedText = ""
                shouldPulse = false
            } else shouldPulse = true
        }

        LaunchedEffect(shouldPulse) {
            if(shouldPulse)
                coroutineScope.launch {
                    delay(pulseRateMs)
                    viewModel.encryptedText = E2EEHandler.encryptMessage(
                        context = context,
                        text = viewModel.text,
                        address = viewModel.address
                    ).first
                    shouldPulse = false
                }
        }
    }

    BackHandler {
        if(searchQuery.isNotBlank()) searchQuery = ""
        else
        backHandler(
            context = context,
            viewModel = viewModel,
            navController = navController,
        )
    }

    ConversationsMainDropDownMenu(
        rememberMenuExpanded,
        isMute = isMute,
        isBlocked = isBlocked,
        isArchived = isArchived,
        isSecure = isSecured,
        searchCallback = {
            searchViewModel.threadId = viewModel.threadId
            navController.navigate(SearchThreadScreen)
        },
        blockCallback = {
            if(isBlocked) {
                viewModel.unblock(context)
            }
            else {
                ConvenientMethods.blockContact(context, viewModel.address)
            }
            isBlocked = BlockedNumberContract.isBlocked(context, viewModel.address)
        },
        deleteCallback = {
            rememberDeleteAlert = true
        },
        secureCallback = {
            showSecureRequestModal = true
        },
        archiveCallback = {
            coroutineScope.launch{
                if(isArchived) viewModel.unArchive(context)
                else viewModel.archive(context)
            }
            backHandler(
                context = context,
                viewModel = viewModel,
                navController = navController,
            )
        },
        muteCallback = {
            coroutineScope.launch {
                if(isMute) viewModel.unMute(context)
                else viewModel.mute(context)
                isMute = viewModel.isMuted(context)
            }
        }
    ) {
        rememberMenuExpanded = false
    }

    Scaffold (
        modifier = Modifier
            .safeDrawingPadding()
            .nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if(searchQuery.isBlank()) {
                        TextButton(onClick = {
                            navController.navigate(ContactDetailsScreen)
                        }) {
                            Column {
                                Row {
                                    Text(
                                        text= if(LocalInspectionMode.current) "Template"
                                        else contactName,
                                        maxLines =1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(end=8.dp),
                                    )
                                    if(isSecured || LocalInspectionMode.current) {
                                        Icon(Icons.Default.Security,
                                            stringResource(R.string.conversation_is_secured)
                                        )
                                    }
                                }
                                if(isSecured || LocalInspectionMode.current) {
                                    Text(
                                        stringResource(R.string.secured),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                    else {
                        SearchTopAppBarText(
                            searchQuery,
                            cancelCallback = { searchQuery = "" }
                        ) {
                            searchIndexes.clear()
                            searchQuery = it
                        }
                    }
                },
                navigationIcon = {
                    if(viewModel.newLayoutInfo == null ||
                        viewModel.newLayoutInfo!!.displayFeatures.isEmpty())
                        IconButton(onClick = {
                            if(searchQuery.isNotBlank()) searchQuery = ""
                            else
                            backHandler(
                                context = context,
                                viewModel = viewModel,
                                navController = navController,
                            )
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.go_back))
                        }

                },
                actions = {
                    if(searchQuery.isBlank()) {
                        if(!isShortCode) {
                            IconButton(onClick = {
                                call(context, viewModel.address)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Call,
                                    contentDescription = stringResource(R.string.call)
                                )
                            }

                            if(!isSecured || LocalInspectionMode.current) {
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

                            }
                        }
                        IconButton(onClick = {
                            rememberMenuExpanded = !rememberMenuExpanded
                        }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.open_menu)
                            )
                        }

                    }
                },
                scrollBehavior = scrollBehaviour
            )
        },
        bottomBar = {
            if(selectedItems.isNotEmpty()) {
                ConversationCrudBottomBar(
                    viewModel,
                    inboxMessagesItems.itemSnapshotList.items,
                    onInfoRequested = {
                        openInfoAlert = true
                        viewModel.selectedMessage = it
                        selectedItems.clear()
                    },
                    onCompleted = { selectedItems.clear() }
                ) {
                    selectedItems.clear()
                }
            }
            else if(searchQuery.isNotBlank()) {
                SearchCounterCompose(
                    index = (searchIndex + 1).toString(),
                    total=searchIndexes.size.toString(),
                    forwardClick = {
                        if(searchIndex + 1 >= searchIndexes.size)
                            searchIndex = 0
                        else searchIndex += 1
                        scope.launch {
                            if(searchIndexes.size >= searchIndex)
                                listState.animateScrollToItem(searchIndexes[searchIndex])
                        }
                    },
                    backwardClick = {
                        if(searchIndex - 1 < 0)
                            searchIndex = searchIndexes.size - 1
                        else searchIndex -= 1
                        scope.launch {
                            listState.animateScrollToItem(searchIndexes[searchIndex])
                        }
                    }
                )
            }
            else if(isShortCode) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.conversation_shortcode_description),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = {
                        openAlertDialog = true
                    }) {
                        Text(
                            stringResource(R.string.conversation_shortcode_action_button),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            else {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChatCompose(
                        value = viewModel.text,
                        encryptedValue = viewModel.encryptedText,
                        subscriptionId = viewModel.subscriptionId,
                        shouldPulse = shouldPulse,
                        simCardChooserCallback = if(dualSim) {
                            { openSimCardChooser = true}
                        } else null,
                        valueChanged = {
                            viewModel.text = it
                        }
                    ) {
                        val text = viewModel.text
                        sendSMS(
                            context = context,
                            text = text,
                            threadId = viewModel.threadId,
                            messageId = System.currentTimeMillis().toString(),
                            address = viewModel.address,
                            conversationsViewModel = viewModel
                        ) {
                            viewModel.text = ""
                            viewModel.encryptedText = ""
                            viewModel.clearDraft(context)
                        }
                    }

                    if(openSimCardChooser) {
                        SimChooser(
                            expanded = openSimCardChooser,
                            onClickCallback = {
                                viewModel.subscriptionId = it
                            }
                        ) {
                            openSimCardChooser = false
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                reverseLayout = true,
            ) {
                items(
                    count = inboxMessagesItems.itemCount,
                    key =  inboxMessagesItems.itemKey{ it.id }
                ) { index ->
                    inboxMessagesItems[index]?.let { conversation ->
                        var showDate by remember { mutableStateOf(index == 0) }

                        var timestamp by remember { mutableStateOf(
                            if(inPreviewMode) "1234567"
                            else Helpers.formatDateExtended(context, conversation.date!!.toLong())) }

                        var date by remember { mutableStateOf(
                            if(inPreviewMode) "1234567"
                            else {
                                deriveMetaDate(conversation) +
                                        if(dualSim && !inPreviewMode) {
                                            " • " + SIMHandler.getSubscriptionName(context,
                                                conversation.subscription_id)
                                        } else ""
                            }) }

                        val position by remember {
                            mutableStateOf(getConversationType(
                                index,
                                conversation,
                                inboxMessagesItems.itemSnapshotList.items)
                            )
                        }

                        var text = if(LocalInspectionMode.current)
                            AnnotatedString(conversation.text ?: "")
                        else AnnotatedString.rememberAutoLinkText(
                            conversation.text ?: "",
                            defaultLinkStyles = TextLinkStyles(
                                SpanStyle( textDecoration = TextDecoration.Underline )
                            )
                        )

                        if(searchQuery.isNotEmpty())
                            text = buildAnnotatedString {
                                val startIndex = text.indexOf(searchQuery, ignoreCase = true)
                                val endIndex = startIndex + searchQuery.length

                                append(text)
                                if (startIndex >= 0) {
                                    addStyle(
                                        style = SpanStyle(
                                            background = Color.Yellow,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        ),
                                        start = startIndex,
                                        end = endIndex
                                    )
                                }
                            }


                        ConversationsCard(
                            text= text,
                            timestamp = timestamp,
                            type= conversation.type,
                            status = ConversationStatusTypes.fromInt(conversation.status)!!,
                            position = position,
                            date = date,
                            showDate = showDate,
                            onClickCallback = {
                                if (selectedItems.isNotEmpty()) {
                                    if (selectedItems.contains(conversation.message_id))
                                        selectedItems.remove(conversation.message_id)
                                    else
                                        selectedItems.add(conversation.message_id!!)
                                }
                                else if(conversation.type == Telephony.Sms.MESSAGE_TYPE_FAILED) {
                                    viewModel.retryDeleteItem.add(conversation)
                                    showFailedRetryModal = true
                                }
                                else {
                                    showDate = !showDate
                                }
                            },
                            onLongClickCallback = {
                                selectedItems.add(conversation.message_id!!)
                            },
                            isSelected = selectedItems.contains(conversation.message_id),
                            isKey = conversation.isIs_key,
                        )

                        val checkIsSecured by remember {
                            derivedStateOf {
                                conversation.isIs_key &&
                                        conversation.type == Telephony.TextBasedSmsColumns
                                    .MESSAGE_TYPE_INBOX
                            }
                        }

                        if(checkIsSecured) {
                            LaunchedEffect(true) {
                                scope.launch{
                                    showSecureAgreeModal = E2EEHandler
                                        .hasPendingApproval(context, viewModel.address)
                                }
                            }
                        }

                    }
                }
            }

            val showScrollBottom by remember {
                derivedStateOf {
                    listState.firstVisibleItemIndex > 0
                }
            }

            if(showScrollBottom) {
                Button(
                    onClick = {
                        searchQuery = ""
                        searchIndexes.clear()
                        searchIndex = 0

                        scope.launch { listState.animateScrollToItem(0) }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.outline
                    ),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                        .clip(CircleShape)
                        .size(50.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.down_to_latest_content),
                        tint= MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            if(openAlertDialog) {
                ShortCodeAlert {
                    openAlertDialog = false
                }
            }

            if(openInfoAlert) {
                MessageInfoAlert(
                    viewModel.selectedMessage!!
                ) {
                    viewModel.selectedMessage = null
                    openInfoAlert = false
                }
            }
        }

        if(showFailedRetryModal) {
            FailedMessageOptionsModal(
                retryCallback = {
                    coroutineScope.launch {
                        viewModel.delete(context, viewModel.retryDeleteItem.first())
                        sendSMS(
                            context=context,
                            text=viewModel.retryDeleteItem.first().text!!,
                            threadId= viewModel.threadId,
                            messageId = System.currentTimeMillis().toString(),
                            address= viewModel.address,
                            conversationsViewModel = viewModel
                        ) {
                            viewModel.retryDeleteItem = arrayListOf()
                            viewModel.clearDraft(context)
                        }
                    }
                },
                deleteCallback = {
                    coroutineScope.launch {
                        viewModel.delete(context, viewModel.retryDeleteItem.first())
                        viewModel.retryDeleteItem = arrayListOf()
                    }
                },
            ){
                showFailedRetryModal = false
            }
        }

        if(showSecureRequestModal || showSecureAgreeModal) {
            SecureRequestAcceptModal(
                viewModel=viewModel,
                isSecureRequest = showSecureRequestModal,
            ){
                if(showSecureAgreeModal) {
                    isSecured = E2EEHandler.isSecured(context, viewModel.address)
                    showSecureAgreeModal = false
                }

                if(showSecureRequestModal)
                    showSecureRequestModal = false
            }
        }

        if(rememberDeleteAlert) {
            DeleteConfirmationAlert(
                confirmCallback = {
                    coroutineScope.launch {
                        viewModel.deleteThread(context)
                        rememberDeleteAlert = false
                        (context as Activity).runOnUiThread {
                            backHandler(
                                context,
                                viewModel,
                                navController
                            )
                        }
                    }
                }
            ) {
                rememberDeleteAlert = false
                selectedItems.clear()
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
            val address = "+123456789"
            val threadId = "1"
            for(i in 0..1) {
                val conversation = Conversation()
                conversation.id = i.toLong()
                conversation.text = stringResource(
                    R.string
                        .settings_add_gateway_server_protocol_meta_description)
                conversation.type = if(!isSend) Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX
                else Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
                conversations.add(conversation)
                isSend = !isSend
            }
            Conversations(navController = rememberNavController(), _items=conversations)
        }
    }
}

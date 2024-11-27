package com.afkanerd.deku.DefaultSMS.ui

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.Telephony
import android.text.InputType
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.expandIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.ListItem
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.InsertComment
import androidx.compose.material.icons.automirrored.twotone.InsertComment
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import androidx.room.util.TableInfo
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.AboutActivity
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.ComposeNewMessageScreen
import com.afkanerd.deku.DefaultSMS.ConversationsScreen
import com.afkanerd.deku.DefaultSMS.Extensions.isScrollingUp
import com.afkanerd.deku.DefaultSMS.HomeScreen
import com.afkanerd.deku.DefaultSMS.Models.Archive
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversationsHandler
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.Models.ThreadsCount
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.SearchThreadScreen
import com.afkanerd.deku.DefaultSMS.SettingsActivity
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationStatusTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.afkanerd.deku.Router.GatewayServers.GatewayServerRoutedActivity
import com.example.compose.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp

enum class InboxType(val value: Int) {
    INBOX(0),
    ARCHIVED(1),
    ENCRYPTED(2),
    BLOCKED(3),
    DRAFTS(4),
    MUTED(5);

    companion object {
        fun fromInt(value: Int): InboxType? {
            return InboxType.entries.find { it.value == value }
        }
    }
}

@Composable
fun SwipeToDeleteBackground(dismissState: SwipeToDismissBoxState? = null) {
    val color = when(dismissState?.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primary
        SwipeToDismissBoxValue.Settled -> Color.Transparent
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Icon(
            Icons.Default.Archive,
            tint = MaterialTheme.colorScheme.onPrimary,
            contentDescription = stringResource(R.string.messages_threads_menu_archive)
        )
    }
}

fun processIntents(
    context: Context,
    intent: Intent,
    defaultRegion: String,
): Triple<String?, String?, String?>?{
    if(intent.action != null &&
        ((intent.action == Intent.ACTION_SENDTO) || (intent.action == Intent.ACTION_SEND))) {
        val sendToString = intent.dataString
        val text = if(intent.hasExtra("sms_body")) intent.getStringExtra("sms_body") else ""
        if (sendToString != null &&
            (sendToString.contains("smsto:") || sendToString.contains("sms:"))
        ) {
            val address = Helpers.getFormatCompleteNumber(sendToString, defaultRegion)
            val threadId =
                ThreadedConversationsHandler.get(context, address)
                    .thread_id

            return Triple(address, threadId, text)
        }
    }
    else if(intent.hasExtra("address")) {
        val address = intent.getStringExtra("address")
//        val threadId = ThreadedConversationsHandler.get(context, address).thread_id
        val threadId = intent.getStringExtra("thread_id")
        return Triple(address, threadId, "")
    }
    return null
}

fun navigateToConversation(
    context: Context,
    viewModel: ThreadedConversationsViewModel? = null,
    conversationsViewModel: ConversationsViewModel,
    address: String,
    threadId: String,
    subscriptionId: Int,
    navController: NavController,
    searchQuery: String? = ""
) {
    conversationsViewModel.address = address
    conversationsViewModel.threadId = threadId
    conversationsViewModel.contactName = ""
    conversationsViewModel.searchQuery = searchQuery ?: ""
    conversationsViewModel.subscriptionId = subscriptionId
    conversationsViewModel.liveData = null
    viewModel?.updateRead(context, threadId)
    navController.navigate(ConversationsScreen)
}

@Preview(showBackground = true)
@Composable
fun ModalDrawerSheetLayout(
    callback: ((InboxType) -> Unit)? = null,
    selectedItemIndex: InboxType = InboxType.INBOX,
    counts: ThreadsCount? = null,
) {
    ModalDrawerSheet {
        Text(
            stringResource(R.string.folders),
            fontSize = 12.sp,
            modifier = Modifier.padding(16.dp))
        HorizontalDivider()
        Column(modifier = Modifier.padding(16.dp)) {
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Inbox,
                        contentDescription = stringResource(R.string.inbox_folder)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_inbox ),
                        fontSize = 14.sp
                    )
                },
                badge = {
                    counts?.let {
                        if(counts.unreadCount > 0)
                            Text(counts.unreadCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.INBOX,
                onClick = { callback?.let{ it(InboxType.INBOX) } }
            )
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Archive,
                        contentDescription = stringResource(R.string.archive_folder)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_archived ),
                        fontSize = 14.sp
                    )
                },
                badge = {
                    counts?.let {
                        if(counts.archivedCount > 0)
                            Text(counts.archivedCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.ARCHIVED,
                onClick = { callback?.let{ it(InboxType.ARCHIVED) } }
            )
            HorizontalDivider()
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Drafts,
                        contentDescription = stringResource(R.string.thread_conversation_type_draft)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_drafts),
                        fontSize = 14.sp
                    )
                },
                badge = {
                    counts?.let {
                        if(counts.draftsCount > 0)
                            Text(counts.draftsCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.DRAFTS,
                onClick = { callback?.let{ it(InboxType.DRAFTS) } }
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Security,
                        contentDescription = stringResource(R.string.encrypted_folder)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_encryption),
                        fontSize = 14.sp
                    )
                },
                badge = {
                    counts?.let {
                        if(counts.encryptedCount > 0)
                            Text(counts.encryptedCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.ENCRYPTED,
                onClick = { callback?.let{ it(InboxType.ENCRYPTED) } }
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.AutoMirrored.Default.VolumeOff,
                        contentDescription = stringResource(R.string.conversation_menu_muted_label)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversation_menu_muted_label),
                        fontSize = 14.sp
                    )
                },
                badge = {
                    counts?.let {
                        if(counts.mutedCount > 0)
                            Text(counts.mutedCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.MUTED,
                onClick = { callback?.let{ it(InboxType.MUTED) } }
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Block,
                        contentDescription = stringResource(R.string.blocked_folder)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_blocked),
                        fontSize = 14.sp
                    )
                },
                 badge = {
                    counts?.let {
                        if(counts.blockedCount > 0)
                            Text(counts.blockedCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.BLOCKED,
                onClick = { callback?.let{ it(InboxType.BLOCKED) } }
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainDropDownMenu(
    expanded: Boolean = false,
    dismissCallback: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.TopEnd)
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { dismissCallback?.let{ it(false) } },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.homepage_menu_routed),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    context.startActivity(
                        Intent(context, GatewayServerRoutedActivity::class.java).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        }
                    )
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.settings_title),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    context.startActivity(
                        Intent(context, SettingsActivity::class.java).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        }
                    )
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.conversation_menu_export),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    TODO()
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.about_deku),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    context.startActivity(
                        Intent(context, AboutActivity::class.java).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ThreadConversationLayout(
    viewModel: ThreadedConversationsViewModel = ThreadedConversationsViewModel(),
    conversationsViewModel: ConversationsViewModel = ConversationsViewModel(),
    navController: NavController,
    _items: List<ThreadedConversations>? = null,
) {
    val inPreviewMode = LocalInspectionMode.current
    val context = LocalContext.current
    viewModel.intent?.let { intent ->
        val defaultRegion = if(inPreviewMode) "cm" else Helpers.getUserCountry(context)
        processIntents(context, intent, defaultRegion)?.let {
            viewModel.intent = null
            it.first?.let{ address ->
                it.second?.let { threadId ->
                    it.third?.let{ message ->
                        conversationsViewModel.text = message
                    }
                    navigateToConversation(
                        context = context,
                        viewModel = viewModel,
                        conversationsViewModel = conversationsViewModel,
                        address = address,
                        threadId = threadId,
                        subscriptionId = SIMHandler.getDefaultSimSubscription(context),
                        navController = navController,
                    )
                }
            }
        }
    }
    conversationsViewModel.text = ""

    val counts by viewModel.getCount(context).observeAsState(null)

    var inboxType by remember { mutableStateOf(viewModel.inboxType) }

    val items: List<ThreadedConversations> by viewModel
        .getAllLiveData(context).observeAsState(emptyList())
    val archivedItems: List<ThreadedConversations> by viewModel
        .archivedLiveData!!.observeAsState(emptyList())
    val encryptedItems: List<ThreadedConversations> by viewModel
        .encryptedLiveData!!.observeAsState(emptyList())
    val blockedItems: List<ThreadedConversations> by viewModel
        .blockedLiveData!!.observeAsState(emptyList())
    val draftsItems: List<ThreadedConversations> by viewModel
        .draftsLiveData!!.observeAsState(emptyList())
    val mutedItems: List<ThreadedConversations> by viewModel
        .mutedLiveData!!.observeAsState(emptyList())

    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var selectedItems = remember { mutableStateListOf<ThreadedConversations>() }

    val selectedIconColors = MaterialTheme.colorScheme.primary
    var selectedItemIndex by remember { mutableStateOf(viewModel.inboxType) }

    var rememberMenuExpanded by remember { mutableStateOf( false)}

    val scope = rememberCoroutineScope()

    LaunchedEffect(items) {
        CoroutineScope(Dispatchers.Default).launch {
            val items = viewModel.getAll(context)
            items.forEach {
                viewModel.updateInformation(
                    context=context,
                    threadId = it.thread_id,
                    contactName =
                    Contacts.retrieveContactName(context, it.address),
                    conversationsViewModel = conversationsViewModel
                )
            }
            viewModel.refreshCount(context)
        }
    }

    BackHandler {
        if(viewModel.inboxType != InboxType.INBOX) {
            viewModel.inboxType = InboxType.INBOX
            selectedItemIndex = InboxType.INBOX
            inboxType = InboxType.INBOX
        }
        else {
            if(context is AppCompatActivity) {
                context.finish()
            }
        }
    }

    MainDropDownMenu(rememberMenuExpanded) {
        rememberMenuExpanded = it
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheetLayout(
                callback = { type ->
                    scope.launch {
                        drawerState.apply {
                            if(isClosed) open() else close()
                            inboxType = type
                            selectedItemIndex = type
                            viewModel.inboxType = type
                        }
                    }
                },
                selectedItemIndex = selectedItemIndex,
                counts = counts,
            )
        },
    ) {
        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            topBar = {
                if(inboxType == InboxType.INBOX && selectedItems.isEmpty()) {
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
                                        if(isClosed) { open() }
                                        else { close() }
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
                                navController.navigate(SearchThreadScreen)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(R.string.search_messages)
                                )
                            }
                            IconButton(onClick = {
                                rememberMenuExpanded = !rememberMenuExpanded
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.open_menu)
                                )
                            }
                        },
                        scrollBehavior = scrollBehaviour
                    )
                }
                else if(!selectedItems.isEmpty()) {
                    TopAppBar(
                        title = {
                            Text(
                                text= "${selectedItems.size} ${stringResource(R.string.selected)}",
                                maxLines =1,
                                color = selectedIconColors,
                                overflow = TextOverflow.Ellipsis)
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                selectedItems.clear()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    tint = selectedIconColors,
                                    contentDescription = stringResource(R.string.cancel_selection)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                if(inboxType == InboxType.ARCHIVED) {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        val threads: List<Archive> = selectedItems.map{
                                            Archive().apply {
                                                thread_id = it.thread_id
                                                is_archived = false
                                            }
                                        }
                                        viewModel.unarchive(context, threads)
                                        selectedItems.clear()
                                    }
                                } else {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        val threads: List<Archive> = selectedItems.map{
                                            Archive().apply {
                                                thread_id = it.thread_id
                                                is_archived = true
                                            }
                                        }
                                        viewModel.archive(context, threads)
                                        selectedItems.clear()
                                    }
                                }
                            }) {
                                if(inboxType == InboxType.ARCHIVED) {
                                    Icon(
                                        imageVector = Icons.Filled.Unarchive,
                                        tint = selectedIconColors,
                                        contentDescription =
                                        stringResource(R.string.unarchive_messages)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Archive,
                                        tint = selectedIconColors,
                                        contentDescription =
                                        stringResource(R.string.messages_threads_menu_archive)
                                    )
                                }
                            }

                            IconButton(onClick = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    val threads: List<String> = selectedItems.map{ it.thread_id }
                                    viewModel.delete(context, threads)
                                    selectedItems.clear()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    tint = selectedIconColors,
                                    contentDescription =
                                    stringResource(R.string.message_threads_menu_delete)
                                )
                            }
                        },
                        scrollBehavior = scrollBehaviour
                    )
                }
                else {
                    TopAppBar(
                        title = {
                            Text(
                                text= when(inboxType) {
                                    InboxType.ARCHIVED ->
                                        stringResource(R.string
                                            .conversations_navigation_view_archived)
                                    InboxType.ENCRYPTED ->
                                        stringResource(R.string
                                            .conversations_navigation_view_encryption)
                                    InboxType.BLOCKED ->
                                        stringResource(R.string
                                            .conversations_navigation_view_blocked)
                                    InboxType.MUTED ->
                                        stringResource(R.string
                                            .conversation_menu_muted_label)
                                    InboxType.DRAFTS ->
                                        stringResource(R.string
                                            .conversations_navigation_view_drafts)
                                    else -> ""
                                },
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
                        scrollBehavior = scrollBehaviour
                    )
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        navController.navigate(ComposeNewMessageScreen)
                    },
                    icon = { Icon( Icons.AutoMirrored.Default.Message,
                        stringResource(R.string.compose_new_message)) },
                    text = { Text(text = stringResource(R.string.compose)) },
                    expanded = listState.isScrollingUp()
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                state = listState
            )  {
                items(
                    items = if(_items == null) when(inboxType) {
                        InboxType.INBOX -> items
                        InboxType.ARCHIVED -> archivedItems
                        InboxType.ENCRYPTED -> encryptedItems
                        InboxType.BLOCKED -> blockedItems
                        InboxType.DRAFTS -> draftsItems
                        InboxType.MUTED -> mutedItems
                    } else _items,
                    key = { it.hashCode() }
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

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                when(it) {
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        CoroutineScope(Dispatchers.Default).launch {
                                            viewModel.archive(context, message.thread_id)
                                        }
                                    }
                                    SwipeToDismissBoxValue.Settled ->
                                        return@rememberSwipeToDismissBoxState false
                                    else -> {}
                                }
                                return@rememberSwipeToDismissBoxState true
                            },
                            positionalThreshold = { it * .75f }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = { SwipeToDeleteBackground(dismissState) }
                        ) {
                            ThreadConversationCard(
                                id = message.thread_id,
                                firstName = firstName,
                                lastName = lastName,
                                content = if(message.snippet.isNullOrBlank())
                                    stringResource(R.string.conversation_threads_secured_content)
                                else message.snippet,
                                date =
                                if(!message.date.isNullOrBlank())
                                    Helpers.formatDate(context, message.date.toLong())
                                else "Tues",
                                isRead = message.isIs_read,
                                isContact = isContact,
//                                unreadCount = message.unread_count,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        if(selectedItems.isEmpty()) {
                                            navigateToConversation(
                                                context = context,
                                                viewModel = viewModel,
                                                conversationsViewModel = conversationsViewModel,
                                                address = message.address,
                                                threadId = message.thread_id,
                                                subscriptionId =
                                                SIMHandler.getDefaultSimSubscription(context),
                                                navController = navController,
                                            )
                                        } else {
                                            if(selectedItems.contains(message))
                                                selectedItems.remove(message)
                                            else
                                                selectedItems.add(message)
                                        }
                                    },
                                    onLongClick = { selectedItems.add(message) }
                                ),
                                isSelected = selectedItems.contains(message),
                                isMuted = message.isIs_mute,
                                isDraft = message.type == Telephony.Sms.MESSAGE_TYPE_DRAFT,
                            )
                        }
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
            ThreadConversationLayout(navController = rememberNavController(), _items = messages)
        }
    }


}


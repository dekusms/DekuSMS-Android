package com.afkanerd.smswithoutborders_libsmsmms.ui

import android.content.Intent
import android.provider.BlockedNumberContract
import android.provider.Telephony
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState.Loading
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.DateTimeUtils
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getNativesLoaded
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactName
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.setNativesLoaded
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsGetEnableSwipeBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.extensions.isScrollingUp
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.DeleteConfirmationAlert
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.GetSwipeBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.ModalDrawerSheetLayout
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.SwipeToDeleteBackground
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.ThreadConversationCard
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.ThreadsMainDropDown
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ComposeNewMessageScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.HomeScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.SearchScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ThreadsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProcessIntents(intent: Intent?) {
    LaunchedEffect(intent) {
        intent?.let { intent ->
            TODO("Implement intent handling")
//            threadsViewModel.processIntents(
//                context,
//                intent,
//                context.getDefaultRegion()
//            )?.let { processedIntents ->
//                threadsViewModel.setNewIntent(null)
//                if( processedIntents.address != null && processedIntents.threadId != null) {
//                    threadsViewModel.navigateToConversation(
//                        threadsViewModel = conversationsViewModel,
//                        address = address,
//                        threadId = threadId,
//                        subscriptionId = SIMHandler.getDefaultSimSubscription(context),
//                        navController = navController,
//                    )
//                }
//            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class
)
@Composable
fun ThreadConversationLayout(
    threadsViewModel: ThreadsViewModel,
    navController: NavController,
    foldOpen: Boolean = false,
) {
    val inPreviewMode = LocalInspectionMode.current
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

    val readPhoneStatePermission =
        rememberPermissionState(requiredReadPhoneStatePermissions)

    var isDefault by remember{ mutableStateOf(inPreviewMode || context.isDefault()) }

    val newIntent by threadsViewModel.newIntent.collectAsState()
    ProcessIntents(newIntent)

    val messagesAreLoading = threadsViewModel.messagesLoading

    var inboxType by remember { mutableStateOf(ThreadsViewModel.InboxType.INBOX )}
    DisposableEffect(lifeCycleOwner) {
        val observer = Observer<ThreadsViewModel.InboxType> { newInboxType ->
            inboxType = newInboxType
        }
        threadsViewModel.selectedInbox.observe(lifeCycleOwner, observer)

        onDispose {
            threadsViewModel.selectedInbox.removeObserver(observer)
        }
    }

    val selectedItems by threadsViewModel.selectedItems.collectAsState()

    val inboxMessagesPagers = threadsViewModel.getThreads(context)
    val archivedMessagesPagers = threadsViewModel.getArchives(context)
//
//    val encryptedMessagesPagers = threadsViewModel
//        .getEncryptedPagingSource(context)
//
//    val draftMessagesPagers = threadsViewModel
//        .getDraftPagingSource(context)
//
//    val mutedMessagesPagers = threadsViewModel
//        .getMutedPagingSource(context)
//
//    val remoteMessagesPagers = threadsViewModel
//        .getRemoteListenersPagingSource(context)

    val inboxMessagesItems = inboxMessagesPagers.collectAsLazyPagingItems()
    val archivedMessagesItems = archivedMessagesPagers.collectAsLazyPagingItems()
//    val encryptedMessagesItems = encryptedMessagesPagers.collectAsLazyPagingItems()
//    val draftMessagesItems = draftMessagesPagers.collectAsLazyPagingItems()
//    val mutedMessagesItems = mutedMessagesPagers.collectAsLazyPagingItems()
//    val remoteMessagesItems = remoteMessagesPagers.collectAsLazyPagingItems()

//    val blockedItems: MutableList<Conversation> = remember { mutableStateListOf() }

    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val slideDeleteItem = remember { mutableStateOf("") }

    val selectedIconColors = MaterialTheme.colorScheme.primary

    var rememberImportMenuExpanded by remember { mutableStateOf( false)}
    var rememberDeleteMenu by remember { mutableStateOf( false)}

    val scope = rememberCoroutineScope()
    val coroutineScope = remember { CoroutineScope(Dispatchers.Default) }

    // TODO: Handle blocked messages
//    LaunchedEffect(inboxType) {
//        selectedItemIndex = inboxType
//        if(inboxType == InboxType.BLOCKED && isDefault) {
//            coroutineScope.launch {
//                threadsViewModel.get(context).let {
//                    blockedItems.addAll(it.filter {
//                        BlockedNumberContract.isBlocked(context, it.address)
//                    })
//                }
//            }
//        }
//    }

//    LaunchedEffect(remoteMessagesItems) {
//        if(!isDefault && remoteMessagesItems.itemCount > 0)
//            inboxType = InboxType.REMOTE_LISTENER
//    }

//    LaunchedEffect(threadsViewModel.importDetails) {
//        rememberImportMenuExpanded = threadsViewModel.importDetails.isNotBlank()
//    }

//    BackHandler {
//        if(threadsViewModel.inboxType != InboxType.INBOX) {
//            threadsViewModel.inboxType = InboxType.INBOX
//            selectedItemIndex = InboxType.INBOX
//            inboxType = InboxType.INBOX
//        }
//        else if(!selectedItems.isEmpty()) {
//            selectedItems.clear()
//        }
//        else {
//            if(context is AppCompatActivity) {
//                context.finish()
//            }
//        }
//    }

    LaunchedEffect(isDefault) {
        if(!context.getNativesLoaded() && isDefault) {
            threadsViewModel.loadNatives(context) {
                context.setNativesLoaded(true)
            }
        }
    }

    var rememberMenuExpanded by remember { mutableStateOf( false)}

    ThreadsMainDropDown(
        navController = navController,
        expanded=rememberMenuExpanded,
        threadsViewModel = threadsViewModel,
    ) {
        rememberMenuExpanded = it
    }

    ModalNavigationDrawer(
        modifier = Modifier.safeDrawingPadding(),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheetLayout(
                callback = { type ->
                    threadsViewModel.setInboxType(type)
                    scope.launch {
                        drawerState.apply {
                            if(isClosed) open() else close()
                        }
                    }
                },
                selectedItemIndex = inboxType,
            )
        },
    ) {
        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            topBar = {
                if(selectedItems.isEmpty() && inboxType == ThreadsViewModel.InboxType.INBOX) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "DekuSMS",
                                style = MaterialTheme.typography.titleLarge
                            )
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
                            if(isDefault || inPreviewMode) {
                                IconButton(onClick = {
                                    navController.navigate(SearchScreenNav())
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = stringResource(R.string.search_messages)
                                    )
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
                        },
                        scrollBehavior = scrollBehaviour
                    )
                }
                else if(selectedItems.isNotEmpty()) {
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
                                threadsViewModel.removeAllSelectedItems()
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
                                if(inboxType == ThreadsViewModel.InboxType.ARCHIVED) {
                                    threadsViewModel.unArchiveThreads(
                                        context, selectedItems)
                                    threadsViewModel.removeAllSelectedItems()
                                } else {
                                    threadsViewModel.archiveThreads(context, selectedItems)
                                    threadsViewModel.removeAllSelectedItems()
                                }
                            }) {
                                if(inboxType == ThreadsViewModel.InboxType.ARCHIVED) {
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
                                rememberDeleteMenu = true
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
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
                                    ThreadsViewModel.InboxType.ARCHIVED ->
                                        stringResource(R.string
                                            .conversations_navigation_view_archived)
                                    ThreadsViewModel.InboxType.BLOCKED ->
                                        stringResource(R.string
                                            .conversations_navigation_view_blocked)
                                    ThreadsViewModel.InboxType.MUTED ->
                                        stringResource(R.string
                                            .conversation_menu_muted_label)
                                    ThreadsViewModel.InboxType.DRAFTS ->
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
                        actions = {},
                        scrollBehavior = scrollBehaviour
                    )
                }
            },
            floatingActionButton = {
                when(inboxType) {
                    ThreadsViewModel.InboxType.INBOX -> {
                        if((isDefault && !messagesAreLoading) || inPreviewMode) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    navController.navigate(ComposeNewMessageScreenNav)
                                },
                                icon = { Icon( Icons.AutoMirrored.Default.Message,
                                    stringResource(R.string.compose_new_message)) },
                                text = { Text(text = stringResource(R.string.compose)) },
                                expanded = listState.isScrollingUp(),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    else -> {}
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                if(!isDefault || !readPhoneStatePermission.status.isGranted) {
                    DefaultCheckMain { isDefault = context.isDefault() }
                }
                if(inPreviewMode || messagesAreLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator()
                        Text(
                            stringResource(R.string.give_it_a_minute),
                            modifier = Modifier.padding(top=8.dp),
                            fontSize = 12.sp
                        )
                    }
                }
                else {
                    Box(
                        modifier = Modifier .fillMaxSize()
                    ) {
                        when(inboxType) {
                            ThreadsViewModel.InboxType.ARCHIVED -> {
                                if(archivedMessagesItems.loadState.refresh != Loading &&
                                    archivedMessagesItems.itemCount < 1)
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            stringResource(R.string.homepage_archive_no_message),
                                            fontSize = 24.sp
                                        )
                                    }
                            }
                            ThreadsViewModel.InboxType.DEVELOPER_MODE -> {
                                TODO("Implement navigate to thread view")
                            }
                            else -> {
                                if(inboxMessagesItems.loadState.refresh != Loading &&
                                    inboxMessagesItems.itemCount < 1)
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            stringResource(R.string.homepage_no_message),
                                            fontSize = 24.sp
                                        )
                                    }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        )  {
                            items(
                                count = when(inboxType) {
                                    ThreadsViewModel.InboxType.ARCHIVED -> archivedMessagesItems.itemCount
//                                ThreadsViewModel.InboxType.BLOCKED -> blockedItems.size
//                                ThreadsViewModel.InboxType.ENCRYPTED -> encryptedMessagesItems.itemCount
//                                ThreadsViewModel.InboxType.DRAFTS -> draftMessagesItems.itemCount
//                                ThreadsViewModel.InboxType.MUTED -> mutedMessagesItems.itemCount
//                                ThreadsViewModel.InboxType.REMOTE_LISTENER -> remoteMessagesItems.itemCount
                                    else -> inboxMessagesItems.itemCount
                                },
                                key = when(inboxType) {
                                    ThreadsViewModel.InboxType.ARCHIVED ->
                                        archivedMessagesItems.itemKey{ it.threadId }
//                                ThreadsViewModel.InboxType.BLOCKED -> {{ blockedItems[it].id }}
//                                ThreadsViewModel.InboxType.ENCRYPTED -> encryptedMessagesItems.itemKey{ it.id }
//                                ThreadsViewModel.InboxType.DRAFTS -> draftMessagesItems.itemKey{ it.id }
//                                ThreadsViewModel.InboxType.MUTED -> mutedMessagesItems.itemKey{ it.id }
//                                ThreadsViewModel.InboxType.REMOTE_LISTENER -> remoteMessagesItems.itemKey{ it.id }
                                    else -> inboxMessagesItems.itemKey{ it.threadId }
                                }
                            ) { index ->
                                val thread = when(inboxType) {
                                    ThreadsViewModel.InboxType.ARCHIVED -> archivedMessagesItems[index]
                                    else -> inboxMessagesItems[index]
//                                InboxType.ENCRYPTED -> encryptedMessagesItems[index]
//                                InboxType.BLOCKED -> blockedItems[index]
//                                InboxType.DRAFTS -> draftMessagesItems[index]
//                                InboxType.MUTED -> mutedMessagesItems[index]
//                                InboxType.REMOTE_LISTENER -> remoteMessagesItems[index]
                                }

                                thread?.address?.let { address ->
                                    val isBlocked = if(isDefault)
                                        BlockedNumberContract.isBlocked(context, address)
                                    else false

                                    val contactName = if(isDefault)
                                        context.retrieveContactName(address)
                                    else address

                                    var firstName = address
                                    var lastName = ""

                                    if (!contactName.isNullOrEmpty()) {
                                        contactName.split(" ").let {
                                            firstName = it[0]
                                            if (it.size > 1)
                                                lastName = it[1]
                                        }
                                    }

                                    val dismissState = GetSwipeBehaviour(thread, inboxType)

//                                val date = if(thread.date > 0) DateTimeUtils
//                                    .formatDate(context, (thread.date * 1000L)) ?: "" else "Tues"

                                    val date = if(!inPreviewMode) DateTimeUtils.formatDate(
                                        context,
                                        thread.date
                                    ) ?: "" else "Tues"

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        gesturesEnabled = context.settingsGetEnableSwipeBehaviour,
                                        backgroundContent = {
                                            SwipeToDeleteBackground(
                                                dismissState,
                                                inboxType == ThreadsViewModel.InboxType.ARCHIVED
                                            )
                                        }
                                    ) {
                                        ThreadConversationCard(
                                            id = thread.threadId,
                                            firstName = firstName,
                                            lastName = lastName,
                                            phoneNumber = address,
                                            content = thread.snippet,
                                            date = date,
                                            isRead = !thread.unread,
                                            isContact = isDefault && !contactName.isNullOrBlank(),
                                            isBlocked = isBlocked,
                                            modifier = Modifier.combinedClickable(
                                                onClick = {
                                                    if(selectedItems.isEmpty()) {
                                                        if(!foldOpen) navController.navigate(
                                                            ConversationsScreenNav(address))
                                                        else navController
                                                            .navigate(HomeScreenNav(address))
                                                    } else {
                                                        threadsViewModel.setSelectedItems(
                                                            selectedItems.toMutableList().apply {
                                                                if(selectedItems.contains(thread))
                                                                    remove(thread)
                                                                else add(thread)
                                                            }
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    threadsViewModel.setSelectedItems(
                                                        selectedItems.toMutableList().apply {
                                                            if(selectedItems.contains(thread))
                                                                remove(thread)
                                                            else add(thread)
                                                        }
                                                    )
                                                }
                                            ),
                                            isSelected = selectedItems.contains(thread),
                                            isMuted = thread.isMute,
                                            type = thread.type,
                                            unreadCount = thread.unreadCount
                                        )
                                    }
                                }
                            }
                        }

                        if(rememberDeleteMenu) {
                            DeleteConfirmationAlert(
                                confirmCallback = {
                                    threadsViewModel.deleteThreads(
                                        context,
                                        selectedItems
                                    )
                                    threadsViewModel.removeAllSelectedItems()
                                    rememberDeleteMenu = false
                                }
                            ) {
                                rememberDeleteMenu = false
                                threadsViewModel.removeAllSelectedItems()
                            }
                        }

//                    if(rememberImportMenuExpanded) {
//                        val importConversations by remember { mutableStateOf(threadsViewModel
//                            .importAll(context, detailsOnly = true)) }
//                        val numThreads by remember { mutableStateOf(
//                            importConversations.map { it.thread_id }.toSet()
//                        ) }
//                        ImportDetails(
//                            numOfConversations = importConversations.size,
//                            numOfThreads = numThreads.size,
//                            resetConfirmCallback = {
//                                coroutineScope.launch {
//                                    threadsViewModel.clear(context)
//                                    threadsViewModel.importAll(context)
//                                    threadsViewModel.importDetails = ""
//                                }
//                            },
//                            confirmCallback = {
//                                coroutineScope.launch {
//                                    threadsViewModel.importAll(context)
//                                    threadsViewModel.importDetails = ""
//                                }
//                            }) {
//                            threadsViewModel.importDetails = ""
//                        }
//                    }
                    }
                }

            }
        }
    }
}

@Preview
@Composable
fun PreviewMessageCard() {
    Surface(Modifier.safeDrawingPadding()) {
        val messages: MutableList<Threads> =
            remember { mutableListOf( ) }
        for(i in 0..10) {
            val thread = Threads(
                threadId = i.toInt(),
                address = "$i",
                snippet = "Hello world: $i",
                date = System.currentTimeMillis(),
                unread = true,
                isMute = true,
                type = Telephony.Sms.MESSAGE_TYPE_SENT,
                conversationId = i.toLong(),
                isArchive = false
            )
            messages.add(thread)
        }
        val threadsViewModel: ThreadsViewModel = viewModel()
        ThreadConversationLayout(
            threadsViewModel = threadsViewModel,
            navController = rememberNavController(),
        )
    }
}

@Preview
@Composable
fun PreviewMessageCardRemoteListeners_Preview() {
    Surface(Modifier.safeDrawingPadding()) {
        val threadsViewModel: ThreadsViewModel = viewModel()
        ThreadConversationLayout(
            threadsViewModel = threadsViewModel,
            navController = rememberNavController(),
        )
    }
}
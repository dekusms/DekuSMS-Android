package com.afkanerd.smswithoutborders_libsmsmms.ui.components

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.afkanerd.smswithoutborders_libsmsmms.BuildConfig
import com.afkanerd.smswithoutborders_libsmsmms.extensions.toHslColor
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.exportRawWithColumnGuesses
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactPhoto
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.SettingsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ThreadsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream

@Composable
fun DeleteConfirmationAlert(
    confirmCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null,
) {
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.inverseSurface,
        title = {
            Text(
                stringResource(R.string.messages_thread_delete_confirmation_title),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.messages_thread_delete_confirmation_text),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        onDismissRequest = { dismissCallback?.invoke() },
        confirmButton = {
            TextButton(
                onClick = { confirmCallback?.invoke() }
            ) {
                Text(
                    stringResource(R.string.messages_thread_delete_confirmation_yes),
                    color = MaterialTheme.colorScheme.errorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dismissCallback?.invoke() }
            ) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}


@Composable
fun ImportDetails(
    numOfConversations: Int = 0,
    numOfThreads: Int = 0,
    confirmCallback: (() -> Unit)? = null,
    resetConfirmCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null,
) {
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.inverseSurface,
        title = {
            Text(
                stringResource(R.string.import_conversations),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.threads) + numOfThreads,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    stringResource(R.string.conversations) + numOfConversations,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        onDismissRequest = { dismissCallback?.invoke() },
        confirmButton = {
            if(BuildConfig.DEBUG)
                TextButton(
                    onClick = {resetConfirmCallback?.invoke()}
                ) {
                    Text(
                        stringResource(R.string.reset_and_import),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

            TextButton(
                onClick = { confirmCallback?.invoke() }
            ) {
                Text(
                    stringResource(R.string.conversation_menu_import),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dismissCallback?.invoke() }
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    )
}

@Composable
private fun ThreadConversationsAvatar(
    context: Context,
    id: Int,
    firstName: String,
    lastName: String,
    phoneNumber: String,
    isContact: Boolean = true
) {

    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        if (isContact) {
            val contactPhotoUri = remember(phoneNumber) {
                context.retrieveContactPhoto(phoneNumber)
            }
            if (!contactPhotoUri.isNullOrEmpty() && contactPhotoUri != "null") {
                AsyncImage(
                    model = contactPhotoUri,
                    contentDescription = "Contact Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                val color = remember(id, firstName, lastName) {
                    Color("$id / $firstName".toHslColor())
                }
                val initials = (firstName.take(1) + lastName.take(1)).uppercase()
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(SolidColor(color))
                }
                Text(text = initials, style = MaterialTheme.typography.titleSmall, color = Color.White)
            }
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = "",
                Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
fun ThreadConversationCard(
    phoneNumber: String,
    id: Int,
    firstName: String,
    lastName: String,
    content: String,
    date: String,
    isRead: Boolean,
    isContact: Boolean,
    unreadCount: Int = 0,
    modifier: Modifier,
    isSelected: Boolean = false,
    isMuted: Boolean = false,
    isBlocked: Boolean = false,
    type: Int,
    mms: Boolean = false
) {
    val context = LocalContext.current

    var weight = FontWeight.Bold
    val colorHeadline = when {
        isRead || isBlocked -> {
            weight = FontWeight.Normal
            MaterialTheme.colorScheme.secondary
        }
        else -> MaterialTheme.colorScheme.onBackground
    }
    val colorContent = when(type) {
        Telephony.Sms.MESSAGE_TYPE_FAILED ->
            MaterialTheme.colorScheme.error
        Telephony.Sms.MESSAGE_TYPE_OUTBOX -> MaterialTheme.colorScheme.secondary
        else -> colorHeadline
    }

    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = if(isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.background
        ),
        headlineContent = {
            Row {
                Text(
                    text = "$firstName $lastName",
                    color = colorHeadline,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = weight
                )

                if(isMuted)
                    Icon(Icons.Default.NotificationsOff,
                        stringResource(R.string.thread_muted),
                        modifier = Modifier.size(16.dp)
                    )

                if(isBlocked)
                    Icon(Icons.Filled.Block,
                        stringResource(R.string.contact_is_blocked),
                        modifier = Modifier.size(16.dp)
                    )
            }
        },
        supportingContent = {
            Text(
                text = when(type) {
                    Telephony.Sms.MESSAGE_TYPE_DRAFT ->
                        stringResource(R.string.thread_conversation_type_draft) + ": $content"
                    Telephony.Sms.MESSAGE_TYPE_QUEUED,
                    Telephony.Sms.MESSAGE_TYPE_OUTBOX ->
                        stringResource(R.string.sms_status_sending)+ ": $content"
                    Telephony.Sms.MESSAGE_TYPE_FAILED ->
                        stringResource(R.string.sms_status_failed_only)+ ": $content"
                    Telephony.Sms.MESSAGE_TYPE_SENT ->
                        stringResource(R.string.messages_thread_you)+ " $content"
                    else -> if(!mms) content else stringResource(R.string.image)
                },
                color = colorContent,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = if(
                    type == Telephony.Sms.MESSAGE_TYPE_DRAFT ||
                    type == Telephony.Sms.MESSAGE_TYPE_QUEUED ||
                    type == Telephony.Sms.MESSAGE_TYPE_FAILED
                    ) FontStyle.Italic else null,
                fontWeight = weight,
                maxLines = if(isRead) 1 else 3,
            )
        },
        trailingContent = {
            Text(
                text = date,
                color = colorContent,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = weight,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        },
        leadingContent = {
            ThreadConversationsAvatar(
                context,
                id,
                firstName,
                lastName,
                phoneNumber,
                isContact
            )
        }
    )
}

@Composable
fun ModalDrawerSheetLayout(
    callback: ((ThreadsViewModel.InboxType) -> Unit)? = null,
    selectedItemIndex: ThreadsViewModel.InboxType = ThreadsViewModel.InboxType.INBOX,
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
                    Text("0", fontSize = 14.sp)
                },
                selected = selectedItemIndex == ThreadsViewModel.InboxType.INBOX,
                onClick = { callback?.let{ it(ThreadsViewModel.InboxType.INBOX) } }
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
                    Text("0", fontSize = 14.sp)
                },
                selected = selectedItemIndex == ThreadsViewModel.InboxType.ARCHIVED,
                onClick = { callback?.let{ it(ThreadsViewModel.InboxType.ARCHIVED) } }
            )

            HorizontalDivider(Modifier.padding(8.dp))

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
                    Text("0", fontSize = 14.sp)
                },
                selected = selectedItemIndex == ThreadsViewModel.InboxType.DRAFTS,
                onClick = { callback?.let{ it(ThreadsViewModel.InboxType.DRAFTS) } }
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
                },
                selected = selectedItemIndex == ThreadsViewModel.InboxType.MUTED,
                onClick = { callback?.let{ it(ThreadsViewModel.InboxType.MUTED) } }
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
                },
                selected = selectedItemIndex == ThreadsViewModel.InboxType.BLOCKED,
                onClick = { callback?.let{ it(ThreadsViewModel.InboxType.BLOCKED) } }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ThreadsNavMenuItems(
    navController: NavController,
    expanded: Boolean = false,
    threadsViewModel: ThreadsViewModel,
    threadMenuItems: Map<String, () -> Unit>? = null,
    dismissCallback: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    val defaultPermission = rememberPermissionState(Manifest.permission.READ_SMS)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")) { uri ->
        println(uri)
        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                with(context.contentResolver.openFileDescriptor(uri, "w")) {
                    this?.fileDescriptor.let { fd ->
                        val fileOutputStream = FileOutputStream(fd);
                        fileOutputStream.write(context.exportRawWithColumnGuesses()
                            .encodeToByteArray())
                        fileOutputStream.close();
                    }
                    this?.close();

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context,
                            context.getString(R.string.conversations_exported_complete),
                            Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                // TODO: implement
//                val stringBuilder = StringBuilder()
//                context.contentResolver.openInputStream(uri)?.use { inputStream ->
//                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
//                        var line: String? = reader.readLine()
//                        while (line != null) {
//                            stringBuilder.append(line)
//                            line = reader.readLine()
//                        }
//                    }
//                }
//                context.importSmsMessages(context,stringBuilder.toString())
//                CoroutineScope(Dispatchers.Main).launch {
//                    Toast.makeText(context,
//                        context.getString(R.string.conversations_import_complete),
//                        Toast.LENGTH_LONG).show();
//                }
            }
        }
    }


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
                        text=stringResource(R.string.settings_title),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    navController.navigate(SettingsScreenNav)
                }
            )

            if(defaultPermission.status.isGranted || LocalInspectionMode.current) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.conversation_menu_export),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        dismissCallback?.let { it(false) }
                        val filename = "Deku_SMS_All_Backup" + System.currentTimeMillis() + ".json";
                        exportLauncher.launch(filename)
                    }
                )

                if(BuildConfig.DEBUG)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text= stringResource(R.string.conversation_menu_import),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            dismissCallback?.let { it(false) }
                            importLauncher.launch("application/json")
                        }
                    )
            }

            HorizontalDivider()

            threadMenuItems?.let { dropMenuItem ->
                dropMenuItem.forEach { entry ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text= entry.key,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            entry.value()
                            dismissCallback?.let { it(false) }
                        }
                    )
                }
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text(
                        text= stringResource(R.string.reset),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.invoke(false)
                    threadsViewModel.loadNatives(context, true){
                        Toast.makeText(context,
                            context.getString(R.string.reset_complete), Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}

@Composable
fun SwipeToDeleteBackground(
    dismissState: SwipeToDismissBoxState? = null,
    inArchive: Boolean = false
) {
    var arrangement = Arrangement.End
    val color = when(dismissState?.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            arrangement = Arrangement.Start
            MaterialTheme.colorScheme.error
        }
        SwipeToDismissBoxValue.EndToStart -> {
            MaterialTheme.colorScheme.primary
        }
        SwipeToDismissBoxValue.Settled -> Color.Transparent
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = arrangement
    ) {
        Icon(
            when(dismissState?.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                else -> {
                    when {
                        inArchive -> Icons.Default.Unarchive
                        else -> Icons.Default.Archive
                    }
                }
            },
            tint = MaterialTheme.colorScheme.onPrimary,
            contentDescription = stringResource(R.string.messages_threads_menu_archive)
        )
    }
}

@Preview(showBackground = true, name = "Delete Confirmation Light")
@Preview(showBackground = true, name = "Delete Confirmation Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DeleteConfirmationAlertPreview() {
    DeleteConfirmationAlert(
        confirmCallback = {},
        dismissCallback = {}
    )
}

@Preview(showBackground = true, name = "Import Details Light")
@Preview(showBackground = true, name = "Import Details Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ImportDetailsPreview() {
    ImportDetails(
        numOfConversations = 150,
        numOfThreads = 25,
        confirmCallback = {},
        resetConfirmCallback = {},
        dismissCallback = {}
    )
}

@Preview(showBackground = true, name = "Modal Drawer Light")
@Preview(showBackground = true, name = "Modal Drawer Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ModalDrawerSheetLayoutPreview() {
    ModalDrawerSheetLayout(
        selectedItemIndex = ThreadsViewModel.InboxType.INBOX,
    )
}

@Preview(name = "MainMenu Light")
@Preview(name = "MainMenu Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainMenuDropDown_Preview() {
    ThreadsNavMenuItems(
        navController = rememberNavController(),
        expanded = true,
        threadsViewModel = remember { ThreadsViewModel() }
    )
}

@Preview
@Composable
fun ThreadConversationCard_Preview() {
    ThreadConversationCard(
        phoneNumber = "+237652156811",
        id = 1,
        firstName = "Jane",
        lastName = "Doe",
        content = "Hello world",
        date = "today",
        isRead = false,
        isContact = false,
        unreadCount = 0,
        modifier = Modifier,
        isSelected = false,
        isMuted = false,
        isBlocked = false,
        type = 0
    )
}


@Composable
fun GetSwipeBehaviour(
    thread: Threads,
    inboxType: ThreadsViewModel.InboxType
): SwipeToDismissBoxState {
    return rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when(it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    TODO()
                    return@rememberSwipeToDismissBoxState false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    TODO()
                    when(inboxType) {
                        ThreadsViewModel.InboxType.ARCHIVED -> TODO()
                        else -> TODO()
                    }
                    return@rememberSwipeToDismissBoxState true
                }
                SwipeToDismissBoxValue.Settled ->
                    return@rememberSwipeToDismissBoxState false
            }
        },
        positionalThreshold = { it * .85f }
    )

}



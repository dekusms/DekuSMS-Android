package com.afkanerd.deku.DefaultSMS.ui.Components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.util.TableInfo
import coil3.compose.AsyncImage
import com.afkanerd.deku.DefaultSMS.AboutActivity
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Extensions.toHslColor
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.Models.ThreadsCount
import com.afkanerd.deku.DefaultSMS.SettingsActivity
import com.afkanerd.deku.DefaultSMS.ui.InboxType
import com.afkanerd.deku.DefaultSMS.ui.ThreadConversationLayout
import com.afkanerd.deku.RemoteListenersScreen
import com.afkanerd.deku.Router.GatewayServers.GatewayServerRoutedActivity
import com.example.compose.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.file.WatchEvent

@Preview
@Composable
fun DeleteConfirmationAlert(
    confirmCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null,
) {
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.secondary,
        title = {
            Text(
                stringResource(R.string.messages_thread_delete_confirmation_title),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.messages_thread_delete_confirmation_text),
                    color = MaterialTheme.colorScheme.onSecondary,
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
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dismissCallback?.invoke() }
            ) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    )
}

@Preview
@Composable
fun ImportDetails(
    numOfConversations: Int = 0,
    numOfThreads: Int = 0,
    confirmCallback: (() -> Unit)? = null,
    resetConfirmCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null,
) {
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.secondary,
        title = {
            Text(
                stringResource(R.string.import_conversations),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.threads) + numOfThreads,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    stringResource(R.string.conversations) + numOfConversations,
                    color = MaterialTheme.colorScheme.onSecondary,
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
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }

            TextButton(
                onClick = { confirmCallback?.invoke() }
            ) {
                Text(
                    stringResource(R.string.conversation_menu_import),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dismissCallback?.invoke() }
            ) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    )
}

@Composable
private fun ThreadConversationsAvatar(
    context: Context,
    id: String,
    firstName: String,
    lastName: String,
    phoneNumber: String,
    isContact: Boolean = true
) {

    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        if (isContact) {
            val contactPhotoUri = remember(phoneNumber) {
                Contacts.retrieveContactPhoto(context, phoneNumber)
            }
            if (contactPhotoUri.isNotEmpty() && contactPhotoUri != "null") {
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
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
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

@Preview(showBackground = true)
@Composable
fun ThreadConversationCard(
    phoneNumber: String = "0612345678",
    id: String = "id",
    firstName: String = "Jane",
    lastName: String = "",
    content: String = "Text Template",
    date: String = "Tues",
    isRead: Boolean = false,
    isContact: Boolean = false,
    unreadCount: Int = 0,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isMuted: Boolean = LocalInspectionMode.current,
    isBlocked: Boolean = LocalInspectionMode.current,
    type: Int? = if(LocalInspectionMode.current)
        Telephony.Sms.MESSAGE_TYPE_FAILED else null
) {
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
                    Icon(Icons.AutoMirrored.Default.VolumeOff, stringResource(R.string.thread_muted))

                if(isBlocked)
                    Icon(Icons.Filled.Block, stringResource(R.string.contact_is_blocked))
            }
        },
        supportingContent = {
            Text(
                text = when(type) {
                    Telephony.Sms.MESSAGE_TYPE_DRAFT ->
                        stringResource(R.string.thread_conversation_type_draft) + ": $content"
                    Telephony.Sms.MESSAGE_TYPE_OUTBOX ->
                        stringResource(R.string.sms_status_sending)+ ": $content"
                    Telephony.Sms.MESSAGE_TYPE_FAILED ->
                        stringResource(R.string.sms_status_failed_only)+ ": $content"
                    else -> content
                },
                color = colorContent,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = if(
                    type == Telephony.Sms.MESSAGE_TYPE_DRAFT ||
                    type == Telephony.Sms.MESSAGE_TYPE_OUTBOX ||
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
                LocalContext.current,
                id,
                firstName,
                lastName,
                phoneNumber,
                isContact
            )
        }
    )
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
                },
                selected = selectedItemIndex == InboxType.BLOCKED,
                onClick = { callback?.let{ it(InboxType.BLOCKED) } }
            )

            HorizontalDivider(Modifier.padding(8.dp))

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.CloudSync,
                        contentDescription = stringResource(R.string.remote_listeners)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.remote_listeners),
                        fontSize = 14.sp
                    )
                },
                badge = {
                },
                selected = selectedItemIndex == InboxType.REMOTE_LISTENER,
                onClick = { callback?.let{ it(InboxType.REMOTE_LISTENER) } }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ThreadsMainDropDown(
    expanded: Boolean = false,
    conversationViewModel: ConversationsViewModel = ConversationsViewModel(),
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
                        fileOutputStream.write(conversationViewModel
                            .getAllExport(context).encodeToByteArray());
                        // Let the document provider know you're done by closing the stream.
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
        println(uri)
        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val stringBuilder = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                }
                conversationViewModel.importDetails = stringBuilder.toString()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context,
                        context.getString(R.string.conversations_import_complete),
                        Toast.LENGTH_LONG).show();
                }
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
                    context.startActivity(
                        Intent(context, SettingsActivity::class.java).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        }
                    )
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

@Preview
@Composable
fun MainMenuDropDown_Preview() {
    AppTheme(darkTheme = true) {
        ThreadsMainDropDown(
            true,
            ConversationsViewModel()
        )
    }
}

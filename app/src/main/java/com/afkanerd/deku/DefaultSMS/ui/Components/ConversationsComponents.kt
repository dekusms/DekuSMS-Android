package com.afkanerd.deku.DefaultSMS.ui.Components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Base64
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import coil.request.ImageRequest
import coil3.compose.AsyncImage
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.ui.Conversations
import com.afkanerd.deku.MainActivity
import com.example.compose.AppTheme
import com.jakewharton.rxbinding.view.RxMenuItem.icon
import io.getstream.avatarview.AvatarView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@Composable
fun SearchCounterCompose(
    index: String = "0",
    total: String = "10",
    forwardClick: (() -> Unit)? = null,
    backwardClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$index/$total ${stringResource(R.string.results_found)}",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.weight(4f))
            IconButton(onClick = {
                forwardClick?.let{ it() }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
                    contentDescription = stringResource(R.string.move_search_backwards),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(onClick = {
                backwardClick?.let{ it() }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                    contentDescription = stringResource(R.string.move_search_forwards),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBarText(
    searchQuery: String = "",
    cancelCallback: (() -> Unit)? = null,
    searchCallback: ((String) -> Unit)? = null,
) {
    var searchQuery by remember { mutableStateOf(searchQuery) }
    val interactionsSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = searchQuery,
        onValueChange = {
            searchQuery = it
            if(it.isEmpty())
                cancelCallback?.let{ it() }
            else searchCallback?.let{ it(searchQuery)}
        },
        maxLines = 7,
        singleLine = false,
        textStyle = TextStyle(color= MaterialTheme.colorScheme.onBackground),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        modifier = Modifier.fillMaxWidth()
    ) {
        TextFieldDefaults.DecorationBox(
            value = searchQuery,
            visualTransformation = VisualTransformation.None,
            innerTextField = it,
            singleLine = false,
            enabled = true,
            interactionSource = interactionsSource,
            trailingIcon = {
                IconButton(onClick = {
                    searchQuery = ""
                    cancelCallback?.let{ it() }
                }) {
                    Icon(Icons.Default.Close, stringResource(R.string.cancel_search))
                }
            },
            placeholder = {
                Text(
                    text= stringResource(R.string.text_message),
                    color = MaterialTheme.colorScheme.outline
                )
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


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, )
@Composable
fun ChatCompose(
    value: String = "",
    encryptedValue: String = if(LocalInspectionMode.current)
        Base64.encodeToString(LoremIpsum().values.first().encodeToByteArray(), Base64.DEFAULT)
    else "",
    valueChanged: ((String) -> Unit)? = null,
    subscriptionId: Int = -1,
    shouldPulse: Boolean = LocalInspectionMode.current,
    simCardChooserCallback: (() -> Unit)? = null,
    sentCallback: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current
    val interactionsSource = remember { MutableInteractionSource() }

    var mmsImageUri: Uri? by remember { mutableStateOf(null) }

    val imagePicker = mmsImagePicker { uri ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flag)
        Log.d("PhotoPicker", "Selected URI: $uri")
        mmsImageUri = uri
    }

    Column(
        modifier = Modifier
            .imePadding()
            .height(IntrinsicSize.Min)
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(24.dp, 24.dp, 24.dp, 24.dp))
            .background(MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom
            ) {
                IconButton(onClick = {
                    imagePicker.launch(PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(
                        Icons.Outlined.PhotoLibrary,
                        stringResource(R.string.send_mms_photo),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Column(modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f)
                .fillMaxSize()
            ) {
                if(shouldPulse)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                if(encryptedValue.isNotBlank() || LocalInspectionMode.current) {
                    Text(
                        encryptedValue,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        maxLines = 4,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Divider()
                }
                
                if(mmsImageUri != null || LocalInspectionMode.current) {
                    ComposeMmsImage(mmsImageUri)
                }

                Column(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = {
                            valueChanged?.invoke(it)
                        },
                        maxLines = 7,
                        singleLine = false,
                        textStyle = TextStyle(color= MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    ) {
                        TextFieldDefaults.DecorationBox(
                            value = value,
                            visualTransformation = VisualTransformation.None,
                            innerTextField = it,
                            singleLine = false,
                            enabled = true,
                            interactionSource = interactionsSource,
                            placeholder = {
                                Text(
                                    text= stringResource(R.string.text_message),
                                    color = MaterialTheme.colorScheme.outline
                                )
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
            }

            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row {
                    simCardChooserCallback?.let {
                        IconButton(
                            onClick = { it() },
                        ) {
                            if(LocalInspectionMode.current) {
                                Icon(
                                    Icons.Outlined.SimCard,
                                    stringResource(R.string.send_message),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            } else {
                                if(subscriptionId > -1) {
                                    val iconBitmap = SIMHandler.getSubscriptionBitmap(context, subscriptionId)
                                        .asImageBitmap()
                                    Image(iconBitmap, stringResource(R.string.choose_sim_card))
                                }
                            }
                        }

                    }

                    if(value.isNotBlank() || LocalInspectionMode.current) {
                        IconButton(
                            onClick = { sentCallback?.invoke() },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Default.Send,
                                stringResource(R.string.send_message),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
//                .padding(top = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Column {
                if(encryptedValue.isNotBlank() || value.isNotBlank() || inPreviewMode) {
                    val length = if(inPreviewMode) "10/140"
                    else getSMSCount(context, encryptedValue.ifBlank { value })
                    Text(
                        length,
                        color= MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

            }
        }
    }
}

fun getSMSCount(context: Context, text: String): String {
    val smsManager = context.getSystemService(SmsManager::class.java)
    val messages: List<String> = smsManager.divideMessage(text)
    val segmentCount = messages[messages.size - 1].length
    return segmentCount.toString() + "/" + messages.size
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailedMessageOptionsModal(
    retryCallback: (() -> Unit)? = null,
    deleteCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val state = rememberStandardBottomSheetState(
        initialValue = if(LocalInspectionMode.current) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false
    )

    ModalBottomSheet(
        onDismissRequest = { dismissCallback?.invoke() },
        sheetState = state,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            TextButton(
                onClick = {
                    retryCallback?.invoke()
                    scope
                        .launch{
                            state.hide()
                        }
                        .invokeOnCompletion {
                            dismissCallback?.invoke()
                        }
                },
                modifier = Modifier.align(Alignment.Start),
            ) {
                Icon(
                    Icons.AutoMirrored.Default.Send,
                    stringResource(R.string.resend_message),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end=8.dp),
                )

                Text(stringResource(R.string.resend_message))
            }

            TextButton(
                onClick = {
                    deleteCallback?.invoke()
                    scope
                        .launch{
                            state.hide()
                        }
                        .invokeOnCompletion {
                            dismissCallback?.invoke()
                        }
                },
                modifier = Modifier.align(Alignment.Start),
            ) {
                Icon(
                    Icons.Filled.Delete,
                    stringResource(R.string.delete_message),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end=8.dp),
                )

                Text(stringResource(R.string.delete_message1))
            }
        }
    }
}

@Composable
fun ShortCodeAlert(
    dismissCallback: (() -> Unit)? = null
) {
    val context = LocalContext.current
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.primary,
        text = {
            Text(
                context.getString(R.string.conversation_shortcode_learn_more_text),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        onDismissRequest = { dismissCallback?.invoke() },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = { dismissCallback?.invoke() }
            ) {
                Text(
                    context.getString(R.string.conversation_shortcode_learn_more_ok),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Composable
fun MessageInfoAlert(
    conversation: Conversation,
    dismissCallback: (() -> Unit)? = null,
) {
    val type = stringResource(R.string.text_message_1)
    val priority = stringResource(R.string.normal)
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.primary,
        title = {
            Text(
                stringResource(R.string.message_details),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.type, type),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    stringResource(R.string.priority, priority),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if(conversation.type == Telephony.Sms.MESSAGE_TYPE_INBOX)
                    Text(
                        stringResource(R.string.from, conversation.address ?: ""),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                Text(
                    stringResource(
                        R.string.to, if (conversation.type == Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                            conversation.address ?: ""
                        else "N/A"
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    stringResource(
                        R.string.sent, if (conversation.type == Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                            formatDate(conversation.date?.toLong() ?: 0L)
                        else formatDate(conversation.date_sent?.toLong() ?: 0L)
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if(conversation.type == Telephony.Sms.MESSAGE_TYPE_INBOX)
                    Text(
                        stringResource(
                            R.string.received,
                            formatDate(conversation.date?.toLong() ?: 0L)
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
            }
        },
        onDismissRequest = { dismissCallback?.invoke() },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = { dismissCallback?.invoke() }
            ) {
                Text(
                    stringResource(R.string.close),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:ss", Locale.getDefault())
    return format.format(date)
}


@Composable
fun SimChooser(
    expanded: Boolean = LocalInspectionMode.current,
    onClickCallback: ((Int) -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.BottomCenter)
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { dismissCallback?.invoke() },
        ) {
            if(!LocalInspectionMode.current)
            SIMHandler.getSimCardInformation(context).forEach {
                DropdownMenuItem(
                    leadingIcon = {
                        if(LocalInspectionMode.current) {
                            Icon(
                                Icons.Outlined.SimCard,
                                stringResource(R.string.send_message),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                            val iconBitmap = SIMHandler
                                .getSubscriptionBitmap(context, it.subscriptionId)
                                .asImageBitmap()
                            Image(iconBitmap, stringResource(R.string.choose_sim_card))
                        }
                    },
                    text = {
                        Text(
                            text = it.carrierName.toString(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        onClickCallback?.invoke(it.subscriptionId)
                        dismissCallback?.invoke()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun ConversationCrudBottomBar(
    viewModel: ConversationsViewModel = ConversationsViewModel(),
    items: List<Conversation> = emptyList(),
    onInfoRequested: (Conversation) -> Unit = {},
    onCompleted: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    BottomAppBar (
        actions = {
            Row {
                IconButton(onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        onCancel?.let { it() }
                    }
                }) {
                    Icon(Icons.Default.Close, stringResource(R.string.cancel_selected_messages))
                }

                Text(
                    viewModel.selectedItems.size.toString(),
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(Modifier.weight(1f))

                if(viewModel.selectedItems.size < 2) {
                    IconButton(onClick = {
                        val conversations = items.first {
                            it.message_id in viewModel.selectedItems
                        }
                        onInfoRequested(conversations)
                    }) {
                        Icon(Icons.Filled.Info, stringResource(R.string.message_information))
                    }

                    IconButton(onClick = {
                        val conversation = items.firstOrNull {
                            it.message_id in viewModel.selectedItems
                        }
                        copyItem(context, conversation?.text!!)
                        onCompleted?.invoke()
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
                        val conversation = items.firstOrNull {
                            it.message_id in viewModel.selectedItems
                        }
                        shareItem(context, conversation?.text!!)
                        onCompleted?.let { it() }
                    }) {
                        Icon(Icons.Filled.Share, stringResource(R.string.share_message))
                    }
                }

                IconButton(onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        val conversations = items.filter {
                            it.message_id in viewModel.selectedItems
                        }
                        viewModel.delete(context, conversations)
                        onCompleted?.let { it() }
                    }
                }) {
                    Icon(Icons.Filled.Delete, stringResource(R.string.delete_message))
                }
            }

        }
    )
}

private fun copyItem(context: Context, text: String) {
    val clip = ClipData.newPlainText(text, text)
    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(clip)

    Toast.makeText(
        context, context.getString(R.string.conversation_copied),
        Toast.LENGTH_SHORT
    ).show()
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
            MainActivity::class.java.name
        )
    )
    shareIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponentNames)
    context.startActivity(shareIntent)
}

@Preview(showBackground = true, name = "Search Counter Light")
@Preview(showBackground = true, name = "Search Counter Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchCounterComposePreview() {
    AppTheme {
        SearchCounterCompose(
            index = "5",
            total = "20",
            forwardClick = {},
            backwardClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Search TopAppBar Light")
@Preview(showBackground = true, name = "Search TopAppBar Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchTopAppBarTextPreview() {
    AppTheme {
        SearchTopAppBarText(
            searchQuery = "Sample search",
            cancelCallback = {},
            searchCallback = {}
        )
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Chat Compose Light")
@Preview(showBackground = true, name = "Chat Compose Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChatComposePreview() {
    AppTheme {
        ChatCompose(
            value = "Hello there!",
            encryptedValue = "U2FsdGVkX1+...", // Example encrypted
            shouldPulse = true,
            sentCallback = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Failed Message Modal Light")
@Preview(showBackground = true, name = "Failed Message Modal Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FailedMessageOptionsModalPreview() {
    AppTheme {
        // For ModalBottomSheet, it's good to have a visible state in preview
        FailedMessageOptionsModal(
            retryCallback = {},
            deleteCallback = {},
            dismissCallback = {}
        )
    }
}

@Preview(showBackground = true, name = "Short Code Alert Light")
@Preview(showBackground = true, name = "Short Code Alert Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ShortCodeAlertPreview() {
    AppTheme {
        ShortCodeAlert(dismissCallback = {})
    }
}

@Preview(showBackground = true, name = "SIM Chooser Light")
@Preview(showBackground = true, name = "SIM Chooser Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SimChooserPreview() {
    AppTheme {
        SimChooser(
            expanded = true, // Make it visible in preview
            onClickCallback = {},
            dismissCallback = {}
        )
    }
}

@Preview(showBackground = true, name = "SIM Chooser Light")
@Preview(showBackground = true, name = "SIM Chooser Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessageInfoAlert_Preview() {
    AppTheme {
        val conversation = Conversation()
        conversation.address = "+2371234567"
        conversation.date = System.currentTimeMillis().toString()
        conversation.date_sent = System.currentTimeMillis().toString()
        conversation.type = Telephony.Sms.MESSAGE_TYPE_INBOX
        MessageInfoAlert(
            conversation
        ) {}
    }
}

@Composable
fun mmsImagePicker(
    callback: (Uri) -> Unit
): ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> {
    // Registers a photo picker activity launcher in single-select mode.
    return rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            callback(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
}

@Composable
fun ComposeMmsImage(uri: Uri?) {
    val size = 100.dp;
    Column {
        if(LocalInspectionMode.current) {
            Image(
                painter = painterResource(R.drawable.github_mark),
                contentDescription = stringResource(R.string.mms_selected_image),
                modifier = Modifier
                    .padding(4.dp)
                    .size(size)
                    .clip(RoundedCornerShape(24.dp, 24.dp, 24.dp, 24.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        else {
            println("Rendering MMS: $uri")
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.mms_selected_image),
                modifier = Modifier
                    .padding(4.dp)
                    .size(size)
                    .clip(RoundedCornerShape(24.dp, 24.dp, 24.dp, 24.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

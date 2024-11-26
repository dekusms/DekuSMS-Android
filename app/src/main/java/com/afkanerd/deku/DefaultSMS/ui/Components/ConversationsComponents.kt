package com.afkanerd.deku.DefaultSMS.ui.Components

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.R
import com.jakewharton.rxbinding.view.RxMenuItem.icon
import kotlinx.coroutines.launch

@Preview(showBackground = true)
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
            fontSize = 18.sp,
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
                    contentDescription = stringResource(R.string.move_search_backwards)
                )
            }

            IconButton(onClick = {
                backwardClick?.let{ it() }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                    contentDescription = stringResource(R.string.move_search_forwards)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
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


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ChatCompose(
    value: String = "",
    valueChanged: ((String) -> Unit)? = null,
    subscriptionId: Int = -1,
    sentCallback: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val interactionsSource = remember { MutableInteractionSource() }

    Row(modifier = Modifier
        .height(IntrinsicSize.Min)
        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
        .clip(RoundedCornerShape(24.dp, 24.dp, 24.dp, 24.dp))
        .background(MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .weight(1f)
            .fillMaxSize()) {
            BasicTextField(
                value = value,
                onValueChange = { valueChanged?.invoke(it) },
                maxLines = 7,
                singleLine = false,
                textStyle = TextStyle(color= MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth()
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

        Column(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row {
                IconButton(onClick = { TODO() },
                    enabled = true,
                ) {
                    if(subscriptionId == -1) {
                        Icon(
                            Icons.Outlined.SimCard,
                            stringResource(R.string.send_message),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        val iconBitmap = SIMHandler.getSubscriptionBitmap(context, subscriptionId)
                            .asImageBitmap()
                        Image(iconBitmap, stringResource(R.string.choose_sim_card))
                    }
                }

                IconButton(onClick = { sentCallback?.invoke() },
                    enabled = value.isNotBlank(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FailedMessageOptionsModal(
    retryCallback: (() -> Unit)? = null,
    deleteCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null
) {
//    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state = rememberStandardBottomSheetState(
        initialValue = if(BuildConfig.DEBUG) SheetValue.Expanded else SheetValue.Hidden,
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

@Preview
@Composable
fun ShortCodeAlert(
    dismissCallback: (() -> Unit)? = null
) {
    val context = LocalContext.current
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.secondary,
        text = {
            Text(
                context.getString(R.string.conversation_shortcode_learn_more_text),
                color = MaterialTheme.colorScheme.onSecondary
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
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    )
}
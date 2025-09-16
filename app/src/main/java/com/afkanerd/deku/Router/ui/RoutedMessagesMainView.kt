package com.afkanerd.deku.Router.ui

import android.content.Context
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.GatewayClientsListScreen
import com.afkanerd.deku.Router.Models.RouterHandler
import com.afkanerd.deku.Router.ui.viewModels.GatewayServerViewModel
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.DateTimeUtils
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.retrieveContactName
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.ConversationsCard
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.ThreadConversationCard
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.SearchScreenNav
import com.example.compose.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutedMessagesMainView(
    navController: NavController,
    viewModel: GatewayServerViewModel,
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current
    val routedMessages by viewModel.workFlowItems.collectAsState(emptyList())

    viewModel.getActiveWorkManagerItems(context)

    val isDefault by remember { mutableStateOf(inPreviewMode || context.isDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                title = {Text(stringResource(R.string.routed_messages))},
                actions = {
                    IconButton(onClick = {
                        navController.navigate(GatewayClientsListScreen)
                    }) {
                        Icon(Icons.AutoMirrored.Default.List,
                            stringResource(R.string.list_gateway_clients)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            LazyColumn {
                items(
                    items = routedMessages,
                    key =  { it.id }
                ) { conversation ->
                    var firstName by remember { mutableStateOf(conversation.sms?.address) }
                    var lastName by remember { mutableStateOf("") }
                    val address = conversation.sms?.address!!

                    val contactName = if(context.isDefault())
                        context.retrieveContactName(address)
                    else address

                    if (!contactName.isNullOrEmpty()) {
                        contactName.split(" ").let {
                            firstName = it[0]
                            if (it.size > 1)
                                lastName = it[1]
                        }
                    }

                    val date = if(!inPreviewMode) DateTimeUtils.formatDate(
                        context,
                        conversation.sms?.date!!
                    ) ?: "" else "Tues"

                    ThreadConversationCard(
                        id = conversation.sms?.thread_id!!,
                        phoneNumber = conversation.sms?.address!!,
                        firstName = firstName!!,
                        lastName = lastName,
                        content = conversation.sms?.body!!,
                        date = date,
                        isRead = true, // TODO: get actual later
                        isContact = isDefault && !contactName.isNullOrBlank(),
                        unreadCount = 0,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {}
                        ),
                        isSelected = false,
                        isMuted = false,
                        isBlocked = false,
                        type = conversation.sms?.type!!,
                        mms = conversation.mms?.thread_id != null,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RoutedMessagesMainViewPreview() {
    AppTheme {
        RoutedMessagesMainView(
            rememberNavController(),
            remember{ GatewayServerViewModel() }
        )
    }
}

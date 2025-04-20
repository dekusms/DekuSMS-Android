package com.afkanerd.deku.RemoteListeners.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.HomeScreen
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenerQueuesViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListeners
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionHandler
import com.afkanerd.deku.RemoteListeners.components.NotificationPermissionComposable
import com.afkanerd.deku.RemoteListeners.components.PhoneStatePermissionComposable
import com.afkanerd.deku.RemoteListeners.components.RemoteListenerCards
import com.afkanerd.deku.RemoteListeners.components.SMSPermissionComposable
import com.afkanerd.deku.RemoteListeners.modals.RemoteListenerModal
import com.afkanerd.deku.RemoteListeners.modals.RemoteListenerSMSPermissionsModal
import com.afkanerd.deku.RemoteListenersAddScreen
import com.afkanerd.deku.RemoteListenersQueuesScreen
import com.example.compose.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val requiredSendSMSPermission = Manifest.permission.SEND_SMS
//const val requiredReceiveSMSPermission = Manifest.permission.RECEIVE_SMS
const val requiredReceiveSMSPermission = Manifest.permission.READ_SMS

const val requiredNotificationsPermissions = Manifest.permission.POST_NOTIFICATIONS
const val requiredReadPhoneStatePermissions = Manifest.permission.READ_PHONE_STATE

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun RMQMainComposable(
    _remoteListeners: List<RemoteListeners> = emptyList<RemoteListeners>(),
    remoteListenerViewModel: RemoteListenersViewModel,
    remoteListenerQueuesViewModel: RemoteListenerQueuesViewModel,
    conversationsViewModel: ConversationsViewModel,
    navController: NavController,
) {
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val remoteListeners: List<RemoteListeners> = if(LocalInspectionMode.current) _remoteListeners
    else remoteListenerViewModel.get(context).observeAsState(emptyList()).value

    val rmqConnectionHandlers: List<RMQConnectionHandler> =
        if(LocalInspectionMode.current) emptyList<RMQConnectionHandler>()
    else remoteListenerViewModel.getRmqConnections().observeAsState(emptyList()).value

    var showRemoteListenerModal by remember { mutableStateOf(false) }

    val smsReadSMSState = rememberPermissionState(requiredReceiveSMSPermission)
    val smsSendSMSState = rememberPermissionState(requiredSendSMSPermission)

    val notificationPermission = rememberPermissionState(requiredNotificationsPermissions)
    val readPhoneStatePermission = rememberPermissionState(requiredReadPhoneStatePermissions)

    val getDefaultPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context)
                sharedPreferences.edit() {
                    putBoolean(context.getString(R.string.configs_load_natives), true)
                }
                CoroutineScope(Dispatchers.Default).launch {
                    conversationsViewModel.reset(context)
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Messages loaded!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    var showPermissionModal by remember { mutableStateOf(false) }

    LaunchedEffect(remoteListeners) {
        if(remoteListeners.isNotEmpty() &&
            (!smsReadSMSState.status.isGranted || !smsSendSMSState.status.isGranted)) {
            showPermissionModal = true
        }
    }

    BackHandler {
        remoteListenerViewModel.remoteListener = null
        if(!navController.popBackStack(HomeScreen, false)) {
            navController.navigate(HomeScreen) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.remote_listeners)) },
                navigationIcon = {
                    IconButton(onClick = {
                        remoteListenerViewModel.remoteListener = null
                        if(!navController.popBackStack(HomeScreen, false)) {
                            navController.navigate(HomeScreen) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.return_back)
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                remoteListenerViewModel.remoteListener = null
                navController.navigate(RemoteListenersAddScreen)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_remote_listener)
                )
            }
        },
        modifier = Modifier
            .nestedScroll(scrollBehaviour.nestedScrollConnection),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(8.dp)
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(!notificationPermission.status.isGranted || LocalInspectionMode.current) {
                    NotificationPermissionComposable()
                }

                if(remoteListeners.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            stringResource(R.string.no_remote_listeners),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                else {
                    if(!readPhoneStatePermission.status.isGranted || LocalInspectionMode.current) {
                        PhoneStatePermissionComposable()
                    }

                    if((!smsSendSMSState.status.isGranted || !smsReadSMSState.status.isGranted) ||
                        LocalInspectionMode.current) {
                        SMSPermissionComposable()
                    }

                    Spacer(Modifier.padding(8.dp))
                    Text(
                        stringResource(R.string.only_1_connection_at_a_time_due_to_the_bottleneck_between_channels_and_phone_radios_ability_to_sms_messages_in_parallel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        "${stringResource(R.string.click_to_add_queues)}   " +
                                stringResource(R.string.press_and_hold_to_manage),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )

                    Spacer(Modifier.padding(4.dp))

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = remoteListeners,
                            key = { _, remoteListener -> remoteListener.id}
                        ) { _, remoteListener ->
                            RemoteListenerCards(
                                remoteListener,
                                if(LocalInspectionMode.current) true else
                                rmqConnectionHandlers.find{ remoteListener.id == it.id}
                                    ?.connection?.isOpen == true,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        remoteListenerViewModel.remoteListener = remoteListener
                                        navController.navigate(RemoteListenersQueuesScreen)
                                    },
                                    onLongClick = {
                                        remoteListenerViewModel.remoteListener = remoteListener
                                        showRemoteListenerModal = true
                                    }
                                ),
                            )
                        }
                    }
                }
            }

            if(showPermissionModal) {
                RemoteListenerSMSPermissionsModal(
                    showModal = showPermissionModal,
                    makeDefaultCallback = {
                        getDefaultPermissionLauncher.launch(makeDefault(context))
                        showPermissionModal = false
                    },
                ) {
                    showPermissionModal = false
                }
            }

            if(showRemoteListenerModal) {
                val activated = remoteListenerViewModel.remoteListener?.activated == true
                RemoteListenerModal(
                    showModal = showRemoteListenerModal,
                    activated = activated,
                    editCallback = {
                        showRemoteListenerModal = false
                        navController.navigate(RemoteListenersAddScreen)
                    },
                    connectionCallback = {
                        if(activated) {
                            //Deactivating
                            remoteListenerViewModel.remoteListener?.activated = false
                            RemoteListenersHandler.stopListening(
                                context,
                                remoteListenerViewModel.remoteListener!!
                            )
                        }
                        else {
                            //Activating
                            if(!smsReadSMSState.status.isGranted ||
                                !smsSendSMSState.status.isGranted) {
                                showPermissionModal = true
                            } else {
                                CoroutineScope(Dispatchers.Default).launch {
                                    if(remoteListenerQueuesViewModel
                                            .getList(
                                                context,
                                                remoteListenerViewModel.remoteListener?.id!!
                                            ).isEmpty()) {

                                        launch(Dispatchers.Main) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Add queues to activate...",
                                                    Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    else {
                                        remoteListenerViewModel.remoteListener
                                            ?.activated = true
                                        launch(Dispatchers.Main) {
                                            RemoteListenersHandler.startListening(
                                                context,
                                                remoteListenerViewModel.remoteListener!!
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        showRemoteListenerModal = false
                    },
                    deleteCallback = {
                        CoroutineScope(Dispatchers.Default).launch {
                            remoteListenerQueuesViewModel.delete(
                                context,
                                remoteListenerViewModel.remoteListener!!.id
                            )
                            remoteListenerViewModel.delete(
                                remoteListenerViewModel.remoteListener!!
                            )
                            showRemoteListenerModal = false
                        }
                    }
                ) {
                    showRemoteListenerModal = false
                }
            }
        }
    }
}



fun makeDefault(context: Context): Intent {
    // TODO: replace this with checking other permissions - since this gives null in level 35
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(ROLE_SERVICE) as RoleManager
        roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
    } else {
        Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
    }
}

fun openNotificationSettings(context: Context) {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // For Android 8.0 and higher
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // For Android 5.0 to 7.1
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
        intent.putExtra("app_package", context.packageName)
        intent.putExtra("app_uid", context.applicationInfo.uid)
    }
    context.startActivity(intent)
}

@Composable
@Preview
fun ConnectionCards_Preview() {
    AppTheme {
        val remoteListeners = RemoteListeners()
        remoteListeners.id = 0
        remoteListeners.hostUrl = "amqp://example.com"
        remoteListeners.virtualHost = "/"
        remoteListeners.port = 5671
        remoteListeners.username = "example_user"

        val remoteListeners1 = RemoteListeners()
        remoteListeners1.id = 1
        remoteListeners1.hostUrl = "amqp://example.com"
        remoteListeners1.virtualHost = "/"
        remoteListeners1.port = 5671
        remoteListeners1.username = "example_user"

        RMQMainComposable(
            listOf(remoteListeners, remoteListeners1),
            RemoteListenersViewModel(),
            RemoteListenerQueuesViewModel(),
            ConversationsViewModel(),
            rememberNavController(),
        )
    }
}

@Composable
@Preview
fun ConnectionEmptyCards_Preview() {
    AppTheme {
        RMQMainComposable(
            emptyList(),
            RemoteListenersViewModel(),
            RemoteListenerQueuesViewModel(),
            ConversationsViewModel(),
            rememberNavController(),
        )
    }
}

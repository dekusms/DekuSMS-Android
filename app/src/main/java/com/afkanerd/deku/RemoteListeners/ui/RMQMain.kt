package com.afkanerd.deku.RemoteListeners.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.RemoteListeners.Models.RemoteListeners
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.components.RemoteListenerCards
import com.example.compose.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenerQueuesViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionHandler
import com.afkanerd.deku.RemoteListeners.modals.RemoteListenerModal
import com.afkanerd.deku.RemoteListeners.modals.RemoteListenerSMSPermissionsModal
import com.afkanerd.deku.RemoteListenersAddScreen
import com.afkanerd.deku.RemoteListenersQueuesScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    val requiredSMSPermissions = Manifest.permission.SEND_SMS
    val requiredNotificationsPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
        } else {
            Manifest.permission.FOREGROUND_SERVICE
        }

    val context = LocalContext.current
    val activity = LocalActivity.current

    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val remoteListeners: List<RemoteListeners> = if(LocalInspectionMode.current) _remoteListeners
    else remoteListenerViewModel.get(context).observeAsState(emptyList()).value

    val rmqConnectionHandlers: List<RMQConnectionHandler> =
        if(LocalInspectionMode.current) emptyList<RMQConnectionHandler>()
    else remoteListenerViewModel.getRmqConnections().observeAsState(emptyList()).value

    var showRemoteListenerModal by remember { mutableStateOf(false) }

    val getNotificationsPermissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            if(isGranted) {
                Toast.makeText(context, "Well done!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "We cannot do without this one...", Toast.LENGTH_LONG)
                    .show()
            }
        }

    val getSMSPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
            if(isGranted) {
                Toast.makeText(context, "Well done!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "I'd be back!", Toast.LENGTH_LONG).show()
            }
        }

    val getDefaultPermission =
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

    val smsPermissions = rememberPermissionState(requiredSMSPermissions)
    var showPermissionModal by remember { mutableStateOf(!smsPermissions.status.isGranted) }

    val notificationPermission = rememberPermissionState(requiredNotificationsPermissions)

    BackHandler {
        remoteListenerViewModel.remoteListener = null
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.remote_listeners)) },
                navigationIcon = {
                    IconButton(onClick = {
                        remoteListenerViewModel.remoteListener = null
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.return_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        remoteListenerViewModel.remoteListener = null
                        navController.navigate(RemoteListenersAddScreen)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircleOutline,
                            contentDescription = stringResource(R.string.new_remote_listener)
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
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
            Column {
                if( remoteListeners.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            stringResource(R.string.no_remote_listeners),
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
                else {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            stringResource(R.string.click_to_add_queues),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.padding(4.dp))
                        Text(
                            stringResource(R.string.press_and_hold_to_manage),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(imageVector =  Icons.Outlined.Info, "")
                                Text(
                                    stringResource(R.string.only_1_connection_at_a_time_due_to_the_bottleneck_between_channels_and_phone_radios_ability_to_sms_messages_in_parallel),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier
                                        .padding(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.padding(8.dp))

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
                        getDefaultPermission.launch(makeDefault(context))
                        showPermissionModal = false
                    },
                    grantPermissionsCallback = {
                        getSMSPermissionLauncher.launch(requiredSMSPermissions)
                        showPermissionModal = false
                    }
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
                        if(!notificationPermission.status.isGranted) {
                            getNotificationsPermissionsLauncher
                                .launch(requiredNotificationsPermissions)
                        }
                        else if(activated) {
                            //Deactivating
                            remoteListenerViewModel.remoteListener?.activated = false
                            RemoteListenersHandler.stopListening(
                                context,
                                remoteListenerViewModel.remoteListener!!
                            )
                        }
                        else {
                            //Activating
                            when {
                                checkSelfPermission(
                                    context,
                                    requiredSMSPermissions
                                ) == PackageManager.PERMISSION_GRANTED -> {
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
                                            remoteListenerViewModel.remoteListener?.activated = true
                                            launch(Dispatchers.Main) {
                                                RemoteListenersHandler.startListening(
                                                    context,
                                                    remoteListenerViewModel.remoteListener!!
                                                )
                                            }
                                        }
                                    }
                                }

                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    activity!!,
                                    requiredSMSPermissions
                                ) -> {
                                    TODO()
                                }
                                else -> {
                                    getSMSPermissionLauncher.launch(requiredSMSPermissions)
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

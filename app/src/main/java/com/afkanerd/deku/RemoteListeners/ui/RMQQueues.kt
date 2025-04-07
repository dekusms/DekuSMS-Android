package com.afkanerd.deku.RemoteListeners.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.multiprocess.RemoteWorkerService
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenerQueuesViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersQueues
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListenersHandler
import com.afkanerd.deku.RemoteListeners.RMQ.RMQConnectionService
import com.afkanerd.deku.RemoteListeners.components.RemoteListenersQueuesCard
import com.afkanerd.deku.RemoteListeners.modals.RemoteListenerAddQueuesModal
import com.afkanerd.deku.RemoteListenersScreen
import com.example.compose.AppTheme
import com.rabbitmq.client.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.notify

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RMQQueuesComposable(
    _remoteListenersQueues: List<RemoteListenersQueues> = emptyList(),
    remoteListenersViewModel: RemoteListenersViewModel,
    navController: NavController
) {
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var showRemoteListenerAddQueuesModal by remember { mutableStateOf(false) }
    val remoteListenersQueuesViewModel = RemoteListenerQueuesViewModel()

    val remoteListenersQueues: List<RemoteListenersQueues> =
        if(LocalInspectionMode.current) _remoteListenersQueues
        else remoteListenersQueuesViewModel.get(context,
            remoteListenersViewModel.remoteListener!!.id
        ).observeAsState(emptyList()).value

    val lifeCycleOwner = LocalLifecycleOwner.current
    var channelsObserver: MutableLiveData<MutableMap<RemoteListenersQueues, List<Channel>>> =
        MutableLiveData()

    val channels by channelsObserver
        .observeAsState(emptyMap<RemoteListenersQueues, List<Channel>>())

    LaunchedEffect(channels) {
        channels.forEach {
            it.value.forEach {
                println("LE - RMQ channel number: ${it.channelNumber} ${it.isOpen}")
            }
        }
    }

    if(!LocalInspectionMode.current)
        remoteListenersViewModel.binder.getService().getRmqConnections()
            .observe(lifeCycleOwner) {
                it.find { it.id == remoteListenersViewModel.remoteListener!!.id }.let {
                    it!!.getChannelsLiveData().observe(lifeCycleOwner) { queueChannels ->
                        channelsObserver.value = queueChannels
                        queueChannels.forEach {
                            it.value.forEach {
                                println("RMQ channel number: ${it.channelNumber} ${it.isOpen}")
                            }
                        }
                    }
                }
            }

    BackHandler {
        remoteListenersQueuesViewModel.remoteListenerQueues = null
        navController.popBackStack(RemoteListenersScreen, false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "${remoteListenersViewModel.remoteListener?.username} Queues") },
                navigationIcon = {
                    IconButton(onClick = {
                        remoteListenersQueuesViewModel.remoteListenerQueues = null
                        navController.popBackStack(RemoteListenersScreen, false)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Return back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showRemoteListenerAddQueuesModal = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircleOutline,
                            contentDescription = "New remote listener"
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
                if( remoteListenersQueues.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            stringResource(R.string.no_queues_added),
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
                else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = remoteListenersQueues,
                            key = { _, remoteListenerQueue -> remoteListenerQueue.id}
                        ) { _, remoteListenerQueue ->
                            RemoteListenersQueuesCard(
                                remoteListenersQueues = remoteListenerQueue,
                                channel1 = channels[remoteListenerQueue]?.getOrNull(0),
                                channel2 = channels[remoteListenerQueue]?.getOrNull(1),
                            ) {
                                remoteListenersQueuesViewModel.remoteListenerQueues =
                                    remoteListenerQueue
                                showRemoteListenerAddQueuesModal = true
                            }
                        }
                    }
                }
            }

            if(showRemoteListenerAddQueuesModal) {
                RemoteListenerAddQueuesModal(
                    showModal = showRemoteListenerAddQueuesModal,
                    remoteListenersQueue = remoteListenersQueuesViewModel.remoteListenerQueues,
                    remoteListener = remoteListenersViewModel.remoteListener!!,
                    onClickCallback = {
                        val newRemoteListenerQueues = it
                        remoteListenersQueuesViewModel.remoteListenerQueues?.let {
                            newRemoteListenerQueues.id = it.id
                        }

                        CoroutineScope(Dispatchers.Default).launch {
                            if(remoteListenersQueuesViewModel.remoteListenerQueues != null)
                                remoteListenersQueuesViewModel.update(newRemoteListenerQueues)
                            else
                                remoteListenersQueuesViewModel.insert(newRemoteListenerQueues)

                            showRemoteListenerAddQueuesModal = false

                            RemoteListenersHandler.onOffAgain(context,
                                remoteListenersViewModel.remoteListener!!)
                        }
                    },
                ) {
                    showRemoteListenerAddQueuesModal = false
                }
            }
        }
    }
}


@Composable
@Preview
fun RMQQueuesComposable_Preview() {
    val rlq = RemoteListenersQueues()
    rlq.id = 0
    rlq.name = "rlq"
    rlq.binding1Name = "binding1"
    rlq.binding2Name = "binding2"

    AppTheme {
        RMQQueuesComposable(
            listOf(rlq),
            RemoteListenersViewModel(LocalContext.current),
            rememberNavController()
        )
    }
}

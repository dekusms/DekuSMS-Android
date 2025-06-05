package com.afkanerd.deku

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.compose.AppTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.SearchViewModel
import com.afkanerd.deku.DefaultSMS.ui.ComposeNewMessage
import com.afkanerd.deku.DefaultSMS.ui.ContactDetails
import com.afkanerd.deku.DefaultSMS.ui.Conversations
import com.afkanerd.deku.DefaultSMS.ui.SearchThreadsMain
import com.afkanerd.deku.DefaultSMS.ui.ThreadConversationLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.afkanerd.deku.DefaultSMS.Models.DevMode
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.ui.LogcatMain
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenerQueuesViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.ui.RMQAddComposable
import com.afkanerd.deku.RemoteListeners.ui.RMQMainComposable
import com.afkanerd.deku.RemoteListeners.ui.RMQQueuesComposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class MainActivity : AppCompatActivity(){

    private lateinit var navController: NavHostController

    private val conversationViewModel: ConversationsViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
//    private val remoteListenersViewModel: RemoteListenersViewModel by viewModels()
    private lateinit var remoteListenersViewModel: RemoteListenersViewModel
    private val remoteListenersProjectsViewModel:
            RemoteListenerQueuesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Fix for three-button nav not properly going edge-to-edge.
            // TODO: https://issuetracker.google.com/issues/298296168
            window.isNavigationBarContrastEnforced = false
        }

        remoteListenersViewModel = RemoteListenersViewModel(applicationContext)

        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity)
                    .collect { newLayoutInfo ->
                        onLayoutInfoChanged(newLayoutInfo)
                    }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("New intent instance called....")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if(intent.action == Intent.ACTION_VIEW &&
            intent.getStringExtra("view") == DevMode.viewLogCat) {
            navController.navigate(LogcatScreen)
        }
        else {
            intent.let {
                conversationViewModel.setNewIntent(it)
            }
        }
    }

    private fun onLayoutInfoChanged(newLayoutInfo: WindowLayoutInfo) {
        conversationViewModel.newLayoutInfo = newLayoutInfo
        setContent {
            AppTheme {
                navController = rememberNavController()
                Surface(Modifier
                    .fillMaxSize()
                ) {
                    val isFolded by remember {
                        mutableStateOf(newLayoutInfo.displayFeatures.isNotEmpty())
                    }
                    NavHost(
                        modifier = Modifier,
                        navController = navController,
                        startDestination = HomeScreen,
                    ) {
                        if(!isFolded) {
                            composable<HomeScreen>{
                                HomeScreenComposable()
                            }
                            composable<ConversationsScreen> {
                                ConversationScreenComposable()
                            }
                            composable<ComposeNewMessageScreen>{
                                ComposeNewMessageScreenComposable()
                            }
                            composable<SearchThreadScreen>{
                                SearchThreadScreenComposable()
                            }
                            composable<ContactDetailsScreen>{
                                ContactDetailsScreenComposable()
                            }
                            composable<RemoteListenersScreen>{
                                RMQMainComposable(
                                    remoteListenerViewModel = remoteListenersViewModel,
                                    remoteListenerQueuesViewModel =
                                        remoteListenersProjectsViewModel,
                                    conversationsViewModel = conversationViewModel,
                                    navController = navController
                                )
                            }
                            composable<RemoteListenersAddScreen>{
                                RMQAddComposable(
                                    navController = navController,
                                    remoteListenerViewModel = remoteListenersViewModel
                                )
                            }
                            composable<RemoteListenersQueuesScreen>{
                                RMQQueuesComposable(
                                    remoteListenersViewModel = remoteListenersViewModel,
                                    navController = navController
                                )
                            }
                            composable<LogcatScreen>{
                                LogcatMain()
                            }
                        }
                        else {
                            composable<HomeScreen>{
                                Folded()
                            }
                        }
                    }

                    handleIntent(intent)
                }
            }
        }
    }

    @Composable
    fun Folded() {
        Row {
            Column(modifier = Modifier.fillMaxWidth(0.5f)){
                HomeScreenComposable()
            }

            if(conversationViewModel.address.isNotEmpty() &&
                conversationViewModel.threadId.isNotEmpty()
            )
                Column { ConversationScreenComposable() }
            else
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NoMessageSelected()
                }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun NoMessageSelected() {
        Text(
            stringResource(
                R.string
                .select_a_conversation_from_the_list_on_the_left),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    fun HomeScreenComposable() {
        ThreadConversationLayout(
            conversationsViewModel = conversationViewModel,
            navController = navController,
        )
    }

    @Composable
    fun ConversationScreenComposable() {
        Conversations(
            viewModel=conversationViewModel,
            searchViewModel=searchViewModel,
            navController=navController
        )
    }

    @Composable
    fun ComposeNewMessageScreenComposable() {
        ComposeNewMessage(
            conversationsViewModel = conversationViewModel,
            navController=navController
        )
    }

    @Composable
    fun SearchThreadScreenComposable() {
        SearchThreadsMain(
            viewModel = searchViewModel,
            conversationsViewModel = conversationViewModel,
            navController = navController
        )
    }

    @Composable
    fun ContactDetailsScreenComposable() {
        ContactDetails(
            conversationsViewModel = conversationViewModel,
            searchViewModel = searchViewModel,
            navController = navController
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        CoroutineScope(Dispatchers.Default).launch {
            if(conversationViewModel.text.isNotEmpty())
                conversationViewModel.insertDraft(applicationContext)
        }
    }

    override fun onResume() {
        super.onResume()
        readLogcat(applicationContext)
    }
}

fun readLogcat(context: Context): Flow<String> = flow {
    val logBuilder = StringBuilder()
    try {
        // Execute logcat command with filter for app's logs
        val process = Runtime.getRuntime().exec("logcat -d *:V")
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

        // Get app's package name to filter logs
        val packageName = context.packageName

        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            // Filter logs containing app's package name
            if (line?.contains(packageName) == true) {
                emit(line)
            }
        }
        bufferedReader.close()
        process.destroy()
    } catch (e: IOException) {
        e.printStackTrace()
        emit(e.message.toString())
    }
}.flowOn(Dispatchers.IO)
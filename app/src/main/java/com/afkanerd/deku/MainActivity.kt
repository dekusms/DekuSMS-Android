package com.afkanerd.deku

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.compose.AppTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.afkanerd.deku.DefaultSMS.ui.ComposeNewMessage
import com.afkanerd.deku.DefaultSMS.ui.ContactDetails
import com.afkanerd.smswithoutborders_libsmsmms.ui.SearchThreadsMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ContactsViewModel
import com.afkanerd.deku.DefaultSMS.Models.DevMode
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenerQueuesViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.ui.RMQQueuesComposable
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.ui.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.ui.ThreadConversationLayout
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.NavHostControllerInstance
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.HomeScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.SearchScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.SearchViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ThreadsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class MainActivity : AppCompatActivity(){

    private lateinit var navController: NavHostController

    private val conversationViewModel: ConversationsViewModel by viewModels()
    private val threadsViewModel: ThreadsViewModel by viewModels()
    private val contactsViewModel: ContactsViewModel by viewModels()

    private lateinit var searchViewModel: SearchViewModel
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

        searchViewModel = SearchViewModel(getDatabase().threadsDao()!!)
        remoteListenersViewModel = RemoteListenersViewModel(applicationContext)

        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity)
                    .collect { newLayoutInfo ->
                        setContent {
                            AppTheme {
                                navController = rememberNavController()
                                Surface(Modifier
                                    .fillMaxSize()
                                ) {
                                    NavHostControllerInstance(
                                        newLayoutInfo = newLayoutInfo,
                                        navController = navController,
                                        threadsViewModel = threadsViewModel,
                                        searchViewModel = searchViewModel,
                                    ) {
                                    }
                                }
                            }
                        }
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
                // TODO("Implement this)
//                conversationViewModel.setNewIntent(it)
            }
        }
    }
}

package com.afkanerd.deku

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.compose.AppTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.WindowInfoTracker
import com.afkanerd.deku.DefaultSMS.AboutActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ContactsViewModel
import com.afkanerd.deku.DefaultSMS.Models.DevMode
import com.afkanerd.deku.DefaultSMS.ui.SecureConversationComposable
import com.afkanerd.deku.DefaultSMS.ui.viewModels.SecureConversationViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenerQueuesViewModel
import com.afkanerd.deku.RemoteListeners.Models.RemoteListener.RemoteListenersViewModel
import com.afkanerd.deku.RemoteListeners.ui.RMQMainComposable
import com.afkanerd.deku.Router.GatewayServers.GatewayServerRoutedActivity
import com.afkanerd.lib_smsmms_android.R
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NEW_NOTIFICATION_ACTION
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.makeE16PhoneNumber
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.NavHostControllerInstance
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.CustomsConversationsViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.SearchViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ThreadsViewModel


class MainActivity : AppCompatActivity(){

    private lateinit var navController: NavHostController

    private val threadsViewModel: ThreadsViewModel by viewModels()
    private val secureViewModel: SecureConversationViewModel by viewModels()

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
                            navController = rememberNavController()
                            AppTheme {
                                Surface(Modifier
                                    .fillMaxSize()
                                ) {
                                    NavHostControllerInstance(
                                        newLayoutInfo = newLayoutInfo,
                                        navController = navController,
                                        threadsViewModel = threadsViewModel,
                                        searchViewModel = searchViewModel,
                                        threadsMainMenuItems = {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text= stringResource(R.string.homepage_menu_routed),
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                },
                                                onClick = {
                                                    navController.navigate(RemoteForwardingScreen)
                                                    it(false)
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text= stringResource(R.string.remote_listeners),
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                },
                                                onClick = {
                                                    navController.navigate(RemoteForwardingScreen)
                                                    it(false)
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text= stringResource(R.string.about_deku),
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                },
                                                onClick = {
                                                    navController.navigate(AboutScreen)
                                                    it(false)
                                                }
                                            )
                                        },
                                        customMenuItems = {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text= stringResource(com.afkanerd.deku.DefaultSMS.R.string.secure),
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                },
                                                onClick = {
                                                    secureViewModel.setModal(true)
                                                }
                                            )
                                        },
                                        conversationsCustomViewModel = secureViewModel, //This can be an array
                                        conversationsCustomComposable = { vm ->
                                            SecureConversationComposable(
                                                vm as SecureConversationViewModel)
                                        }
                                    ) {
                                        composable<RemoteListenersScreen> {
                                            RMQMainComposable(
                                                remoteListenerViewModel = remoteListenersViewModel,
                                                remoteListenerQueuesViewModel = remoteListenersProjectsViewModel,
                                                navController = navController
                                            )
                                        }
                                        composable<RemoteForwardingScreen> {
                                            startActivity(
                                                Intent(applicationContext,
                                                    GatewayServerRoutedActivity::class.java))
                                            finish()
                                        }
                                        composable<AboutScreen> {
                                            startActivity(
                                                Intent(applicationContext,
                                                    AboutActivity::class.java))
                                            finish()
                                        }
                                    }

                                    processIntent(navController)
                                }
                            }
                        }
                    }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(::navController.isInitialized)
            processIntent(navController, intent)
    }

    private fun processIntent(navController: NavController, newIntent: Intent? = null) {
        val intent = newIntent ?: intent
        when(intent.action) {
            intent.NEW_NOTIFICATION_ACTION -> {
                val address = intent.getStringExtra("address")
                address?.let {
                    intent.removeExtra("address")
                    navController.navigate(ConversationsScreenNav(address))
                }
            }
            Intent.ACTION_SEND -> {

            }
            Intent.ACTION_SENDTO -> {
                intent.data?.let { uri ->
                    val address = makeE16PhoneNumber(uri.toString())

                    val text = intent.getStringExtra("sms_body")
                        ?: intent.getStringExtra(Intent.EXTRA_TEXT)

                    intent.removeExtra("sms_body")
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    intent.data = null

                    navController.navigate(ConversationsScreenNav(
                        address = address,
                        text = text,
                    ))
                }
            }
        }
    }
}

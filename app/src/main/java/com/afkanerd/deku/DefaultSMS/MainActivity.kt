package com.afkanerd.deku.DefaultSMS

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.example.compose.AppTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.SearchViewModel
import com.afkanerd.deku.DefaultSMS.ui.ComposeNewMessage
import com.afkanerd.deku.DefaultSMS.ui.Conversations
import com.afkanerd.deku.DefaultSMS.ui.SearchThreadsMain
import com.afkanerd.deku.DefaultSMS.ui.ThreadConversationLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object HomeScreen
@Serializable
object ConversationsScreen
@Serializable
object ComposeNewMessageScreen
@Serializable
object SearchThreadScreen

class MainActivity : AppCompatActivity(){

    lateinit var navController: NavHostController

    val viewModel: ThreadedConversationsViewModel by viewModels()
    val conversationViewModel: ConversationsViewModel by viewModels()
    val searchViewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkLoadNatives()

        setContent {
            AppTheme {
                navController = rememberNavController()
                Surface(Modifier
                    .padding(bottom=16.dp)
                    .fillMaxSize()
                ) {

                    NavHost(
                        modifier = Modifier,
                        navController = navController,
                        startDestination = HomeScreen,
                    ) {
                        composable<HomeScreen>{
                            ThreadConversationLayout(
                                viewModel=viewModel,
                                conversationsViewModel=conversationViewModel,
                                intent=intent,
                                navController = navController,
                            )
                        }

                        composable<ConversationsScreen>{
                            Conversations(
                                viewModel=conversationViewModel,
                                searchViewModel=searchViewModel,
                                navController=navController
                            )
                        }

                        composable<ComposeNewMessageScreen>{
                            ComposeNewMessage(
                                conversationsViewModel = conversationViewModel,
                                threadsViewModel = viewModel,
                                navController=navController
                            )
                        }

                        composable<SearchThreadScreen>{
                            SearchThreadsMain(
                                viewModel = searchViewModel,
                                conversationsViewModel = conversationViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkLoadNatives() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if(sharedPreferences.getBoolean(getString(R.string.configs_load_natives), false)){
            CoroutineScope(Dispatchers.Default).launch {
                viewModel.reset(applicationContext)
            }
            sharedPreferences.edit()
                .putBoolean(getString(R.string.configs_load_natives), false)
                .apply()
        }

    }

    override fun onResume() {
        super.onResume()
        checkLoadNatives()
    }
}
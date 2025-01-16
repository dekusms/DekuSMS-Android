package com.afkanerd.deku.DefaultSMS

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.Telephony
import android.text.Layout
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.compose.AppTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ContactsViewModel
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import androidx.window.layout.WindowMetricsCalculator
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
import kotlinx.serialization.Serializable
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Serializable
object HomeScreen
@Serializable
object ConversationsScreen
@Serializable
object ComposeNewMessageScreen
@Serializable
object SearchThreadScreen
@Serializable
object ContactDetailsScreen

class MainActivity : AppCompatActivity(){

    lateinit var navController: NavHostController

    val conversationViewModel: ConversationsViewModel by viewModels()
    val searchViewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkLoadNatives()

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

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        this.intent = intent
        navController.navigate(HomeScreen)
    }

    private fun checkLoadNatives() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if(sharedPreferences.getBoolean(getString(R.string.configs_load_natives), false)){
            CoroutineScope(Dispatchers.Default).launch {
                conversationViewModel.reset(applicationContext)
            }
            sharedPreferences.edit()
                .putBoolean(getString(R.string.configs_load_natives), false)
                .apply()
        }

    }

    override fun onResume() {
        super.onResume()
        checkIsDefault()
    }

    fun onLayoutInfoChanged(newLayoutInfo: WindowLayoutInfo) {
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
                        }
                        else {
                            composable<HomeScreen>{
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
                        }
                    }
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun NoMessageSelected() {
        Text(
            stringResource(R.string
                .select_a_conversation_from_the_list_on_the_left),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    fun HomeScreenComposable() {
        ThreadConversationLayout(
            conversationsViewModel = conversationViewModel,
            intent = intent,
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

    fun checkIsDefault() {
        val defaultName = Telephony.Sms.getDefaultSmsPackage(applicationContext)
        if(defaultName.isNullOrBlank() || packageName != defaultName) {
            when {
                ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED -> {
                    startActivity(Intent(this, DefaultCheckActivity::class.java))
                    finish()
                }
            }
        }
    }

}
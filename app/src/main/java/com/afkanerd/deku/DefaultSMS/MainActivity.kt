package com.afkanerd.deku.DefaultSMS

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.Layout
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
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import androidx.window.layout.WindowMetricsCalculator
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.SearchViewModel
import com.afkanerd.deku.DefaultSMS.Models.ExportImportHandlers
import com.afkanerd.deku.DefaultSMS.ui.ComposeNewMessage
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if(resultCode == RESULT_OK) {
            resultData?.let {
                val uri: Uri? = resultData.data
                // Perform operations on the document using its URI.

                uri?.let {
                    CoroutineScope(Dispatchers.Default).launch {
                        if (requestCode == ExportImportHandlers.exportRequestCode) {
                            with(contentResolver.openFileDescriptor(uri, "w")) {
                                this?.fileDescriptor.let { fd ->
                                    val fileOutputStream = FileOutputStream(fd);
                                    fileOutputStream.write(conversationViewModel
                                        .getAllExport(applicationContext).encodeToByteArray());
                                    // Let the document provider know you're done by closing the stream.
                                    fileOutputStream.close();
                                }
                                this?.close();

                                runOnUiThread {
                                    Toast.makeText(applicationContext,
                                        getString(R.string.conversations_exported_complete),
                                        Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        else if(requestCode == ExportImportHandlers.importRequestCode) {
                            val stringBuilder = StringBuilder()
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                                    var line: String? = reader.readLine()
                                    while (line != null) {
                                        stringBuilder.append(line)
                                        line = reader.readLine()
                                    }
                                }
                            }
                            conversationViewModel.importDetails = stringBuilder.toString()
//                            conversationViewModel.importAll(applicationContext,
//                                stringBuilder.toString())
//                            runOnUiThread {
//                                Toast.makeText(applicationContext,
//                                    getString(R.string.conversations_exported_complete),
//                                    Toast.LENGTH_LONG).show();
//                            }
                        }
                    }
                }
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()

        CoroutineScope(Dispatchers.Default).launch {
            if(conversationViewModel.text.isNotEmpty())
                conversationViewModel.insertDraft(applicationContext)
        }
    }

}
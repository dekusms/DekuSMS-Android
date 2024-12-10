package com.afkanerd.deku.DefaultSMS

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ContactsViewModel
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
import java.io.FileOutputStream

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

    val viewModel: ThreadedConversationsViewModel by viewModels()
    val conversationViewModel: ConversationsViewModel by viewModels()
    val searchViewModel: SearchViewModel by viewModels()
    val contactsViewModel: ContactsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkLoadNatives()

        setContent {
            AppTheme {
                navController = rememberNavController()
                Surface(Modifier
                    .fillMaxSize()
                ) {

                    NavHost(
                        modifier = Modifier,
                        navController = navController,
                        startDestination = HomeScreen,
                    ) {
                        composable<HomeScreen>{
                            ThreadConversationLayout(
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

                        composable<ContactDetailsScreen> {
                            ContactDetails(
                                contactsViewModel = contactsViewModel,
                                conversationViewModel = conversationViewModel,
                                navController = navController,
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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 777 && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.let {
                val uri: Uri? = resultData.data
                // Perform operations on the document using its URI.

                uri?.let {
                    CoroutineScope(Dispatchers.Default).launch {
                        with(contentResolver.openFileDescriptor(uri, "w")) {
                            this?.fileDescriptor.let { fd ->
                                val fileOutputStream = FileOutputStream(fd);
                                fileOutputStream.write(viewModel
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
                }
            }
        }
    }

}
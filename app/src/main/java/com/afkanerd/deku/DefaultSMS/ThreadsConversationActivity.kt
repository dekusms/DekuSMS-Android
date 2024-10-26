package com.afkanerd.deku.DefaultSMS

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.TableInfo
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.example.compose.AppTheme
import com.google.android.material.button.MaterialButton
import androidx.compose.runtime.getValue

class ThreadsConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(Modifier.fillMaxSize()) {
                    ThreadConversationLayout()
                }
            }
        }
    }

    data class Messages(
        var id: String,
        val firstName: String,
        val lastName: String,
        val content: String)

    @Composable
    fun ThreadConversationLayout() {
        val viewModel: ThreadedConversationsViewModel by viewModels()
        val items: List<ThreadedConversations> by
        viewModel.getAllLiveData(applicationContext).observeAsState(emptyList())

        var messages = remember {
            mutableStateListOf(
                Messages("0", "Jane", "Doe", "Hello world"),
                Messages("1", "Jane", "Doe", "Hello world"),
                Messages("2", "Jane", "Doe", "Hello world"),
                Messages("3", "Jane", "Doe", "Hello world"),
                Messages("4", "Jane", "Doe", "Hello world"),
                Messages("5", "Jane", "Doe", "Hello world"),
                Messages("6", "Jane", "Doe", "Hello world"),
                Messages("7", "Jane", "Doe", "Hello world"),
                Messages("8", "Jane", "Doe", "Hello world"),
                Messages("9", "Jane", "Doe", "Hello world")
            )
        }

        Column {
            Button(onClick = {
                val lastId = messages.last().id.toInt() + 1
                messages.add(Messages(lastId.toString(), "Jane: $lastId", "Doe", "Hello world"))
                println(messages)
            }) {
                Text("Add")
            }
            LazyColumn {
                items(
                    items = items,
                    key = { threadConversation ->
                        threadConversation.thread_id
                    }
                ) { message ->
                    ThreadConversationCard(
                        id = message.thread_id,
                        firstName = message.address,
                        lastName = if(!message.contact_name.isNullOrBlank()) message.contact_name else "",
                        content = message.snippet
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    fun PreviewMessageCard() {
        AppTheme(darkTheme = true) {
            Surface(Modifier.fillMaxSize()) {
                ThreadConversationLayout()
            }
        }
    }

}
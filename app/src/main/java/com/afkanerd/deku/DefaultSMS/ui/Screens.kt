package com.afkanerd.deku.DefaultSMS.ui

import kotlinx.serialization.Serializable

sealed class Screens(val route: String) {
    object ThreadConversations : Screens("threads")

    object Conversation : Screens("conversations/{threadId}/{address}") {
        fun createRoute(threadId: String, address: String) = "threads/$threadId/$address"
    }
}

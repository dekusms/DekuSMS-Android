package com.afkanerd.smswithoutborders_libsmsmms.ui.screens

import android.content.Intent
import kotlinx.serialization.Serializable

@Serializable
data class HomeScreenNav(
    val address: String? = null,
    val query: String? = null
)

@Serializable
data class ConversationsScreenNav(
    val address: String,
    val query: String? = null
)

@Serializable
data class SearchScreenNav(val address: String? = null)

@Serializable
object ContactDetailsNav

@Serializable
object ComposeNewMessageNav

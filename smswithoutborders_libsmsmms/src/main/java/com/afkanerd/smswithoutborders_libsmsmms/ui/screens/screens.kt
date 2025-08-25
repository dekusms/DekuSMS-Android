package com.afkanerd.smswithoutborders_libsmsmms.ui.screens

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
data class HomeScreenNav(
    val address: String? = null,
    val query: String? = null,
)

@Serializable
data class ConversationsScreenNav(
    val address: String,
    val query: String? = null,
    val text: String? = null,
    val threadId: Int? = null,
)

@Serializable
data class SearchScreenNav(val address: String? = null)

@Serializable
data class ContactDetailsScreenNav(val address: String)

@Serializable
object ComposeNewMessageScreenNav

@Serializable
object SettingsScreenNav

@Serializable
data class ImageViewScreenNav(
    var contentUri: String,
    var address: String,
    var date: String,
    var filename: String,
    var mimeType: String,
)
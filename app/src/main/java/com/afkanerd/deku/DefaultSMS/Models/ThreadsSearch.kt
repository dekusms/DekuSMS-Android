package com.afkanerd.deku.DefaultSMS.Models

data class ThreadsSearch(
    val count: Int,
    val threadId: String,
    val text: String,
    val date: String
)

data class ThreadsCount(
    val archivedCount: Int,
    val unreadCount: Int,
    val draftsCount: Int,
)
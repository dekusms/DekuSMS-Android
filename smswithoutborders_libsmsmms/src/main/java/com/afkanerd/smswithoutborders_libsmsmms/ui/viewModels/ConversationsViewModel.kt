package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.afkanerd.smswithoutborders_libsmsmms.Extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations

class ConversationsViewModel: ViewModel() {

    fun add(context: Context, conversation: Conversations): Long? {
        return context.getDatabase().conversationDao()?.insert(conversation)
    }

    fun update(context: Context, conversation: Conversations): Int? {
        return context.getDatabase().conversationDao()?.update(conversation)
    }
}
package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

open class CustomsConversationsViewModel : ViewModel() {
    var showModal by mutableStateOf(false)
        private set

    fun setModal(show: Boolean) {
        showModal = show
    }
}
package com.afkanerd.deku.DefaultSMS.ui

import androidx.compose.runtime.Composable
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.SecureRequestAcceptModal

@Composable
fun SecureConversationComposable() {
    SecureRequestAcceptModal(
        isSecureRequest = true,
    ){ }
}
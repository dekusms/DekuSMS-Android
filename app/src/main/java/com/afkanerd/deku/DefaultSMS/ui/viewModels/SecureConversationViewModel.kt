package com.afkanerd.deku.DefaultSMS.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.data.models.SecureConversation
import com.afkanerd.smswithoutborders_libsmsmms.data.Addresses
import kotlinx.coroutines.flow.Flow


class SecureConversationViewModel: ViewModel() {

    fun get(
        context: Context,
        address: Addresses
    ) : Flow<SecureConversation?> {
        val datastore = Datastore.getDatastore(context)
        return datastore.secureConversationDao().fetch(address.getFullE164Address)
    }
}
package com.afkanerd.deku.DefaultSMS.Models

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

object DatastoreHandler {
    val Context.conversationConfigurationsData:
            DataStore<Preferences> by preferencesDataStore(name = "configurations")

    fun getDatastore(context: Context): DataStore<Preferences> {
        return context.conversationConfigurationsData
    }
}
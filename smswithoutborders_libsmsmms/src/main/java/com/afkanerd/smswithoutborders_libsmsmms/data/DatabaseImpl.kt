package com.afkanerd.smswithoutborders_libsmsmms.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.dao.ConversationDao
import com.afkanerd.smswithoutborders_libsmsmms.data.dao.ThreadsDao
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.smsMmsNatives
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Archive
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Threads
import kotlin.concurrent.Volatile

@Database(
    entities = [
        Archive::class,
        smsMmsNatives.Sms::class,
        smsMmsNatives.Mms::class,
        smsMmsNatives.MmsPart::class,
        smsMmsNatives.MmsAddr::class,
        Threads::class],
    version = 1
)
abstract class DatabaseImpl : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao?
    abstract fun threadsDao(): ThreadsDao?

    companion object {
        @Volatile
        private var datastore: DatabaseImpl? = null
        var databaseName: String = "lib_DekuSMS"

        @Synchronized
        fun getDatabaseImpl(context: Context): DatabaseImpl {
            if (datastore == null) {
                datastore = create(context)
            }
            return datastore!!
        }

        private fun create(context: Context): DatabaseImpl {
            return databaseBuilder(context, DatabaseImpl::class.java, databaseName)
                .enableMultiInstanceInvalidation()
                .build()
        }
    }
}

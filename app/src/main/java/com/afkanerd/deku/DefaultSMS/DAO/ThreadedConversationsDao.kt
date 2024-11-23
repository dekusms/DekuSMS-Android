package com.afkanerd.deku.DefaultSMS.DAO

import android.content.Context
import android.provider.Telephony
import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Models.Archive
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.Models.ThreadsSearch

@Dao
interface ThreadedConversationsDao {
    @Query("SELECT * FROM ThreadedConversations ORDER BY date DESC")
    fun getAll(): MutableList<ThreadedConversations>

    @Query(
        "SELECT * FROM ThreadedConversations WHERE is_archived = 0 AND is_blocked = 0 " +
                "ORDER BY date DESC"
    )
    fun getInbox(): LiveData<MutableList<ThreadedConversations>>

    @Query("SELECT * FROM ThreadedConversations WHERE is_archived = 1 ORDER BY date DESC")
    fun getArchived(): LiveData<MutableList<ThreadedConversations>>

    @Query("SELECT * FROM ThreadedConversations WHERE is_secured = 1 ORDER BY date DESC")
    fun getEncrypted(): LiveData<MutableList<ThreadedConversations>>

    @Query("SELECT * FROM ThreadedConversations WHERE is_blocked = 1 ORDER BY date DESC")
    fun getBlocked(): LiveData<MutableList<ThreadedConversations>>

    @Query(
        "SELECT * FROM ThreadedConversations WHERE is_archived = 0 AND is_blocked = 0 " +
                "ORDER BY date DESC"
    )
    fun getAllWithoutArchived(): PagingSource<Int, ThreadedConversations>

    @Query("SELECT * FROM ThreadedConversations WHERE is_archived = 0 AND is_read = 0 ORDER BY date DESC")
    fun getAllUnreadWithoutArchived(): PagingSource<Int, ThreadedConversations>

    @Query("SELECT * FROM ThreadedConversations WHERE is_mute = 1 ORDER BY date DESC")
    fun getMuted(): PagingSource<Int, ThreadedConversations>

    @Query(
        ("SELECT COUNT(Conversation.id) FROM Conversation, ThreadedConversations WHERE " +
                "Conversation.thread_id = ThreadedConversations.thread_id AND " +
                "is_archived = 0 AND read = 0")
    )
    fun getCountUnread(): Int

    @Query(
        "SELECT COUNT(Conversation.id) FROM Conversation WHERE " +
                "Conversation.thread_id IN (:ids) AND read = 0"
    )
    fun getCountUnread(ids: MutableList<String>): Int

    @Query("SELECT COUNT(*) FROM ThreadedConversations WHERE is_secured = 1")
    fun getCountEncrypted(): Int

    @Query(
        "SELECT COUNT(ThreadedConversations.thread_id) FROM ThreadedConversations " +
                "WHERE is_blocked = 1"
    )
    fun getCountBlocked(): Int

    @Query(
        "SELECT COUNT(ThreadedConversations.thread_id) FROM ThreadedConversations " +
                "WHERE is_mute = 1"
    )
    fun getCountMuted(): Int

    @Query(
        ("SELECT Conversation.address, " +
                "Conversation.text as snippet, " +
                "Conversation.thread_id, " +
                "Conversation.date, Conversation.type, " +
                "ThreadedConversations.msg_count, ThreadedConversations.is_archived, " +
                "ThreadedConversations.is_blocked, ThreadedConversations.is_read, " +
                "ThreadedConversations.is_shortcode, ThreadedConversations.contact_name, " +
                "ThreadedConversations.is_mute, ThreadedConversations.is_secured, " +
                "ThreadedConversations.isSelf, ThreadedConversations.subscription_id, " +
                "ThreadedConversations.formatted_datetime, ThreadedConversations.unread_count " +
                "FROM Conversation, ThreadedConversations WHERE " +
                "Conversation.type = :type AND ThreadedConversations.thread_id = Conversation.thread_id " +
                "ORDER BY Conversation.date DESC")
    )
    fun getThreadedDrafts(type: Int): PagingSource<Int, ThreadedConversations>

    @Query(
        ("SELECT COUNT(ThreadedConversations.thread_id) " +
                "FROM Conversation, ThreadedConversations WHERE " +
                "Conversation.type = :type AND ThreadedConversations.thread_id = Conversation.thread_id " +
                "ORDER BY Conversation.date DESC")
    )
    fun getThreadedDraftsListCount(type: Int): Int

    @Query("DELETE FROM ThreadedConversations WHERE ThreadedConversations.type = :type")
    fun deleteForType(type: Int): Int

    @Query("DELETE FROM Conversation WHERE Conversation.type = :type")
    fun clearConversationType(type: Int): Int

    @Transaction
    fun clearDrafts(type: Int) {
        clearConversationType(type)
        deleteForType(type)
    }

    @Query("UPDATE ThreadedConversations SET is_read = :read")
    fun updateAllRead(read: Int): Int

    @Query("UPDATE Conversation SET read = :read")
    fun updateAllReadConversation(read: Int): Int

    @Query("UPDATE ThreadedConversations SET is_read = :read WHERE thread_id IN(:ids)")
    fun updateAllRead(read: Int, ids: MutableList<String>): Int

    @Query("UPDATE ThreadedConversations SET is_read = :read WHERE thread_id = :id")
    fun updateAllRead(read: Int, id: Long): Int

    @Query("UPDATE ThreadedConversations SET is_mute = :muted WHERE thread_id = :id")
    fun updateMuted(muted: Int, id: String): Int

    @Query("UPDATE ThreadedConversations SET is_mute = :muted WHERE thread_id IN(:ids)")
    fun updateMuted(muted: Int, ids: MutableList<String>): Int

    @Query("UPDATE ThreadedConversations SET is_mute = 0 WHERE is_mute = 1")
    fun updateUnMuteAll(): Int

    @Query("UPDATE Conversation SET read = :read WHERE thread_id IN(:ids)")
    fun updateAllReadConversation(read: Int, ids: MutableList<String>): Int

    @Query("UPDATE Conversation SET read = :read WHERE thread_id = :id")
    fun updateAllReadConversation(read: Int, id: Long): Int

    @Transaction
    fun updateRead(read: Int) {
        updateAllRead(read)
        updateAllReadConversation(read)
    }

    @Transaction
    fun updateRead(read: Int, ids: MutableList<String>) {
        updateAllRead(read, ids)
        updateAllReadConversation(read, ids)
    }

    @Transaction
    fun updateRead(read: Int, id: Long) {
        updateAllRead(read, id)
        updateAllReadConversation(read, id)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(threadedConversationsList: MutableList<ThreadedConversations>): MutableList<Long>

    @Query("SELECT * FROM ThreadedConversations WHERE thread_id =:thread_id")
    fun get(thread_id: String): ThreadedConversations

    @Query("SELECT * FROM ThreadedConversations WHERE thread_id IN (:threadIds)")
    fun getList(threadIds: List<String>): MutableList<ThreadedConversations>

    @Query("SELECT * FROM ThreadedConversations WHERE address =:address")
    fun getByAddress(address: String): ThreadedConversations

    @Query(
        "SELECT * FROM ThreadedConversations WHERE address IN(:addresses) AND is_archived = 0 " +
                "ORDER BY date DESC"
    )
    fun getByAddress(addresses: MutableList<String>): PagingSource<Int, ThreadedConversations>

    @Query("SELECT * FROM ThreadedConversations WHERE address NOT IN(:addresses)")
    fun getNotInAddress(addresses: MutableList<String>): PagingSource<Int, ThreadedConversations>

    @Query("SELECT address FROM ThreadedConversations WHERE thread_id IN (:threadedConversationsList)")
    fun findAddresses(threadedConversationsList: MutableList<String>): MutableList<String>

    @Query(
        "SELECT COUNT(Conversation.id) as count, Conversation.thread_id as threadId, " +
                "Conversation.text, Conversation.date " +
                "FROM Conversation, ThreadedConversations " +
                "where text like '%' || :searchString || '%' and " +
                "Conversation.thread_id = ThreadedConversations.thread_id " +
                "GROUP BY ThreadedConversations.thread_id ORDER BY Conversation.date DESC"
    )
    fun search(searchString: String): MutableList<ThreadsSearch>

    @Query(
        "SELECT COUNT(Conversation.id) as count, Conversation.thread_id as threadId, " +
                "Conversation.text, Conversation.date " +
                "FROM Conversation " +
                "where thread_id = :threadId and text like '%' || :searchString || '%' " +
                "GROUP BY thread_id ORDER BY Conversation.date DESC"
    )
    fun searchThreadId(searchString: String, threadId: String): MutableList<ThreadsSearch>

    @Insert
    fun _insert(threadedConversations: ThreadedConversations): Long

    @Transaction
    fun insertThreadFromConversation(
        context: Context,
        conversation: Conversation
    ): ThreadedConversations {
        // TODO: Here is the culprit

        /* - Import things are:
        1. Dates
        2. Snippet
        3. ThreadId
         */

        val dates = conversation.date
        val snippet = conversation.text
        val threadId = conversation.thread_id
        val address = conversation.address

        val type = conversation.type

        val isRead = type == Telephony.Sms.MESSAGE_TYPE_OUTBOX || conversation.isRead
        val isSecured = conversation.isIs_encrypted

        var threadedConversations = Datastore.getDatastore(context)
            .threadedConversationsDao()
            .get(conversation.thread_id!!)
        threadedConversations.setDate(dates)
        threadedConversations.setSnippet(snippet)
        threadedConversations.setIs_read(isRead)
        threadedConversations.setIs_secured(isSecured)
        threadedConversations.setAddress(address)
        threadedConversations.setType(type)

        update(context, threadedConversations)
        threadedConversations = Datastore.getDatastore(context).threadedConversationsDao()
            .get(conversation.thread_id!!)

        return threadedConversations
    }

    @Transaction
    fun insertThreadAndConversation(
        context: Context,
        conversation: Conversation
    ): ThreadedConversations {
        /* - Import things are:
        1. Dates
        2. Snippet
        3. ThreadId
         */

        val id = Datastore.getDatastore(context).conversationDao()._insert(conversation)
        val dates = conversation.date
        val snippet = conversation.text
        val threadId = conversation.thread_id
        val address = conversation.address

        val type = conversation.type

        val isRead = type != Telephony.Sms.MESSAGE_TYPE_INBOX || conversation.isRead
        val isSecured = conversation.isIs_encrypted

        var insert = false
        val unreadCount = Datastore.getDatastore(context).conversationDao()
            .getUnreadCount(threadId!!)
        var threadedConversations: ThreadedConversations = get(threadId)
        if (threadedConversations == null) {
            threadedConversations = ThreadedConversations()
            threadedConversations.setThread_id(threadId)
            insert = true
        }
        threadedConversations.setDate(dates)
        threadedConversations.setSnippet(snippet)
        threadedConversations.setIs_read(isRead)
        threadedConversations.setIs_secured(isSecured)
        threadedConversations.setAddress(address)
        threadedConversations.setType(type)
        threadedConversations.setUnread_count(unreadCount)

        if (insert) _insert(threadedConversations)
        else {
            update(context, threadedConversations)
        }

        return threadedConversations
    }

    @Update
    fun _update(threadedConversations: ThreadedConversations): Int

    @Transaction
    fun update(context: Context, threadedConversations: ThreadedConversations): Int {
        if (threadedConversations.getDate() == null || threadedConversations.getDate()
                .isEmpty()
        ) threadedConversations.setDate(
            Datastore.getDatastore(context).conversationDao()
                .fetchLatestForThread(threadedConversations.getThread_id())!!.date
        )
        return _update(threadedConversations)
    }

    @Delete
    fun _delete(threadedConversations: ThreadedConversations)

    @Query("DELETE FROM ThreadedConversations WHERE thread_id IN(:ids)")
    fun _delete(ids: List<String>)

    @Transaction
    fun delete(context: Context, ids: List<String>) {
        for (threadedConversations in getList(ids)) {
//            try {
//                String keystoreAlias =
//                        E2EEHandler.deriveKeystoreAlias(context, threadedConversations.getAddress(), 0);
//                E2EEHandler.clear(context, keystoreAlias);
//            } catch (KeyStoreException | NumberParseException |
//                     InterruptedException |
//                     NoSuchAlgorithmException | IOException |
//                     CertificateException e) {
//                e.printStackTrace();
//            }
        }
        _delete(ids)
        Datastore.getDatastore(context).conversationDao().deleteAll(ids)
    }

    @Transaction
    fun delete(context: Context, threadedConversations: ThreadedConversations) {
//        try {
//            String keystoreAlias =
//                    E2EEHandler.deriveKeystoreAlias(context, threadedConversations.getAddress(), 0);
//            E2EEHandler.clear(context, keystoreAlias);
//        } catch (KeyStoreException | NumberParseException |
//                 InterruptedException |
//                 NoSuchAlgorithmException | IOException |
//                 CertificateException e) {
//            e.printStackTrace();
//        }

        _delete(threadedConversations)
        Datastore.getDatastore(context).conversationDao()
            .deleteAll(mutableListOf<String>(threadedConversations.getThread_id()))
    }

    @Query("DELETE FROM threadedconversations")
    fun deleteAll()

    @Update(entity = ThreadedConversations::class)
    fun archive(archiveList: List<Archive>)

    @Update(entity = ThreadedConversations::class)
    fun unarchive(archiveList: List<Archive>)

    @Transaction
    fun updateAllRead(context: Context, threadId: String, isRead: Boolean) {
        Datastore.getDatastore(context).conversationDao().updateRead(isRead, threadId)
        val threadedConversations =
            Datastore.getDatastore(context).threadedConversationsDao().get(threadId)
        if (threadedConversations != null) {
            threadedConversations.setIs_read(isRead)
            threadedConversations.setUnread_count(0)

            Datastore.getDatastore(context).threadedConversationsDao()
                .update(context, threadedConversations)
        }
    }
}

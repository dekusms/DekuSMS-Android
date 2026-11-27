package com.afkanerd.deku.DefaultSMS.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SecureConversationType(val value: Int) {
    REQUEST(1),
    REQUESTED(2),
    ACKNOWLEDGE(3),
    ACKNOWLEDGED(4);

    companion object {
        private val map = entries.associateBy(SecureConversationType::value)

        // Returns null if the code doesn't match any enum constant
        fun fromInt(value: Int): SecureConversationType? = map[value]
    }
}

@Entity
data class SecureConversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String,
    var type: Int,
    val isRequester: Boolean,
    val sessionId: ByteArray,
    val timestamp: ByteArray,
    var privateKey: ByteArray,
    var publicKey: ByteArray,
    val peerPublicKey: ByteArray? = null,
    var sharedSecret: ByteArray? = null,
    val date: Long = System.currentTimeMillis()
) : AutoCloseable {
    override fun close() {
        privateKey.fill(0)
    }

}
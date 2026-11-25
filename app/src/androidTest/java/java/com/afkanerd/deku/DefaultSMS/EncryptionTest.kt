package java.com.afkanerd.deku.DefaultSMS

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.afkanerd.deku.DefaultSMS.data.EncryptionUtils
import com.afkanerd.deku.DefaultSMS.extensions.toRaw32Bytes
import junit.framework.TestCase.assertEquals
import org.apache.commons.codec.binary.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random


@RunWith(AndroidJUnit4::class)
class EncryptionTest {

    @Test
    fun isSameSecret() {
        val encryptionUtils = EncryptionUtils()

        val aliceKeypair = encryptionUtils.generateX25519Keys()
        val bobKeypair = encryptionUtils.generateX25519Keys()

        val alicePublicKey = aliceKeypair.keyPair.public.toRaw32Bytes()
        val bobPublicKey = bobKeypair.keyPair.public.toRaw32Bytes()

        assertEquals(32, alicePublicKey.size)
        assertEquals(32, bobPublicKey.size)

        val saltData: ByteArray = Random.nextBytes(16)

        // Bob's device
        val bobSharedSecret = encryptionUtils.computeX25519SharedSecret(
            keyPair = encryptionUtils.getX25519KeyPairFromRawPrivate(bobKeypair.keyPair.private.toRaw32Bytes()),
            requesterPublicKey = alicePublicKey,
            recipientPublicKey = bobPublicKey,
            peerPublicKey = alicePublicKey,
            saltData = saltData
        )

        val aliceSharedSecret = encryptionUtils.computeX25519SharedSecret(
            keyPair = encryptionUtils.getX25519KeyPairFromRawPrivate(aliceKeypair.keyPair.private.toRaw32Bytes()),
            requesterPublicKey = alicePublicKey,
            recipientPublicKey = bobPublicKey,
            peerPublicKey = bobPublicKey,
            saltData = saltData
        )

        assertArrayEquals(bobSharedSecret.encoded, aliceSharedSecret.encoded)
    }

    @Test
    fun canHandshake() {
        val initiationRequest = EncryptionUtils.Handshake.makeRequest()

        assertEquals(EncryptionUtils.Handshake.HANDSHAKE_REQ_SIZE, initiationRequest.request.size)
        assert(EncryptionUtils.Handshake.isIncomingRequest(initiationRequest.request))

        val acceptInitiationRequest = EncryptionUtils.Handshake.acceptRequest(initiationRequest.request)
        assertArrayEquals(acceptInitiationRequest.sessionId, initiationRequest.sessionId)
        assertArrayEquals(acceptInitiationRequest.peerPublicKey, initiationRequest.publicKey)

        val ackResponse = EncryptionUtils.Handshake.makeAcknowledgementRequest(
            acceptInitiationRequest.sessionId,
            acceptInitiationRequest.peerPublicKey,
            timestamp = initiationRequest.timestamp
        )

        assertEquals(EncryptionUtils.Handshake.HANDSHAKE_ACK_SIZE, ackResponse.response.size)
        assert(EncryptionUtils.Handshake.isAckResponse(ackResponse.response))

        val acceptedRequest = EncryptionUtils.Handshake.acceptAckRequest(ackResponse.response)
        assertArrayEquals(initiationRequest.sessionId, acceptedRequest.sessionId)
        assertArrayEquals(ackResponse.publicKey.toRaw32Bytes(), acceptedRequest.peerPublicKey)
    }

    @Test
    fun canOob() {
        val alicePublicKey = android.util.Base64.decode("YW1b2pvL1mU++q1EVO6wy1MfGKraFkgp/rn+0qOJxSM=", android.util.Base64.DEFAULT)
        val bobPublicKey = android.util.Base64.decode("HD2tS9uYdz591mnTbgqH+ipcPd5ChdshHFLClLwdllc=", android.util.Base64.DEFAULT)

        val aliceAddress = "+1234567890".encodeToByteArray()
        val bobAddress = "+1234567891".encodeToByteArray()

        val expected = "eb90b082a418edc8f80ca23fdd9da57938b5129d74662d222697745aabecaba9".hexToByteArray()

        val generatedOob = EncryptionUtils.Handshake.generateOobVerification(
            requesterPublicKey = alicePublicKey,
            recipientPublicKey = bobPublicKey,
            requesterAddress = aliceAddress,
            recipientAddress = bobAddress
        )

        assertArrayEquals(expected, generatedOob)
    }
}
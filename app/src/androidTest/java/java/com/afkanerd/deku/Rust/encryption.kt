package java.com.afkanerd.deku.Rust

import com.afkanerd.deku.DefaultSMS.data.Cryptography
import com.afkanerd.deku.rust.ExceededMaxSkipMessages
import com.afkanerd.deku.rust.FailedToDecryptPayload
import com.afkanerd.deku.rust.FailedToDeserializeState
import com.afkanerd.deku.rust.FailedToEncryptPayload
import com.afkanerd.deku.rust.FailedToSerializeHeader
import com.afkanerd.deku.rust.FailedToSerializeState
import com.afkanerd.deku.rust.FailedToStoreFinalDecryptionValues
import com.afkanerd.deku.rust.FailedToStoreFinalEncryptionValues
import com.afkanerd.deku.rust.RatchetInitFailedForAlice
import com.afkanerd.deku.rust.RatchetInitFailedForBob
import com.afkanerd.deku.rust.RatchetResult
import com.afkanerd.deku.rust.RustyRatchetBridge
import com.afkanerd.deku.rust.ratchetCall
import com.android.mms.service_alt.DownloadRequest.persist
import junit.framework.TestCase.assertEquals
import okhttp3.internal.http2.Header
import org.junit.Assert.assertArrayEquals
import org.junit.Test


class EncryptionTest {


    fun decryptMessage(state: ByteArray, ct: ByteArray, header: ByteArray, ad: ByteArray) =
        ratchetCall { RustyRatchetBridge.decrypt(state, ct, header, ad) }

    @Test
    fun simpleRatchet() {
        var aliceState = RustyRatchetBridge.newState()
        var bobState = RustyRatchetBridge.newState()

        val sk = Cryptography.generateSecureRandom(32)

        val alicePrivate = "f8d7ef06f5b7c38d00d5d43b1d528af4621b1bcce3cddcf5c03ed36e3d8eb943".hexToByteArray()
        val alicePublic = "bae06b517b1de9675df982630aade00e9572ed3cce6f2a4806c736f24ea09c5d".hexToByteArray()

        val bobPrivate = "b02c292ba617a92893531d7fc6b59d99cd8f1f7957113430c6fe66c39f26b55c".hexToByteArray()
        val bobPublic = "5322d992ca856162ced81a364614191b090159563b74f317978da4acc96d1800".hexToByteArray()

        aliceState = RustyRatchetBridge.initAlice(
            state = aliceState,
            sk = sk,
            bob_dh_public_key = bobPublic
        )

        bobState = RustyRatchetBridge.initBob(
            state = bobState,
            sk = sk,
            bob_keypair = bobPrivate
        )

        val ad = Cryptography.generateSecureRandom(32)
        var input = Cryptography.generateSecureRandom(32)
        var (newAliceState, ciphertext, header) = RustyRatchetBridge.encrypt(
            state = aliceState,
            plaintextText = input,
            ad = ad
        )
        aliceState = newAliceState

        val (newBobState, payload) = RustyRatchetBridge.decrypt(
            state = bobState,
            header = header,
            ciphertext = ciphertext,
            ad = ad
        )
        bobState = newBobState

        assertArrayEquals(input, payload)

        input = Cryptography.generateSecureRandom(32)

        val (newBobState1, newCiphertext, newHeader) = RustyRatchetBridge.encrypt(
            state = bobState,
            plaintextText = input,
            ad = ad
        )
        bobState = newBobState1

        val (newAliceState1, newPayload) = RustyRatchetBridge.decrypt(
            state = aliceState,
            header = newHeader,
            ciphertext = newCiphertext,
            ad = ad
        )
        aliceState = newAliceState1
        assertArrayEquals(input, newPayload)

        val (newBobState2, newCiphertext1, newHeader1) = RustyRatchetBridge.encrypt(
            state = bobState,
            plaintextText = input,
            ad = ad
        )
        bobState = newBobState2
        val (state, newPayload2) = RustyRatchetBridge.decrypt(
            state = aliceState,
            header = newHeader1,
            ciphertext = newCiphertext1,
            ad = ad
        )
        aliceState = state
        assertArrayEquals(input, newPayload2)
        for(i in 0..100) {
            val (state, newCiphertext, newHeader) = RustyRatchetBridge.encrypt(
                state = bobState,
                plaintextText = input,
                ad = ad
            )
            ciphertext = newCiphertext
            header = newHeader
            bobState = state
        }

        when (val r = decryptMessage(aliceState, ciphertext, header, ad)) {
            is RatchetResult.Ok -> {
                val (newState, plaintext) = r.value   // adjust to your array order
                assertArrayEquals(input, plaintext)
            }
            is RatchetResult.Failure -> when (r.error) {
//                is FailedToDecryptPayload -> dropMessage()          // tampered, duplicate, or wrong session
//                is ExceededMaxSkipMessages -> requestRehandshake()   // too far out of sync, not retryable
//                is FailedToDeserializeState,
//                is FailedToSerializeState -> markSessionCorrupt()   // stored state is bad
//                is FailedToSerializeHeader,
//                is FailedToEncryptPayload -> reportInternalError(r.error)
//                is FailedToStoreFinalEncryptionValues,
//                is FailedToStoreFinalDecryptionValues -> reportInternalError(r.error)
//                is RatchetInitFailedForAlice,
//                is RatchetInitFailedForBob -> abortHandshake()
                else -> { throw (r.error) }
            }
        }
    }
}
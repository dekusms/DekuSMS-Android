package java.com.afkanerd.deku.DefaultSMS

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.afkanerd.deku.DefaultSMS.data.EncryptionUtils
import com.afkanerd.deku.DefaultSMS.extensions.toRaw32Bytes
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class EncryptionTest {

    @Test
    fun canHandshake() {
        val encryptionUtils = EncryptionUtils()

        val aliceKeypair = EncryptionUtils.Handshake().generateX25519Keys()
        val bobKeypair = encryptionUtils.generateX25519Keys()

        val alicePublicKey = aliceKeypair.keyPair.public.toRaw32Bytes()
        val bobPublicKey = bobKeypair.keyPair.public.toRaw32Bytes()

        assertEquals(32, alicePublicKey.size)
        assertEquals(32, bobPublicKey.size)

        // Bob's device
        val bobSharedSecret = encryptionUtils.computeX25519SharedSecret(
            bobKeypair.keyPair.private,
            encryptionUtils.raw32BytesToX25519PublicKey(alicePublicKey)
        )

        val aliceSharedSecret = encryptionUtils.computeX25519SharedSecret(
            aliceKeypair.keyPair.private,
            encryptionUtils.raw32BytesToX25519PublicKey(bobPublicKey)
        )

        assertArrayEquals(bobSharedSecret.encoded, aliceSharedSecret.encoded)
    }
}
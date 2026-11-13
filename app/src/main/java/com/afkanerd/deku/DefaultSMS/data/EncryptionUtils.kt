package com.afkanerd.deku.DefaultSMS.data

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.bouncycastle.jcajce.interfaces.XDHPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.DestroyFailedException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class EncryptionUtils {
    init {
        setupBouncyCastle()
    }

    class Handshake(
        address: String
    ) {
        companion object {
            /** HANDSHAKE_REQ_SIZE =
             *
             * 16 Byte session ID (UUID)
             * 32 Bytes EC public key
             * 8 Bytes timestamp
             */
            const val HANDSHAKE_REQ_SIZE = 56

            fun isIncomingRequest(data: ByteArray) : Boolean{
                if (data.size != HANDSHAKE_REQ_SIZE)
                    return false

                TODO("Validate against timestamp")

                return true
            }
        }

        fun hasRequests(address: String) : Boolean {
            TODO("Check if user has a pending request")
        }

        @OptIn(ExperimentalUuidApi::class)
        fun makeRequest(publicKey: ByteArray) : Pair<ByteArray, ByteArray> {
            val sessionId = Uuid.random().toByteArray()

            val timestamp = ByteBuffer.allocate(Long.SIZE_BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(System.currentTimeMillis())
                .array()

            val request = sessionId + publicKey + timestamp
            return Pair(sessionId, request)
        }

        fun save() {
            TODO("Save this handshake against the address")
        }
    }

    class CloseableKeyPair(val keyPair: KeyPair) : AutoCloseable {
        override fun close() {
            try {
                if (!keyPair.private.isDestroyed) {
                    keyPair.private.destroy()
                }
            } catch (_: DestroyFailedException) {
                // Ignore if the underlying provider does not support destruction
            }
        }
    }

    fun generateX25519Keys(): CloseableKeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("X25519", "BC")
        return CloseableKeyPair(keyPairGenerator.generateKeyPair())
    }

    fun computeX25519SharedSecret(
        privateKey: PrivateKey,
        peerPublicKey: PublicKey
    ): SecretKey {
        val keyAgreement = KeyAgreement.getInstance("X25519", "BC")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(peerPublicKey, true)
        return deriveAesKeyFromSharedSecret(keyAgreement.generateSecret())
    }

    private fun deriveAesKeyFromSharedSecret(
        sharedSecret: ByteArray,
        salt: ByteArray? = null,
        info: ByteArray = "ECDH-AES-256-GCM".toByteArray(Charsets.UTF_8),
        keyLengthBytes: Int = 32
    ): SecretKey {
        val hkdf = HKDFBytesGenerator(SHA256Digest())

        val params = HKDFParameters(sharedSecret, salt, info)
        hkdf.init(params)

        val derivedKeyBytes = ByteArray(keyLengthBytes)
        hkdf.generateBytes(derivedKeyBytes, 0, keyLengthBytes)

        sharedSecret.fill(0)

        return SecretKeySpec(derivedKeyBytes, "AES").also {
            derivedKeyBytes.fill(0)
        }
    }

    private fun setupBouncyCastle() {
        val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
        if (provider != null && provider::class.java != BouncyCastleProvider::class.java) {
            // Remove Android's built-in stripped BC provider
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        }
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun raw32BytesToX25519PublicKey(rawBytes: ByteArray): PublicKey {
        require(rawBytes.size == 32) { "X25519 raw public key must be exactly 32 bytes" }
        setupBouncyCastle()

        val pubParams = X25519PublicKeyParameters(rawBytes, 0)
        val spki = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubParams)
        val keyFactory = KeyFactory.getInstance("X25519", "BC")

        return keyFactory.generatePublic(X509EncodedKeySpec(spki.encoded))
    }

}
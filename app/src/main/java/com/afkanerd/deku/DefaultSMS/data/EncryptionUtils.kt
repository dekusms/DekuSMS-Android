package com.afkanerd.deku.DefaultSMS.data

import com.afkanerd.deku.DefaultSMS.extensions.isValidTimestampWithinHours
import com.afkanerd.deku.DefaultSMS.extensions.toRaw32Bytes
import org.bouncycastle.crypto.EphemeralKeyPair
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.bouncycastle.jcajce.interfaces.XDHPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.time.format.DateTimeParseException
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.DestroyFailedException
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class EncryptionUtils {
    init {
        setupBouncyCastle()
    }

    class Handshake(
        address: String
    ) {
        fun hasRequests(address: String) : Boolean {
            TODO("Check if user has a pending request")
        }


        fun save() {
            TODO("Save this handshake against the address")
        }

        companion object {
            /** HANDSHAKE_REQ_SIZE =
             *
             * 16 Byte session ID (UUID)
             * 32 Bytes x25519 static public key
             * 8 Bytes timestamp
             */
            const val HANDSHAKE_REQ_SIZE = 56

            /** HANDSHAKE_ACK_SIZE =
             *
             * 16 Byte session ID (UUID)
             * 32 Bytes x25519 static public key
             * 60 Bytes x25519 chacha encrypted ephemeral public key
             * 32 Bytes chacha encrypted payload
             */
            const val HANDSHAKE_ACK_SIZE = 140
            const val MAX_VALID_HOURS = 3L
            const val ADDITIONAL_DATA_PAYLOAD = "DekuSMS-X25519-CHACHAPOLY1305-HANDSHAKE-PAYLOAD"
            const val ADDITIONAL_DATA_EPHEMERAL_KEY = "DekuSMS-X25519-CHACHAPOLY1305-HANDSHAKE-EPHEMERAL_KEY"

            @OptIn(ExperimentalUuidApi::class)
            fun makeRequest() : Pair<Pair<ByteArray, ByteArray>, CloseableKeyPair> {
                val sessionId = Uuid.random().toByteArray()

                val timestamp = ByteBuffer.allocate(Long.SIZE_BYTES)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putLong(System.currentTimeMillis())
                    .array()

                val keyPair = EncryptionUtils().generateX25519Keys()
                val request = sessionId + keyPair.keyPair.public.toRaw32Bytes() + timestamp
                return Pair(Pair(sessionId, request), keyPair)
            }

            fun isIncomingRequest(data: ByteArray) : Boolean{
                return data.size == HANDSHAKE_REQ_SIZE && data.takeLast(8).toByteArray().run {
                    isValidTimestampWithinHours(MAX_VALID_HOURS)
                }
            }

            data class AcknowledgementResponse(
                val sharedSecret: SecretKey,
                val ephemeralKeyPair: KeyPair,
                val response: ByteArray,
            )

            @OptIn(ExperimentalUuidApi::class)
            fun makeAcknowledgementRequest(
                sessionId: ByteArray,
                peerPublicKey: ByteArray,
            ) : AcknowledgementResponse {
                val encryptionUtils = EncryptionUtils()
                val ppk = encryptionUtils.raw32BytesToX25519PublicKey(peerPublicKey)
                encryptionUtils.generateX25519Keys().use { keypair ->
                    val sharedSecret = encryptionUtils.computeX25519SharedSecret(
                        keypair.keyPair.private,
                        ppk,
                    )

                    val plaintext: ByteArray = Random.nextBytes(4)
                    try {
                        encryptionUtils.generateX25519Keys().use { ephemeralKeypair ->
                            val encryptionSecret = encryptionUtils.computeX25519SharedSecret(
                                ephemeralKeypair.keyPair.private,
                                ppk,
                            )

                            val encryptedEphemeralKey = encryptionUtils.encrypt(
                                key = encryptionSecret,
                                plaintext = ephemeralKeypair.keyPair.public.toRaw32Bytes(),
                                associatedData = ADDITIONAL_DATA_EPHEMERAL_KEY.toByteArray()
                            )

                            val ciphertext = encryptionUtils.encrypt(
                                key = encryptionSecret,
                                plaintext = plaintext,
                                associatedData = ADDITIONAL_DATA_EPHEMERAL_KEY.toByteArray()
                            )

                            val response = sessionId +
                                    keypair.keyPair.public.toRaw32Bytes() +
                                    encryptedEphemeralKey +
                                    ciphertext

                            return AcknowledgementResponse(
                                sharedSecret = sharedSecret,
                                ephemeralKeyPair = ephemeralKeypair.keyPair,
                                response = response
                            )
                        }
                    } catch (e: Exception) {
                        throw e
                    }
                }
            }
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

    fun raw32BytesToX25519PrivateKey(rawBytes: ByteArray): PrivateKey {
        require(rawBytes.size == 32) { "X25519 raw public key must be exactly 32 bytes" }
        setupBouncyCastle()

        val pubParams = X25519PublicKeyParameters(rawBytes, 0)
        val spki = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubParams)
        val keyFactory = KeyFactory.getInstance("X25519", "BC")

        return keyFactory.generatePrivate(X509EncodedKeySpec(spki.encoded))
    }

    private val secureRandom = SecureRandom()

    fun encrypt(
        key: SecretKey,
        plaintext: ByteArray,
        associatedData: ByteArray? = null
    ): ByteArray {
        require(key.encoded?.size == 32) { "ChaCha20-Poly1305 requires a 256-bit (32-byte) key" }

        val nonce = ByteArray(NONCE_SIZE_BYTES).also { secureRandom.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(nonce))

        associatedData?.let { cipher.updateAAD(it) }

        val ciphertextWithTag = cipher.doFinal(plaintext)
        return ByteArray(NONCE_SIZE_BYTES + ciphertextWithTag.size).apply {
            System.arraycopy(nonce, 0, this, 0, NONCE_SIZE_BYTES)
            System.arraycopy(ciphertextWithTag, 0, this, NONCE_SIZE_BYTES, ciphertextWithTag.size)
        }
    }

    fun decrypt(
        key: SecretKey,
        payload: ByteArray,
        associatedData: ByteArray? = null
    ): ByteArray {
        require(key.encoded?.size == 32) { "ChaCha20-Poly1305 requires a 256-bit (32-byte) key" }
        require(payload.size > NONCE_SIZE_BYTES) { "Payload is too short to contain a nonce" }

        val nonce = payload.copyOfRange(0, NONCE_SIZE_BYTES)
        val ciphertextWithTag = payload.copyOfRange(NONCE_SIZE_BYTES, payload.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(nonce))

        associatedData?.let { cipher.updateAAD(it) }

        return cipher.doFinal(ciphertextWithTag)
    }

    fun keyFromBytes(rawKey: ByteArray): SecretKey {
        require(rawKey.size == 32) { "Key must be exactly 32 bytes" }
        return SecretKeySpec(rawKey, KEY_ALGORITHM)
    }

    companion object {
        private const val TRANSFORMATION = "ChaCha20-Poly1305"
        private const val KEY_ALGORITHM = "ChaCha20"
        private const val NONCE_SIZE_BYTES = 12
        private const val KEY_SIZE_BITS = 256
    }

}
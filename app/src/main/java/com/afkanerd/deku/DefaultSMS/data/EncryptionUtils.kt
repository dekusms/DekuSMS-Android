package com.afkanerd.deku.DefaultSMS.data

import android.content.Context
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.data.EncryptionUtils.Handshake.Companion.ADDITIONAL_DATA_EPHEMERAL_KEY
import com.afkanerd.deku.DefaultSMS.data.EncryptionUtils.Handshake.Companion.KEY_ALGORITHM
import com.afkanerd.deku.DefaultSMS.data.EncryptionUtils.Handshake.Companion.NONCE_SIZE_BYTES
import com.afkanerd.deku.DefaultSMS.data.EncryptionUtils.Handshake.Companion.OOB_INFO
import com.afkanerd.deku.DefaultSMS.data.EncryptionUtils.Handshake.Companion.TRANSFORMATION
import com.afkanerd.deku.DefaultSMS.data.models.SecureConversation
import com.afkanerd.deku.DefaultSMS.data.models.SecureConversationType
import com.afkanerd.deku.DefaultSMS.extensions.isValidTimestampWithinHours
import com.afkanerd.deku.DefaultSMS.extensions.toRaw32Bytes
import com.afkanerd.smswithoutborders_libsmsmms.data.Addresses
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import org.bouncycastle.crypto.EphemeralKeyPair
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.bouncycastle.jcajce.interfaces.XDHPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.helpers.Reporter.info
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
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
            const val HANDSHAKE_ACK_SIZE = 48
            const val MAX_VALID_HOURS = 3L
            const val ADDITIONAL_DATA_EPHEMERAL_KEY = "DekuSMS-X25519-CHACHAPOLY1305-HANDSHAKE-EPHEMERAL_KEY"

            data class InitiationRequest(
                val sessionId: ByteArray,
                val publicKey: ByteArray,
                val request: ByteArray,
                val timestamp: ByteArray,
            )

            fun makeRequest(
                context: Context,
                address: Addresses,
            ) : InitiationRequest {
                val sessionId = Uuid.random().toByteArray()

                val timestamp = ByteBuffer.allocate(Long.SIZE_BYTES)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putLong(System.currentTimeMillis())
                    .array()

                val keyPair = EncryptionUtils().generateX25519Keys()
                val publicKey = keyPair.keyPair.public.toRaw32Bytes()
                val request = sessionId + publicKey + timestamp

                keyPair.use { kp ->
                    val database = Datastore.getDatastore(context)
                    database.secureConversationDao().insert(
                        SecureConversation(
                            type = SecureConversationType.REQUEST.value,
                            sessionId = sessionId,
                            address = address.getFullE164Address,
                            timestamp = timestamp,
                            privateKey = kp.keyPair.private.toRaw32Bytes(),
                            publicKey = publicKey,
                            isRequester = true
                        )
                    )
                }
                return InitiationRequest(
                    sessionId,
                    publicKey,
                    request,
                    timestamp
                )
            }

            fun save() {

            }

            fun isIncomingRequest(data: ByteArray) : Boolean{
                return data.size == HANDSHAKE_REQ_SIZE && data.takeLast(8).toByteArray().run {
                    isValidTimestampWithinHours(MAX_VALID_HOURS)
                }
            }


            data class AcceptInitiationRequest(
                val sessionId: ByteArray,
                val peerPublicKey: ByteArray,
                val timestamp: ByteArray,
            )

            fun acceptRequest(
                context: Context,
                address: Addresses,
                data: ByteArray,
            ) : AcceptInitiationRequest {
                val sessionId = data.take(16).toByteArray()
                val timestamp = data.takeLast(8).toByteArray()
                if (!timestamp.isValidTimestampWithinHours(MAX_VALID_HOURS)) {
                    throw IllegalArgumentException("Invalid timestamp")
                }
                val peerPublicKey = data.copyOfRange(16, 16 + 32)

                val database = Datastore.getDatastore(context)
                database.secureConversationDao().insert(
                    SecureConversation(
                        type = SecureConversationType.ACKNOWLEDGE.value,
                        sessionId = sessionId,
                        address = address.getFullE164Address,
                        timestamp = timestamp,
                        privateKey = ByteArray(0),
                        publicKey = ByteArray(0),
                        peerPublicKey = peerPublicKey,
                        isRequester = false
                    )
                )

                return AcceptInitiationRequest(
                    sessionId,
                    peerPublicKey,
                    timestamp
                )
            }

            fun isAckResponse(data: ByteArray): Boolean {
                return data.size == HANDSHAKE_ACK_SIZE
            }

            data class AcknowledgementResponse(
                val sharedSecret: SecretKey,
                val publicKey: PublicKey,
                val response: ByteArray,
            )

            fun makeAcknowledgementRequest(
                context: Context,
                secureConversation: SecureConversation
            ) : AcknowledgementResponse {
                val encryptionUtils = EncryptionUtils()
                encryptionUtils.generateX25519Keys().use { keypair ->
                    require(secureConversation.peerPublicKey != null) { "Peer public key cannot be null"}

                    val sharedSecret = encryptionUtils.computeX25519SharedSecret(
                        keyPair = keypair.keyPair,
                        requesterPublicKey = secureConversation.peerPublicKey,
                        recipientPublicKey = keypair.keyPair.public.toRaw32Bytes(),
                        peerPublicKey = secureConversation.peerPublicKey,
                        saltData = secureConversation.timestamp
                    )

                    val response = secureConversation.sessionId +
                            keypair.keyPair.public.toRaw32Bytes()

                    val database = Datastore.getDatastore(context)
                    database.secureConversationDao().update(
                        secureConversation.apply {
                            this.privateKey = keypair.keyPair.private.toRaw32Bytes()
                            this.publicKey = keypair.keyPair.public.toRaw32Bytes()
                            this.type = SecureConversationType.ACKNOWLEDGED.value
                            this.sharedSecret = sharedSecret.encoded
                        }
                    )

                    return AcknowledgementResponse(
                        sharedSecret = sharedSecret,
                        publicKey = keypair.keyPair.public,
                        response = response
                    )
                }
            }

            data class AcknowledgementRequest(
                val sessionId: ByteArray,
                val peerPublicKey: ByteArray,
            )

            fun acceptAckRequest(
                response: ByteArray,
                secureConversation: SecureConversation
            ) : AcknowledgementRequest{
                val sessionId = response.take(16).toByteArray()

                val pc = 16 + 32
                val peerPublicKey = response.copyOfRange(16, pc)

                val encryptionUtils = EncryptionUtils()
                val sharedSecret = encryptionUtils.computeX25519SharedSecret(
                    keyPair = keypair.keyPair,
                    requesterPublicKey = secureConversation.peerPublicKey,
                    recipientPublicKey = keypair.keyPair.public.toRaw32Bytes(),
                    peerPublicKey = secureConversation.peerPublicKey,
                    saltData = secureConversation.timestamp
                )

                return AcknowledgementRequest(
                    sessionId = sessionId,
                    peerPublicKey = peerPublicKey,
                )
            }

            fun generateOobVerification(
                requesterPublicKey: ByteArray,
                recipientPublicKey: ByteArray,
                requesterAddress: ByteArray,
                recipientAddress: ByteArray,
            ) : ByteArray {
                return MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                        OOB_INFO.toByteArray() +
                                requesterPublicKey +
                                recipientPublicKey +
                                requesterAddress +
                                recipientAddress
                    )
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
        keyPair: KeyPair,
        requesterPublicKey: ByteArray,
        recipientPublicKey: ByteArray,
        peerPublicKey: ByteArray,
        saltData: ByteArray,
    ): SecretKey {
        val keyAgreement = KeyAgreement.getInstance("X25519", "BC")
        keyAgreement.init(keyPair.private)
        keyAgreement.doPhase(raw32BytesToX25519PublicKey(peerPublicKey), true)

        val salt = MessageDigest
            .getInstance("SHA-256")
            .digest(requesterPublicKey + recipientPublicKey + saltData)

        return deriveAesKeyFromSharedSecret(
            sharedSecret = keyAgreement.generateSecret(),
            salt = salt,
            info = ADDITIONAL_DATA_EPHEMERAL_KEY.encodeToByteArray(),
        )
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

    fun getX25519KeyPairFromRawPrivate(rawPrivateKeyBytes: ByteArray): KeyPair {
        require(rawPrivateKeyBytes.size == 32) { "X25519 raw private key must be 32 bytes" }

        // Ensure Bouncy Castle provider is registered
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }

        // 1. Load private key parameters and derive the public key
        val privParams = X25519PrivateKeyParameters(rawPrivateKeyBytes, 0)
        val pubParams = privParams.generatePublicKey()

        // 2. Wrap into standard JCA PrivateKey and PublicKey objects
        val keyFactory = KeyFactory.getInstance("X25519", BouncyCastleProvider.PROVIDER_NAME)

        // Convert Bouncy Castle params to JCA keys via ASN.1/DER encoding
        val privateKeyInfo = org.bouncycastle.crypto.util.PrivateKeyInfoFactory.createPrivateKeyInfo(privParams)
        val publicKeyInfo = org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubParams)

        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKeyInfo.encoded))
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyInfo.encoded))

        return KeyPair(publicKey, privateKey)
    }

    companion object {
        private const val TRANSFORMATION = "ChaCha20-Poly1305"
        private const val KEY_ALGORITHM = "ChaCha20"
        private const val NONCE_SIZE_BYTES = 12
        private const val OOB_INFO = "DekuSMS-OOB-v1"
    }

}
package com.afkanerd.deku.DefaultSMS.extensions

import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.jcajce.interfaces.XDHPrivateKey
import org.bouncycastle.jcajce.interfaces.XDHPublicKey
import java.security.PrivateKey
import java.security.PublicKey

fun PublicKey.toRaw32Bytes(): ByteArray {
    val xdhKey = this as XDHPublicKey
    return xdhKey.uEncoding // Returns the exact 32-byte raw public key
}

fun PrivateKey.toRaw32Bytes(): ByteArray {
    // Parse PKCS#8 bytes into lightweight parameters to extract the raw 32 bytes
    val keyParams = PrivateKeyFactory.createKey(this.encoded) as X25519PrivateKeyParameters
    return keyParams.encoded
}


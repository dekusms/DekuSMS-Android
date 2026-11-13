package com.afkanerd.deku.DefaultSMS.extensions

import org.bouncycastle.jcajce.interfaces.XDHPublicKey
import java.security.PublicKey

fun PublicKey.toRaw32Bytes(): ByteArray {
    val xdhKey = this as XDHPublicKey
    return xdhKey.uEncoding // Returns the exact 32-byte raw public key
}


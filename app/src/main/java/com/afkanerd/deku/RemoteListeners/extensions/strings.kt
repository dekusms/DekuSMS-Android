package com.afkanerd.deku.RemoteListeners.extensions

import android.util.Base64

fun String.isBase64Encoded(): Boolean {
    try {
        val decodedBytes: ByteArray? = Base64.decode(this, Base64.DEFAULT)

        val reEncodedString: String = Base64.encodeToString(decodedBytes,
            Base64.DEFAULT).replace("\\n", "")

        return this.replace("\\n".toRegex(), "") == reEncodedString
    } catch (e: Exception) {
        return false
    }
}

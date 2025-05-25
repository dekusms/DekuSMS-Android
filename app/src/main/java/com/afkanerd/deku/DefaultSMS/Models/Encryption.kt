package com.afkanerd.deku.DefaultSMS.Models

open class Encryption {

    var id: Int = 0

    var state: ByteArray? = null

    var peerPublicKey: ByteArray? = null

    var publicKey: ByteArray? = null

    var peerAddress: String? = null

    var version: Int = 0
}
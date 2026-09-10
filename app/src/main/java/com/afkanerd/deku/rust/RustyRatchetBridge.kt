package com.afkanerd.deku.rust

object RustyRatchetBridge {

    init {
        System.loadLibrary("rusty_ratchet_bridge")
    }

    external fun newState(): Long
}
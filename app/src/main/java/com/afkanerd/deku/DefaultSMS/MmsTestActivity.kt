package com.afkanerd.deku.DefaultSMS

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction

class MmsTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mmsInternalInjectionTest()
    }

    fun mmsInternalInjectionTest() {
        val settings = Settings()
        settings.useSystemSending = true

        val transaction = Transaction(this, settings)

        val message = Message("Hello world 1", "+237652156811")
        message.setImage(ContextCompat.getDrawable(this, R.drawable.ic_stat_name)!!.toBitmap())
        transaction.sendNewMessage(message, Transaction.NO_THREAD_ID)
//        transaction.sendNewMessage(message, 1)
    }
}
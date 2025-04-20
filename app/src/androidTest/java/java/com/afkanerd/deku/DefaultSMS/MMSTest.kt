package java.com.afkanerd.deku.DefaultSMS

import android.Manifest
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.afkanerd.deku.DefaultSMS.R
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MMSTest {

    @get:Rule
    val grantReadSmsPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_SMS)

    @get:Rule
    val grantReadMmsPermissionRule = GrantPermissionRule.grant(Manifest.permission.SEND_SMS)

    @Test
    fun mmsInternalInjectionTest() {
        val settings = Settings()
        settings.useSystemSending = true

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val transaction = Transaction(context, settings)

        val message = Message("Hello world 1", "+237652156811")
        message.setImage(ContextCompat.getDrawable(context, R.drawable.ic_stat_name)!!.toBitmap())
        transaction.sendNewMessage(message, Transaction.NO_THREAD_ID)
    }
}
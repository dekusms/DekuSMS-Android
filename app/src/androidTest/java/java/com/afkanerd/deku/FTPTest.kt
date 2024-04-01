package java.com.afkanerd.deku

import android.content.Context
import android.util.Log
import androidx.paging.LOG_TAG
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.afkanerd.deku.DefaultSMS.R
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.InetAddress
import java.nio.charset.Charset
import java.util.Properties


@SmallTest
class FTPTest {

    val properties: Properties = Properties()
    lateinit var context: Context
    val ftpClient: FTPClient = FTPClient()

    @Before
    fun init() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val inputStream = context.resources.openRawResource(R.raw.ftp)
        properties.load(inputStream)

    }

    @Test
    fun ftpConnection() {
        val host = properties.getProperty("host") + ":" + properties.getProperty("port")
        ftpClient.connect(InetAddress.getByName(host));
        ftpClient.login(properties.getProperty("username"), properties.getProperty("password"));
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        // TODO: this should become a configuration
//            ftpClient.enterLocalPassiveMode()

        if(ftpClient.replyCode in 200..299) {
            if(ftpClient.changeWorkingDirectory(properties.getProperty("directory"))) {
                ftpClient.makeDirectory(properties.getProperty("directory"))
                ftpClient.changeWorkingDirectory(properties.getProperty("directory"))
            }
            val body = saveToJson()
            ftpClient.storeFile(properties.getProperty("remotePath"),
                    body.byteInputStream(Charset.defaultCharset()))
        } else {
            Log.e(LOG_TAG, "Failed to connect to FTP server")
        }
    }

    fun saveToJson(): String {
        val json = JSONObject()
        json.put("component1", "url")
        json.put("component2", "url")
        return json.toString()
    }

}
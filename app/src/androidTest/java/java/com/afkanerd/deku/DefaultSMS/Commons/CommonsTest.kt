package java.com.afkanerd.deku.DefaultSMS.Commons

import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ThreadedConversationsViewModel
import org.junit.Test

@SmallTest
class CommonsTest {

    @Test
    fun commonsTest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val data = "[{" +
                "    \"_mk\": null," +
                "    \"address\": \"+16505556789_19\"," +
                "    \"data\": null," +
                "    \"date\": \"1733237444789\"," +
                "    \"date_sent\": null," +
                "    \"error_code\": 0," +
                "    \"formatted_date\": null," +
                "    \"id\": 949," +
                "    \"isIs_encrypted\": false," +
                "    \"isIs_image\": false," +
                "    \"isIs_key\": false," +
                "    \"isRead\": true," +
                "    \"message_id\": \"19949\"," +
                "    \"num_segments\": 0," +
                "    \"status\": 32," +
                "    \"subscription_id\": 0," +
                "    \"tag\": null," +
                "    \"text\": \"sasdfasdfasdfasdf\"," +
                "    \"thread_id\": \"19\"," +
                "    \"type\": 3" +
                "  }]"
        val tViewModel = ThreadedConversationsViewModel()
        val response = tViewModel.importAll(context, data)
        println(response)
    }

}
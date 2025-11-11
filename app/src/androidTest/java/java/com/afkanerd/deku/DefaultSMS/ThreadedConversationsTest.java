package java.com.afkanerd.deku.DefaultSMS;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.afkanerd.deku.DefaultSMS.DAO.ConversationDao;
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ThreadedConversationsTest {

    Context context;

   public ThreadedConversationsTest() {
       context = InstrumentationRegistry.getInstrumentation().getTargetContext();
   }
}

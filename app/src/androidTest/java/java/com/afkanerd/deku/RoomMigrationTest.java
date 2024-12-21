package java.com.afkanerd.deku;

import android.content.Context;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteStatement;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.afkanerd.deku.Datastore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)

public class RoomMigrationTest {
    private static final String TEST_DB = Datastore.databaseName;

    @Rule
    public MigrationTestHelper helper;

    Context context;
    public RoomMigrationTest() {
        this.context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                Datastore.class.getCanonicalName(), new FrameworkSQLiteOpenHelperFactory());
    }

}

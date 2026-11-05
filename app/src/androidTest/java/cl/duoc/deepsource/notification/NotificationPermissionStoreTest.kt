package cl.duoc.deepsource.notification

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationPermissionStoreTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("notification_permission", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("notification_permission", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun firstStartDefaultsOffAndPreferenceSurvivesRecreation() {
        val store = NotificationPermissionStore(context)
        assertFalse(store.notificationsEnabled())

        assertTrue(store.setNotificationsEnabled(true))
        assertTrue(NotificationPermissionStore(context).notificationsEnabled())

        assertFalse(store.setNotificationsEnabled(false))
        assertFalse(NotificationPermissionStore(context).notificationsEnabled())
    }
}

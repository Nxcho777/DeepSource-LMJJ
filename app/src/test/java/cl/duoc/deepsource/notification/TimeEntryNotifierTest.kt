package cl.duoc.deepsource.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import cl.duoc.deepsource.domain.model.TimeEntry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TimeEntryNotifierTest {
    @Test
    fun respectsOptInAndPostsOneNotificationPerSavedEntry() {
        val context: Context = RuntimeEnvironment.getApplication()
        val manager = context.getSystemService(NotificationManager::class.java)
        val store = NotificationPermissionStore(context)
        val entry = TimeEntry("entry-ev4", "atlas", "u", 75, "2026-11-26", "Registro")
        val notifier = AndroidTimeEntryNotifier(context) { true }
        assertFalse(notifier.notifySaved(entry, "Atlas"))
        assertTrue(manager.activeNotifications.isEmpty())
        store.setNotificationsEnabled(true)
        assertFalse(AndroidTimeEntryNotifier(context) { false }.notifySaved(entry, "Atlas"))
        assertTrue(manager.activeNotifications.isEmpty())
        assertTrue(notifier.notifySaved(entry, "Atlas"))
        assertTrue(notifier.notifySaved(entry, "Atlas"))
        val active = manager.activeNotifications.single()
        assertEquals("entry-ev4", active.tag)
        assertEquals("Horas registradas correctamente", active.notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("Se registraron 1 h 15 min en Atlas", active.notification.extras.getString(Notification.EXTRA_TEXT))
        assertNotNull(active.notification.contentIntent)
        assertEquals(AndroidTimeEntryNotifier.CHANNEL_ID, active.notification.channelId)
        assertTrue(AndroidTimeEntryNotifier.canPostNotifications(context))
        shadowOf(manager).setNotificationsEnabled(false)
        assertFalse(AndroidTimeEntryNotifier.canPostNotifications(context))
    }
}

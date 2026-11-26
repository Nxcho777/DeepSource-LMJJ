package cl.duoc.deepsource.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Looper
import android.widget.FrameLayout
import android.widget.TextView
import cl.duoc.deepsource.MainActivity
import cl.duoc.deepsource.R
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.navigation.AppDestination
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import org.robolectric.util.ReflectionHelpers
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class WidgetFlowsTest {
    private val app get() = RuntimeEnvironment.getApplication() as Application

    @Before
    fun resetSingleton() {
        ReflectionHelpers.setStaticField(DeepSourceDatabase::class.java, "instance", null)
    }

    @After
    fun closeSingleton() {
        ReflectionHelpers.getStaticField<DeepSourceDatabase?>(DeepSourceDatabase::class.java, "instance")?.close()
        resetSingleton()
    }

    @Test
    fun listExcludesArchivedAndCompletedProjectsAndRendersProgress() = runBlocking {
        val database = DeepSourceDatabase.getInstance(app)
        val repo = DeepSourceRepository(database)
        repo.seedIfNeeded()
        database.dao().archiveProject("project-2")
        repo.finishProject("user-admin", "project-3")
        repo.addTimeEntry("project-4", "user-1", 2000, "2026-11-26", "Sobretiempo")
        val controller = Robolectric.buildService(DeepSourceWidgetService::class.java).create()
        try {
            val factory = controller.get().onGetViewFactory(Intent())
            factory.onCreate()
            factory.onDataSetChanged()
            assertEquals(2, factory.count)
            val atlas = factory.getViewAt(0)!!.apply(app, FrameLayout(app))
            assertEquals("Proyecto Atlas", atlas.findViewById<TextView>(R.id.widget_project_name).text.toString())
            assertEquals("25%", atlas.findViewById<TextView>(R.id.widget_project_percent).text.toString())
            val overtime = factory.getViewAt(1)!!.apply(app, FrameLayout(app))
            assertEquals("Lumen CRM", overtime.findViewById<TextView>(R.id.widget_project_name).text.toString())
            assertTrue(overtime.findViewById<TextView>(R.id.widget_project_percent).text.toString().removeSuffix("%").toInt() > 100)
            assertEquals("project-1".hashCode().toLong(), factory.getItemId(0))
            assertTrue(factory.hasStableIds())
            assertEquals(1, factory.viewTypeCount)
            assertNull(factory.getViewAt(9))
            assertNull(factory.loadingView)
            factory.onDestroy()
            assertEquals(0, factory.count)
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun widgetOpensManualRegistrationOrControlsTheSelectedTimer() {
        val manager = AppWidgetManager.getInstance(app)
        val widgetHost = shadowOf(manager)
        val id = widgetHost.createWidget(DeepSourceWidgetProvider::class.java, R.layout.widget_deepsource)
        val provider = widgetHost.getAppWidgetProviderFor(id)
        fun action(name: String) = Intent("cl.duoc.deepsource.widget.$name")
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        fun status() = widgetHost.getViewFor(id).findViewById<TextView>(R.id.widget_timer_status).text.toString()
        val select = action("SELECT").putExtra("widget_project_id", "project-1")
            .putExtra("widget_project_name", "Atlas").putExtra("widget_project_icon_index", 2)
        provider.onReceive(app, Intent(select).putExtra("widget_open_register", true))
        val started = shadowOf(app).nextStartedActivity
        assertEquals(MainActivity::class.java.name, started.component?.className)
        assertEquals(AppDestination.RegisterTime.route, started.getStringExtra(MainActivity.EXTRA_DESTINATION))
        assertEquals("project-1", started.getStringExtra(MainActivity.EXTRA_PROJECT_ID))

        provider.onReceive(app, select)
        assertEquals("Listo para iniciar", status())
        WidgetTimer.setTask(app, id, "general-project-1", "Trabajo general")
        provider.onReceive(app, action("START"))
        assertEquals("En progreso", status())
        ShadowSystemClock.advanceBy(Duration.ofSeconds(65))
        provider.onReceive(app, action("AURORA_FRAME"))
        provider.onReceive(app, action("PAUSE"))
        assertEquals("En pausa", status())
        assertTrue(WidgetTimer.elapsed(app, id) >= 65000)
        provider.onReceive(app, action("PAUSE"))
        assertEquals("En progreso", status())
        provider.onReceive(app, action("BACK"))
        assertFalse(WidgetTimer.isDetail(app, id))
        provider.onReceive(app, select)
        provider.onAppWidgetOptionsChanged(app, manager, id, Bundle())
        DeepSourceWidgetProvider.requestRefresh(app)
        provider.onDeleted(app, intArrayOf(id))
        assertNull(WidgetTimer.project(app, id))
    }

    @Test
    fun widgetStoresElapsedTimeAgainstTheTaskAndRestoresAnUnsuccessfulSave() {
        val repo = DeepSourceRepository.getInstance(app)
        val tasks = runBlocking { repo.getTasks("project-1") }
        assertTrue(tasks.size >= 2)
        val manager = AppWidgetManager.getInstance(app)
        val host = shadowOf(manager)
        val id = host.createWidget(DeepSourceWidgetProvider::class.java, R.layout.widget_deepsource)
        val provider = host.getAppWidgetProviderFor(id)
        val prefix = "cl.duoc.deepsource.widget."
        fun intent(suffix: String) = Intent(prefix + suffix).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        fun broadcast(suffix: String) {
            app.sendBroadcast(intent(suffix))
            shadowOf(Looper.getMainLooper()).idle()
        }
        fun await(condition: () -> Boolean) {
            val deadline = System.nanoTime() + 5_000_000_000L
            while (!condition() && System.nanoTime() < deadline) Thread.sleep(20)
            assertTrue(condition())
        }
        app.registerReceiver(provider, IntentFilter().apply {
            addAction(prefix + "NEXT_TASK")
            addAction(prefix + "START")
            addAction(prefix + "FINISH")
        })
        try {
            WidgetTimer.select(app, id, "project-1", "Atlas", 0)
            broadcast("NEXT_TASK")
            await { WidgetTimer.taskId(app, id) == tasks[0].id }
            broadcast("NEXT_TASK")
            await { WidgetTimer.taskId(app, id) == tasks[1].id }
            WidgetTimer.select(app, id, "project-1", "Atlas", 0)
            broadcast("START")
            await { WidgetTimer.status(app, id) == "running" && WidgetTimer.taskId(app, id) == tasks[0].id }
            ShadowSystemClock.advanceBy(Duration.ofSeconds(61))
            broadcast("FINISH")
            await { WidgetTimer.project(app, id) == null }
            val minutes = runBlocking { DeepSourceDatabase.getInstance(app).dao().getTaskMinutes(tasks[0].id) }
            assertEquals(2, minutes)

            WidgetTimer.select(app, id, "missing-project", "No disponible", 0)
            WidgetTimer.setTask(app, id, "missing-task", "No disponible")
            provider.onReceive(app, intent("START"))
            ShadowSystemClock.advanceBy(Duration.ofSeconds(20))
            broadcast("FINISH")
            await { WidgetTimer.status(app, id) == "paused" }
            assertEquals("missing-task", WidgetTimer.taskId(app, id))
            assertTrue(WidgetTimer.elapsed(app, id) >= 20_000L)
        } finally {
            app.unregisterReceiver(provider)
            provider.onDeleted(app, intArrayOf(id))
        }
    }
}

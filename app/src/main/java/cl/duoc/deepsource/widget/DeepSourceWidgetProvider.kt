package cl.duoc.deepsource.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import cl.duoc.deepsource.MainActivity
import cl.duoc.deepsource.R
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.ProjectStatus
import cl.duoc.deepsource.navigation.AppDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeepSourceWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = DeepSourceRepository.getInstance(context)
                val projects = repository.getProjects()
                val userName = repository.getUser(DeepSourceRepository.DEFAULT_USER_ID)?.name.orEmpty()
                ids.forEach { id -> manager.updateAppWidget(id, createViews(context, userName, projects)) }
            } catch (error: Exception) {
                Log.e("DeepSourceWidget", "No se pudo actualizar el widget", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createViews(context: Context, userName: String, projects: List<Project>): RemoteViews = RemoteViews(context.packageName, R.layout.widget_deepsource).apply {
        setTextViewText(R.id.widget_user, userName)
        removeAllViews(R.id.widget_projects)
        // Four rows is the deliberate visual contract of this widget. Keeping that
        // limit also prevents a long project list from making a launcher widget
        // unexpectedly tall.
        projects.take(MAX_VISIBLE_PROJECTS).forEach { project ->
            val row = RemoteViews(context.packageName, R.layout.widget_project_row)
            val statusLabel = project.status.widgetLabel()
            row.setTextViewText(R.id.widget_project_name, project.name)
            row.setTextViewText(R.id.widget_project_state, statusLabel)
            row.setTextViewText(R.id.widget_project_hours, project.registeredTimeLabel)
            row.setInt(R.id.widget_project_status, "setColorFilter", project.status.color.toInt())
            row.setContentDescription(R.id.widget_project_status, statusLabel)
            row.setOnClickPendingIntent(R.id.widget_project_row, registerIntent(context, project.id))
            addView(R.id.widget_projects, row)
        }
        setOnClickPendingIntent(R.id.widget_add_time, registerIntent(context, null))
    }

    private fun registerIntent(context: Context, projectId: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_REGISTER_TIME
            putExtra(MainActivity.EXTRA_DESTINATION, AppDestination.RegisterTime.route)
            projectId?.let { putExtra(MainActivity.EXTRA_PROJECT_ID, it) }
        }
        return PendingIntent.getActivity(context, projectId?.hashCode() ?: 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun ProjectStatus.widgetLabel(): String = when (this) {
        ProjectStatus.Finished -> "Finalizado"
        else -> label.substringBefore(" ·")
    }

    companion object {
        private const val MAX_VISIBLE_PROJECTS = 4
        private const val ACTION_OPEN_REGISTER_TIME = "cl.duoc.deepsource.action.OPEN_REGISTER_TIME"

        fun requestRefresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, DeepSourceWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                context.sendBroadcast(Intent(context, DeepSourceWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                })
            }
        }
    }
}

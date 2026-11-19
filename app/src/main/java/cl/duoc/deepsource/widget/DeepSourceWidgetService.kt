
package cl.duoc.deepsource.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import cl.duoc.deepsource.MainActivity
import cl.duoc.deepsource.R
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.ProjectStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import android.util.Log

class DeepSourceWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(
        intent: Intent
    ): RemoteViewsFactory {
        return ProjectListFactory(applicationContext)
    }
}

private class ProjectListFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var projects: List<Project> = emptyList()

    override fun onCreate() {
        // Los datos se cargan en onDataSetChanged.
    }

    override fun onDataSetChanged() {
        try {
            projects = runBlocking {
                withContext(Dispatchers.IO) {
                    DeepSourceRepository
                        .getInstance(context)
                        .getProjects()
                        .filterNot { it.isCompleted }
                }
            }
        } catch (error: Exception) {
            Log.e(
                "DeepSourceWidget",
                "Error cargando proyectos del widget",
                error
            )
            projects = emptyList()
        }
    }

    override fun onDestroy() {
        projects = emptyList()
    }

    override fun getCount(): Int = projects.size

    override fun getViewAt(position: Int): RemoteViews? {
        val project = projects.getOrNull(position)
            ?: return null

        val row = RemoteViews(
            context.packageName,
            R.layout.widget_project_row
        )

        row.setTextViewText(
            R.id.widget_project_name,
            project.name
        )

        row.setImageViewResource(
            R.id.widget_project_icon,
            when (project.iconIndex) {
                1 -> R.drawable.ic_icono_2
                2 -> R.drawable.ic_icono_3
                3 -> R.drawable.ic_icono_4
                else -> R.drawable.ic_icono_1
            }
        )

        val percentage = if (project.totalMinutes > 0) {
            (
                    project.registeredMinutes.toDouble() /
                            project.totalMinutes.toDouble() * 100
                    ).toInt()
        } else {
            0
        }

        row.setTextViewText(
            R.id.widget_project_percent,
            "$percentage%"
        )

        val activeBar = when (project.status) {
            ProjectStatus.Initial ->
                R.id.widget_progress_blue

            ProjectStatus.Medium ->
                R.id.widget_progress_yellow

            ProjectStatus.NoTime ->
                R.id.widget_progress_red

            ProjectStatus.Overtime ->
                R.id.widget_progress_black

            ProjectStatus.Finished ->
                R.id.widget_progress_teal
        }

        val progressBars = listOf(
            R.id.widget_progress_blue,
            R.id.widget_progress_yellow,
            R.id.widget_progress_red,
            R.id.widget_progress_black,
            R.id.widget_progress_teal
        )

        progressBars.forEach { barId ->
            row.setViewVisibility(
                barId,
                if (barId == activeBar) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            )

            row.setProgressBar(
                barId,
                100,
                percentage.coerceIn(0, 100),
                false
            )
        }

        val clickIntent = Intent().apply {
            putExtra("widget_project_id", project.id)
            putExtra("widget_project_name", project.name)
        }

        row.setOnClickFillInIntent(
            R.id.widget_project_action,
            clickIntent
        )

        row.setOnClickFillInIntent(
            R.id.widget_project_name,
            clickIntent
        )

        return row
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return projects.getOrNull(position)
            ?.id
            ?.hashCode()
            ?.toLong()
            ?: position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}

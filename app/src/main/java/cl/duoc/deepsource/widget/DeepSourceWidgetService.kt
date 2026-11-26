
package cl.duoc.deepsource.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import cl.duoc.deepsource.MainActivity
import cl.duoc.deepsource.ui.component.ProjectIcons
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
                        .filterNot { it.isCompleted || it.archived }
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
            ProjectIcons.resource(project.iconIndex)
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

        // El fondo del ícono utiliza exactamente el mismo estado que la barra.
        // Los íconos originales y sus índices no se modifican.
        val iconBackground = when (project.status) {
            ProjectStatus.Initial -> R.drawable.widget_icon_progress_blue
            ProjectStatus.Medium -> R.drawable.widget_icon_progress_yellow
            ProjectStatus.NoTime -> R.drawable.widget_icon_progress_red
            ProjectStatus.Overtime -> R.drawable.widget_icon_progress_black
            ProjectStatus.Finished -> R.drawable.widget_icon_progress_teal
        }
        row.setInt(R.id.widget_project_icon, "setBackgroundResource", iconBackground)

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
            putExtra("widget_project_icon_index", project.iconIndex)
        }

        // La fila abre el registro manual; el botón abre el cronómetro del widget.
        row.setOnClickFillInIntent(
            R.id.widget_project_row,
            Intent(clickIntent).putExtra("widget_open_register", true)
        )
        row.setOnClickFillInIntent(R.id.widget_project_action, clickIntent)

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

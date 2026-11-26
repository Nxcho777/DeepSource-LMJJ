package cl.duoc.deepsource.widget

import android.content.Context
import android.os.SystemClock

internal object WidgetTimer {
    private fun prefs(context: Context) = context.getSharedPreferences("deepsource_widget_timer", Context.MODE_PRIVATE)
    private fun key(id: Int, name: String) = "$id.$name"
    fun isDetail(context: Context,id: Int) = prefs(context).getBoolean(key(id,"detail"),false)
    fun showDetail(context: Context,id: Int) { prefs(context).edit().putBoolean(key(id,"detail"),true).apply() }
    fun showList(context: Context,id: Int) { prefs(context).edit().putBoolean(key(id,"detail"),false).apply() }
    fun project(context: Context, id: Int) = prefs(context).getString(key(id,"project"), null)
    fun taskId(context: Context, id: Int) = prefs(context).getString(key(id, "taskId"), null)
    fun taskName(context: Context, id: Int) = prefs(context).getString(key(id, "taskName"), "Seleccionar tarea") ?: "Seleccionar tarea"
    fun setTask(context: Context, id: Int, taskId: String, name: String) {
        prefs(context).edit().putString(key(id, "taskId"), taskId).putString(key(id, "taskName"), name).apply()
    }
    fun iconIndex(context: Context,id: Int) = prefs(context).getInt(key(id,"iconIndex"),0)
    fun name(context: Context, id: Int) = prefs(context).getString(key(id,"name"), "") ?: ""
    fun status(context: Context, id: Int) = prefs(context).getString(key(id,"status"), "ready") ?: "ready"
    private fun stored(context: Context, id: Int) = prefs(context).getLong(key(id,"accumulated"), 0L)
    fun elapsed(context: Context, id: Int): Long {
        val saved = stored(context,id)
        if (status(context,id) != "running") return saved
        val startMono = prefs(context).getLong(key(id,"startMono"), SystemClock.elapsedRealtime())
        val startWall = prefs(context).getLong(key(id,"startWall"), System.currentTimeMillis())
        val monoDelta = SystemClock.elapsedRealtime() - startMono
        val delta = if (monoDelta >= 0L) monoDelta else (System.currentTimeMillis() - startWall).coerceAtLeast(0L)
        return saved + delta.coerceAtLeast(0L)
    }
    fun select(context: Context, id: Int, projectId: String, projectName: String, iconIndex: Int) {
        prefs(context).edit().putString(key(id,"project"),projectId).putString(key(id,"name"),projectName).putInt(key(id,"iconIndex"),iconIndex)
            .remove(key(id,"taskId")).remove(key(id,"taskName"))
            .putBoolean(key(id,"detail"),true).putString(key(id,"status"),"ready").putLong(key(id,"accumulated"),0L).apply()
    }
    fun selectOrRestore(context: Context, id: Int, projectId: String, projectName: String, iconIndex: Int) {
        val current = project(context,id)
        if (current == projectId) {
            showDetail(context,id)
            return
        }
        // No se permite cambiar el proyecto mientras el cronómetro está corriendo:
        // se evita asignar accidentalmente horas al proyecto equivocado.
        if (current != null && status(context,id) == "running") {
            showDetail(context,id)
            return
        }
        val pref = prefs(context)
        if (current != null && status(context,id) == "paused") {
            pref.edit()
                .putLong(key(id,"saved.$current"),elapsed(context,id))
                .putString(key(id,"savedTask.$current"),taskId(context,id))
                .putString(key(id,"savedTaskName.$current"),taskName(context,id))
                .apply()
        }
        val saved = pref.getLong(key(id,"saved.$projectId"),0L)
        select(context,id,projectId,projectName,iconIndex)
        if (saved > 0L) {
            pref.edit().putLong(key(id,"accumulated"),saved)
                .putString(key(id,"taskId"),pref.getString(key(id,"savedTask.$projectId"),null))
                .putString(key(id,"taskName"),pref.getString(key(id,"savedTaskName.$projectId"),null))
                .putString(key(id,"status"),"paused").apply()
        }
    }

    fun start(context: Context, id: Int) {
        if (project(context,id)==null || status(context,id)=="running") return
        prefs(context).edit().putLong(key(id,"startMono"),SystemClock.elapsedRealtime())
            .putLong(key(id,"startWall"),System.currentTimeMillis())
            .putString(key(id,"status"),"running").apply()
    }
    fun pause(context: Context, id: Int) {
        if (status(context,id)!="running") return
        val ms=elapsed(context,id)
        prefs(context).edit().putLong(key(id,"accumulated"),ms).putString(key(id,"status"),"paused").apply()
    }
    fun finish(context: Context,id: Int) {
        pause(context,id)
        prefs(context).edit().putString(key(id,"status"),"saving").apply()
    }
    fun restore(context: Context,id: Int) {
        prefs(context).edit().putString(key(id,"status"),"paused").apply()
    }
    fun clear(context: Context,id: Int) {
        val current = project(context,id)
        if (current != null) prefs(context).edit().remove(key(id,"saved.$current"))
            .remove(key(id,"savedTask.$current")).remove(key(id,"savedTaskName.$current")).apply()
        prefs(context).edit().remove(key(id,"project")).remove(key(id,"name"))
            .remove(key(id,"status")).remove(key(id,"taskId")).remove(key(id,"taskName")).remove(key(id,"iconIndex")).remove(key(id,"detail")).remove(key(id,"accumulated")).apply()
    }
}

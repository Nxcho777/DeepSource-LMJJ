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
    fun select(context: Context, id: Int, projectId: String, projectName: String) {
        prefs(context).edit().putString(key(id,"project"),projectId).putString(key(id,"name"),projectName)
            .putBoolean(key(id,"detail"),true).putString(key(id,"status"),"ready").putLong(key(id,"accumulated"),0L).apply()
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
        prefs(context).edit().remove(key(id,"project")).remove(key(id,"name"))
            .remove(key(id,"status")).remove(key(id,"detail")).remove(key(id,"accumulated")).apply()
    }
}

package cl.duoc.deepsource.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import cl.duoc.deepsource.MainActivity
import cl.duoc.deepsource.R
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.navigation.AppDestination
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class DeepSourceWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val ACTION_SELECT = "cl.duoc.deepsource.widget.SELECT"
        private const val ACTION_START = "cl.duoc.deepsource.widget.START"
        private const val ACTION_PAUSE = "cl.duoc.deepsource.widget.PAUSE"
        private const val ACTION_FINISH = "cl.duoc.deepsource.widget.FINISH"
        private const val ACTION_BACK = "cl.duoc.deepsource.widget.BACK"

        fun requestRefresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context,DeepSourceWidgetProvider::class.java))
            ids.forEach { updateWidget(context,manager,it) }
        }

        private fun action(context: Context, widgetId: Int, kind: String): PendingIntent {
            val intent=Intent(context,DeepSourceWidgetProvider::class.java).apply {
                action=kind
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId)
                data=Uri.parse("deepsource://timer/$widgetId/$kind")
            }
            return PendingIntent.getBroadcast(context,widgetId*17+kind.hashCode(),intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views=RemoteViews(context.packageName,R.layout.widget_deepsource)
            val selected=WidgetTimer.project(context,widgetId)
            val detail=selected!=null && WidgetTimer.isDetail(context,widgetId)
            views.setViewVisibility(R.id.widget_projects,if(detail) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.widget_empty,if(detail) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.widget_timer_panel,if(detail) View.VISIBLE else View.GONE)

            val service=Intent(context,DeepSourceWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId)
                data=Uri.parse("deepsource://widget/projects/$widgetId")
            }
            views.setRemoteAdapter(R.id.widget_projects,service)
            views.setEmptyView(R.id.widget_projects,R.id.widget_empty)
            views.setPendingIntentTemplate(R.id.widget_projects,action(context,widgetId,ACTION_SELECT))
            views.setOnClickPendingIntent(R.id.widget_back,action(context,widgetId,ACTION_BACK))
            views.setOnClickPendingIntent(R.id.widget_start,action(context,widgetId,ACTION_START))
            views.setOnClickPendingIntent(R.id.widget_pause,action(context,widgetId,ACTION_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_finish,action(context,widgetId,ACTION_FINISH))
            val home=Intent(context,MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_DESTINATION,AppDestination.Projects.route)
                action="cl.duoc.deepsource.action.OPEN_PROJECTS"
            }
            views.setOnClickPendingIntent(R.id.widget_header,PendingIntent.getActivity(context,10001,home,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            if(detail) {
                val status=WidgetTimer.status(context,widgetId)
                val elapsed=WidgetTimer.elapsed(context,widgetId)
                views.setTextViewText(R.id.widget_selected_name,WidgetTimer.name(context,widgetId))
                views.setChronometer(R.id.widget_timer,SystemClock.elapsedRealtime()-elapsed,null,status=="running")
                views.setTextViewText(R.id.widget_timer_status,when(status) {
                    "running"->"En progreso";"paused"->"En pausa";"saving"->"Guardando…";else->"Listo para iniciar"
                })
                val ready=status=="ready" || status=="saving"
                views.setViewVisibility(R.id.widget_start,if(ready) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.widget_timer_actions,if(ready) View.GONE else View.VISIBLE)
                views.setTextViewText(R.id.widget_pause,if(status=="paused") "▶  Reanudar" else "Ⅱ  Pausar")
                if(status=="saving") views.setTextViewText(R.id.widget_start,"Guardando…")
            }
            manager.updateAppWidget(widgetId,views)
            if(!detail) manager.notifyAppWidgetViewDataChanged(widgetId,R.id.widget_projects)
        }
    }

    override fun onUpdate(context: Context,manager: AppWidgetManager,ids: IntArray) {
        ids.forEach { updateWidget(context,manager,it) }
    }

    override fun onDeleted(context: Context,ids: IntArray) {
        ids.forEach { WidgetTimer.clear(context,it) }
    }

    override fun onReceive(context: Context,intent: Intent) {
        super.onReceive(context,intent)
        val manager=AppWidgetManager.getInstance(context)
        if(intent.action==AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val ids=intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                ?:manager.getAppWidgetIds(ComponentName(context,DeepSourceWidgetProvider::class.java))
            ids.forEach { updateWidget(context,manager,it) }
            return
        }
        val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
        if(id==AppWidgetManager.INVALID_APPWIDGET_ID) return
        when(intent.action) {
            ACTION_SELECT -> {
                val projectId=intent.getStringExtra("widget_project_id") ?: return
                val name=intent.getStringExtra("widget_project_name") ?: return
                // Evita descartar accidentalmente una sesión en curso.
                if(WidgetTimer.project(context,id)==null || WidgetTimer.status(context,id)=="ready")
                    WidgetTimer.select(context,id,projectId,name)
                else WidgetTimer.showDetail(context,id)
            }
            ACTION_START -> WidgetTimer.start(context,id)
            ACTION_PAUSE -> if(WidgetTimer.status(context,id)=="running") WidgetTimer.pause(context,id)
                else WidgetTimer.start(context,id)
            ACTION_BACK -> WidgetTimer.showList(context,id)
            ACTION_FINISH -> {
                if(WidgetTimer.status(context,id)=="saving") return
                val project=WidgetTimer.project(context,id) ?: return
                val elapsed=WidgetTimer.elapsed(context,id)
                WidgetTimer.finish(context,id)
                updateWidget(context,manager,id)
                val pendingResult=goAsync()
                Thread {
                    try {
                        // El modelo actual almacena minutos enteros; se redondea hacia arriba
                        // para no perder una sesión parcial de menos de un minuto.
                        val minutes=((elapsed+59999L)/60000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                        if(minutes>0) runBlocking {
                            val userId=context.getSharedPreferences("deepsource_widget_timer",Context.MODE_PRIVATE)
                                .getString("activeUserId",DeepSourceRepository.DEFAULT_USER_ID)
                                ?:DeepSourceRepository.DEFAULT_USER_ID
                            DeepSourceRepository.getInstance(context).addTimeEntry(
                                project,userId,minutes,LocalDate.now().toString(),"Tiempo registrado desde widget"
                            )
                        }
                        WidgetTimer.clear(context,id)
                    } catch(e: Exception) {
                        Log.e("DeepSourceWidget","No se pudo guardar la sesión",e)
                        WidgetTimer.restore(context,id)
                    } finally {
                        updateWidget(context,manager,id)
                        pendingResult.finish()
                    }
                }.start()
                return
            }
            else -> return
        }
        updateWidget(context,manager,id)
    }
}

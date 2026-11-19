package cl.duoc.deepsource.widget

import android.app.PendingIntent
import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.os.Bundle
import android.util.TypedValue
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
        private const val ACTION_OPEN_REGISTER = "cl.duoc.deepsource.widget.OPEN_REGISTER"
        private const val ACTION_START = "cl.duoc.deepsource.widget.START"
        private const val ACTION_PAUSE = "cl.duoc.deepsource.widget.PAUSE"
        private const val ACTION_FINISH = "cl.duoc.deepsource.widget.FINISH"
        private const val ACTION_BACK = "cl.duoc.deepsource.widget.BACK"
        private const val ACTION_AURORA_FRAME = "cl.duoc.deepsource.widget.AURORA_FRAME"
        private const val AURORA_INTERVAL_MS = 60_000L

        private fun auroraAlarm(context: Context, widgetId: Int): PendingIntent {
            val intent = Intent(context, DeepSourceWidgetProvider::class.java).apply {
                action = ACTION_AURORA_FRAME
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse("deepsource://aurora/$widgetId")
            }
            return PendingIntent.getBroadcast(
                context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun scheduleAurora(context: Context, widgetId: Int, running: Boolean) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pending = auroraAlarm(context, widgetId)
            alarm.cancel(pending)
            if (running) {
                // Non-wakeup, inexact alarm: may be delayed by Android/launcher.
                alarm.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + AURORA_INTERVAL_MS, pending)
            }
        }


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
            val selected=WidgetTimer.project(context,widgetId)
            val detail=selected!=null && WidgetTimer.isDetail(context,widgetId)
            // Dos layouts separados: Android nunca puede dibujar la lista detrás del reloj.
            val views=RemoteViews(context.packageName,
                if(detail) R.layout.widget_deepsource_timer else R.layout.widget_deepsource)

            if(detail) {
                views.setOnClickPendingIntent(R.id.widget_back,action(context,widgetId,ACTION_BACK))
                views.setOnClickPendingIntent(R.id.widget_start,action(context,widgetId,ACTION_START))
                views.setOnClickPendingIntent(R.id.widget_pause,action(context,widgetId,ACTION_PAUSE))
                views.setOnClickPendingIntent(R.id.widget_finish,action(context,widgetId,ACTION_FINISH))
            } else {
                val service=Intent(context,DeepSourceWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId)
                    data=Uri.parse("deepsource://widget/projects/$widgetId")
                }
                views.setRemoteAdapter(R.id.widget_projects,service)
                views.setEmptyView(R.id.widget_projects,R.id.widget_empty)
                views.setPendingIntentTemplate(R.id.widget_projects,action(context,widgetId,ACTION_SELECT))
            }
            val home=Intent(context,MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_DESTINATION,AppDestination.Projects.route)
                action="cl.duoc.deepsource.action.OPEN_PROJECTS"
            }
            views.setOnClickPendingIntent(R.id.widget_header,PendingIntent.getActivity(context,10001,home,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            if (!detail) {
                val profileIntent = Intent(context, MainActivity::class.java).apply {
                    action = "cl.duoc.deepsource.action.OPEN_PROFILE"
                    putExtra(MainActivity.EXTRA_DESTINATION, AppDestination.Profile.route)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_more,
                    PendingIntent.getActivity(context, 10002, profileIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                )
            }
            if(detail) {
                val status=WidgetTimer.status(context,widgetId)
                val elapsed=WidgetTimer.elapsed(context,widgetId)
                // El fondo Aurora animado vive detrás del contenido en el XML,
                // igual que en la lista. No sustituirlo por un drawable estático.
                // Ajustar tipografía al tamaño que reporta el launcher.
                val options = manager.getAppWidgetOptions(widgetId)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180)
                val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
                val compact = minHeight < 135
                val spacious = minHeight >= 220
                views.setTextViewTextSize(R.id.widget_selected_name, TypedValue.COMPLEX_UNIT_SP,
                    if (compact) 15f else if (spacious) 21f else 18f)
                views.setTextViewTextSize(R.id.widget_timer, TypedValue.COMPLEX_UNIT_SP,
                    if (compact) 25f else if (spacious) 35f else 31f)
                views.setTextViewTextSize(R.id.widget_timer_status, TypedValue.COMPLEX_UNIT_SP,
                    if (compact) 10f else 12f)
                val buttonText = if (minWidth < 220) 11f else 13f
                views.setTextViewTextSize(R.id.widget_start, TypedValue.COMPLEX_UNIT_SP, buttonText)
                views.setTextViewTextSize(R.id.widget_pause, TypedValue.COMPLEX_UNIT_SP, buttonText)
                views.setTextViewTextSize(R.id.widget_finish, TypedValue.COMPLEX_UNIT_SP, buttonText)
                views.setImageViewResource(R.id.widget_selected_icon, when (WidgetTimer.iconIndex(context,widgetId)) {
                    1 -> R.drawable.ic_icono_2
                    2 -> R.drawable.ic_icono_3
                    3 -> R.drawable.ic_icono_4
                    else -> R.drawable.ic_icono_1
                })
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
        ids.forEach {
            updateWidget(context,manager,it)
            scheduleAurora(context,it,
                WidgetTimer.status(context,it) == "running" && WidgetTimer.isDetail(context,it))
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, manager, appWidgetId, newOptions)
        updateWidget(context, manager, appWidgetId)
        scheduleAurora(context,appWidgetId,
            WidgetTimer.status(context,appWidgetId) == "running" && WidgetTimer.isDetail(context,appWidgetId))
    }

    override fun onDeleted(context: Context,ids: IntArray) {
        ids.forEach {
            scheduleAurora(context, it, false)
            WidgetTimer.clear(context,it)
        }
    }

    override fun onDisabled(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        manager.getAppWidgetIds(ComponentName(context, DeepSourceWidgetProvider::class.java))
            .forEach { scheduleAurora(context, it, false) }
        super.onDisabled(context)
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
        if (intent.action == ACTION_AURORA_FRAME) {
            if (WidgetTimer.status(context,id) == "running" && WidgetTimer.isDetail(context,id)) {
                val prefs = context.getSharedPreferences("deepsource_widget_aurora", Context.MODE_PRIVATE)
                val next = (prefs.getInt("frame.$id", 0) + 1) % 4
                prefs.edit().putInt("frame.$id", next).apply()
                updateWidget(context,manager,id)
                scheduleAurora(context,id,true)
            } else {
                scheduleAurora(context,id,false)
            }
            return
        }
        when(intent.action) {
            ACTION_SELECT -> {
                // El botón ▶ abre el registro de horas de ese proyecto.
                // Pulsar el resto de la fila conserva la selección del cronómetro.
                if (intent.getBooleanExtra("widget_open_register", false)) {
                    val projectId = intent.getStringExtra("widget_project_id") ?: return
                    val open = Intent(context, MainActivity::class.java).apply {
                        action = "cl.duoc.deepsource.action.REGISTER_FROM_WIDGET"
                        putExtra(MainActivity.EXTRA_DESTINATION, AppDestination.RegisterTime.route)
                        putExtra(MainActivity.EXTRA_PROJECT_ID, projectId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(open)
                    return
                }
                val projectId=intent.getStringExtra("widget_project_id") ?: return
                val name=intent.getStringExtra("widget_project_name") ?: return
                // Evita descartar accidentalmente una sesión en curso.
                // Cada fila envía su ID y nombre. Si existe un cronómetro pausado,
                // se conserva al cambiar de proyecto y se recupera al regresar.
                WidgetTimer.selectOrRestore(
                    context, id, projectId, name,
                    intent.getIntExtra("widget_project_icon_index",0)
                )
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
        scheduleAurora(context,id,
            WidgetTimer.status(context,id) == "running" && WidgetTimer.isDetail(context,id))
    }
}

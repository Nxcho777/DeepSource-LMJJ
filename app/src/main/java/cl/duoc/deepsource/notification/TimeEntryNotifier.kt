package cl.duoc.deepsource.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.deepsource.MainActivity
import cl.duoc.deepsource.R
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.toTimeLabel

fun interface TimeEntryNotifier {
    /** Returns false when Android is not allowed to display notifications. */
    fun notifySaved(entry: TimeEntry, projectName: String): Boolean
}

class AndroidTimeEntryNotifier(
    context: Context,
    private val canPost: (Context) -> Boolean = ::canPostNotifications
) : TimeEntryNotifier {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(NotificationManager::class.java)
    private val permissionStore = NotificationPermissionStore(appContext)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Registros de horas", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Confirmaciones de horas registradas en proyectos"
                }
            )
        }
    }

    override fun notifySaved(entry: TimeEntry, projectName: String): Boolean {
        if (!permissionStore.notificationsEnabled() || !canPost(appContext)) return false

        // No destination extra: a cold launch starts at Login, and a warm launch keeps its task.
        val launchIntent = Intent(appContext, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_time)
            .setContentTitle("Horas registradas correctamente")
            .setContentText(notificationContent(entry.durationMinutes, projectName))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        // A stable tag prevents a retry for this entry from creating a second visible alert.
        manager.notify(entry.id, 0, notification)
        return true
    }

    companion object {
        const val CHANNEL_ID = "time_entry_saved"

        fun canPostNotifications(context: Context): Boolean =
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                    context.getSystemService(NotificationManager::class.java)
                        .getNotificationChannel(CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE)
    }
}

fun notificationContent(durationMinutes: Int, projectName: String): String =
    "Se registraron ${if (durationMinutes < 60) "$durationMinutes min" else durationMinutes.toTimeLabel()} en $projectName"

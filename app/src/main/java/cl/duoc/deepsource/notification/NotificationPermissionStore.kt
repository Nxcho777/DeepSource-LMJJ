package cl.duoc.deepsource.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Records that the runtime dialog was already shown, including when it was denied. */
class NotificationPermissionStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("notification_permission", Context.MODE_PRIVATE)

    fun shouldOfferRequest(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
        !preferences.getBoolean("requested", false)

    fun markRequested() {
        preferences.edit().putBoolean("requested", true).apply()
    }
}

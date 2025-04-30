package com.example.proyecto.ui

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

@SuppressLint("MissingPermission")
fun showNotification(context: Context, title: String, content: String) {
    // 1. Crear canal de notificación (necesario para Android 8+)
    val channelId = "mi_canal_id"
    val channelName = "Mi Canal"
    val importance = NotificationManager.IMPORTANCE_DEFAULT

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // 2. Construir la notificación
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono obligatorio
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    // 3. Mostrar la notificación
    with(NotificationManagerCompat.from(context)) {
        notify(1, builder.build())
    }
}
package com.example.proyecto.ui.viewmodel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import com.example.proyecto.R

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var significantMotionSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        // 1) Obtenemos el SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // 2) Pedimos el sensor de movimiento significativo (one-shot)
        significantMotionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 3) Arrancamos el servicio en primer plano para evitar que el sistema lo mate
        startForeground(FOREGROUND_ID, buildForegroundNotification())

        // 4) Registramos nuestro listener “one-shot”
        significantMotionSensor?.let { sensor ->
            sensorManager.requestTriggerSensor(
                SignificantMotionTrigger(this),
                sensor
            )
        }

        // Si el sistema mata el servicio, que lo reinicie (START_STICKY)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent) {
        /*  NO será llamado para TYPE_SIGNIFICANT_MOTION */
        // Solo si registrases un listener continuo (e.g. acelerómetro)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No lo usamos aquí
    }

    override fun onDestroy() {
        // Limpiamos registros (seguro no quede nada)
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    /** Construye la notificación de “Foreground Service” */
    private fun buildForegroundNotification(): Notification {
        val channelId = "sensor_service_channel"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                "Servicio de Sensor",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(chan)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Servicio de Sensor Activo")
            .setContentText("Escuchando evento de movimiento significativo")
            .setSmallIcon(R.drawable.servicioengranajes)  // icono
            .build()
    }

    /** Envía la notificación de alerta cuando el sensor dispara */
    internal fun showAlertNotification(title: String, text: String) {
        val channelId = "sensor_alerts_channel"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                "Alertas de Sensor",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(chan)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // icono de notificación
            .setAutoCancel(true)
            .build()

        nm.notify(ALERT_ID, notification)
    }

    companion object {
        private const val FOREGROUND_ID = 1
        private const val ALERT_ID      = 2
    }
}

/**
 * Para sensores “one-shot” como TYPE_SIGNIFICANT_MOTION
 * este listener solo se dispara una vez.
 */
private class SignificantMotionTrigger(
    val service: SensorService
) : TriggerEventListener() {
    override fun onTrigger(event: TriggerEvent?) {
        // 5) Cuando el sensor detecta “movimiento significativo”:
        service.showAlertNotification(
            title = "¡Movimiento Detectado!",
            text  = "Recuerda registrar cualquier actividad física que realices."
        )
        // 6) Si queremos volver a escuchar, hay que volver a registrar:
        service.onStartCommand(null, 0, 0)
    }
}
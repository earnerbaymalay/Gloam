package com.gloam.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gloam.MainActivity
import com.gloam.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: return
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val title = if (type == "sunrise") "Good morning ☀️" else "Good evening 🌙"
        val text = if (type == "sunrise") 
            "Take a moment to reflect on how you're feeling"
        else 
            "Time for your evening reflection"
        
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(if (type == "sunrise") 1 else 2, notification)
    }
    
    private fun createNotificationChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Journal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for morning and evening journaling"
            }
            manager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val CHANNEL_ID = "gloam_reminders"
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule notifications after device reboot
            NotificationScheduler.scheduleFromPrefs(context)
        }
    }
}

object NotificationScheduler {
    
    fun scheduleSunriseNotification(context: Context, time: LocalTime) {
        scheduleNotification(context, time, "sunrise", 1001)
    }
    
    fun scheduleSunsetNotification(context: Context, time: LocalTime) {
        scheduleNotification(context, time, "sunset", 1002)
    }
    
    private fun scheduleNotification(context: Context, time: LocalTime, type: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", type)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
    
    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        listOf(1001, 1002).forEach { requestCode ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
    
    fun scheduleFromPrefs(context: Context) {
        // Load saved location and schedule based on sun times
        val prefs = context.getSharedPreferences("gloam_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("latitude", -33.8688f).toDouble()
        val lon = prefs.getFloat("longitude", 151.2093f).toDouble()
        
        val sunTimes = SunCalculator.calculate(lat, lon)
        scheduleSunriseNotification(context, sunTimes.sunrise)
        scheduleSunsetNotification(context, sunTimes.sunset)
    }
}

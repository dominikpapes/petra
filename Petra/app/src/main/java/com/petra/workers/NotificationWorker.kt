package com.petra.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.petra.R

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val petName = inputData.getString("pet_name") ?: ""
        val activityType = inputData.getString("activity_type") ?: ""
        val activityTime = inputData.getString("activity_time") ?: ""

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("pet_activity_channel", "Pet Activity Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "pet_activity_channel")
            .setContentTitle("$petName: $activityType")
            .setContentText("Due: $activityTime")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }
}
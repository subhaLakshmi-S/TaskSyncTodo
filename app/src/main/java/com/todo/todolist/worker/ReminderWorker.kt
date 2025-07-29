package com.todo.todolist.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.todo.todolist.R

class ReminderWorker(context: Context, workerParams: WorkerParameters):
    Worker(context, workerParams) {
    override fun doWork(): Result {
        showNotification("Task Reminder", "Don't forget to check your tasks today!")
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(applicationContext, "todo channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        with(NotificationManagerCompat.from(applicationContext)){
            notify(1001, builder.build())
        }

    }
}
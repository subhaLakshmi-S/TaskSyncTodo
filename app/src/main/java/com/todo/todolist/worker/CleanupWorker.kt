package com.todo.todolist.worker

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.todo.todolist.data.TodoDatabase
import java.time.LocalDate


class CleanupWorker(context: Context, workerParams: WorkerParameters):
    CoroutineWorker(context, workerParams){
        private val dao= TodoDatabase.getDatabase(context).todoDao()

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val yesterday = LocalDate.now().minusDays(5).toString()
        dao.deleteOldCompleted("1",yesterday)
        return Result.success()
    }
}
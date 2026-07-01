package com.example.oneminutelanguage.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            WidgetUpdater.refreshWidget(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

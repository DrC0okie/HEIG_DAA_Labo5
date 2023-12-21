package ch.heigvd.daa.labo5.cache

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ch.heigvd.daa.labo5.cache.Cache

class ClearCacheWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Cache.clear()
        return Result.success()
    }
}
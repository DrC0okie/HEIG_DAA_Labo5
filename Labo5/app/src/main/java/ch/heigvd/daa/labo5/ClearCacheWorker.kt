package ch.heigvd.daa.labo5

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ClearCacheWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    companion object {
        private val TAG = ClearCacheWorker::class.qualifiedName
    }

    override fun doWork(): Result {
        Log.d(TAG, "Cleared cache dir")
        Cache.clear()
        return Result.success()
    }
}
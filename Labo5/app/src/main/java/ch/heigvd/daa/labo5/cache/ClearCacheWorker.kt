package ch.heigvd.daa.labo5.cache

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ch.heigvd.daa.labo5.cache.Cache

/**
 * A worker class that handles clearing of the cache directory when scheduled.
 * @param appContext The context passed to the worker.
 * @param workerParams Parameters to setup the internal state of this worker.
 * @author Timothée Van Hove, Léo Zmoos
 */
class ClearCacheWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    /**
     * The main logic of the worker that executes the task.
     * Clears the application's cache directory. If the operation is successful,
     * @return [Result] representing the outcome of the work.
     */
    override fun doWork(): Result {
        Cache.clear()
        return Result.success()
    }
}
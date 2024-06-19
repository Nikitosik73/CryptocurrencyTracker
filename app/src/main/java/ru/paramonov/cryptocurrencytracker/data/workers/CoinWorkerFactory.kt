package com.example.cryptoapp.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ru.paramonov.cryptocurrencytracker.data.workers.ChildWorkerFactory
import javax.inject.Inject
import javax.inject.Provider

class CoinWorkerFactory @Inject constructor(
    private val workerProvides: @JvmSuppressWildcards Map<Class<out ListenableWorker>, Provider<ChildWorkerFactory>>
): WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            RefreshDataWorker::class.qualifiedName -> {
                val child = workerProvides[RefreshDataWorker::class.java]?.get()
                return child?.create(appContext, workerParameters)
            }
            else -> null
        }
    }
}
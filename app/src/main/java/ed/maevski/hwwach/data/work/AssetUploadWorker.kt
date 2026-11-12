package ed.maevski.hwwach.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.irepositories.IAssetUploadScheduler
import ed.maevski.hwwach.domain.models.UploadRunResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Фоновая отправка активов. Запускается при появлении сети, переживает закрытие приложения.
 * Сбой сети или сервера — повтор с нарастающей паузой; после MAX_ATTEMPTS актив уходит в FAILED,
 * чтобы очередь не стояла вечно из-за одного битого снимка
 */
@HiltWorker
class AssetUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val assetRepository: IAssetRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val giveUp = runAttemptCount >= MAX_ATTEMPTS
        return try {
            when (assetRepository.uploadQueue(giveUp)) {
                UploadRunResult.DONE -> Result.success()
                UploadRunResult.RETRY_LATER -> Result.retry()
            }
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            // зависшие в UPLOADING заберёт следующий проход
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "asset-upload"
        private const val MAX_ATTEMPTS = 8
    }
}

@Singleton
class AssetUploadScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : IAssetUploadScheduler {

    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<AssetUploadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        // идёт отправка — новый проход встанет за ней и заберёт то, что добавили только что
        WorkManager.getInstance(context)
            .enqueueUniqueWork(AssetUploadWorker.WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
}

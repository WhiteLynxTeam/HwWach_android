package ed.maevski.hwwach.data.repositories

import android.util.Log
import ed.maevski.hwwach.BuildConfig
import ed.maevski.hwwach.di.WarmupOkHttpClient
import ed.maevski.hwwach.domain.irepositories.IServerWarmupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Оба бэкенда на бесплатном Render засыпают без трафика, холодный старт — до минуты.
 * Повторяем /health на каждом, пока не ответит 2xx, но не дольше ~90 секунд.
 * Пока сервис просыпается, Render держит запрос открытым, поэтому пауза между попытками
 * наступает только после таймаута или ошибки — частых запросов нет.
 */
@Singleton
class ServerWarmupRepositoryImpl @Inject constructor(
    @WarmupOkHttpClient private val okHttpClient: OkHttpClient
) : IServerWarmupRepository {

    private val lastAwakeAt = AtomicLong(0L)

    override suspend fun wakeUp(): Boolean {
        // Оба сервера недавно отвечали — считаем их бодрыми, не дёргаем повторно
        if (System.currentTimeMillis() - lastAwakeAt.get() < MIN_INTERVAL_MS) return true

        val awake = withContext(Dispatchers.IO) {
            coroutineScope {
                listOf(BuildConfig.BASE_AUTH_URL, BuildConfig.BASE_MAIN_URL)
                    .map { baseUrl -> async { waitUntilAwake(baseUrl + HEALTH_PATH) } }
                    .awaitAll()
                    .all { it }
            }
        }
        if (awake) lastAwakeAt.set(System.currentTimeMillis())
        return awake
    }

    private suspend fun waitUntilAwake(url: String): Boolean {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < MAX_WAIT_MS) {
            if (ping(url)) return true
            delay(RETRY_DELAY_MS)
        }
        Log.w(TAG, "Warmup $url: нет ответа за ${MAX_WAIT_MS / 1000} с")
        return false
    }

    private fun ping(url: String): Boolean =
        try {
            okHttpClient.newCall(Request.Builder().url(url).get().build()).execute().use { response ->
                Log.d(TAG, "Warmup $url -> ${response.code}")
                response.isSuccessful
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Сервер ещё просыпается или нет сети — пробуем снова
            Log.w(TAG, "Warmup $url failed: ${e.message}")
            false
        }

    private companion object {
        const val TAG = "ServerWarmup"
        const val HEALTH_PATH = "health"
        const val MIN_INTERVAL_MS = 60_000L
        const val MAX_WAIT_MS = 90_000L
        const val RETRY_DELAY_MS = 3_000L
    }
}

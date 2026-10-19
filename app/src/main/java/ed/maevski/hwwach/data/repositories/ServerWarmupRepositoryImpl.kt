package ed.maevski.hwwach.data.repositories

import android.util.Log
import ed.maevski.hwwach.BuildConfig
import ed.maevski.hwwach.di.WarmupOkHttpClient
import ed.maevski.hwwach.domain.irepositories.IServerWarmupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerWarmupRepositoryImpl @Inject constructor(
    @WarmupOkHttpClient private val okHttpClient: OkHttpClient
) : IServerWarmupRepository {

    private val lastWakeUpAt = AtomicLong(0L)

    override suspend fun wakeUp() {
        // Не дёргаем серверы повторно, если будили их совсем недавно
        val now = System.currentTimeMillis()
        val last = lastWakeUpAt.get()
        if (now - last < MIN_INTERVAL_MS || !lastWakeUpAt.compareAndSet(last, now)) return

        withContext(Dispatchers.IO) {
            coroutineScope {
                listOf(BuildConfig.BASE_AUTH_URL, BuildConfig.BASE_MAIN_URL)
                    .map { baseUrl -> async { ping(baseUrl + HEALTH_PATH) } }
                    .awaitAll()
            }
        }
    }

    // Результат не важен: любой ответ (даже 404) означает, что сервис проснулся
    private fun ping(url: String) {
        try {
            okHttpClient.newCall(Request.Builder().url(url).get().build()).execute().use { response ->
                Log.d(TAG, "Warmup $url -> ${response.code}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Warmup $url failed: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "ServerWarmup"
        const val HEALTH_PATH = "health"
        const val MIN_INTERVAL_MS = 60_000L
    }
}

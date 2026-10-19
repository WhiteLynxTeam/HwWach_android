package ed.maevski.hwwach.domain.irepositories

interface IServerWarmupRepository {
    /** Шлёт лёгкие запросы на оба бэкенда, чтобы «разбудить» их (Render усыпляет бесплатные сервисы) */
    suspend fun wakeUp()
}

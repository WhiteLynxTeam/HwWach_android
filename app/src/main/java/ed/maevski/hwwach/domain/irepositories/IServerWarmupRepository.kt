package ed.maevski.hwwach.domain.irepositories

interface IServerWarmupRepository {
    /**
     * Будит оба бэкенда (Render усыпляет бесплатные сервисы) и ждёт их ответа.
     * true — оба ответили, false — хотя бы один не проснулся за отведённое время.
     */
    suspend fun wakeUp(): Boolean
}

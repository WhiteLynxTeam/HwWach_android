package ed.maevski.hwwach.domain.models

// Очередь отправки активов: сколько ждёт и что не ушло (с причиной)
data class UploadQueueState(
    val queued: Int = 0,
    val failed: List<FailedUpload> = emptyList(),
) {
    val isEmpty: Boolean get() = queued == 0 && failed.isEmpty()
}

data class FailedUpload(
    val clientId: String,
    val name: String,
    val message: String,
)

// Чем закончился проход очереди: всё обработано или осталась сеть/сервер — повторить позже
enum class UploadRunResult { DONE, RETRY_LATER }

package ed.maevski.hwwach.domain.irepositories

// Запуск фоновой отправки очереди активов (ждёт сеть, переживает закрытие приложения)
interface IAssetUploadScheduler {
    fun schedule()
}

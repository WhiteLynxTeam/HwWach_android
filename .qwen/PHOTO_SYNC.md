# Синхронизация фотографий — Подробная документация

> Детальное описание механизма синхронизации фото с сервером.
> Для понимания общей архитектуры смотри PROJECT.MD → раздел "Принцип SSOT".

## Содержание
1. [Архитектура SSOT для фото](#1-архитектура-ssot-для-фото)
2. [Умная синхронизация (smartSyncPhotos)](#2-умная-синхронизация-smartsyncphotos)
3. [Статусы загрузки](#3-статусы-загрузки)
4. [Жизненный цикл фото](#4-жизненный-цикл-фото)
5. [Загрузка изображений (Coil)](#5-загрузка-изображений-coil)
6. [API Reference](#6-api-reference)

---

## 1. Архитектура SSOT для фото

### Диаграмма потока данных

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   API       │────→│   Room DB   │←────│   Flow      │────→│    UI       │
│  (Retrofit) │     │   (SSOT)    │     │             │     │  (Compose)  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
       ↑                   ↓
       │            ┌─────────────┐
       └─────────────│  syncPhotos │ (инициализация / pull-to-refresh)
                    └─────────────┘
```

### Правила

| Операция | Метод | Возвращает | Когда вызывать |
|----------|-------|------------|----------------|
| **Синхронизация** | `syncPhotos()` | `DomainResult<Unit>` | При старте экрана, pull-to-refresh |
| **Получение данных** | `getAllPhotosFlow()` | `Flow<List<Photo>>` | Подписка в UI, данные из БД |
| **Отправка на сервер** | `syncPendingPhotos()` | - | После добавления новых фото |

**Важно:** UI никогда не получает данные напрямую из `syncPhotos()`. Только через `Flow` из локальной БД.

---

## 2. Умная синхронизация (smartSyncPhotos)

### SQL-алгоритм

```sql
-- 1. Удалить SYNCED фото, которых больше нет на сервере
DELETE FROM photos 
WHERE status = 'SYNCED' 
  AND clientId NOT IN (<server_ids>);

-- 2. Получить существующие локальные пути
SELECT clientId, localFilePath FROM photos WHERE localFilePath IS NOT NULL;

-- 3. INSERT OR REPLACE с сохранением localFilePath
INSERT OR REPLACE INTO photos (...)
```

### Почему это важно

- **N+1 проблема решена**: Всего 3 SQL запроса независимо от количества фото
- **Локальные файлы сохраняются**: Нет лишних сетевых запросов
- **Безопасное удаление**: Только SYNCED фото удаляются, PENDING/UPLOADING/FAILED не трогаем

---

## 3. Статусы загрузки

| Статус | Описание | Действие |
|--------|----------|----------|
| `PENDING` | Ожидает загрузки | `syncPendingPhotos()` начнёт загрузку |
| `UPLOADING` | В процессе загрузки | Блокируется при старте (сбрасывается в FAILED) |
| `UPLOADED` | Загружено, ждёт подтверждения | `resumeUploadedPhotos()` отправит complete-upload |
| `SYNCED` | Полностью синхронизировано | Участвует в умной синхронизации |
| `FAILED` | Ошибка загрузки | `retrySyncFailedPhotos()` повторит попытку |

---

## 4. Жизненный цикл фото

### Сценарий 1: Добавление нового фото

```
Пользователь выбирает фото
        ↓
SavePhotoUseCase → localFilePath в БД (status = PENDING)
        ↓
Пользователь нажимает "Синхронизировать"
        ↓
syncPendingPhotos()
    ├── getUploadUrl (API)
    ├── upload файла
    ├── completeUpload (API)
    └── сохраняет serverUuid + remoteUrl (status = SYNCED)
```

### Сценарий 2: Получение фото с другого устройства

```
Пользователь открывает экран
        ↓
syncPhotos()
    ├── Получает список с сервера
    ├── smartSyncPhotos() → сохраняет в БД
    └── UI обновляется через Flow
        ↓
PriorityAsyncImage загружает по remoteUrl (локального файла ещё нет)
```

### Сценарий 3: Реанимация после краша

```
Приложение крашнулось во время UPLOADING
        ↓
Пользователь открывает приложение
        ↓
resetStuckUploads() → UPLOADING становится FAILED
        ↓
retrySyncFailedPhotos() → повторяет загрузку
```

---

## 5. Загрузка изображений (Coil)

### PriorityAsyncImage

```kotlin
@Composable
fun PriorityAsyncImage(
    photo: Photo,
    contentDescription: String?
) {
    val imageUri = photo.localPath?.let { path ->
        if (File(path).exists()) Uri.fromFile(File(path)) else null
    } ?: photo.remoteUrl

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUri)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription
    )
}
```

### Приоритет загрузки

1. `localFilePath` → если файл существует локально
2. `remoteUrl` → если локального файла нет

Это предотвращает повторную загрузку уже кэшированных изображений.

---

## 6. API Reference

### PhotoRepositoryImpl

```kotlin
// Синхронизация с сервером
suspend fun syncPhotos(): DomainResult<Unit>

// Получение данных из SSOT (Flow из БД)
fun getAllPhotosFlow(): Flow<List<Photo>>

// Отправка PENDING фото на сервер
suspend fun syncPendingPhotos()

// Реанимация после краша
suspend fun resetStuckUploads()
suspend fun resumeUploadedPhotos()
suspend fun retrySyncFailedPhotos()
```

### PhotoDao

```kotlin
// Умная синхронизация (транзакция)
@Transaction
suspend fun smartSyncPhotos(serverPhotos: List<PhotoEntity>)

// Удаление SYNCED фото, которых нет в списке
@Query("DELETE FROM photos WHERE status = 'SYNCED' AND clientId NOT IN (:clientIds)")
suspend fun deleteSyncedPhotosNotInList(clientIds: List<String>)

// Получение путей для JOIN-оптимизации
@Query("SELECT clientId, localFilePath FROM photos WHERE localFilePath IS NOT NULL")
suspend fun getExistingLocalPaths(): List<PhotoLocalPath>

// Полное обновление после completeUpload
@Query("UPDATE photos SET status = :status, serverUuid = :serverUuid, remoteUrl = :remoteUrl WHERE clientId = :clientId")
suspend fun completeUploadWithUrl(clientId: String, serverUuid: String, remoteUrl: String, status: String)
```

### Use Cases

```kotlin
// Разовая синхронизация при старте экрана
class SyncPhotosUseCase(repository: IPhotoRepository)

// Подписка на все фото из БД
class GetAllPhotosUseCase(repository: IPhotoRepository)

// Сохранение нового фото (status = PENDING)
class SavePhotoUseCase(repository: IPhotoRepository)

// Отправка PENDING на сервер
class SyncPendingPhotosUseCase(repository: IPhotoRepository)

// Реанимация после краша
class ResetStuckUploadsUseCase(repository: IPhotoRepository)
class ResumeUploadedPhotosUseCase(repository: IPhotoRepository)
class RetrySyncFailedPhotosUseCase(repository: IPhotoRepository)
```

---

## Связанные файлы

- `PhotoRepositoryImpl.kt` — реализация репозитория
- `PhotoDao.kt` — DAO с SQL-запросами
- `IPhotoRepository.kt` — интерфейс репозитория
- `ImageLoadingExtensions.kt` — расширения Coil
- `AddScreenViewModel.kt` — пример использования в UI

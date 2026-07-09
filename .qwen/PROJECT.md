# HW Watch — Структура проекта

> Android-приложение с чистой архитектурой (Clean Architecture)
> Стек: Jetpack Compose, Hilt, Room, Retrofit, Kotlin Coroutines, MVI паттерн

## Технологии

- **UI**: Jetpack Compose + Material3
- **DI**: Hilt (+ KSP)
- **Local**: Room (SQLite), Preferences DataStore
- **Network**: Retrofit + OkHttp Logging Interceptor
- **Async**: Kotlin Coroutines + Flow
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose
- **Target SDK**: 36, **Min SDK**: 24
- **JVM**: Java 17

## Базовый пакет: `ed.maevski.hwwach`

```
ed.maevski.hwwach
│
├── HwWachApplication.kt          # Entry point приложения
├── MainActivity.kt               # Главная Activity
│
├── 📦 DATA LAYER
│   │
│   ├── local/                    # Локальное хранилище (Room + DataStore)
│   │   ├── dao/
│   │   │   ├── AssetDao.kt
│   │   │   ├── AssetPhotoCrossRefDao.kt
│   │   │   └── PhotoDao.kt
│   │   ├── entity/
│   │   │   ├── AssetEntity.kt
│   │   │   ├── AssetPhotoCrossRef.kt
│   │   │   └── PhotoEntity.kt
│   │   ├── model/
│   │   │   └── Token.kt
│   │   ├── AppDatabase.kt
│   │   └── PreferencesDataStore.kt
│   │
│   ├── remote/                   # Сетевой слой (Retrofit)
│   │   ├── api/
│   │   │   ├── PhotosApi.kt
│   │   │   └── UserApi.kt
│   │   ├── interceptor/
│   │   │   └── TokenInterceptor.kt
│   │   └── model/
│   │       ├── auth/
│   │       │   ├── AuthResponseDto.kt
│   │       │   └── AuthUserRequest.kt
│   │       ├── photo/
│   │       │   ├── PhotoDto.kt
│   │       │   └── PhotosResponseDto.kt
│   │       ├── reg/
│   │       │   ├── RegResponseDto.kt
│   │       │   ├── RegStatusEnumDto.kt
│   │       │   ├── RegStatusResponseDto.kt
│   │       │   └── RegUserRequest.kt
│   │       └── user/
│   │           └── UserResponseDto.kt
│   │
│   ├── mappers/                  # Мапперы DTO ↔ Domain ↔ Entity (11 файлов)
│   │   ├── AssetDomainToEntityMapper.kt
│   │   ├── AssetEntityToDomainMapper.kt
│   │   ├── PhotoDomainToEntityMapper.kt
│   │   ├── PhotoDtoToDomainMapper.kt
│   │   ├── PhotoEntityToDomainMapper.kt
│   │   ├── RegResponseDtoToUserDomainMapper.kt
│   │   ├── RegStatusEnumDtoToDomainMapper.kt
│   │   ├── RegStatusResponseDtoToRegStatusDomainMapper.kt
│   │   ├── ResponseErrorMapper.kt
│   │   ├── UserDomainToAuthUserRequestMapper.kt
│   │   └── UserDomainToRegUserRequestMapper.kt
│   │
│   └── repositories/             # Реализации репозиториев (5 файлов)
│       ├── AssetRepositoryImpl.kt
│       ├── PhotoRepositoryImpl.kt
│       ├── TokenRepositoryImpl.kt
│       ├── UserProfileRepositoryImpl.kt
│       └── UserRepositoryImpl.kt
│
├── 📦 DOMAIN LAYER
│   │
│   ├── DomainResult.kt           # Обёртка результата операций
│   │
│   ├── irepositories/            # Интерфейсы репозиториев (5 файлов)
│   │   ├── IAssetRepository.kt
│   │   ├── IPhotoRepository.kt
│   │   ├── ITokensRepository.kt
│   │   ├── IUserProfileRepository.kt
│   │   └── IUserRepository.kt
│   │
│   ├── models/                   # Доменные модели (9 файлов)
│   │   ├── Asset.kt
│   │   ├── AssetUploadStatusEnum.kt
│   │   ├── MainDestinationEnum.kt
│   │   ├── Photo.kt
│   │   ├── PhotoUploadStatusEnum.kt
│   │   ├── RegStatus.kt
│   │   ├── RegStatusEnum.kt
│   │   ├── Token.kt
│   │   └── User.kt
│   │
│   └── usecases/                 # Use Case'ы (9 файлов)
│       ├── AuthApiUseCase.kt
│       ├── CheckRegistrationUseCase.kt
│       ├── DeletePhotoUseCase.kt
│       ├── GetAllPhotosUseCase.kt
│       ├── GetAssetsUseCase.kt
│       ├── GetStartMainScreenDestinationUseCase.kt
│       ├── GetUserInfoUseCase.kt
│       ├── LoginWithProfileUseCase.kt
│       ├── RegApiUseCase.kt
│       ├── ResetStuckUploadsUseCase.kt
│       ├── ResumeUploadedPhotosUseCase.kt
│       ├── RetrySyncFailedPhotosUseCase.kt
│       ├── SavePhotoUseCase.kt
│       ├── SyncPendingPhotosUseCase.kt
│       └── SyncPhotosUseCase.kt
│
├── 📦 DI LAYER (Hilt)
│   ├── DatabaseModule.kt         # Провайды Room, DAO
│   ├── NetworkModule.kt          # Провайды Retrofit/OkHttp
│   ├── HttpClientQualifiers.kt   # Квалификаторы HTTP-клиентов
│   ├── RepositoryModule.kt       # Биндинги репозиториев
│   └── UseCaseModule.kt          # Биндинги use case'ов
│
├── 📦 UI / PRESENTATION LAYER (Jetpack Compose + MVI)
│   │
│   ├── App.kt                    # Компонент верхнего уровня (NavHost)
│   ├── FlowNavigation.kt         # Навигация между Flow (Start ↔ Main)
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   ├── navflow/
│   │   │
│   │   ├── startflow/            # Flow авторизации и регистрации
│   │   │   ├── LoginTextField.kt
│   │   │   ├── StartFlowNavigation.kt
│   │   │   ├── authscreen/
│   │   │   │   ├── AuthScreen.kt
│   │   │   │   ├── AuthScreenContract.kt
│   │   │   │   └── AuthScreenViewModel.kt
│   │   │   └── regscreen/
│   │   │       ├── RegScreen.kt
│   │   │       ├── RegScreenContract.kt
│   │   │       └── RegScreenViewModel.kt
│   │   │
│   │   └── mainflow/            # Главный Flow приложения
│   │       ├── MainFlowNavigation.kt
│   │       ├── mainscreen/
│   │       │   ├── MainScreen.kt
│   │       │   ├── MainScreenContract.kt
│   │       │   ├── MainScreenViewModel.kt
│   │       │   ├── BottomNavigationBar.kt
│   │       │   └── InnerMainFlowNavigation.kt
│   │       └── bottom_menu_screens/
│   │           ├── assets/
│   │           │   ├── AssetsScreen.kt
│   │           │   ├── AssetsScreenContract.kt
│   │           │   └── AssetsScreenViewModel.kt
│   │           ├── gallery/
│   │           │   └── GalleryScreen.kt (и др.)
│   │           ├── loading/
│   │           │   └── LoadingScreen.kt
│   │           └── profile/
│   │               └── ProfileScreen.kt
│   │
│   ├── models/                   # UI-модели (пусто)
│   └── style/                    # UI-стили (пусто)
│
└── 📦 COMMON
    └── extensions/               # Extension-функции (пусто)
```

## Сводка

| Слой | Пакет | Кол-во |
|---|---|---|
| **Data: Local** (Room, DataStore) | `data.local.*` | 7 |
| **Data: Remote** (Retrofit API, DTO) | `data.remote.*` | 14 |
| **Data: Mappers** | `data.mappers` | 11 |
| **Data: Repositories** | `data.repositories` | 5 |
| **Domain: Interfaces** | `domain.irepositories` | 5 |
| **Domain: Models** | `domain.models` | 9 |
| **Domain: UseCases** | `domain.usecases` | 8 |
| **DI** (Hilt Modules) | `di` | 5 |
| **UI/ Presentation** | `ui.*` | ~25 |
| **App Entry** | корень | 2 |

## Архитектура

### Направление зависимостей
```
UI → Domain → Data
```
- **UI** зависит от **Domain** (use cases, модели)
- **Domain** НЕ зависит ни от кого (чистый Kotlin без Android/фреймворков)
- **Data** зависит от **Domain** (реализует интерфейсы репозиториев)
- **DI** собирает всё вместе через Hilt

### Паттерн экранов — MVI
Каждый экран следует паттерну MVI:
- `*Contract.kt` — определяет State/Event/UIAction
- `*ViewModel.kt` — обрабатывает события, обновляет State через use cases
- `*Screen.kt` — Compose-компонент, рендерит State, шлёт Events

### Навигация
- **StartFlow** — авторизация и регистрация (AuthScreen, RegScreen)
- **MainFlow** — главный экран с Bottom Navigation:
  - **Assets** — экран реестра активов
  - **Gallery** — экран галереи фото
  - **Loading** — экран загрузки
  - **Profile** — экран профиля

### Хранение данных
- **Room**: активы (AssetEntity), фото (PhotoEntity), связь многие-ко-многим (AssetPhotoCrossRef)
- **DataStore**: токены (Token)

### Сетевые API
- **UserApi**: аутентификация, регистрация, получение информации о пользователе
- **PhotosApi**: загрузка и получение фото
- **TokenInterceptor**: автоматическая подстановка токена в заголовки

## Принцип SSOT (Single Source of Truth)

> **Локальная база данных (Room) — единственный источник правды для UI.**
> Подробнее: см. `PHOTO_SYNC.md`

### Правила
- ✅ UI получает данные **только через Flow** из локальной БД
- ✅ `syncPhotos()` — разовая синхронизация сервер → БД, возвращает `Unit`
- ❌ Нельзя возвращать данные API напрямую в UI

```
API → Room (SSOT) → Flow → UI
         ↑
   syncPhotos()
```

### Ключевые методы

| Операция | Метод | Возвращает |
|----------|-------|------------|
| Синхронизация | `syncPhotos()` | `DomainResult<Unit>` |
| Получение данных | `getAllPhotosFlow()` | `Flow<List<Photo>>` |
| Отправка на сервер | `syncPendingPhotos()` | - |

## Фото-синхронизация (кратко)

> Полная документация: `PHOTO_SYNC.md`

### Умная синхронизация (`smartSyncPhotos`)
1. Удаляет SYNCED фото, которых нет на сервере
2. Сохраняет существующие `localFilePath` (JOIN-оптимизация)
3. INSERT OR REPLACE с мержем данных

### Статусы фото
`PENDING` → `UPLOADING` → `UPLOADED` → `SYNCED`

### Загрузка изображений
`PriorityAsyncImage` — приоритет: `localPath > remoteUrl`

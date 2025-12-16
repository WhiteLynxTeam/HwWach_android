hwwach/
└── app/
    └── src/main/java/com/whitelynxteam/hwwach/
        │
        │   └── extensions/
        ├── data/                                       # Слой данных
        │   ├── local/                                  # Локальное хранилище
        ├── common/
        │   │   └── model/
        │   ├── mappers/                                # Мапперы
        │   ├── remote/                                 # Удаленный доступ
        │   │   ├── api/
        │   │   ├── interceptor/
        │   │   └── model/                              # Убрана подпапка user
        │   └── repositories/                           # Реализации репозиториев
        │
        ├── di/                                         # Внедрение зависимостей
        │
        ├── domain/                                     # Бизнес-логика
        │   ├── irepositories/                          # Интерфейсы репозиториев
        │   ├── models/                                 # Доменные модели
        │   └── usecases/                               # Бизнес-логика
        │
        └── ui/                                         # Пользовательский интерфейс
            ├── models/                                 # UI модели
            ├── navflow/                                # Навигационные потоки
            │   ├── mainflow/                           # Убраны все подкаталоги и файлы
            │   └── startflow/                          # Стартовый поток
            ├── navigation/                             # Конфигурация навигации
            ├── style/                                  # Стили
            ├── theme/                                  # Тема
            └── App.kt                                  # Компонент приложения (Jetpack Compose)
     
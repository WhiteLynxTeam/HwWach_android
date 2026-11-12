package ed.maevski.hwwach.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ed.maevski.hwwach.data.local.dao.AssetDao
import ed.maevski.hwwach.data.local.dao.AssetPhotoCrossRefDao
import ed.maevski.hwwach.data.local.dao.CategoryDao
import ed.maevski.hwwach.data.local.dao.LocationDao
import ed.maevski.hwwach.data.local.dao.PhotoDao
import ed.maevski.hwwach.data.local.dao.PhotoGroupDao
import ed.maevski.hwwach.data.local.entity.AssetEntity
import ed.maevski.hwwach.data.local.entity.AssetPhotoCrossRef
import ed.maevski.hwwach.data.local.entity.CategoryEntity
import ed.maevski.hwwach.data.local.entity.LocationEntity
import ed.maevski.hwwach.data.local.entity.PhotoEntity
import ed.maevski.hwwach.data.local.entity.PhotoGroupEntity

@Database(
    entities = [PhotoEntity::class, AssetEntity::class, AssetPhotoCrossRef::class, CategoryEntity::class, LocationEntity::class, PhotoGroupEntity::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun assetDao(): AssetDao
    abstract fun assetPhotoCrossRefDao(): AssetPhotoCrossRefDao
    abstract fun categoryDao(): CategoryDao
    abstract fun locationDao(): LocationDao
    abstract fun photoGroupDao(): PhotoGroupDao
}

// 5 → 6: место и МОЛ у актива, причина ошибки отправки, справочник мест.
// Без миграции Room пересоздал бы базу — пропали бы ещё не отправленные фото и активы
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE assets ADD COLUMN locationKind TEXT NOT NULL DEFAULT 'unset'")
        db.execSQL("ALTER TABLE assets ADD COLUMN locationId TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN locationPath TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN molMode TEXT NOT NULL DEFAULT 'DEFAULT'")
        db.execSQL("ALTER TABLE assets ADD COLUMN molPersonId TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN errorMessage TEXT")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS locations (" +
                "id TEXT NOT NULL, parentId TEXT, name TEXT NOT NULL, kind TEXT NOT NULL, " +
                "path TEXT NOT NULL, synced INTEGER NOT NULL, PRIMARY KEY(id))"
        )
    }
}

// 6 → 7: группы снимков и подписи к фото (разбор галереи на телефоне)
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE photos ADD COLUMN groupId TEXT")
        db.execSQL("ALTER TABLE photos ADD COLUMN note TEXT")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS photo_groups (" +
                "id TEXT NOT NULL, name TEXT NOT NULL, note TEXT, locationKind TEXT NOT NULL, " +
                "locationId TEXT, locationPath TEXT, personId TEXT, personName TEXT, " +
                "personIsMol INTEGER NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(id))"
        )
    }
}

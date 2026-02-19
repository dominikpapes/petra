package com.petra.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.time.LocalDateTime

@Database(entities = [Pet::class, PetActivity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun petActivityDao(): PetActivityDao
}

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }
}

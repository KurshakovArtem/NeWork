package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.PostDao
import ru.netology.nework.entity.Converter
import ru.netology.nework.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)

@TypeConverters(Converter::class)
abstract class AppDb : RoomDatabase() {
    abstract val postDao: PostDao
}
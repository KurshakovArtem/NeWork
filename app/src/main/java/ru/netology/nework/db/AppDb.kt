package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.entity.Converter
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.UserEntity

@Database(entities = [PostEntity::class, UserEntity::class], version = 1, exportSchema = false)

@TypeConverters(Converter::class)
abstract class AppDb : RoomDatabase() {
    abstract val postDao: PostDao
    abstract val userDao: UserDao
}
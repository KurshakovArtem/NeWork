package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.UserEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: List<EventEntity>)

    @Query("SELECT * FROM EventEntity WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): EventEntity?

    @Query(
        """
        UPDATE EventEntity SET
                likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id;
    """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
        UPDATE EventEntity SET
                participatedByMe = CASE WHEN participatedByMe THEN 0 ELSE 1 END
                WHERE id = :id;
    """
    )
    suspend fun participantsById(id: Long)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}
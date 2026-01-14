package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Event

interface EventRepository {

    val events: Flow<List<Event>>

    suspend fun getAllEvents()
    suspend fun likeEventById(id: Long)
    suspend fun removeEventBiId(id: Long)

}
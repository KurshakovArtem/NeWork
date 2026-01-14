package ru.netology.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.PostApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dto.Event
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventEntity.Companion.fromEvent
import ru.netology.nework.entity.PostEntity.Companion.fromDto
import ru.netology.nework.entity.toEvent
import ru.netology.nework.entity.toEventEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val apiService: PostApiService,
    private val eventDao: EventDao
): EventRepository  {

    override val events: Flow<List<Event>> = eventDao.getAllEvents()
        .map(List<EventEntity>::toEvent)
        .flowOn(Dispatchers.Default)

    override suspend fun getAllEvents() {
        try {
            val body = apiService.getAllEvents()
            eventDao.insert(body.toEventEntity())
        } catch (_: Exception) {
            throw RuntimeException("Load Events Error")
        }
    }

    override suspend fun likeEventById(id: Long) {
        val isLiked =
            eventDao.getEventById(id)?.toEvent()?.likedByMe ?: throw RuntimeException("DB error")
        try {
            eventDao.likeById(id)
            val body = if (!isLiked) apiService.likeEventById(id) else apiService.dislikeEventById(id)
            eventDao.insert(fromEvent(body))
        } catch (_: Exception) {
            eventDao.likeById(id)
            throw RuntimeException("Server error")
        }
    }

    override suspend fun removeEventBiId(id: Long) {
        val oldPost = eventDao.getEventById(id)?.toEvent() ?: throw RuntimeException("DB error")
        try {
            eventDao.removeById(id)
            apiService.removeEventById(id)
        } catch (_: Exception) {
            eventDao.insert(fromEvent(oldPost))
            throw RuntimeException("Server error")
        }
    }


}
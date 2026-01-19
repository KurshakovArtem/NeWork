package ru.netology.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.PostApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventRequest
import ru.netology.nework.dto.Media
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventEntity.Companion.fromEvent
import ru.netology.nework.entity.toEvent
import ru.netology.nework.entity.toEventEntity
import ru.netology.nework.enumeration.AttachmentType
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val apiService: PostApiService,
    private val eventDao: EventDao
) : EventRepository {

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
            val body =
                if (!isLiked) apiService.likeEventById(id) else apiService.dislikeEventById(id)
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

    override suspend fun participantsById(id: Long) {
        val isParticipated =
            eventDao.getEventById(id)?.toEvent()?.participatedByMe
                ?: throw RuntimeException("DB error")
        try {
            eventDao.participantsById(id)
            val body =
                if (!isParticipated) apiService.addParticipantsById(id) else apiService.removeParticipantsById(
                    id
                )
            eventDao.insert(fromEvent(body))
        } catch (_: Exception) {
            eventDao.participantsById(id)
            throw RuntimeException("Server error")
        }
    }

    override suspend fun saveEvent(event: Event, attach: File?) {
        try {
            val media = attach?.let {
                upload(it)
            }
            val eventWithAttachment = event.copy(attachment = media?.let {
                Attachment(url = it.url, type = AttachmentType.IMAGE)
            })
            val eventRequest = EventRequest(
                id = eventWithAttachment.id,
                content = eventWithAttachment.content,
                coords = eventWithAttachment.coords,
                link = eventWithAttachment.link,
                attachment = eventWithAttachment.attachment,
                speakerIds = eventWithAttachment.speakerIds,
                datetime = eventWithAttachment.datetime,
                type = eventWithAttachment.type.toString()
            )
            val eventFromServer =
                apiService.saveEvent(eventRequest)

            eventDao.insert(fromEvent(eventFromServer))

        } catch (_: Exception) {
            throw RuntimeException("Save error")
        }
    }

    override suspend fun editEvent(event: Event) {
        try {
            val eventFromServer = apiService.saveEvent(event)
            eventDao.insert(fromEvent(eventFromServer))
        } catch (_: Exception) {
            throw RuntimeException("Edit error")
        }
    }

    private suspend fun upload(file: File): Media {
        try {
            return apiService.upload(
                MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody()
                )
            )
        } catch (_: Exception) {
            println("ощибка загрузки изображения")
            throw RuntimeException("ощибка загрузки изображения")
        }
    }


}
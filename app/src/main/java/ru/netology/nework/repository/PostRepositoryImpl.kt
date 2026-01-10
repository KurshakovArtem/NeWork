package ru.netology.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.PostApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.PostRequest
import ru.netology.nework.dto.User
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostEntity.Companion.fromDto
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.entity.toUser
import ru.netology.nework.entity.toUserEntity
import ru.netology.nework.enumeration.AttachmentType
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val apiService: PostApiService,
    private val postDao: PostDao,
    private val userDao: UserDao,
) : PostRepository {


    override val data = postDao.getAllPosts()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override val usersData = userDao.getAllUsers()
        .map(List<UserEntity>::toUser)
        .flowOn(Dispatchers.Default)

    override suspend fun getAllPosts() {
        try {
            val body = apiService.getAllPosts()
            postDao.insert(body.toEntity())
        } catch (_: Exception) {

        }
    }

    override suspend fun getAllUsers(): List<User> {
        try {
            val body = apiService.getAllUsers()
            userDao.insert(body.toUserEntity())
            return body
        } catch (_: Exception) {
            return emptyList()
        }
    }

    override suspend fun savePost(post: Post, attach: File?) {
        try {
            val media = attach?.let {
                upload(it)
            }
            val postWithAttachment = post.copy(attachment = media?.let {
                Attachment(url = it.url, type = AttachmentType.IMAGE)
            })
            val postRequest = PostRequest(
                id = postWithAttachment.id,
                content = postWithAttachment.content,
                coords = postWithAttachment.coords,
                link = postWithAttachment.link,
                attachment = postWithAttachment.attachment,
                mentionIds = postWithAttachment.mentionIds,
            )

            val postFromServer =
                apiService.save(postRequest)

            postDao.insert(PostEntity.fromDto(postFromServer))

        } catch (_: Exception) {
            throw RuntimeException("Save error")
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


    override suspend fun likePostById(id: Long) {
        val isLiked =
            postDao.getPostById(id)?.toDto()?.likedByMe ?: throw RuntimeException("DB error")
        try {
            postDao.likeById(id)
            val body = if (!isLiked) apiService.likeById(id) else apiService.dislikeById(id)
            postDao.insert(fromDto(body))
        } catch (_: Exception) {
            postDao.likeById(id)
            throw RuntimeException("Server error")
        }

    }

    override suspend fun removePostsBiId(id: Long) {
        val oldPost = postDao.getPostById(id)?.toDto() ?: throw RuntimeException("DB error")
        try {
            postDao.removeById(id)
            apiService.removeById(id)
        } catch (_: Exception) {
            postDao.insert(fromDto(oldPost))
            throw RuntimeException("Server error")
        }
    }


}
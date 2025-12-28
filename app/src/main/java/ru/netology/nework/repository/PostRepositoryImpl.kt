package ru.netology.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.netology.nework.api.PostApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.ResponsePost
import ru.netology.nework.dto.toPost
import ru.netology.nework.entity.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor (
    private val apiService: PostApiService,
    private val postDao: PostDao,
) : PostRepository {

    override val data = flow {
        emit(
        apiService.getAllPosts().map(ResponsePost::toPost)
        )
    }
        .flowOn(Dispatchers.Default)

    override suspend fun getAllPosts() {
        val body = apiService.getAllPosts().map(ResponsePost::toPost)
        postDao.insert(body.toEntity())
    }
}
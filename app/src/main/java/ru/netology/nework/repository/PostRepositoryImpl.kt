package ru.netology.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.netology.nework.api.PostApiService
import ru.netology.nework.dto.Post
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor (
    private val apiService: PostApiService
) : PostRepository {

    override val data = flow {
        emit(
        apiService.getAllPosts()
        )
    }
        .flowOn(Dispatchers.Default)

    override suspend fun getAllPosts() {
        apiService.getAllPosts()
    }
}
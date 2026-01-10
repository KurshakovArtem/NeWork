package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import java.io.File

interface PostRepository {
    val data: Flow<List<Post>>
    val usersData: Flow<List<User>>
    suspend fun getAllPosts()
    suspend fun likePostById(id: Long)
    suspend fun removePostsBiId(id: Long)
    suspend fun getAllUsers(): List<User>

    suspend fun savePost(post: Post, attach: File?)


}
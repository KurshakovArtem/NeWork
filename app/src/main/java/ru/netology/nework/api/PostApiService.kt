package ru.netology.nework.api

import kotlinx.coroutines.flow.Flow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import ru.netology.nework.BuildConfig
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.ResponsePost
import java.util.concurrent.TimeUnit


private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"

fun client(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()


interface PostApiService {

    @GET("posts")
    suspend fun getAllPosts(): List<ResponsePost>

}
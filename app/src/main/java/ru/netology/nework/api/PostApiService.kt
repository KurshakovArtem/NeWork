package ru.netology.nework.api

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nework.BuildConfig
import ru.netology.nework.dto.AuthState
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.PostRequest
import ru.netology.nework.dto.User
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
    suspend fun getAllPosts(): List<Post>

    @POST("posts/{id}/likes")
    suspend fun likePostById(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislikePostById(@Path("id") id: Long): Post

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Event

    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Long): Event

    @DELETE("posts/{id}")
    suspend fun removePostById(@Path("id") id: Long)

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long)

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): AuthState

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): AuthState

    @Multipart
    @POST("users/registration")
    suspend fun registerWithPhoto(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part,
    ): AuthState

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Media

    @POST("posts")
    suspend fun save(@Body post: PostRequest): Post

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @GET("users")
    suspend fun getAllUsers(): List<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long?): User


    @GET("events")
    suspend fun getAllEvents(): List<Event>
}
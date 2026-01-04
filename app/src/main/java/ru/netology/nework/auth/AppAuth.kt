package ru.netology.nework.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.PostApiService
import ru.netology.nework.dto.AuthState
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {

    private val idKey = "id"
    private val tokenKey = "token"
    private val avatarKey = "avatar"

    private val prefs =
        context.applicationContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)
        val avatar = prefs.getString(avatarKey, null)
        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            prefs.edit { clear() }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token, avatar))
        }
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun apiService(): PostApiService
    }

    suspend fun sendLoginPassword(username: String, password: String) {
        try {
            val authState =
                getApiService(context).updateUser(username, password)
            setAuth(authState.id, authState.token, authState.avatar)
        } catch (_: Exception) {
            setAuth(0L, null, null)
            throw RuntimeException("Ошибка авторизации")
        }
    }

    suspend fun sendRegistration(nickname: String, login: String, password: String) {
        try {
            val authState = getApiService(context).registerUser(
                login,
                password,
                nickname
            )
            setAuth(authState.id, authState.token, authState.avatar)
        } catch (_: Exception) {
            setAuth(0L, null, null)
            throw RuntimeException("Ошибка регистрации")
        }
    }

    suspend fun sendRegistrationWithPhoto(
        nickname: String,
        login: String,
        password: String,
        file: File
    ) {
        try {
            val authState = getApiService(context).registerWithPhoto(
                login = login.toRequestBody("text/plain".toMediaType()),
                pass = password.toRequestBody("text/plain".toMediaType()),
                name = nickname.toRequestBody("text/plain".toMediaType()),
                media = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody()
                )
            )
            setAuth(authState.id, authState.token, authState.avatar)
        } catch (_: Exception) {
            setAuth(0L, null, null)
            throw RuntimeException("Ошибка регистрации")
        }
    }

    @Synchronized
    fun setAuth(id: Long, token: String?, avatar: String?) {
        _authStateFlow.value = AuthState(id, token, avatar)
        prefs.edit {
            putLong(idKey, id)
            putString(tokenKey, token)
            putString(avatarKey, avatar)
        }
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        prefs.edit { clear() }
    }


    private fun getApiService(context: Context): PostApiService {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context,
            AppAuthEntryPoint::class.java
        )
        return hiltEntryPoint.apiService()
    }
}
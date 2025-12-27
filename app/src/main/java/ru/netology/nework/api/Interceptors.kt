package ru.netology.nework.api

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.nework.BuildConfig


fun loggingInterceptor() = HttpLoggingInterceptor()
    .apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

fun apiKeyInterceptor(apiKey: String): Interceptor = Interceptor { chain ->
    val requestWithApiKey = chain.request().newBuilder()
        .header("Api-Key", apiKey)
        .build()
    chain.proceed(requestWithApiKey)
}

//    fun authInterceptor(auth: AppAuth) = fun(chain: Interceptor.Chain): Response {
//        auth.authStateFlow.value.token?.let { token ->
//            val newRequest = chain.request().newBuilder()
//                .addHeader("Authorization", token)
//                .build()
//            return chain.proceed(newRequest)
//        }
//
//        return chain.proceed(chain.request())
//    }

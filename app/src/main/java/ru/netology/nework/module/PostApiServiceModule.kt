package ru.netology.nework.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.BuildConfig

import ru.netology.nework.api.PostApiService
import ru.netology.nework.api.apiKeyInterceptor
import ru.netology.nework.api.client
import ru.netology.nework.api.loggingInterceptor
import ru.netology.nework.api.retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PostApiServiceModule {

    @Provides
    @Singleton
    fun providePostApiService(): PostApiService {
        return retrofit(
            client(
                apiKeyInterceptor(BuildConfig.API_KEY),
                loggingInterceptor(),
                //authInterceptor(auth)
            )
        ).create(PostApiService::class.java)
    }
}
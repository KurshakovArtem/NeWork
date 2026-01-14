package ru.netology.nework.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.repository.EventRepositoryImpl
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.PostRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindsEventRepository(impl: EventRepositoryImpl): EventRepository

}
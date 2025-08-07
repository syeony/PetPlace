package com.example.petplace.di

import com.example.petplace.data.remote.ChatApiService
import com.example.petplace.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(chatApiService: ChatApiService): ChatRepository {
        return ChatRepository(chatApiService)
    }
}
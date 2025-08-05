package com.example.petplace.di

import com.example.petplace.data.remote.FeedApiService
import com.example.petplace.data.repository.FeedRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {
    /* 1) Retrofit → FeedApiService */
    @Provides
    @Singleton
    fun provideFeedApiService(
        @Named("Server") retrofit: Retrofit
    ): FeedApiService = retrofit.create(FeedApiService::class.java)

    /* 2) Repository */
    @Provides
    @Singleton
    fun provideFeedRepository(
        api: FeedApiService
    ): FeedRepository = FeedRepository(api)
}

package com.example.petplace.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {
    /* 1) Retrofit → FeedApiService */
//    @Provides
//    @Singleton
//    fun provideFeedApiService(
//        @Named("Server") retrofit: Retrofit
//    ): FeedApiService = retrofit.create(FeedApiService::class.java)

//    /* 2) Repository */
//    @Provides
//    @Singleton
//    fun provideFeedRepository(
//        api: FeedApiService
//    ): FeedRepository = FeedRepository(api)
}

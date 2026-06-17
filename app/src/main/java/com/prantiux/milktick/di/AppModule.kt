package com.prantiux.milktick.di

import android.content.Context
import com.prantiux.milktick.data.local.AppDatabase
import com.prantiux.milktick.repository.LocalRepository
import com.prantiux.milktick.repository.MainRepository
import com.prantiux.milktick.repository.RemoteSyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideLocalRepository(database: AppDatabase): LocalRepository {
        return LocalRepository(database)
    }

    @Provides
    @Singleton
    fun provideRemoteSyncService(localRepository: LocalRepository): RemoteSyncService {
        return RemoteSyncService(localRepository)
    }

    @Provides
    @Singleton
    fun provideMainRepository(
        @ApplicationContext context: Context,
        localRepository: LocalRepository,
        remoteSyncService: RemoteSyncService
    ): MainRepository {
        return MainRepository(context, localRepository, remoteSyncService)
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(SingletonComponent::class)
interface RepositoryEntryPoint {
    fun mainRepository(): MainRepository
}

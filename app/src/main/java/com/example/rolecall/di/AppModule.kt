package com.example.rolecall.di

import android.content.Context
import androidx.room.Room
import com.example.rolecall.data.local.AppDatabase
import com.example.rolecall.data.local.dao.MatchHistoryDao
import com.example.rolecall.data.local.dao.ResumeDao
import com.example.rolecall.data.local.dao.SavedJobDao
import com.example.rolecall.data.local.dao.GeneratedResumeDao
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.network.ResumeCreationRepository
import com.example.rolecall.network.TokenManager
import com.example.rolecall.network.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Networking ───────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideFastAPIRepository(tokenManager: TokenManager): FastAPIRepository {
        return FastAPIRepository(tokenManager)
    }

    @Provides
    @Singleton
    fun provideResumeCreationRepository(tokenManager: TokenManager): ResumeCreationRepository {
        return ResumeCreationRepository(tokenManager)
    }

    // ── Room Database ────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rolecall_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSavedJobDao(database: AppDatabase): SavedJobDao {
        return database.savedJobDao()
    }

    @Provides
    @Singleton
    fun provideResumeDao(database: AppDatabase): ResumeDao {
        return database.resumeDao()
    }

    @Provides
    @Singleton
    fun provideMatchHistoryDao(database: AppDatabase): MatchHistoryDao {
        return database.matchHistoryDao()
    }

    @Provides
    @Singleton
    fun provideJobRepository(
        savedJobDao: SavedJobDao,
        resumeDao: ResumeDao,
        matchHistoryDao: MatchHistoryDao
    ): JobRepository {
        return JobRepository(savedJobDao, resumeDao, matchHistoryDao)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(tokenManager: TokenManager): ProfileRepository {
        return ProfileRepository(tokenManager)
    }

    @Provides
    @Singleton
    fun provideGeneratedResumeDao(database: AppDatabase): GeneratedResumeDao {
        return database.generatedResumeDao()
    }
}
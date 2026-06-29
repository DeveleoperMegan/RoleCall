package com.example.rolecall.di

import android.content.Context
import androidx.room.Room
import com.example.rolecall.data.local.AppDatabase
import com.example.rolecall.data.local.dao.MatchHistoryDao
import com.example.rolecall.data.local.dao.ResumeDao
import com.example.rolecall.data.local.dao.SavedJobDao
import com.example.rolecall.data.remote.ResumeApiService
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.data.repository.ResumeRepository
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.network.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Networking ──

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
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideResumeApiService(retrofit: Retrofit): ResumeApiService {
        return retrofit.create(ResumeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideResumeRepository(apiService: ResumeApiService): ResumeRepository {
        return ResumeRepository(apiService)
    }

    // ── Room Database ──

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
}
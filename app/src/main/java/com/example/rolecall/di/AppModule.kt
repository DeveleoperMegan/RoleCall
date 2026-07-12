package com.example.rolecall.di

import android.content.Context
import androidx.room.Room
import com.example.rolecall.data.local.AppDatabase
import com.example.rolecall.data.local.dao.MatchHistoryDao
import com.example.rolecall.data.local.dao.ResumeDao
import com.example.rolecall.data.local.dao.SavedJobDao
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.network.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ──────────────────────────────────────────────────────────────────────────────
// AppModule
// Hilt dependency injection module. Provides singleton instances of:
//   - TokenManager (stores/retrieves JWT in EncryptedSharedPreferences)
//   - FastAPIRepository (Ktor-based HTTP client with Bearer auth)
//   - Room database and all DAOs
//   - JobRepository (local DB operations)
//
// Note: There is no Retrofit or OkHttp here. All networking goes through
// FastAPIRepository which uses the Ktor client from ApiClient. That client
// already has the auth plugin configured to send the JWT as a Bearer token.
// ──────────────────────────────────────────────────────────────────────────────

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
}
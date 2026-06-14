package com.example.rolecall.di
import android.content.Context
import androidx.room.Room
import com.example.rolecall.data.local.AppDatabase
import com.example.rolecall.data.local.dao.MatchHistoryDao
import com.example.rolecall.data.local.dao.ResumeDao
import com.example.rolecall.data.local.dao.SavedJobDao
import com.example.rolecall.data.repository.JobRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rolecall_database"
        ).build()
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

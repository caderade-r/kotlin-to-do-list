package edu.uark.cm288.to_dolist.ui

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.uark.cm288.to_dolist.data.local.ToDoListDatabase
import edu.uark.cm288.to_dolist.data.local.ToDoListRepository
import edu.uark.cm288.to_dolist.data.local.ToDoDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideToDoListDatabase(@ApplicationContext context: Context): ToDoListDatabase {
        return Room.databaseBuilder(
            context,
            ToDoListDatabase::class.java,
            "todo_list_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideToDoListDao(database: ToDoListDatabase): ToDoDao {
        return database.toDoDao()
    }

    @Provides
    @Singleton
    fun provideToDoListRepository(toDoDao: ToDoDao): ToDoListRepository {
        return ToDoListRepository(toDoDao)
    }
}

package edu.uark.cm288.to_dolist.data.local

import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import edu.uark.cm288.to_dolist.domain.ToDoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todo_items ORDER BY id ASC")
    fun getAllToDoItems(): Flow<List<ToDoEntity>>

    @Query("SELECT * FROM todo_items WHERE id = :id")
    suspend fun getToDoItemById(id: Int): ToDoEntity?

    @Insert
    suspend fun insertToDoItem(toDoItem: ToDoEntity): Long

    @Update
    suspend fun updateToDoItem(toDoItem: ToDoEntity)

    @Delete
    suspend fun deleteToDoItem(toDoItem: ToDoEntity)
}

@Database(entities = [ToDoEntity::class], version = 2)
abstract class ToDoListDatabase : RoomDatabase() {
    abstract fun toDoDao(): ToDoDao

    companion object {
        @Volatile
        private var INSTANCE: ToDoListDatabase? = null

        fun getDatabase(context: Context): ToDoListDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoListDatabase::class.java,
                    "todo_list_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

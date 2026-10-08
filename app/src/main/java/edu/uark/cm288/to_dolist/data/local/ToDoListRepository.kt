package edu.uark.cm288.to_dolist.data.local

import android.util.Log
import edu.uark.cm288.to_dolist.domain.ToDoEntity
import kotlinx.coroutines.flow.Flow

class ToDoListRepository(private val toDoDao: ToDoDao) {

    val ToDoList: Flow<List<ToDoEntity>> = toDoDao.getAllToDoItems()

    suspend fun getToDoItemById(id: Int): ToDoEntity? = toDoDao.getToDoItemById(id)

    suspend fun insertToDoItem(toDoItem: ToDoEntity): Long {
        return toDoDao.insertToDoItem(toDoItem)
    }

    suspend fun updateToDoItem(toDoItem: ToDoEntity) {
        toDoDao.updateToDoItem(toDoItem)
    }

    suspend fun deleteToDoItem(toDoItem: ToDoEntity) {
        toDoDao.deleteToDoItem(toDoItem)
    }
}

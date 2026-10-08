package edu.uark.cm288.to_dolist.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class ToDoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dueDate: String,
    val dueTime: String = "",
    val alarmEnabled: Boolean = true,
    val isCompleted: Boolean = false
)

package edu.uark.cm288.to_dolist.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.uark.cm288.to_dolist.data.local.ToDoListRepository
import edu.uark.cm288.to_dolist.domain.ToDoEntity
import edu.uark.cm288.to_dolist.notification.ToDoAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToDoListViewModel @Inject constructor(
    private val repository: ToDoListRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val toDoList: StateFlow<List<ToDoEntity>> = repository.ToDoList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val itemId: Int = savedStateHandle.get<Int>("id") ?: 0

    var currentItem = mutableStateOf<ToDoEntity?>(null)
        private set

    init {
        if (itemId > 0) {
            viewModelScope.launch {
                currentItem.value = repository.getToDoItemById(itemId)
            }
        }
    }

    fun saveToDoItem(
        title: String,
        description: String,
        dueDate: String,
        dueTime: String,
        alarmEnabled: Boolean,
        isCompleted: Boolean = false
    ) {
        viewModelScope.launch {
            if (itemId > 0) {
                currentItem.value?.let { existing ->
                    val updated = existing.copy(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        alarmEnabled = alarmEnabled,
                        isCompleted = isCompleted
                    )
                    repository.updateToDoItem(updated)
                    if (alarmEnabled && dueDate.isNotBlank() && !isCompleted) {
                        ToDoAlarmScheduler.scheduleAlarm(context, updated)
                    } else {
                        ToDoAlarmScheduler.cancelAlarm(context, updated.id)
                    }
                }
            } else {
                val newEntity = ToDoEntity(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    alarmEnabled = alarmEnabled,
                    isCompleted = isCompleted
                )
                val newId = repository.insertToDoItem(newEntity).toInt()
                val savedEntity = newEntity.copy(id = newId)
                if (alarmEnabled && dueDate.isNotBlank() && !isCompleted) {
                    ToDoAlarmScheduler.scheduleAlarm(context, savedEntity)
                }
            }
        }
    }

    fun updateToDoItem(item: ToDoEntity) {
        viewModelScope.launch {
            repository.updateToDoItem(item)
            if (item.alarmEnabled && item.dueDate.isNotBlank() && !item.isCompleted) {
                ToDoAlarmScheduler.scheduleAlarm(context, item)
            } else {
                ToDoAlarmScheduler.cancelAlarm(context, item.id)
            }
        }
    }

    fun deleteToDoItem(item: ToDoEntity) {
        viewModelScope.launch {
            ToDoAlarmScheduler.cancelAlarm(context, item.id)
            repository.deleteToDoItem(item)
        }
    }

    fun deleteCurrentItem() {
        viewModelScope.launch {
            currentItem.value?.let { item ->
                ToDoAlarmScheduler.cancelAlarm(context, item.id)
                repository.deleteToDoItem(item)
            }
        }
    }

    suspend fun getToDoItemById(id: Int): ToDoEntity? {
        return repository.getToDoItemById(id)
    }
}

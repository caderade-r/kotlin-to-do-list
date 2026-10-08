package edu.uark.cm288.to_dolist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.uark.cm288.to_dolist.data.local.ToDoDao
import edu.uark.cm288.to_dolist.data.local.ToDoListDatabase
import edu.uark.cm288.to_dolist.domain.ToDoEntity
import edu.uark.cm288.to_dolist.notification.ToDoAlarmScheduler
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ToDoListAssignmentTest {

    private lateinit var db: ToDoListDatabase
    private lateinit var toDoDao: ToDoDao
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ToDoListDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        toDoDao = db.toDoDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCreateReadUpdateDeleteTask() = runBlocking {
        // 1. Create a new Task
        val newTask = ToDoEntity(
            title = "Test Task",
            description = "Test Description",
            dueDate = "2026-03-30",
            dueTime = "14:30",
            alarmEnabled = true,
            isCompleted = false
        )
        val insertId = toDoDao.insertToDoItem(newTask)
        assertTrue(insertId > 0)

        // 2. Read / Select task and display details
        val fetchedItem = toDoDao.getToDoItemById(insertId.toInt())
        assertNotNull(fetchedItem)
        assertEquals("Test Task", fetchedItem?.title)
        assertEquals("Test Description", fetchedItem?.description)
        assertEquals("2026-03-30", fetchedItem?.dueDate)
        assertEquals("14:30", fetchedItem?.dueTime)
        assertFalse(fetchedItem?.isCompleted == true)

        // 3. Update Title, Content, and Due Date
        val updatedItem = fetchedItem!!.copy(
            title = "Updated Title",
            description = "Updated Description",
            dueDate = "2026-04-01",
            dueTime = "10:00"
        )
        toDoDao.updateToDoItem(updatedItem)
        val reFetched = toDoDao.getToDoItemById(insertId.toInt())
        assertEquals("Updated Title", reFetched?.title)
        assertEquals("Updated Description", reFetched?.description)
        assertEquals("2026-04-01", reFetched?.dueDate)
        assertEquals("10:00", reFetched?.dueTime)

        // 7. Update Completion Status
        val completedItem = reFetched!!.copy(isCompleted = true)
        toDoDao.updateToDoItem(completedItem)
        val finalFetched = toDoDao.getToDoItemById(insertId.toInt())
        assertTrue(finalFetched?.isCompleted == true)

        // 4. Delete Task
        toDoDao.deleteToDoItem(finalFetched!!)
        val deletedCheck = toDoDao.getToDoItemById(insertId.toInt())
        assertNull(deletedCheck)
    }

    @Test
    fun testAlarmSchedulerDateParsing() {
        // 8. Test Alarm Scheduling & Date Parsing
        val rawMillis = ToDoAlarmScheduler.resolveRawDueTime("2026-03-30", "14:30")
        assertNotNull(rawMillis)
        assertTrue((rawMillis ?: 0L) > 0L)
    }
}

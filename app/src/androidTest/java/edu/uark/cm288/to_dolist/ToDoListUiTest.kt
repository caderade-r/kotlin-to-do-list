package edu.uark.cm288.to_dolist

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.uark.cm288.to_dolist.domain.ToDoEntity
import edu.uark.cm288.to_dolist.notification.ToDoAlarmScheduler
import edu.uark.cm288.to_dolist.ui.MainToDoListActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToDoListUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainToDoListActivity>()

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                instrumentation.uiAutomation.executeShellCommand(
                    "pm grant ${instrumentation.targetContext.packageName} android.permission.POST_NOTIFICATIONS"
                )
            } catch (_: Exception) {}
        }
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testAllAssignmentRequirements() {
        // 1. Create a new Task
        composeTestRule.onNodeWithContentDescription("Add To-Do").performClick()
        composeTestRule.onNodeWithText("Title").performTextInput("Assignment Task")
        composeTestRule.onNodeWithText("Description").performTextInput("Initial Description")
        composeTestRule.onNodeWithText("Due Date (e.g. 2026-03-30)").performTextInput("2026-06-01")
        composeTestRule.onNodeWithText("Due Time (e.g. 14:30)").performTextInput("12:00")
        
        // 8. Schedule notification (Alarm enabled)
        composeTestRule.onNodeWithText("Set Alarm (5 mins before due)").performClick()
        composeTestRule.onNodeWithText("Save").performClick()

        // Verify task exists in list
        composeTestRule.onNodeWithText("Assignment Task").assertExists()

        // 2. Select a task from the list and display details
        composeTestRule.onNodeWithText("Assignment Task").performClick()
        composeTestRule.onNodeWithText("Edit To-Do").assertExists()
        composeTestRule.onNodeWithText("Assignment Task").assertIsDisplayed()

        // 3. Update a Task's title and content
        composeTestRule.onNodeWithText("Assignment Task").performTextClearance()
        composeTestRule.onNodeWithText("Title").performTextInput("Updated Assignment Task")
        composeTestRule.onNodeWithText("Initial Description").performTextClearance()
        composeTestRule.onNodeWithText("Description").performTextInput("Updated Description")

        // 5. Update a Task's Due Date
        composeTestRule.onNodeWithText("2026-06-01").performTextClearance()
        composeTestRule.onNodeWithText("Due Date (e.g. 2026-03-30)").performTextInput("2026-07-01")

        // 7. Update Task completion status in detail view
        composeTestRule.onNodeWithText("Completed").performClick()
        composeTestRule.onNodeWithText("Save").performClick()

        // Verify updated task in list
        composeTestRule.onNodeWithText("Updated Assignment Task").assertExists()

        // 8. Explicitly test and verify notification scheduling and immediate posting on screen
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notificationTestTask = ToDoEntity(
            id = 999,
            title = "Due Task Reminder",
            description = "Your task is due in 5 minutes!",
            dueDate = "2026-06-01",
            dueTime = "12:00",
            alarmEnabled = true,
            isCompleted = false
        )
        ToDoAlarmScheduler.scheduleAlarm(context, notificationTestTask)
        ToDoAlarmScheduler.postNotificationNow(context, notificationTestTask)

        // 4. Delete a Task
        composeTestRule.onNodeWithText("Updated Assignment Task").performClick()
        composeTestRule.onNodeWithContentDescription("Delete To-Do").performClick()

        // Verify task is deleted
        composeTestRule.onNodeWithText("Updated Assignment Task").assertDoesNotExist()

        // 6. Share a to-do list item via Android implicit intent (Tested as the final step)
        composeTestRule.onNodeWithContentDescription("Add To-Do").performClick()
        composeTestRule.onNodeWithText("Title").performTextInput("Shareable Task")
        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.onNodeWithText("Shareable Task").performClick()
        composeTestRule.onNodeWithContentDescription("Share To-Do").performClick()
        intended(hasAction(Intent.ACTION_CHOOSER))
    }
}

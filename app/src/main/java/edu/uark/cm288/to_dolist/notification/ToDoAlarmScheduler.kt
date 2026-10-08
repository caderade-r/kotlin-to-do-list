package edu.uark.cm288.to_dolist.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import edu.uark.cm288.to_dolist.domain.ToDoEntity
import java.text.SimpleDateFormat
import java.util.*

object ToDoAlarmScheduler {

    private const val TAG = "ToDoAlarmScheduler"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(context: Context, task: ToDoEntity) {
        if (!task.alarmEnabled && task.dueDate.isBlank()) {
            Log.d(TAG, "Alarm not enabled and due date blank for task ${task.id}")
            return
        }

        val rawMillis = resolveRawDueTime(task.dueDate, task.dueTime) ?: run {
            Log.d(TAG, "Could not resolve due time for task ${task.id}")
            return
        }

        val currentTime = System.currentTimeMillis()
        val triggerTimeMillis = if (rawMillis > currentTime) {
            rawMillis
        } else {
            currentTime + 2000L
        }

        Log.d(TAG, "Scheduling alarm for task ${task.id} ('${task.title}') at $triggerTimeMillis")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ToDoAlarmReceiver::class.java).apply {
            putExtra(ToDoAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ToDoAlarmReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(ToDoAlarmReceiver.EXTRA_TASK_DESCRIPTION, task.description.ifBlank { "Task due now." })
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "AlarmClock successfully set for task ${task.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set AlarmClock, falling back to inexact alarm", e)
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } catch (innerE: Exception) {
                Log.e(TAG, "Failed to set fallback alarm", innerE)
            }
        }
    }

    fun cancelAlarm(context: Context, taskId: Int) {
        Log.d(TAG, "Cancelling alarm for task $taskId")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ToDoAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun postNotificationNow(context: Context, task: ToDoEntity) {
        val intent = Intent(context, ToDoAlarmReceiver::class.java).apply {
            putExtra(ToDoAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ToDoAlarmReceiver.EXTRA_TASK_TITLE, "${task.title} (Due in 5 mins)")
            putExtra(ToDoAlarmReceiver.EXTRA_TASK_DESCRIPTION, task.description.ifBlank { "Your task is due shortly." })
        }
        context.sendBroadcast(intent)
    }

    fun resolveRawDueTime(dueDate: String, dueTime: String): Long? {
        val trimmedDate = dueDate.trim()
        val trimmedTime = dueTime.trim()

        return if (trimmedDate.isNotBlank() && trimmedTime.isNotBlank()) {
            parseDateTime("$trimmedDate $trimmedTime")
        } else if (trimmedDate.isNotBlank()) {
            parseDateTime(trimmedDate)
        } else if (trimmedTime.isNotBlank()) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            parseDateTime("$todayStr $trimmedTime")
        } else {
            null
        }
    }

    private fun parseDateTime(dateStr: String): Long? {
        dateStr.toLongOrNull()?.let { return it }

        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
            SimpleDateFormat("HH:mm", Locale.getDefault())
        )

        for (format in formats) {
            try {
                format.isLenient = false
                val date = format.parse(dateStr)
                if (date != null) {
                    val calendar = Calendar.getInstance().apply { time = date }
                    if (calendar.get(Calendar.YEAR) <= 1970) {
                        val today = Calendar.getInstance()
                        calendar.set(Calendar.YEAR, today.get(Calendar.YEAR))
                        calendar.set(Calendar.MONTH, today.get(Calendar.MONTH))
                        calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
                    }
                    return calendar.timeInMillis
                }
            } catch (_: Exception) {
            }
        }
        return null
    }
}

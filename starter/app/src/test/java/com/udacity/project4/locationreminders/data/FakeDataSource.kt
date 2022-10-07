package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlin.Result.Companion.success

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(): ReminderDataSource {

    var remindersList: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean){
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if(shouldReturnError){
            Result.Error("no reminders found")
        }else{
            Result.Success(remindersList.values.toList())
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError)
            return Result.Error("test exception")
        for(reminder in remindersList.values){
            if(reminder.id == id)
                return Result.Success(reminder)
        }
        return Result.Error("reminder not found")
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }


}
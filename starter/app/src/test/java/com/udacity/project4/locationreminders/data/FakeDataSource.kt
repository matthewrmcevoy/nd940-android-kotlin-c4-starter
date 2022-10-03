package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlin.Result.Companion.success

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()): ReminderDataSource {

private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean){
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if(shouldReturnError){
            Result.Error("no reminders found")
        }else{
            Result.Success(ArrayList(reminders))
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.let{
            for(reminder in it){
                if (reminder.id == id)
                    return Result.Success(reminder)
            }
        }
        return Result.Error("ID MISMATCH OR NOT FOUND")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}
package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest(remindersDao: RemindersDao, ioDispatcher: CoroutineDispatcher = Dispatchers.IO): RemindersLocalRepository(remindersDao, ioDispatcher){
    var remindersList: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false

    fun setReturnError(value: Boolean){
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError){
            return Result.Error("Error in test")
        }
        return Result.Success(remindersList.values.toList())
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError){
            return Result.Error("Error in test")
        }
        remindersList[id]?.let{
            return Result.Success(it)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList[reminder.id] = reminder
    }


}
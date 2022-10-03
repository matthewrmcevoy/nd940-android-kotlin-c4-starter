package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    @Before
    fun initdB(){
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java).build()
    }
    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderThenGetReminderById() = runBlockingTest {
        val reminder1 = ReminderDTO("Title","Desc","Location",0.0, 0.0)
        database.reminderDao().saveReminder(reminder1)

        val retrievedReminder = database.reminderDao().getReminderById(reminder1.id)

        assertThat<ReminderDTO>(retrievedReminder as ReminderDTO, notNullValue())
        assertThat(retrievedReminder.id, `is`(reminder1.id))
        assertThat(retrievedReminder.title, `is`(reminder1.title))
        assertThat(retrievedReminder.description, `is`(reminder1.description))
        assertThat(retrievedReminder.location, `is`(reminder1.location))
        assertThat(retrievedReminder.latitude, `is`(reminder1.latitude))
        assertThat(retrievedReminder.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun saveRemindersandRetrieveAll() = runBlockingTest {
        val reminder1 = ReminderDTO("Title","Desc","Location",0.0, 0.0)
        val reminder2 = ReminderDTO("Title","Desc","Location",0.0, 0.0)
        val reminder3 = ReminderDTO("Title","Desc","Location",0.0, 0.0)
        val reminder4 = ReminderDTO("Title","Desc","Location",0.0, 0.0)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)
        database.reminderDao().saveReminder(reminder4)

        val reminderList = database.reminderDao().getReminders()

        assertThat(reminderList.size, `is`(4))
    }

}
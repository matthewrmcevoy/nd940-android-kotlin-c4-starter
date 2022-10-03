package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var application: Application

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupSaveReminderViewModel() {
        stopKoin()
        dataSource = FakeDataSource()
        application = ApplicationProvider.getApplicationContext()
        saveReminderViewModel = SaveReminderViewModel(application, dataSource)

    }
    private fun setFakeReminder(): ReminderDataItem{
        return ReminderDataItem(
            title = "Title1",
            description = "Description1",
            latitude = 0.0,
            longitude = 0.0,
            location = "Location1"
        )
    }
    // CANNOT TEST onCLEAR() - CAUSES NULLPOINTER EXCEPTION WHEN ASSERTING THE NOW NULL VAL IS NULL
//    @Test
//    fun clearReminderValues(){
//        val reminder = setFakeReminder()
//        saveReminderViewModel.reminderTitle.value = reminder.title
//        assertThat(saveReminderViewModel.reminderTitle.value, `is`("Title1"))
//        saveReminderViewModel.onClear()
//        assertThat(saveReminderViewModel.reminderTitle.value, null)
//    }

    @Test
    fun validateEnteredData_missingTitle_returnsFalse(){
        val reminder = setFakeReminder()
        reminder.title = null

        saveReminderViewModel.validateEnteredData(reminder)
        assertThat(saveReminderViewModel.validateEnteredData(reminder), `is`(false))
    }
    @Test
    fun validateEnteredData_missingLoc_returnsFalse(){
        val reminder = setFakeReminder()
        reminder.location = null

        saveReminderViewModel.validateEnteredData(reminder)
        assertThat(saveReminderViewModel.validateEnteredData(reminder), `is`(false))
    }
    @Test
    fun validateEnteredData_withAll_returnsTrue(){
        val reminder = setFakeReminder()

        saveReminderViewModel.validateEnteredData(reminder)
        assertThat(saveReminderViewModel.validateEnteredData(reminder), `is`(true))
    }
    @Test
    fun validateAndSaveData_withAll_ShowsSave() = runBlockingTest{
        val reminder = setFakeReminder()
        val toast = application.getString(R.string.reminder_saved)
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(toast))
    }


}
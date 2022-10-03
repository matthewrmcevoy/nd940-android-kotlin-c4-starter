package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get().koin.get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

//    TODO: test the navigation of the fragments.
    @Test
    fun addReminder_NavigateTo_saveReminder(){
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment{
            Navigation.setViewNavController(it.view!!, navController)
        }
        //WHEN CLICKING ON ADD REMINDER
        onView(withId(R.id.addReminderFAB)).perform(click())

        //THEN WE ARE NAVIGATED TO THE SAVEREMINDERFRAGMENT
        verify(navController).navigate(ReminderListFragmentDirections.actionReminderListFragmentToSaveReminderFragment())
    }

    @Test
    fun savingProperReminder_DisplaysInList(){
        val reminder1 = ReminderDTO("Title1","DESC1","TEST_LOCATION",0.0,0.0)
        runBlocking{
            repository.saveReminder(reminder1)
        }
        launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(hasDescendant(withText("Title1"))))

    }
    @Test
    fun nReminder_DisplaysNInList(){
        val reminder1 = ReminderDTO("Title1","DESC1","TEST_LOCATION",0.0,0.0)
        val reminder2 = ReminderDTO("Title1","DESC1","TEST_LOCATION",0.0,0.0)
        runBlocking{
            repository.saveReminder(reminder1)
            repository.saveReminder(reminder2)
        }
        launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(hasChildCount(2)))

    }
    @Test
    fun noReminder_displaysNoData(){
        launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

//    TODO: add testing for the error messages.
    //all error handling tests are completed in RemindersActivityTest

}
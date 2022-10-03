package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
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
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Test
    fun noReminders_ShowsNoDataImage() = runBlocking{
        repository.deleteAllReminders()
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun addReminder_NavigatesToSaveReminderFragment() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Check that a view of the Fragment we expect is displayed
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))

        activityScenario.close()
    }
    @Test
    fun saveReminderWithNoData_YieldsError() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))

        activityScenario.close()
    }
    @Test
    fun saveReminderNoLocation_YieldsError()= runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Test1"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Desc1"))
        onView(withId(R.id.saveReminder)).perform(click())
        delay(2000)
        onView(withText(R.string.err_select_location)).check(matches(isDisplayed()))


        activityScenario.close()
    }

    @Test
    fun saveProperReminder_isVisibleInListView()= runBlocking{
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Test1"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Desc1"))
        onView(withId(R.id.selectLocation)).perform(click())
        delay(2000)
        onView(withId(R.id.map)).perform(click())
        delay(2000)
        onView(withId(R.id.save_loc_bttn)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        delay(2000)
        onView(withText("Test1")).check(matches(isDisplayed()))
        activityScenario.close()

    }




}

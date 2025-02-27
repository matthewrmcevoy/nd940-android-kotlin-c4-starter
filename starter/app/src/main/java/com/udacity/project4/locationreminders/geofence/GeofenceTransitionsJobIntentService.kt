package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //        TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            Log.i("geoFenceJobIntServ","enqueueWork properly called")
            Log.i("geofenceJobIntServ", intent.action!!)
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        Log.i("geoFenceJobIntServ","onHandleWork properly called")
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //TODO call @sendNotification
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.i("geoFenceJobIntServ","checking GeofencingEvent.fromIntent $geofencingEvent")
        if(geofencingEvent.hasError()){
            Log.i("geoFenceJobIntServ","${geofencingEvent.errorCode}")
        }
        Log.i("geoFenceJobIntServ","geofenceTransition is ${geofencingEvent.geofenceTransition}")
        if(geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            Log.i("geoFenceJobIntServ", "geofence entered. Let's send some notifications")
            sendNotification(geofencingEvent.triggeringGeofences)
        }
    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
    Log.i("geoFenceJobIntServ","sendNotification called")

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            for (Geofence in triggeringGeofences) {
               val requestId = Geofence.requestId
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                            )
                    )
                }
            }
        }
    }

}

package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

const val ACTION_GEOFENCE_EVENT = "GEOFENCE_EVENT"
private const val REQUEST_FOREGROUND_ONLY = 123
private const val REQUEST_FOREBACK_PERM = 321
private const val REQUEST_LOCATION_ON = 55
const val GEOFENCE_RANGE = 300f

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var newGeofenceReminder: ReminderDataItem


    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val geofencePendingIntent: PendingIntent by lazy{
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }


        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            newGeofenceReminder = ReminderDataItem(
                title, description, location, latitude, longitude
            )
           //Must enable geofencing permissions prior to saving and setting geofencing so check permissions on save
            checkPermsStartGeofencing()

        }
    }

    private fun checkPermsStartGeofencing(){
        if(foregroundAndBackgroundLocationsPermsApproved()){
            checkLocationSettingsStartGeofence()
        }
        else{
            requestForeBackgroundLocationPerms()
        }
    }
    private fun foregroundAndBackgroundLocationsPermsApproved() : Boolean {
        //if running Q or later, require both foreground and background permissions
        return if(runningQOrLater) {
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        // otherwise, only require foreground location
        }else{
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    private fun requestForeBackgroundLocationPerms(){
        if(foregroundAndBackgroundLocationsPermsApproved()){
            return
        }else if(runningQOrLater){
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            requestPermissions(permissions, REQUEST_FOREBACK_PERM)
        }else{
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissions, REQUEST_FOREGROUND_ONLY)
        }
    }

    private fun checkLocationSettingsStartGeofence(resolve: Boolean = true){
        val locationRequest = LocationRequest.create().apply{
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnCompleteListener {
            if(it.isSuccessful){
                addNewGeofence()
            }
        }
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if(exception is ResolvableApiException && resolve){
                try{
                    activity?.let{
                        exception.startResolutionForResult(it, REQUEST_LOCATION_ON)
                    }
                }catch(sendEx: IntentSender.SendIntentException){
                    Log.i("SaveRemFrag","Could not resolve location settings " + sendEx.message)
                }
            }else{
                Snackbar.make(this.requireView(),R.string.location_required_error, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok){
                    checkLocationSettingsStartGeofence()
                }.show()
            }
        }
    }

    private fun addNewGeofence(){
        if(::newGeofenceReminder.isInitialized){
            if(_viewModel.validateEnteredData(newGeofenceReminder)){
                val geofence = Geofence.Builder()
                    .setRequestId(newGeofenceReminder.id)
                    .setCircularRegion(newGeofenceReminder.latitude!!,newGeofenceReminder.longitude!!,GEOFENCE_RANGE)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                Log.i("saveRemFrag","newGeofenceReminderID is ${newGeofenceReminder.id}")

                val geofenceRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .addGeofence(geofence)
                    .build()

                geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                       // Snackbar.make(requireView(),"Geofence Added!",Snackbar.LENGTH_SHORT).show()
                        //Toast.makeText(requireContext(),"Geofence Added!", Toast.LENGTH_LONG).show()
                        Log.i("saveRemFrag","GEOFENCE ADDED")
                        //_viewModel.navigationCommand.value=NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
                    }
                    addOnFailureListener {
                        //Snackbar.make(requireView(),R.string.geofences_not_added, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok){null}.show()
                        Log.i("saveRemFrag","Geofence could not be added")
                        if(it.message != null){
                            //Toast.makeText(requireContext(),it.message, Toast.LENGTH_LONG).show()
                            Log.i("saveRemFrag","GEOFENCE NOT ADDED due to: " + it.message)
                        }
                    }
                }
                _viewModel.validateAndSaveReminder(newGeofenceReminder)
                _viewModel.onClear()
                //_viewModel.navigationCommand.value=NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}

package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var poiMarker: Marker
    lateinit var savedLocation: LatLng


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add style to the map
        binding.saveLocBttn.setOnClickListener {
            onLocationSelected()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap){
        map= googleMap
        var curLat = 0.00
        var curLong = 0.00
        val mapZoomLvl = 16f
        enableMyLocation()
        if(map.isMyLocationEnabled){
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.i("SaveLocFrag","LastLocation found +${location.time}")
                curLat = location.latitude
                curLong = location.longitude
                val curLoc = LatLng(curLat, curLong)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, mapZoomLvl))
                }else{
                    Log.i("SaveLocFrag","location came up null")
                }
            }
        }else{
            enableMyLocation()
        }
        setPoiClick(map)
//        map.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener{
//            override fun onMarkerDragStart(p0: Marker?) {
//                marker.position
//            }
//
//            override fun onMarkerDragEnd(p0: Marker?) {
//                if (p0 != null) {
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(p0.position, 16.0f))
//                    marker.position = p0.position
//                }
//            }
//
//            override fun onMarkerDrag(p0: Marker?) {
//                marker.position = p0!!.position
//            }
//        })
        //val homeLoc = LatLng(homeLat, homeLong)
    }

//    private fun setMapLongClick(map: GoogleMap){
//        map.setOnMapLongClickListener { location ->
//
//
//            val snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", location.latitude, location.longitude)
//
//            marker = map.addMarker(
//                MarkerOptions()
//                    .position(location)
//                    .title(getString(R.string.dropped_pin))
//                    .snippet(snippet)
//                    .draggable(true)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
//
////            if(marker.position == location) {
////                marker.remove()
////            }
//        }
//    }
    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener { poi ->
            //val snippet = String.format(Locale.getDefault(), "LatLng: %1$.5f", poi.latLng)
            map.clear()
            poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    //.snippet(snippet)
            )
            poiMarker?.showInfoWindow()
            Log.i("loc","$poiMarker")
        }
    }


    private fun enableMyLocation(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            map.isMyLocationEnabled = true
            return
        }else{
            ActivityCompat.requestPermissions(requireActivity(),arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_TURN_DEVICE_LOCATION_ON)
        }
    }

    private fun onLocationSelected() {
        if(::poiMarker.isInitialized) {
            Log.i("saveLoc", "${poiMarker.position}")
            savedLocation = poiMarker.position
            _viewModel.reminderSelectedLocationStr.value = poiMarker.title
            _viewModel.latitude.value = poiMarker.position.latitude
            _viewModel.longitude.value = poiMarker.position.longitude
            findNavController().navigate(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
        }else{
            Snackbar.make(this.requireView(),R.string.select_location, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok){
                null
            }.show()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}

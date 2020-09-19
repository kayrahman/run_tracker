package com.kay.runningtrack.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.kay.runningtrack.MainActivity
import com.kay.runningtrack.R
import com.kay.runningtrack.util.Constants.ACTION_PAUSE_SERVICE
import com.kay.runningtrack.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.kay.runningtrack.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.kay.runningtrack.util.Constants.ACTION_STOP_SERVICE
import com.kay.runningtrack.util.Constants.FASTEST_LOCATION_INTERVAL
import com.kay.runningtrack.util.Constants.LOCATION_UPDATE_INTERVAL
import com.kay.runningtrack.util.Constants.NOTIFICATION_CHANNEL_ID
import com.kay.runningtrack.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.kay.runningtrack.util.Constants.NOTIFICATION_ID
import com.kay.runningtrack.util.TrackingUtility
import timber.log.Timber



typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>


class TrackingService : LifecycleService(){

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var isFirstRun = true

    companion object {
      //  val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    }else{
                        startForegroundService()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()

                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = FusedLocationProviderClient(this)
        postInitialValues()

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })


    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())

    }


    private fun pauseService() {
        isTracking.postValue(false)
       // isTimerEnabled = false
    }



    @SuppressLint("MissingPermission")
    fun updateLocationTracking(isTracking:Boolean){
        if(isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object: LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if(isTracking.value!!){
            result?.locations?.let { locations ->
                for (location in locations) {
                    addPathPoint(location)
                    Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")

                    Log.d(
                        "new_location",
                        "NEW LOCATION: ${location.latitude}, ${location.longitude}"
                    )
                }
            }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    private fun startForegroundService() {

        isTracking.postValue(true)
        addEmptyPolyline()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }


        val notificationBuilder  = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())


        startForeground(NOTIFICATION_ID, notificationBuilder.build())

    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,0,Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

}

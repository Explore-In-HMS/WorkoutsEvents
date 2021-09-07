/*
 * Copyright 2021. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hms.referenceapp.workouts.ui.eventcreation

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.dbmodels.EventList
import com.hms.referenceapp.workouts.model.events.EventDate
import com.hms.referenceapp.workouts.utils.Constants
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.*
import kotlinx.android.synthetic.main.activity_event_creation.*
import kotlinx.android.synthetic.main.dialog_map.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class EventCreationActivity : AppCompatActivity(), OnMapReadyCallback {

    private val eventCreateViewModel: EventCreationViewModel by viewModel()
    private var eventCategoryList: ArrayList<String> = arrayListOf("Select Category")
    private val eventList: EventList = EventList()
    private var eventDate: EventDate = EventDate(-1, -1, -1, -1, -1)
    private var eventCity = ""

    private lateinit var dialog: Dialog
    private val eventCheck = mutableMapOf<String, Boolean>(
        "eventDate" to false,
        "eventCategory" to false,
        "eventName" to false,
        "eventCity" to false,
        "eventLatLng" to false
    )

    private var hMap: HuaweiMap? = null
    private var eventAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_creation)
        requestPermissions()
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_map)
        edit_text_event_address.setOnClickListener {
            openMapDialog(savedInstanceState)
        }
        eventCreateViewModel.getEventCategoryList()
        observeLiveData()
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item, eventCategoryList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_category.adapter = adapter
        spinner_category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    eventCheck["eventCategory"] = false
                } else {
                    eventList.eventDetailId =
                        eventCreateViewModel.eventDetailList.value?.get(position - 1)?.id.toString()
                    eventCheck["eventCategory"] = true
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.i(Constants.eventCreationActivityTAG, "No category selected")
            }

        }
        val durationAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, resources.getStringArray(R.array.duration_array)
        )
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_duration.adapter = durationAdapter
        spinner_duration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                eventList.duration = Constants.durationArray[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.i(Constants.eventCreationActivityTAG, "No category selected")
            }

        }
        button_create_event.setOnClickListener {
            createEvent()
        }
        edit_text_event_time.setOnClickListener {
            selectEventTime()
        }
        edit_text_event_date.setOnClickListener {
            selectEventDate()
        }
    }

    private fun observeLiveData() {
        eventCreateViewModel.eventDetailList.observe(this, { eventDetailList ->
            eventDetailList?.let {
                for (item in eventDetailList) {
                    eventCategoryList.add(item.name)
                }
            }
        })
    }

    private fun createEvent() {
        val eventName = edit_text_event_name.text.toString()
        eventName.let { name ->
            if (name.isNotEmpty() && name.isNotBlank()) {
                eventList.eventName = name
                eventCheck["eventName"] = true
            }
        }
        val eventDescription = edit_text_event_description.text.toString()
        eventDescription.let { desc ->
            if (desc.isNotEmpty() && desc.isNotBlank()) {
                eventList.description = desc
            } else {
                eventList.description = ""
            }
        }
        checkDate()
        try {
            if (checkState(eventCheck.values)) {
                eventCreateViewModel.upsertEvent(eventList)
                Toast.makeText(
                    this,
                    "Event created successfully. You can check it out",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Please select necessary areas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Event creation failed. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
            Log.i(Constants.eventCreationActivityTAG, e.message.toString())
        }


    }

    private fun checkDate() {
        if (eventDate.year > 0 && eventDate.hour > 0) {
            val calendar = Calendar.getInstance()
            calendar.set(
                eventDate.year,
                eventDate.month,
                eventDate.day,
                eventDate.hour,
                eventDate.minute
            )
            eventList.date = calendar.timeInMillis
            eventCheck["eventDate"] = true
        }
    }

    private fun checkState(values: MutableCollection<Boolean>): Boolean {
        var trueCount = 0
        values.forEach { value ->
            if (value) {
                trueCount++
            }
        }
        return trueCount == values.size
    }

    @SuppressLint("MissingPermission")
    private fun initMap(savedInstanceState: Bundle?, mapView: MapView) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.bundleApiKey)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync {
            this.hMap = it
            hMap?.isMyLocationEnabled = true
            hMap?.uiSettings?.isMyLocationButtonEnabled = true
            val locationManager: LocationManager =
                getSystemService(LOCATION_SERVICE) as LocationManager
            val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val location =
                CameraUpdateFactory.newLatLng(LatLng(gpsLoc!!.latitude, gpsLoc.longitude))
            hMap?.animateCamera(location)
            hMap?.setOnMapClickListener { latLng ->
                createMarker(latLng)
                try {
                    eventAddress = eventCreateViewModel.getAddress(baseContext, latLng)
                    eventCity = eventCreateViewModel.getCity(baseContext, latLng)
                    eventList.lattitude = latLng.latitude
                    eventList.longtitude = latLng.longitude
                    eventCheck["eventLatLng"] = true
                    dialog.text_view_event_address_map.text = eventAddress
                } catch (e: Exception) {
                    Toast.makeText(this, "Please select a proper location", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onMapReady(map: HuaweiMap?) {
        hMap = map
        hMap?.isMyLocationEnabled = true
        hMap?.uiSettings?.isMyLocationButtonEnabled = true
    }

    private fun createMarker(latLng: LatLng): Marker {
        hMap?.clear()
        return hMap!!.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Event Location")
                .icon(getBitmapDescriptor(this, R.drawable.icon_pin_event))
        )
    }

    private fun getBitmapDescriptor(context: Context, id: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, id)!!

        val h = vectorDrawable.intrinsicHeight
        val w = vectorDrawable.intrinsicWidth

        vectorDrawable.bounds = Rect(0, 0, w, h)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun requestPermissions() {
        // If the API level is 23 or higher (Android 6.0 or later), you need to dynamically apply for permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(Constants.eventCreationActivityTAG, "sdk >= 23 M")
            // Check whether your app has the specified permission and whether the app operation corresponding to the permission is allowed.
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permissions for your app.
                val strings = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                // Request permissions.
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        }
    }

    private fun selectEventDate() {
        val c = Calendar.getInstance()
        val cYear = c.get(Calendar.YEAR)
        val cMonth = c.get(Calendar.MONTH)
        val cDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this,
            { datePicker, year, month, dayOfMonth ->
                val date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                eventDate.day = dayOfMonth
                eventDate.month = month
                eventDate.year = year
                edit_text_event_date.text = eventCreateViewModel.toEditable(date)
            },
            cYear, cMonth, cDay
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun selectEventTime() {
        val timePickerDialog = TimePickerDialog(
            this,
            { timePicker, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                eventDate.hour = hour
                eventDate.minute = minute
                edit_text_event_time.text = eventCreateViewModel.toEditable(time)
            }, 7, 30, true
        )
        timePickerDialog.show()
    }

    private fun openMapDialog(savedInstanceState: Bundle?) {
        dialog.setContentView(R.layout.dialog_map)
        val mapView: MapView = dialog.findViewById(R.id.event_creation_map_dialog)
        initMap(savedInstanceState, mapView)
        dialog.show()
        dialog.button_map_ok.setOnClickListener {
            if (eventAddress != null) {
                edit_text_event_address.text = eventCreateViewModel.toEditable(eventAddress!!)
                eventList.city = eventCity
                eventCheck["eventCity"] = true
                dialog.cancel()
            } else if (eventAddress == null || eventAddress == Constants.noProperLocation) {
                edit_text_event_address.text =
                    eventCreateViewModel.toEditable(Constants.noProperLocation)
            }
        }
        dialog.button_map_cancel.setOnClickListener {
            mapView.onDestroy()
            dialog.cancel()
        }
    }

}

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
package com.hms.referenceapp.workouts.ui.eventdetail

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.Friends

import com.hms.referenceapp.workouts.model.dbmodels.EventList
import com.hms.referenceapp.workouts.model.events.EventDate

import com.hms.referenceapp.workouts.utils.Constants
import com.hms.referenceapp.workouts.utils.*
import com.hms.referenceapp.workouts.utils.Constants.INTENT_PARAMETER_EVENT
import com.hms.referenceapp.workouts.utils.SharedPreferencesHelper
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import kotlinx.android.synthetic.main.activity_event_creation.*
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.dialog_map.*
import kotlinx.android.synthetic.main.dialog_participants.*
import kotlinx.android.synthetic.main.info_window_event.*
import kotlinx.android.synthetic.main.participants_layout.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class EventDetailActivity() : AppCompatActivity(), OnMapReadyCallback {

    private val eventDetailViewModel: EventDetailViewModel by viewModel()
    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()
    private lateinit var participantRecyclerView: RecyclerView

    private lateinit var progressView: View
    private lateinit var progressBarView: View

    private var eventCategoryList: ArrayList<String> = arrayListOf("Select Category")
    private var categoryIndex = 0
    private var eventDate: EventDate = EventDate(-1, -1, -1, -1, -1)
    private var eventCity = ""
    private var participants = arrayOf<String>()
    private var eventAddress: String? = null
    private var recyclerViewState: Boolean = false
    private lateinit var dialog: Dialog
    private val eventCheck = mutableMapOf<String, Boolean>(
        "eventDate" to true,
        "eventCategory" to false,
        "eventName" to true,
        "eventCity" to true,
        "eventLatLng" to true
    )

    private var hMap: HuaweiMap? = null
    private lateinit var mapView: MapView
    private lateinit var eventList: EventList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        getEventFromIntent()
        requestPermissions()
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_map)
        progressView = findViewById(R.id.view_progress)
        progressBarView = findViewById(R.id.view_progress_bar)
        mapView = findViewById(R.id.event_detail_map)
        initMap(savedInstanceState, mapView, false)
        eventDetailViewModel.openDbZone()
        observeLiveData()
        checkAndSetEventValues()
        checkCreator()
        for (i in User.friendsArray!!) {
            Log.i(Constants.eventDetailActivityTAG, "Friend: $i")
        }
        edit_text_event_address_detail.setOnClickListener {
            openMapDialog(savedInstanceState)
        }
        button_delete.setOnClickListener {
            try {
                eventDetailViewModel.deleteEvent(eventList)
                Toast.makeText(this, "Event deleted successfully.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Something went wrong. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i(Constants.eventDetailActivityTAG, "Error : ${e.message}")
            }
        }

    }

    private fun getEventFromIntent() {
        eventList = EventList(intent.getStringExtra(INTENT_PARAMETER_EVENT))
    }

    private fun toEditable(str: String): Editable {
        return Editable.Factory.getInstance().newEditable(str)
    }

    private fun observeLiveData() {
        eventDetailViewModel.eventDetailList.observe(this, { eventDetailList ->
            eventDetailList?.let {
                var listIndex = 1
                for (item in eventDetailList) {
                    eventCategoryList.add(item.name)
                    if (eventList.eventDetailId == item.id.toString()) {
                        edit_text_event_category_detail.text = toEditable(item.name)
                        spinner_category_detail.setSelection(listIndex)
                    } else {
                        listIndex++
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    private fun initMap(savedInstanceState: Bundle?, mapView: MapView, isEnabled: Boolean) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.bundleApiKey)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync {
            this.hMap = it
            hMap?.isMyLocationEnabled = true
            hMap?.uiSettings?.isMyLocationButtonEnabled = true
            createMarker(LatLng(eventList.lattitude, eventList.longtitude))
            focusEvent()
            if (isEnabled) {
                hMap?.setOnMapClickListener { latLng ->
                    createMarker(latLng)
                    try {
                        eventAddress = eventDetailViewModel.getAddress(baseContext, latLng)
                        eventCity = eventDetailViewModel.getCity(baseContext, latLng)
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
    }

    private fun focusEvent() {
        val coordinate = LatLng(eventList.lattitude, eventList.longtitude)
        val location = CameraUpdateFactory.newLatLngZoom(coordinate, 12F)
        hMap?.animateCamera(location)

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
            Log.i(Constants.eventDetailActivityTAG, "sdk >= 23 M")
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

    private fun checkAndSetEventValues() {
        if (eventList.description != null) {
            edit_text_event_description_detail.text = toEditable(eventList.description)
        }
        edit_text_event_name_detail.text = toEditable(eventList.eventName)
        var durationIndex = 0
        for (item in Constants.durationArray) {
            if (item != eventList.duration) {
                durationIndex++
            }
        }
        val durationStr = resources.getStringArray(R.array.duration_array)
        edit_text_duration_detail.text = toEditable(durationStr[durationIndex])
        val c = Calendar.getInstance()
        c.timeInMillis = eventList.date
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        eventDate.minute = minute
        eventDate.hour = hour
        eventDate.year = year
        eventDate.month = month
        eventDate.day = day
        edit_text_event_time_detail.text =
            toEditable(String.format("%02d:%02d", hour, minute))
        edit_text_event_date_detail.text =
            toEditable(String.format("%02d/%02d/%04d", day, month, year))
        participants = eventList.participants.split(", ").toTypedArray()
        var participantsIdStr = ""
        for ((index, item) in participants.withIndex()) {
            if (index < participants.size - 1) {
                participantsIdStr += "$item,"
            } else {
                participantsIdStr += item
            }
        }

    }

    private fun checkCreator() {
        if (eventList.eventCreaterID.toString() == AGConnectAuth.getInstance().currentUser.uid) {
            edit_text_event_name_detail.isEnabled = true
            edit_text_event_time_detail.isEnabled = true
            edit_text_event_date_detail.isEnabled = true
            edit_text_event_description_detail.isEnabled = true
            edit_text_duration_detail.visibility = View.GONE
            spinner_duration_detail.visibility = View.VISIBLE
            edit_text_event_category_detail.visibility = View.GONE
            spinner_category_detail.visibility = View.VISIBLE
            event_detail_map.visibility = View.GONE
            edit_text_event_address_detail.visibility = View.VISIBLE
            button_delete.visibility = View.VISIBLE
            button_join_event.text = "Update"
            checkUserActions()
        } else {
            val currentUserID = AGConnectAuth.getInstance().currentUser.uid
            if (eventList.participants.contains(currentUserID)) {
                button_join_event.text = "Leave"
            }
            button_join_event.setOnClickListener {
                joinOrLeaveEvent()
            }

        }
    }

    private fun checkUserActions() {
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item, eventCategoryList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_category_detail.adapter = adapter
        spinner_category_detail.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        eventCheck["eventCategory"] = false
                    } else {
                        eventList.eventDetailId =
                            eventDetailViewModel.eventDetailList.value?.get(position - 1)?.id.toString()
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
        spinner_duration_detail.adapter = durationAdapter
        spinner_duration_detail.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    eventList.duration = Constants.durationArray[position]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Log.i(Constants.eventCreationActivityTAG, "No duration selected")
                }

            }
        var durationIndex = 0
        for (durationItem in Constants.durationArray) {
            if (durationItem != eventList.duration) {
                durationIndex++
            }
        }
        spinner_duration_detail.setSelection(durationIndex)
        button_join_event.setOnClickListener {
            updateEvent()
        }
        edit_text_event_time_detail.setOnClickListener {
            selectEventTime()
        }
        edit_text_event_date_detail.setOnClickListener {
            selectEventDate()
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
                edit_text_event_date_detail.text = eventDetailViewModel.toEditable(date)
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
                edit_text_event_time_detail.text = eventDetailViewModel.toEditable(time)
            }, 7, 30, true
        )
        timePickerDialog.show()
    }

    private fun updateEvent() {
        val eventName = edit_text_event_name_detail.text.toString()
        eventName.let { name ->
            if (name.isNotEmpty() && name.isNotBlank()) {
                eventList.eventName = name
                eventCheck["eventName"] = true
            }
        }
        val eventDescription = edit_text_event_description_detail.text.toString()
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
                eventDetailViewModel.getEvent(eventList.id, object :IOnItemSelectListener {
                    override fun onItemSelect(item: Any, position: Int) {
                        eventDetailViewModel.upsertEvent(eventList)
                        Toast.makeText(
                            this@EventDetailActivity,
                            "Event created successfully. You can check it out",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                })
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

    private fun joinOrLeaveEvent() {
        eventDetailViewModel.getEvent(eventList.id, object :IOnItemSelectListener {
            override fun onItemSelect(item: Any, position: Int) {
                val currentUserID = AGConnectAuth.getInstance().currentUser.uid
                if (eventList.participants.contains(currentUserID)) {
                    eventList.participantsCount--
                    eventList.participants = eventList.participants.replace(", $currentUserID", "")
                    button_join_event.text = "Join"

                } else {
                    eventList.participantsCount++
                    eventList.participants += ", $currentUserID"
                    button_join_event.text = "Leave"

                }
                eventDetailViewModel.upsertEvent(eventList)
            }
        })
    }

    private fun openMapDialog(savedInstanceState: Bundle?) {
        dialog.setContentView(R.layout.dialog_map)
        val mapView: MapView = dialog.findViewById(R.id.event_creation_map_dialog)
        initMap(savedInstanceState, mapView, true)
        dialog.show()
        dialog.button_map_ok.setOnClickListener {
            if (eventAddress != null) {
                edit_text_event_address_detail.text = toEditable(eventAddress!!)
                eventList.city = eventCity
                eventCheck["eventCity"] = true
                eventCheck["eventLatLng"] = true
                dialog.cancel()
            } else if (eventAddress == null || eventAddress == Constants.noProperLocation) {
                edit_text_event_address_detail.text =
                    toEditable(Constants.noProperLocation)
                eventCheck["eventCity"] = false
                eventCheck["eventLatLng"] = false
            }
        }
        dialog.button_map_cancel.setOnClickListener {
            mapView.onDestroy()
            dialog.cancel()
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




    private fun TextView.setDrawableRight(drawable: Int) {
        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
    }

}

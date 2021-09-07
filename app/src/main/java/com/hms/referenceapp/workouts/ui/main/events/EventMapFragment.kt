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
package com.hms.referenceapp.workouts.ui.main.events

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.dbmodels.EventDetail
import com.hms.referenceapp.workouts.model.dbmodels.EventList

import com.hms.referenceapp.workouts.ui.eventdetail.EventDetailActivity
import com.hms.referenceapp.workouts.utils.AGC_API_KEY
import com.hms.referenceapp.workouts.utils.Constants
import com.hms.referenceapp.workouts.utils.MAP_PARAMETER_ZOOM_LEVEL_FOCUS
import com.hms.referenceapp.workouts.utils.getDirectionsIntent
import com.hms.referenceapp.workouts.utils.*
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.MapsInitializer
import com.huawei.hms.maps.model.*
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val BUNDLE_ARG_KEY_MAP_VIEW_API_KEY = "bundle_arg_key_map_view_api_key"

class EventMapFragment : Fragment() {

    //Class Constants
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    //Widgets
    private lateinit var huaweiMap: HuaweiMap
    private lateinit var mapView: MapView

    //Variables
    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()
    private val currentUserId = sharedPreferencesHelper.getUserId().toString()
    private val eventsViewModel: EventsViewModel by viewModel()
    private var eventList: ArrayList<EventList> = ArrayList()
    private var eventCategoryList: ArrayList<EventDetail> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_map, container, false)
        init(view, savedInstanceState)
        return view
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

    private fun init(view: View, savedInstanceState: Bundle?) {
        mapView = view.findViewById(R.id.map_view)
        initMap(savedInstanceState)
    }

    private fun initMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(BUNDLE_ARG_KEY_MAP_VIEW_API_KEY)
        }
        MapsInitializer.setApiKey(AGC_API_KEY)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync {
            this.huaweiMap = it

            huaweiMap.setOnMarkerClickListener {
                val selectedMarkerPosition = it.tag as Int
                it.showInfoWindow()
                focusEvent(selectedMarkerPosition)
                true
            }

            huaweiMap.setInfoWindowAdapter(infoWindowAdapter)

            eventsViewModel.listedEvents.observe(viewLifecycleOwner, {
                eventList = it

                addMarkers()
            })

            eventsViewModel.eventCategoryList.observe(viewLifecycleOwner, {
                eventCategoryList = it
            })
        }
    }

    private val infoWindowAdapter = object : HuaweiMap.InfoWindowAdapter {
        override fun getInfoContents(p0: Marker?): View? {
            return null
        }

        override fun getInfoWindow(p0: Marker?): View {
            val itemView = layoutInflater.inflate(R.layout.info_window_event, null)

            val event = eventList[p0!!.tag as Int]
            val eventCategory = getEventCategory(event.eventDetailId.toInt())

            val pictureImageView: ImageView = itemView.findViewById(R.id.image_view_picture)
            val titleTextView: TextView = itemView.findViewById(R.id.text_view_title)
            val descriptionTextView: TextView =
                itemView.findViewById(R.id.text_view_description)
            val timeTextView: TextView = itemView.findViewById(R.id.text_view_time)
            val participant1ImageView: ImageView =
                itemView.findViewById(R.id.image_view_participant_1)
            val participant2ImageView: ImageView =
                itemView.findViewById(R.id.image_view_participant_2)
            val participantCountTextView: TextView =
                itemView.findViewById(R.id.text_view_participant_count)
            val joinButton: Button = itemView.findViewById(R.id.button_join)
            val directionsImageButton: ImageButton =
                itemView.findViewById(R.id.image_button_directions)

            Picasso.get().load(eventCategory.imageUrl).into(pictureImageView)
            titleTextView.text = event.eventName
            descriptionTextView.text = event.description
            timeTextView.text = timeFormatter.format(event.date)

            participantCountTextView.text = event.participantsCount.toString()
            participant1ImageView.visibility = View.GONE
            participant2ImageView.visibility = View.GONE
            /*when (event.participantsCount) {
                0 -> {
                    participant1ImageView.visibility = View.GONE
                    participant2ImageView.visibility = View.GONE
                }
                1 -> {
                    participant1ImageView.visibility = View.VISIBLE
                    participant2ImageView.visibility = View.GONE

                    participant1ImageView.setImageResource(R.drawable.participant_1)
                }
                else -> {
                    participant1ImageView.visibility = View.VISIBLE
                    participant2ImageView.visibility = View.VISIBLE

                    participant1ImageView.setImageResource(R.drawable.participant_1)
                    participant2ImageView.setImageResource(R.drawable.participant_2)
                }
            }*/

            if (currentUserId == event.eventCreaterID.toString()) {
                joinButton.visibility = View.GONE
            } else {
                joinButton.visibility = View.VISIBLE
                if (event.participants.contains(currentUserId)) {//Leave
                    joinButton.text = getString(R.string.text_leave)
                } else {//Join
                    joinButton.text = getString(R.string.text_join)
                }
                joinButton.setOnClickListener {
                    if (event.participants.contains(currentUserId)) {//Leave
                        event.participantsCount--
                        event.participants =
                            event.participants.replace(", $currentUserId", "")

                        joinButton.text = getString(R.string.text_join)
                    } else {//Join
                        event.participantsCount++
                        event.participants += ", $currentUserId"

                        joinButton.text = getString(R.string.text_leave)
                    }
                    participantCountTextView.text = event.participantsCount.toString()
                    eventsViewModel.updateEvent(event)
                }

                directionsImageButton.setOnClickListener {
                    getDirectionsIntent(requireContext(), event.lattitude, event.longtitude)
                }

                pictureImageView.setOnClickListener {
                    val intent = Intent(requireContext(), EventDetailActivity::class.java)
                    intent.putExtra(Constants.INTENT_PARAMETER_EVENT, event.toJSON())
                    startActivity(intent)
                }
            }

            return itemView
        }
    }

    private fun addMarkers() {
        huaweiMap.clear()

        if (eventList.size > 0) {
            for (i in 0 until eventList.size) {
                val event = eventList[i]
                val marker = createMarker(event)
                marker.tag = i
            }

            focusEvent(0)
        }
    }

    private fun createMarker(event: EventList): Marker {
        return huaweiMap.addMarker(
            MarkerOptions()
                .position(LatLng(event.lattitude, event.longtitude))
                .title(event.eventName)
                .icon(getBitmapDescriptor(requireContext(), R.drawable.icon_pin_event))
        )
    }

    private fun focusEvent(position: Int) {
        val venue = eventList[position]
        val coordinate = LatLng(venue.lattitude, venue.longtitude)
        val location = CameraUpdateFactory.newLatLngZoom(coordinate, MAP_PARAMETER_ZOOM_LEVEL_FOCUS)
        huaweiMap.animateCamera(location)
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

    private fun getEventCategory(eventCategoryId: Int): EventDetail {
        for (eventCategory in eventCategoryList) {
            if (eventCategory.id == eventCategoryId) {
                return eventCategory
            }
        }
        return eventCategoryList[0]
    }
}

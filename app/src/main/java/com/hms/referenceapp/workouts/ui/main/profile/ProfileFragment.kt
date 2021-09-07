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
package com.hms.referenceapp.workouts.ui.main.profile

import CircleTransform
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.hms.referenceapp.workouts.R

import com.hms.referenceapp.workouts.ui.main.events.EEventType
import com.hms.referenceapp.workouts.ui.splash.SplashActivity
import com.hms.referenceapp.workouts.utils.*
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class ProfileFragment : Fragment() {

    //Class Constants
    private val TAG = ProfileFragment::class.java.simpleName
    private val PERMISSION_REQUEST_CODE_LOCATION = 53

    //Variables
    private val profileViewModel: ProfileViewModel by viewModel()
    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()
    private val currentUserId = sharedPreferencesHelper.getUserId().toString()

    //Location Elements
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    //Widgets
    private lateinit var progressView: View
    private lateinit var progressBarView: View
    private lateinit var cityTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        progressView = view.findViewById(R.id.view_progress)
        progressBarView = view.findViewById(R.id.view_progress_bar)

        cityTextView = view.findViewById(R.id.text_view_city)

        view.findViewById<ImageButton>(R.id.image_button_logout).setOnClickListener {
            profileViewModel.logout(requireContext())
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
        }

        initProfile(view)
        initViewPager(view)
    }

    private fun initProfile(view: View) {
        val avatarUrl = AGConnectAuth.getInstance().currentUser.photoUrl
        val name = AGConnectAuth.getInstance().currentUser.displayName

        val city = getFromSharedPreferences(
            requireContext(),
            SHARED_PREFERENCES_KEY_USER_CITY,
            DEFAULT_CITY
        ) as String
        val allowedNotificationCount = (getFromSharedPreferences(
            requireContext(),
            SHARED_PREFERENCES_KEY_ALLOWED_NOTIFICATIONS,
            ""
        ) as String).split(",").size - 1

        val avatarImageView = view.findViewById<ImageView>(R.id.image_view_avatar)
        val nameTextView = view.findViewById<TextView>(R.id.text_view_name)
        val eventsTextView = view.findViewById<TextView>(R.id.text_view_events)

        Picasso.get().load(avatarUrl).transform(CircleTransform()).into(avatarImageView)
        nameTextView.text = name
        cityTextView.text = city

        cityTextView.setOnClickListener {
            checkLocationPermission()
        }

        profileViewModel.eventCount.observe(viewLifecycleOwner, {
            eventsTextView.text = it.toString()
        })
        profileViewModel.getEventCount(currentUserId)


    }

    private fun initViewPager(view: View) {
        val upcomingEventListFragment = ProfileEventListFragment.newInstance(EEventType.UPCOMING)
        val historyEventListFragment = ProfileEventListFragment.newInstance(EEventType.HISTORY)
        val fragmentPagerAdapter = object : FragmentPagerAdapter(
            childFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            val fragments = arrayOf(upcomingEventListFragment, historyEventListFragment)

            val fragmentNames = arrayOf(
                getString(R.string.text_upcoming),
                getString(R.string.text_history)
            )

            override fun getCount(): Int {
                return fragments.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return fragmentNames[position]
            }

            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }
        }

        val viewPager = view.findViewById<ViewPager>(R.id.view_pager_events)
        viewPager.adapter = fragmentPagerAdapter
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_events)
        tabLayout.setupWithViewPager(viewPager)
        for (i in 0 until tabLayout.tabCount) {
            val tabTextView = TextView(context)
            tabTextView.text = tabLayout.getTabAt(i)!!.text
            tabTextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.font_t7)

            tabTextView.textSize = resources.getDimension(R.dimen.text_size_xxxsmall)
            tabTextView.setTextColor(
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.color_style_tab_text
                )
            )
            tabTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            tabLayout.getTabAt(i)!!.customView = tabTextView
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val strings = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            requestPermissions(strings, PERMISSION_REQUEST_CODE_LOCATION)
        } else {
            initLocationElements()
        }
    }

    private fun initLocationElements() {
        onProgress(true)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        settingsClient = LocationServices.getSettingsClient(requireActivity())

        locationRequest = LocationRequest()
        locationRequest.interval = 0
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if (p0 != null) {
                    val locations = p0.locations
                    if (!locations.isEmpty()) {
                        for (location in locations) {
                            if (location.accuracy < 50) {
                                onProgress(false)
                                removeLocationUpdatesWithCallback()
                                checkCity(location.latitude, location.longitude)
                            }
                        }
                    }
                }
            }

            override fun onLocationAvailability(p0: LocationAvailability?) {
                super.onLocationAvailability(p0)
                if (p0 != null) {
                    val flag = p0.isLocationAvailable
                    Log.d(TAG, "onLocationAvailability isLocationAvailable: $flag");
                }
            }
        }

        requestLocationUpdatesWithCallback()
    }

    private fun requestLocationUpdatesWithCallback() {
        try {
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(locationRequest)
            val locationSettingsRequest = builder.build()
            settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener {
                    Log.d(TAG, "Check location settings request is completed successfully.")
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                        .addOnSuccessListener {
                            Log.d(TAG, "Request location updates is completed successfully.")
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "Request location updates is failed. Message: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Check location settings request is failed. Message: ${it.message}")
                    val statusCode = (it as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try {
                                val rae = it as ResolvableApiException
                                rae.startResolutionForResult(requireActivity(), 0)
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.d(TAG, "PendingIntent unable to execute request.")
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.d(TAG, "Request location updates is failed. Exception: ${e.message}");
        }
    }

    private fun removeLocationUpdatesWithCallback() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnSuccessListener {
                    Log.d(TAG, "Remove location updates request is completed successfully.");
                }
                .addOnFailureListener {
                    Log.d(TAG, "Remove location updates request is failed. Message: ${it.message}");
                }
        } catch (e: Exception) {
            Log.d(TAG, "Remove location updates request is failed. Exception: ${e.message}");
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE_LOCATION -> {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.text_toast_location_permissions),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    initLocationElements()
                }
            }
        }
    }

    private fun getCity(lat: Double, lng: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        var address: MutableList<Address>? = null
        address = geocoder.getFromLocation(lat, lng, 1)
        return if (address?.size!! > 0) {
            address[0].adminArea
        } else {
            null
        }
    }

    private fun checkCity(lat: Double, lng: Double) {
        val city = getCity(lat, lng)
        if (city == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.text_toast_no_location),
                Toast.LENGTH_LONG
            ).show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.text_dialog_title_your_location))
                .setMessage(
                    getString(
                        R.string.text_dialog_description_location_change,
                        city
                    )
                )
                .setPositiveButton(
                    getString(R.string.text_yes)
                ) { dialog: DialogInterface?, which: Int ->
                    cityTextView.text = city
                }
                .setNegativeButton(
                    getString(R.string.text_no)
                ) { dialogInterface: DialogInterface?, i: Int -> }
                .setCancelable(true)
                .show()
        }
    }


    private fun onProgress(isVisible: Boolean) {
        if (isVisible) {
            progressView.visibility = View.VISIBLE
            progressBarView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.GONE
            progressBarView.visibility = View.GONE
        }
    }
}
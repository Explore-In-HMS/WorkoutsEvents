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

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.text.Editable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.hms.referenceapp.workouts.base.BaseViewModel
import com.hms.referenceapp.workouts.model.dbmodels.EventDetail
import com.hms.referenceapp.workouts.model.dbmodels.EventList
import com.hms.referenceapp.workouts.repo.CloudDBZoneWrapper
import com.hms.referenceapp.workouts.repo.CloudDbUseCase
import com.hms.referenceapp.workouts.utils.Constants
import com.hms.referenceapp.workouts.utils.IOnItemSelectListener
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EventDetailViewModel(private val cloudDBUseCase: CloudDbUseCase): BaseViewModel() {

    val eventDetailList = MutableLiveData<List<EventDetail>>()

    fun openDbZone() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.i("MapActivity", "openDbZone")
            val eventsDetailList = cloudDBUseCase.getEventCategoryList()
            eventDetailList.postValue(eventsDetailList)
        }
    }

    fun getEvent(id: Int, listener: IOnItemSelectListener) {
        CoroutineScope(Dispatchers.Main).launch {
            listener.onItemSelect(cloudDBUseCase.getEvent(id), 0)
        }
    }

    fun upsertEvent(eventList: EventList) {
        CoroutineScope(Dispatchers.Main).launch {
            cloudDBUseCase.upsertEvent(eventList)
        }
    }

    fun getAddress(context: Context, latLng: LatLng): String {
        val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
        var address: MutableList<Address>? = null
        address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return if (address?.size!! > 0) {
            address[0].getAddressLine(0)
        } else {
            Constants.noProperLocation
        }
    }

    fun getCity(context: Context, latLng: LatLng): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var address: MutableList<Address>? = null
        address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return if (address?.size!! > 0) {
            address[0].adminArea
        } else {
            Constants.noProperLocation
        }
    }

    fun toEditable(str: String): Editable {
        return Editable.Factory.getInstance().newEditable(str)
    }

    fun deleteEvent(eventList: EventList) {
        val deleteList: List<EventList> = listOf(eventList)
        CoroutineScope(Dispatchers.Main).launch {
            cloudDBUseCase.deleteEvent(deleteList)
        }
    }


}
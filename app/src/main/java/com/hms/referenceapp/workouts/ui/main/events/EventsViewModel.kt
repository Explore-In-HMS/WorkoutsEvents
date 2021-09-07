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

import androidx.lifecycle.MutableLiveData
import com.hms.referenceapp.workouts.base.BaseViewModel
import com.hms.referenceapp.workouts.model.dbmodels.EventDetail
import com.hms.referenceapp.workouts.model.dbmodels.EventList
import com.hms.referenceapp.workouts.repo.CloudDBZoneWrapper
import com.hms.referenceapp.workouts.repo.CloudDbUseCase
import com.hms.referenceapp.workouts.utils.IOnItemSelectListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class EventsViewModel(val cloudDBUseCase: CloudDbUseCase) : BaseViewModel() {

    //Variables
    val eventList = MutableLiveData<ArrayList<EventList>>()
    val listedEvents = MutableLiveData<ArrayList<EventList>>()
    val eventCategoryList = MutableLiveData<ArrayList<EventDetail>>()
    val selectedEventCategoryId = MutableLiveData(0)
    val onProgress = MutableLiveData<Boolean>()
    val isFabVisible = MutableLiveData<Boolean>()
    val date = MutableLiveData(0L)

    fun getEventList(date: Long) {
        this.date.value = date
        onProgress.postValue(true)
        CoroutineScope(Dispatchers.Main).launch {
            eventCategoryList.postValue(cloudDBUseCase.getEventCategoryList())
            eventList.postValue(cloudDBUseCase.getEventList(date))
            onProgress.postValue(false)
        }
    }

    fun refreshEventList() {
        date.value?.let { getEventList(it) }
    }

    fun getEvent(id: Int, listener: IOnItemSelectListener) {
        onProgress.postValue(true)
        CoroutineScope(Dispatchers.Main).launch {
            listener.onItemSelect(cloudDBUseCase.getEvent(id), 0)
            onProgress.postValue(false)
        }
    }

    fun updateEvent(event: EventList) {
        onProgress.postValue(true)
        CoroutineScope(Dispatchers.Main).launch {
            cloudDBUseCase.upsertEvent(event)
            onProgress.postValue(false)
            getEventList(event.date)
        }
    }

    fun setSelectedEventCategoryId(eventCategoryId: Int) {
        selectedEventCategoryId.postValue(eventCategoryId)
    }

    fun setFabVisible(isVisible: Boolean) {
        isFabVisible.postValue(isVisible)
    }

    fun setListedEvent(listedEvents: ArrayList<EventList>) {
        this.listedEvents.postValue(listedEvents)
    }
}
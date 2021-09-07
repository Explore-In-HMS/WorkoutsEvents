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

import androidx.lifecycle.MutableLiveData
import com.hms.referenceapp.workouts.base.BaseViewModel
import com.hms.referenceapp.workouts.model.dbmodels.EventDetail
import com.hms.referenceapp.workouts.model.dbmodels.EventList
import com.hms.referenceapp.workouts.repo.CloudDBZoneWrapper
import com.hms.referenceapp.workouts.repo.CloudDbUseCase
import com.hms.referenceapp.workouts.ui.main.events.EEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ProfileEventsViewModel(val cloudDBUseCase: CloudDbUseCase) : BaseViewModel() {

    //Variables
    val eventList = MutableLiveData<ArrayList<EventList>>()
    val eventCategoryList = MutableLiveData<ArrayList<EventDetail>>()

    fun getEventList(userId: String, eventType: EEventType) {
        CoroutineScope(Dispatchers.Main).launch {
            eventCategoryList.postValue(cloudDBUseCase.getEventCategoryList())
            eventList.postValue(cloudDBUseCase.getEventList(userId, eventType))
        }
    }
}
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
package com.hms.referenceapp.workouts.repo

import android.util.EventLog
import com.hms.referenceapp.workouts.model.dbmodels.EventDetail
import com.hms.referenceapp.workouts.model.dbmodels.EventList
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutDetail
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutList
import com.hms.referenceapp.workouts.services.CloudDbService
import com.hms.referenceapp.workouts.ui.main.events.EEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class CloudDbUseCase(private val cloudDbService: CloudDbService) {

    init {
        cloudDbService.createObjectType()
        CoroutineScope(Dispatchers.Main).launch {
            cloudDbService.openCloudDBZoneV2()
        }
    }

    fun openDbZone() {
        return cloudDbService.openCloudDBZone()

    }

    fun createObject() {
        return cloudDbService.createObjectType()
    }

    fun closeDbZone() {
        return cloudDbService.closeCloudDBZone()
    }

    suspend fun openZoneV2() {
        return cloudDbService.openCloudDBZoneV2()
    }

    fun deleteDbZone() {
        return cloudDbService.deleteCloudDBZone()
    }

    suspend fun getAllWorkoutList(): List<WorkoutList> {
        return cloudDbService.queryAllWorkoutList()
    }

    suspend fun getAllWorkoutDetail(param: Int): List<WorkoutDetail> {
        return cloudDbService.queryAllWorkoutDetails(value = param)
    }

    suspend fun getEventCategoryList(): ArrayList<EventDetail> {
        return cloudDbService.getEventCategoryList()
    }

    suspend fun getEventList(): ArrayList<EventList> {
        return cloudDbService.getEventList()
    }

    suspend fun getEventList(date: Long): ArrayList<EventList> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val startDate = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endDate = calendar.timeInMillis
        return cloudDbService.getEventList(startDate, endDate)
    }

    suspend fun getEventList(userId: String, eventType: EEventType): ArrayList<EventList> {
        return cloudDbService.getEventList(userId, eventType)
    }

    suspend fun getEvent(id: Int): EventList {
        return cloudDbService.getEvent(id)
    }

    suspend fun getEventCount(userId: String): Long {
        return cloudDbService.getEventCount(userId)
    }

    fun deleteEvent(param: List<EventList>) {
        return cloudDbService.deleteEventInfos(eventInfoList = param)
    }

    suspend fun upsertEvent(param: EventList): String {
        return cloudDbService.upsertEventInfos(eventInfo = param)
    }
}
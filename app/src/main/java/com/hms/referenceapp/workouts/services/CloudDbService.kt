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
package com.hms.referenceapp.workouts.services

import com.hms.referenceapp.workouts.model.dbmodels.*
import com.hms.referenceapp.workouts.ui.main.events.EEventType
import java.util.ArrayList

interface CloudDbService {
    fun createObjectType()
    fun openCloudDBZone()
    suspend fun openCloudDBZoneV2()
    fun closeCloudDBZone()
    fun deleteCloudDBZone()
    suspend fun queryAllWorkoutList(): List<WorkoutList>
    suspend fun queryAllWorkoutDetails(value: Int): List<WorkoutDetail>
    suspend fun getEvent(id: Int): EventList
    suspend fun getEventList(): ArrayList<EventList>
    suspend fun getEventCategoryList(): ArrayList<EventDetail>
    suspend fun queryAllEventDetailWithParameter(value: Int): ArrayList<EventDetail>
    suspend fun upsertEventInfos(eventInfo: EventList): String
    suspend fun getEventList(startDate: Long, endDate: Long): ArrayList<EventList>
    suspend fun getEventList(userId: String, eventType: EEventType): ArrayList<EventList>
    suspend fun getEventCount(userId: String): Long
    fun deleteEventInfos(eventInfoList: List<EventList>)

}
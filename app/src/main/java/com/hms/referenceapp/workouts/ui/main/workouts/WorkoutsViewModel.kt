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
package com.hms.referenceapp.workouts.ui.main.workouts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.referenceapp.workouts.model.Workout
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutDetail
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutList
import com.hms.referenceapp.workouts.repo.CloudDBZoneWrapper
import com.hms.referenceapp.workouts.repo.CloudDbUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutsViewModel : ViewModel() {


    val workoutList = MutableLiveData<List<WorkoutList>>()

    var clouddbmodel = CloudDBZoneWrapper()
    var usecase = CloudDbUseCase(clouddbmodel)


    fun getData() {
        usecase.createObject()
        CoroutineScope(Dispatchers.Main).launch {
            usecase.openZoneV2()
            val values = usecase.getAllWorkoutList()
            workoutList.postValue(values)

        }

}}

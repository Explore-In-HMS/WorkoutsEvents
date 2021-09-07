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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.referenceapp.workouts.model.dbmodels.EventList
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutDetail
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutList
import com.hms.referenceapp.workouts.repo.CloudDBZoneWrapper
import com.hms.referenceapp.workouts.repo.CloudDbUseCase
import com.hms.referenceapp.workouts.utils.IOnListListener
import com.hms.referenceapp.workouts.utils.IOnTaskListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutDetailViewModel : ViewModel() {
    val workoutDetails = MutableLiveData<List<WorkoutDetail>>()
    val workoutList = MutableLiveData<WorkoutList>()

    var clouddbmodel = CloudDBZoneWrapper()
    var usecase = CloudDbUseCase(clouddbmodel)

    //  private val mHandler = MyHandler()

    fun refreshData(id:Int) {
        usecase.createObject()
        CoroutineScope(Dispatchers.Main).launch {
            usecase.openZoneV2()
            val values = usecase.getAllWorkoutList()
            for(value in values){
                if(value.id.equals(id))
                    workoutList.postValue(value)
            }
            val values1=usecase.getAllWorkoutDetail(id)
            workoutDetails.postValue(values1)
        }


    }

}


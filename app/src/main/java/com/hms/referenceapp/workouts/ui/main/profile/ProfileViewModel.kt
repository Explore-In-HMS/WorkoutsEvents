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

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hms.referenceapp.workouts.base.BaseViewModel
import com.hms.referenceapp.workouts.repo.CloudDbUseCase
import com.hms.referenceapp.workouts.ui.main.events.EEventType
import com.hms.referenceapp.workouts.utils.SharedPreferencesHelper
import com.hms.referenceapp.workouts.utils.clearFromSharedPreferences
import com.huawei.agconnect.auth.AGConnectAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(val cloudDBUseCase: CloudDbUseCase) : BaseViewModel() {

    val eventCount = MutableLiveData(0L)

    fun logout(context: Context) {
        if (AGConnectAuth.getInstance().currentUser != null) {
            AGConnectAuth.getInstance().signOut()
        }
        SharedPreferencesHelper(context).removeData()
        clearFromSharedPreferences(context)
    }

    fun getEventCount(userId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            eventCount.postValue(cloudDBUseCase.getEventCount(userId))
        }
    }
}
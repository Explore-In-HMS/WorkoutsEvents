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
package com.hms.referenceapp.workouts.base

import android.app.Application
import android.util.Log
import com.hms.referenceapp.workouts.di.mLocaleModules
import com.hms.referenceapp.workouts.di.mUseCaseModule
import com.hms.referenceapp.workouts.di.mViewModelModules
import com.hms.referenceapp.workouts.repo.CloudDBZoneWrapper
import com.hms.referenceapp.workouts.repo.CloudDbUseCase
import com.huawei.hms.videokit.player.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class WorkoutsEventsApp : Application() {

    private val useCase: CloudDbUseCase by inject()

    override fun onCreate() {
        super.onCreate()
        val context = this

        CoroutineScope(Dispatchers.Main).launch {
            CloudDBZoneWrapper.initAGConnectCloudDB(applicationContext)
            loadKoin()
            useCase.createObject()
        }

        val factoryOptions = WisePlayerFactoryOptionsExt.Builder().build()
        WisePlayerFactory.initFactory(context, factoryOptions, object : InitFactoryCallback {
            override fun onSuccess(wisePlayerFactory: WisePlayerFactory) {
                Log.d("TAG", "onSuccess wisePlayerFactory:$wisePlayerFactory")
                factory = wisePlayerFactory

            }

            override fun onFailure(errorCode: Int, msg: String) {
                Log.d("TAG", "onFailure errorcode:$errorCode reason:$msg")
            }
        })

    }

    override fun onTerminate() {
        super.onTerminate()
        useCase.closeDbZone()
    }

    private fun loadKoin() {
        startKoin {
            androidContext(this@WorkoutsEventsApp)
            modules(
                mViewModelModules,
                mLocaleModules,
                mUseCaseModule
            )
        }
    }

    companion object {

        private var factory: WisePlayerFactory? = null

        fun getWisePlayerFactory(): WisePlayerFactory? {
            return factory
        }

    }

}



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
package com.hms.referenceapp.workouts.ui.splash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.repo.CloudDbUseCase

import com.hms.referenceapp.workouts.ui.login.LoginActivity
import com.hms.referenceapp.workouts.ui.main.MainActivity
import com.hms.referenceapp.workouts.utils.Constants
import com.hms.referenceapp.workouts.utils.SharedPreferencesHelper
import com.huawei.agconnect.auth.AGConnectAuth
import org.koin.android.ext.android.inject

class SplashActivity : Activity() {

    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sharedPreferencesHelper.getUserId() != null && sharedPreferencesHelper.getUserId() != "NaN") {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                if (AGConnectAuth.getInstance().currentUser != null) {
                    AGConnectAuth.getInstance().signOut()
                }
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

        }, 3000)
    }
}

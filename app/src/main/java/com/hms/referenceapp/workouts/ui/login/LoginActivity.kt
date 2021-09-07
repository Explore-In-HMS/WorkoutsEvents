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
package com.hms.referenceapp.workouts.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.login.Status
import com.hms.referenceapp.workouts.repo.CloudDbUseCase
import com.hms.referenceapp.workouts.ui.main.MainActivity
import com.hms.referenceapp.workouts.ui.main.events.EventsFragment
import com.hms.referenceapp.workouts.utils.Constants
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModel()

    private lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginBtn = findViewById(R.id.button_huawei_login)
        loginBtn.setOnClickListener {
            loginViewModel.getSignInIntent {
                startActivityForResult(it, Constants.REQUEST_SIGN_IN_LOGIN)
            }
        }
        observeLiveData()
    }

    private fun observeLiveData() {
        loginViewModel.huaweiSignInResultLiveData.observe(this, Observer {
            when (it.responseType) {
                Status.SUCCESSFUL -> {
                    //startActivity(Intent(this, MainActivity::class.java))

                    Log.i(Constants.loginActivityTAG, getString(R.string.view_model_sign_success))
                    transmitTokenIntoAppGalleryConnect(it.data!!.token)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                }
                Status.ERROR -> {
                    Log.e(
                        Constants.loginActivityTAG,
                        "${getString(R.string.view_model_sign_failed)} ${it.error?.message}"
                    )
                }
                Status.LOADING -> {
                    Log.i(Constants.loginActivityTAG, getString(R.string.loading))
                }
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.REQUEST_SIGN_IN_LOGIN -> {
                    loginViewModel.fetchHuaweiSignInResult(data)
                }
            }
        }
    }

    private fun transmitTokenIntoAppGalleryConnect(accessToken: String) {
        loginViewModel.transmitTokenIntoAppGalleryConnect(accessToken)

    }

    private fun storeUserUid(user: AGConnectUser) {
        val sharedPreferences = getSharedPreferences(
            Constants.packageName,
            Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.userId, user.uid)
        editor.putString(Constants.userName, user.displayName)
        var photoUrl = user.photoUrl
        if (user.photoUrl == null) {
            photoUrl =
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/User_icon_2.svg/1024px-User_icon_2.svg.png"
        }
        editor.putString(Constants.userAvatar, photoUrl)
        editor.apply()
    }
}
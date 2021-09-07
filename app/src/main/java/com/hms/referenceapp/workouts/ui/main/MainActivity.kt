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
package com.hms.referenceapp.workouts.ui.main

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.utils.SharedPreferencesHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    //Class Variables
    private val mainViewModel: MainViewModel by viewModel()
    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()

    //Widgets
    private lateinit var progressView: View
    private lateinit var progressBarView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressView = findViewById(R.id.view_progress)
        progressBarView = findViewById(R.id.view_progress_bar)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        /*navView.setOnNavigationItemReselectedListener {
            //Do not delete this
        }

        navView.setOnNavigationItemSelectedListener { item ->
            if (item.title == getString(R.string.text_events)) {
                navController.popBackStack(R.id.navigation_events, false)
            } else {
                navController.navigateUp()
                navController.navigate(item.itemId)
            }
            true
        }*/

        mainViewModel.nullBarVisibility.observe(this, Observer { it ->
            if (it) {
                navView.visibility = View.VISIBLE
            } else {
                navView.visibility = View.GONE
            }
        })


    }

    override fun onBackPressed() {
        super.onBackPressed()
        mainViewModel.setnullBarVisibility(true)
    }

    private fun onProgress(isVisible: Boolean) {
        if (isVisible) {
            progressView.visibility = View.VISIBLE
            progressBarView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.GONE
            progressBarView.visibility = View.GONE
        }
    }
}
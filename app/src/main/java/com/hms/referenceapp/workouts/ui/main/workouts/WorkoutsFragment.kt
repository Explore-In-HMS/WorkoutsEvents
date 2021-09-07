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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.Workout
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutList
import com.hms.referenceapp.workouts.ui.main.ListObjectsViewModel
import kotlinx.android.synthetic.main.fragment_workout_detail.*
import kotlinx.android.synthetic.main.fragment_workouts.*

class WorkoutsFragment : Fragment() {
    private var workoutListData:ArrayList<WorkoutList> = arrayListOf()
    private lateinit var viewModel: WorkoutsViewModel
    private lateinit var navController: NavController

    private lateinit var ListviewModel: ListObjectsViewModel


    private val workoutAdapter = WorkoutAdapter(arrayListOf(), object : WorkoutAdapter.ItemClickListener {
        override fun clickRow(position: Int) {

            val fieldBundle = bundleOf("workoutData" to workoutListData[position].id.toString())
            view?.let {
                navController.navigate(
                    R.id.action_navigation_workouts_to_navigation_workouts_detail,
                    fieldBundle
                )
            }
        }
    })
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this).get(WorkoutsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_workouts, container, false)

        return root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        viewModel = ViewModelProviders.of(this).get(WorkoutsViewModel::class.java)
        viewModel.getData()

        recyclerView.layoutManager =LinearLayoutManager (this.requireContext(), LinearLayoutManager.VERTICAL,false)

        recyclerView.adapter = workoutAdapter

        observeLiveData()



    }

    private fun observeLiveData() {
        viewModel.workoutList.observe(viewLifecycleOwner, Observer { workouts->
            workouts?.let {
                workoutListData.clear()
                workoutListData=it as ArrayList<WorkoutList>

                workoutAdapter.updateWorkoutList(workouts)
            }
        })
    }



}
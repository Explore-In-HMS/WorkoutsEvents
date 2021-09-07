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


import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.base.WorkoutsEventsApp
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutDetail
import com.hms.referenceapp.workouts.ui.main.MainViewModel
import com.huawei.hms.videokit.player.WisePlayer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_workout_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class WorkoutDetailFragment : Fragment(), WisePlayer.ReadyListener,SurfaceHolder.Callback{
    private lateinit var navController: NavController
    private lateinit var startWorkout: Button
    private var workoutData: String? = null

    private lateinit var viewModel: WorkoutDetailViewModel
    private lateinit var bottomSheet: WorkoutDetailFragment

    private lateinit var player: WisePlayer
    private lateinit var surfaceView: SurfaceView

    private lateinit var exerciseTextView: TextView
    private lateinit var exercisetrueButton: Button

    private lateinit var url:String

    private lateinit var WorkoutDetailList :  ArrayList<WorkoutDetail>

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private val mainViewModel: MainViewModel by viewModel()


    private val workoutDetailAdapter =
        WorkoutDetailAdapter(arrayListOf(), object : WorkoutDetailAdapter.ItemClickListener {
            override fun clickRow(position: Int) {

                showBottomSheet(viewModel.workoutDetails.value!![position])


            }

        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            workoutData = it.getSerializable("workoutData") as String?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_workout_detail, container, false)


        initBottomSheet(view)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val workoutId = arguments?.getSerializable("workoutData") as String
        viewModel = ViewModelProviders.of(this).get(WorkoutDetailViewModel::class.java)
        viewModel.refreshData(workoutId.toInt())
        recycler_view_workout_detail.layoutManager = LinearLayoutManager(context)
        recycler_view_workout_detail.adapter = workoutDetailAdapter
        image_view_info_header_background.setColorFilter(Color.BLACK, PorterDuff.Mode.LIGHTEN);

        startWorkout = view.findViewById(R.id.tab_layout) as Button
        startWorkout.setOnClickListener {
            navController.navigate(
                R.id.action_navigation_workouts_detail_to_workoutDetailVideoActivity
            )
        }
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.workoutDetails.observe(viewLifecycleOwner, Observer { workoutDetails ->
            workoutDetails?.let {

                workoutDetailAdapter.updateWorkoutDetailList(workoutDetails)


            }
        })
        viewModel.workoutList.observe(viewLifecycleOwner, Observer { workouts ->
            workouts?.let {
                Picasso.get().load(workouts.imageUrl).into(image_view_info_header_background)
                main_title.text = workouts.workoutName
                text_view_calorie.text = workouts.calorie.toString() + " cal"
                val value = (workouts.workoutDuration / 60).toString()
                text_view_duration.text = value + " min"
                text_view_equipment.text = workouts.equipment

            }
        })
    }

    private fun initBottomSheet(view: View) {

        bottomSheetBehavior =
            BottomSheetBehavior.from(view.findViewById<View>(R.id.fragment_bottom_sheet))
        exerciseTextView = view?.findViewById(R.id.exercisedetail)!!
        exercisetrueButton = view.findViewById(R.id.checkexercise)!!
        surfaceView =view.findViewById(R.id.surface_view_exercise)
        exercisetrueButton.setOnClickListener{
            player?.stop()
            player?.release()
            val intent = Intent(activity, StartActivity::class.java)
            startActivity(intent)
            activity?.finish()

        }


       hideBottomSheet()

        bottomSheetBehavior.addBottomSheetCallback(object:BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
               if(newState==BottomSheetBehavior.STATE_HIDDEN) {
                   mainViewModel.setnullBarVisibility(true)
               }
                else if(newState==BottomSheetBehavior.STATE_COLLAPSED) {
                   mainViewModel.setnullBarVisibility(false)
               }
            }
        })
    }



    fun showBottomSheet(workoutDetail: WorkoutDetail) {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            initPlayerSettings(workoutDetail.workoutUrl)
            initListeners()



        }
    }

    fun hideBottomSheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun initPlayerSettings(videoUrl:String){
        player = WorkoutsEventsApp.getWisePlayerFactory()!!.createWisePlayer()
        player.setVideoType(0)
        player.cycleMode = 1

        player.setPlayUrl(videoUrl)
        player.setView(surfaceView)
        player.ready()

    }
    private fun initListeners() {
        val surfaceHolder: SurfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
        player.setReadyListener(this)

        player.setPlayEndListener {
            val a = "video"
        }

        player.setLoadingListener(object:WisePlayer.LoadingListener {
            override fun onStartPlaying(p0: WisePlayer?) {
               val b ="vid"
            }



            override fun onLoadingUpdate(p0: WisePlayer?, p1: Int) {
              val c = "url"
            }
        })


    }

    override fun onReady(p0: WisePlayer?) {
        player.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        player.setSurfaceChange()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        player.suspend()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        player.setView(surface_view_exercise)
        player.resume(-1)
    }




}


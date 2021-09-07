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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.base.WorkoutsEventsApp
import com.hms.referenceapp.workouts.services.OnWisePlayerListener
import com.huawei.hms.api.bean.HwAudioPlayItem
import com.huawei.hms.audiokit.player.callback.HwAudioConfigCallBack
import com.huawei.hms.audiokit.player.manager.HwAudioManager
import com.huawei.hms.audiokit.player.manager.HwAudioManagerFactory
import com.huawei.hms.audiokit.player.manager.HwAudioPlayerConfig
import com.huawei.hms.audiokit.player.manager.HwAudioPlayerManager
import com.huawei.hms.videokit.player.InitFactoryCallback
import com.huawei.hms.videokit.player.WisePlayer
import com.huawei.hms.videokit.player.WisePlayerFactory
import com.huawei.hms.videokit.player.WisePlayerFactoryOptions
import kotlinx.android.synthetic.main.fragment_workout_detail_start_flow.*
import java.util.concurrent.TimeUnit
data class WorkoutClass(var exercises: MutableList<String>, var exerciseTimes: MutableList<Int>, var exerciseVideos: MutableList<String>)
private val absWorkout: WorkoutClass = WorkoutClass(
    mutableListOf("NORMAL CRUNCHES","NORMAL CRUNCHES", "PLANK","PLANK","SQUAT","SQUAT","JOG","JOG"),
    mutableListOf(11000, 31000,11000,31000,11000,31000,11000,31000),
    mutableListOf(
        ("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fshort.mp4?token=0277f973-4629-4254-badc-63242e09832e"),("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fshort.mp4?token=0277f973-4629-4254-badc-63242e09832e") ,("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fproduction%20ID_4265287%20(2).mp4?token=ed00eecf-40fe-4cb8-8262-258b0014e247"), ("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fproduction%20ID_4265287%20(2).mp4?token=ed00eecf-40fe-4cb8-8262-258b0014e247"),
        ("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fproduction%20ID_4258998.mp4?token=3dda8fe5-7045-4970-9f80-a8026023b8ac"),("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fproduction%20ID_4258998.mp4?token=3dda8fe5-7045-4970-9f80-a8026023b8ac"),("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fpexels-werner-pfennig-5426486%20(1).mp4?token=977fe1e9-7b18-4bd5-b3e5-131f752f2720"),("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fpexels-werner-pfennig-5426486%20(1).mp4?token=977fe1e9-7b18-4bd5-b3e5-131f752f2720"))
)
class WorkoutDetailStartFlow : Fragment(), WisePlayer.ReadyListener,WisePlayer.PlayEndListener,SurfaceHolder.Callback {
    private  var player:WisePlayer?=null
    private lateinit var factory: WisePlayerFactory
    private lateinit var surfaceView: SurfaceView
    private lateinit var imgview:ImageView
    private lateinit var cdt: CountDownTimer
    var currentIndex = 0
    private lateinit var timerText : TextView
    private lateinit var exerciseNameText: TextView
    private lateinit var skipbutton:TextView
    private lateinit var imageButton:ImageButton
    private var newWorkout : WorkoutClass = WorkoutClass(mutableListOf(), mutableListOf(), mutableListOf())
    private var countDownMs = TimeUnit.SECONDS.toMillis(6)
    private lateinit var mHwAudioManager : HwAudioManager
    private lateinit var mHwAudioPlayerManager: HwAudioPlayerManager
    private val countDownTimer = object : CountDownTimer(countDownMs, 1000) {

        override fun onFinish() {
            countDownMs = 0
            surfaceView.visibility=View.VISIBLE
            skipbutton.visibility=View.VISIBLE
            imgview.visibility=View.INVISIBLE
            initPlayerSettings()
            initListeners()
            startWorkout()
        }

        override fun onTick(millisUntilFinished: Long) {
            countDownMs = millisUntilFinished

            val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toString()
            timerText.text = secondsLeft
            exerciseName.text="Build A Lower Body"+"\n\n"+" START IN"
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_workout_detail_start_flow, container, false)
        surfaceView = view.findViewById<View>(R.id.surface_view) as SurfaceView
        timerText=view.findViewById(R.id.timerText)
        exerciseNameText=view.findViewById(R.id.exerciseName)
        skipbutton=view.findViewById(R.id.skipbutton)
        imgview=view.findViewById(R.id.img_view_workout)
        surfaceView.visibility=View.INVISIBLE
        skipbutton.visibility=View.INVISIBLE
        skipbutton.text="Skip Intro"
        imageButton=view.findViewById(R.id.imageButton)
        imageButton.setOnClickListener(){
            if (mHwAudioPlayerManager.isPlaying) {
                mHwAudioPlayerManager.pause()
            } else {
                mHwAudioPlayerManager.play()
            }

        }
        countDownTimer.start()
        skipbutton.setOnClickListener(){

            cdt.cancel()
            cdt.onFinish()

        }
        var hwAudioPlayerConfig = HwAudioPlayerConfig(activity?.applicationContext)
        HwAudioManagerFactory.createHwAudioManager(hwAudioPlayerConfig,  object :
            HwAudioConfigCallBack {
            override fun onSuccess(audiomanager: HwAudioManager?) {
                if (audiomanager != null) {
                    try {
                        mHwAudioManager = audiomanager
                        mHwAudioPlayerManager = audiomanager.playerManager
                        var cancion: HwAudioPlayItem = HwAudioPlayItem()
                        cancion.setOnlinePath("https://lfmusicservice.hwcloudtest.cn:18084/HMS/audio/Taoge-dayu.mp3");
                        cancion.setOnline(1)
                        cancion.audioTitle = "Jimi Hendrix"
                        cancion.audioId = "123"
                        var canciones:List<HwAudioPlayItem> = listOf(cancion)
                        mHwAudioPlayerManager.playList(canciones,0,0)
                        mHwAudioPlayerManager.setVolume(100)
                        mHwAudioPlayerManager.play()

                    }catch (ex: Exception){
                    }
                }
            }
            override fun onError(p0: Int) {
                Log.d("TAG1", p0.toString())
            }
        })

        return view
    }



    private  fun initPlayerSettings(){

        player= WorkoutsEventsApp.getWisePlayerFactory()!!.createWisePlayer()

        if (player != null) {
            player?.setVideoType(0)
        }

        if (player != null) {
            player?.setCycleMode(1)
        }
        player?.setMute(false)
        newWorkout= absWorkout
        player?.setPlayUrl(newWorkout.exerciseVideos[currentIndex])
        player?.setView(surfaceView);
        player?.ready()



    }


    private fun initListeners() {
        val surfaceHolder: SurfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
        player?.setReadyListener(this)

    }

    override fun onReady(p0: WisePlayer?) {

       player?.start()

    }

    fun init1(){
        player?.setCycleMode(1)
        player?.setPlayUrl(newWorkout.exerciseVideos[currentIndex])
        player?.setView(surfaceView);
        player?.ready()
    }

    override fun onPlayEnd(p0: WisePlayer?) {
        player?.stop()
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        player?.setSurfaceChange()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        player?.stop()
        player?.release()

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        player?.setView(surface_view)
    }



    override fun onStop() {
        super.onStop()
        player?.stop()
        player?.release()
    }
    private fun startWorkout()
    {

        val exercise = newWorkout.exercises[currentIndex]
        exerciseNameText.text = exercise

        var adjustedExerciseTime = newWorkout.exerciseTimes[currentIndex]



        cdt = object : CountDownTimer(adjustedExerciseTime.toLong(), 1000) //Start decreasing the adjusted time by 1 second until it hits 0
        {
            override fun onTick(millisUntilFinished: Long)
            {
                //Set timer text to remaining exercise time
                if(millisUntilFinished>=60000) {
                    timerText.text =
                    "00:"+(millisUntilFinished / 60000).toString() + "m" + ((millisUntilFinished % 60000) / 1000).toString()
                }
                else {
                    if((millisUntilFinished / 1000)>=10)
                    timerText.text ="00:"+(millisUntilFinished / 1000).toString()
                    else
                        timerText.text ="00:0"+(millisUntilFinished / 1000).toString()

                }




            }

            override fun onFinish()
            {


                if(currentIndex+1<newWorkout.exercises.size)
                {
                    currentIndex++
                    player?.reset()
                    init1()
                    initListeners()
                    startWorkout()
                    if(skipbutton.text.equals("Skip Intro")){
                        skipbutton.text="Skip Exercise"
                    }
                    else{
                        skipbutton.text="Skip Intro"
                    }
                }
                else
                {
                    exerciseNameText.text = "WORKOUT COMPLETE!"
                    timerText.visibility = View.INVISIBLE
                    skipbutton.visibility=View.INVISIBLE
                    player?.stop()
                    player?.release()
                    mHwAudioPlayerManager.stop()
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.stop()
        player?.release()
        mHwAudioPlayerManager.stop()
    }
}



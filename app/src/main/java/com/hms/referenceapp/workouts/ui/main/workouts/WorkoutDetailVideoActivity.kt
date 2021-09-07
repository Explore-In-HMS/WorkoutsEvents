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
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.base.WorkoutsEventsApp
import com.hms.referenceapp.workouts.ui.main.MainActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.api.bean.HwAudioPlayItem
import com.huawei.hms.audiokit.player.callback.HwAudioConfigCallBack
import com.huawei.hms.audiokit.player.manager.HwAudioManager
import com.huawei.hms.audiokit.player.manager.HwAudioManagerFactory
import com.huawei.hms.audiokit.player.manager.HwAudioPlayerConfig
import com.huawei.hms.audiokit.player.manager.HwAudioPlayerManager
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.tts.*
import com.huawei.hms.videokit.player.WisePlayer
import com.huawei.hms.videokit.player.WisePlayerFactory
import kotlinx.android.synthetic.main.fragment_workout_detail_start_flow.*
import java.util.concurrent.TimeUnit

data class WorkoutDetailClass(var exercises: MutableList<String>, var exerciseTimes: MutableList<Int>, var exerciseVideos: MutableList<String>)
private val absWorkout: WorkoutClass = WorkoutClass(
    mutableListOf("NORMAL CRUNCHES","NORMAL CRUNCHES", "PLANK","PLANK","SQUAT","SQUAT","JOG","JOG"),
    mutableListOf(11000, 31000,11000,31000,11000,31000,11000,31000),
    mutableListOf(
        ("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fshort.mp4?token=0277f973-4629-4254-badc-63242e09832e"),("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fshort.mp4?token=0277f973-4629-4254-badc-63242e09832e") ,("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fproduction%20ID_4265287%20(2).mp4?token=ed00eecf-40fe-4cb8-8262-258b0014e247"), ("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fproduction%20ID_4265287%20(2).mp4?token=ed00eecf-40fe-4cb8-8262-258b0014e247"),
        ("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fproduction%20ID_4258998.mp4?token=3dda8fe5-7045-4970-9f80-a8026023b8ac"),("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fproduction%20ID_4258998.mp4?token=3dda8fe5-7045-4970-9f80-a8026023b8ac"),("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fpexels-werner-pfennig-5426486%20(1).mp4?token=977fe1e9-7b18-4bd5-b3e5-131f752f2720"),("https://ops-dre.agcstorage.link/v0/sampleapp-0xndu/videos%2Fpexels-werner-pfennig-5426486%20(1).mp4?token=977fe1e9-7b18-4bd5-b3e5-131f752f2720"))
)

class WorkoutDetailVideoActivity : AppCompatActivity() , WisePlayer.ReadyListener, WisePlayer.PlayEndListener,
    SurfaceHolder.Callback {
    private  var player:WisePlayer?=null
    private lateinit var factory: WisePlayerFactory
    private lateinit var surfaceView: SurfaceView
    private lateinit var imgview: ImageView
    private lateinit var cdt: CountDownTimer
    var currentIndex = 0
    private var value:Int=1
    private lateinit var timerText : TextView
    private lateinit var exerciseNameText: TextView
    private lateinit var skipbutton: TextView
    private lateinit var imageButton: ImageButton
    private var newWorkout : WorkoutClass = WorkoutClass(mutableListOf(), mutableListOf(), mutableListOf())
    private var countDownMs = TimeUnit.SECONDS.toMillis(6)
    private lateinit var mHwAudioManager : HwAudioManager
    private lateinit var mHwAudioPlayerManager: HwAudioPlayerManager
    private var sourceText: String = ""
    private var  API_KEY : String  = "client/api_key";
    private  var mlTtsEngine: MLTtsEngine?=null
    private lateinit var mlConfigs: MLTtsConfig
    private lateinit var navController: NavController

    private var callback: MLTtsCallback = object : MLTtsCallback {
        override fun onError(taskId: String, err: MLTtsError) {

        }

        override fun onWarn(taskId: String, warn: MLTtsWarn) {

        }

        override fun onRangeStart(taskId: String, start: Int, end: Int) {
            Log.d("", start.toString())
        }

        override fun onAudioAvailable(
            p0: String?,
            p1: MLTtsAudioFragment?,
            p2: Int,
            p3: android.util.Pair<Int, Int>?,
            p4: Bundle?
        ) {

        }

        override fun onEvent(taskId: String, eventName: Int, bundle: Bundle?) {
            if(eventName==MLTtsConstants.EVENT_PLAY_START){
                mHwAudioPlayerManager.pause()

            }
            if (eventName == MLTtsConstants.EVENT_PLAY_STOP) {
                mHwAudioPlayerManager.play()
            }
            if(eventName==MLTtsConstants.EVENT_PLAY_STOP){
                mlTtsEngine?.shutdown()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_detail_video)
        setApiKey()
         val countDownTimer = object : CountDownTimer(countDownMs, 1000) {

            override fun onFinish() {
                countDownMs = 0
                surfaceView.visibility= View.VISIBLE
                skipbutton.visibility= View.VISIBLE
                imgview.visibility= View.INVISIBLE
                initPlayerSettings()
                initListeners()
                startWorkout()
                startTtsService()

            }

            override fun onTick(millisUntilFinished: Long) {
                countDownMs = millisUntilFinished

                val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toString()
                timerText.text = secondsLeft
                exerciseName.text="Build A Lower Body"+"\n\n"+" START IN"
            }
        }
        surfaceView = findViewById(R.id.surface_view) as SurfaceView
        timerText=findViewById(R.id.timerText)
        exerciseNameText=findViewById(R.id.exerciseName)
        skipbutton=findViewById(R.id.skipbutton)
        imgview=findViewById(R.id.img_view_workout)
        surfaceView.visibility=View.INVISIBLE
        skipbutton.visibility=View.INVISIBLE
        skipbutton.text="Skip Intro"
        imageButton=findViewById(R.id.imageButton)

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
        var hwAudioPlayerConfig = HwAudioPlayerConfig(this.applicationContext)
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
                        mHwAudioPlayerManager.setVolume(1)
                        mHwAudioPlayerManager.play()

                    }catch (ex: Exception){
                    }
                }
            }
            override fun onError(p0: Int) {
                Log.d("TAG1", p0.toString())
            }
        })







    }

    override fun onReady(p0: WisePlayer?) {
        player?.start()
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


    fun init1(){
        player?.setCycleMode(1)
        player?.setPlayUrl(newWorkout.exerciseVideos[currentIndex])
        player?.setView(surfaceView);
        player?.ready()
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

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()
        mHwAudioPlayerManager.stop()
        mlTtsEngine?.pause()

        mlTtsEngine?.shutdown()

    }

   override fun onBackPressed() {
        super.onBackPressed()
       player?.stop()
       player?.setMute(true)
       player?.release()
       mHwAudioPlayerManager.stop()
       value=2
       mlTtsEngine?.stop()
       mlTtsEngine?.shutdown()
       val intent=Intent(this,MainActivity::class.java)
       startActivity(intent)
       finish()
     /*   val firstFragment = WorkoutsFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.navigation_workouts, firstFragment)
        transaction.commit()*/
    }

    private fun startTtsService() {
        sourceText="In a power rack, set the barbell to where your shoulders will be when kneeling. Set up behind the barbell. Kneel on a padded surface. Place yourself underneath the barbell, positioning it across your shoulders.\n" +
                "Tighten your core and drive your glutes forward as you lift the barbell from the rack. Slowly sit back, keeping a braced form with your upper body.\n" +
                "When your butt touches your calves, drive your glutes forward again as you slowly return to the starting position. Repeat."
        mlConfigs = MLTtsConfig()
            .setLanguage(MLTtsConstants.TTS_EN_US)
            .setPerson(MLTtsConstants.TTS_SPEAKER_MALE_EN)
            .setSpeed(1.0f)
            .setVolume(2.0f)
        mlTtsEngine = MLTtsEngine(mlConfigs)
        mlTtsEngine?.setTtsCallback(callback)
        //ID to use for Audio Visualizer.
         if(value==1) {
             val id = mlTtsEngine?.speak(sourceText, MLTtsEngine.QUEUE_APPEND)
         }
        if(value==2){
            val id = mlTtsEngine?.speak(sourceText, MLTtsEngine.EXTERNAL_PLAYBACK)

        }
    }

    private fun setApiKey(){
        val  config  = AGConnectServicesConfig.fromContext(getApplication())
        MLApplication.getInstance().setApiKey(config.getString(API_KEY));
    }




    override fun onPause() {
        super.onPause()

    }

}
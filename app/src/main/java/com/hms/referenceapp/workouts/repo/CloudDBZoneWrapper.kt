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
package com.hms.referenceapp.workouts.repo

import android.content.Context
import android.util.Log
import com.hms.referenceapp.workouts.model.dbmodels.*
import com.hms.referenceapp.workouts.services.CloudDbService
import com.hms.referenceapp.workouts.ui.main.events.EEventType
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hmf.tasks.Task
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CloudDBZoneWrapper() : CloudDbService {
    private val mCloudDB: AGConnectCloudDB = AGConnectCloudDB.getInstance()
    private var mCloudDBZone: CloudDBZone? = null
    private var mRegister: ListenerHandler? = null
    private var mConfig: CloudDBZoneConfig? = null
    // private var mUiCallBack = UiCallBack.DEFAULT

    private var mEventIndex = 0


    private val mReadWriteLock: ReadWriteLock = ReentrantReadWriteLock()

    private val mSnapshotListener = OnSnapshotListener<EventList> { cloudDBZoneSnapshot, e ->
        if (e != null) {
            Log.w(TAG, "onSnapshot: " + e.message)
            return@OnSnapshotListener
        }
        val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
        val eventInfoList: MutableList<EventList> = ArrayList()
        try {
            if (snapshotObjects != null) {
                while (snapshotObjects.hasNext()) {
                    val eventInfo = snapshotObjects.next()
                    eventInfoList.add(eventInfo)
                    updateEventIndex(eventInfo)
                }
            }
            //  mUiCallBack.onSubscribe(workoutInfoList)
        } catch (snapshotException: AGConnectCloudDBException) {
            Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.message)
        } finally {
            cloudDBZoneSnapshot.release()
        }
    }

    override fun createObjectType() {
        try {
            return mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "createObjectType: " + e.message)
        }
    }


    override fun openCloudDBZone() {
        mConfig = CloudDBZoneConfig(
            "WorkoutZone",
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
        )
        mConfig!!.persistenceEnabled = true
        try {
            mCloudDBZone = mCloudDB.openCloudDBZone(mConfig!!, true)
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "openCloudDBZone: " + e.message)
        }
    }


    override suspend fun openCloudDBZoneV2() {
        mConfig = CloudDBZoneConfig(
            "WorkoutZone",
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
        )
        mConfig!!.persistenceEnabled = true
        val task1 = mCloudDB.openCloudDBZone2(mConfig!!, true)

        suspendCoroutine<CloudDBZone> { cont ->
            task1.addOnSuccessListener {
                Log.w(TAG, "open clouddbzone success")
                mCloudDBZone = it
                cont.resume(it)

            }.addOnFailureListener {
                Log.w(TAG, "open clouddbzone failed for " + it.message)
                cont.resumeWithException(it)
            }
        }

    }

    suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
        addOnCompleteListener { task ->
            when {
                task.isSuccessful -> continuation.resume(task.result)
            }
        }
    }

    override fun closeCloudDBZone() {
        try {
            mRegister!!.remove()
            mCloudDB.closeCloudDBZone(mCloudDBZone)
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "closeCloudDBZone: " + e.message)
        }
    }

    override fun deleteCloudDBZone() {
        try {
            mCloudDB.deleteCloudDBZone(mConfig!!.cloudDBZoneName)
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "deleteCloudDBZone: " + e.message)
        }
    }


    /* fun addCallBacks(uiCallBack: MainActivity) {
         mUiCallBack = uiCallBack
     }*/


    /*  fun addSubscription() {
         if (mCloudDBZone == null) {
             Log.w(TAG, "CloudDBZone is null, try re-open it")
             return
         }
         try {
             val snapshotQuery = CloudDBZoneQuery.where(EventList::class.java)
             mRegister = mCloudDBZone!!.subscribeSnapshot(snapshotQuery,
                     CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener)
         } catch (e: AGConnectCloudDBException) {
             Log.w(TAG, "subscribeSnapshot: " + e.message)
         }
     }*/

    override suspend fun queryAllWorkoutList(): ArrayList<WorkoutList> {
        var allWorkouts: ArrayList<WorkoutList> = arrayListOf()

        val cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(WorkoutList::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )

        suspendCoroutine<ArrayList<WorkoutList>> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {

                val workoutlistresult = cloudDBZoneQueryTask.result.snapshotObjects

                while (workoutlistresult.hasNext()) {
                    var item = workoutlistresult.next()
                    allWorkouts.add(item)
                }
                cont.resume(allWorkouts)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return allWorkouts
    }

    override suspend fun queryAllWorkoutDetails(value: Int): ArrayList<WorkoutDetail> {
        var allWorkoutsDetail: ArrayList<WorkoutDetail> = arrayListOf()

        val cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(WorkoutDetail::class.java).equalTo("workoutId", value),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        suspendCoroutine<ArrayList<WorkoutDetail>> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {

                val workoutlistresult = cloudDBZoneQueryTask.result.snapshotObjects

                while (workoutlistresult.hasNext()) {
                    var item = workoutlistresult.next()
                    allWorkoutsDetail.add(item)
                }
                cont.resume(allWorkoutsDetail)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return allWorkoutsDetail
    }

    override suspend fun getEventList(): ArrayList<EventList> {
        val allEventList: ArrayList<EventList> = arrayListOf()

        val cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(EventList::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        suspendCoroutine<ArrayList<EventList>> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {

                val eventlistresult = cloudDBZoneQueryTask.result.snapshotObjects

                while (eventlistresult.hasNext()) {
                    var item = eventlistresult.next()
                    allEventList.add(item)
                }
                cont.resume(allEventList)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return allEventList
    }

    override suspend fun getEventList(userId: String, eventType: EEventType): ArrayList<EventList> {
        val eventList: ArrayList<EventList> = arrayListOf()

        val now = Calendar.getInstance().timeInMillis

        val cloudDBZoneQueryTask: Task<CloudDBZoneSnapshot<EventList>>
        if (eventType.equals(EEventType.UPCOMING)) {
            cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
                CloudDBZoneQuery.where(EventList::class.java).greaterThan("date", now)
                    .contains("participants", userId).orderByDesc("date"),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )
        } else {
            cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
                CloudDBZoneQuery.where(EventList::class.java).lessThan("date", now)
                    .contains("participants", userId).orderByDesc("date"),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )
        }

        suspendCoroutine<ArrayList<EventList>> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {

                val eventlistresult = cloudDBZoneQueryTask.result.snapshotObjects

                while (eventlistresult.hasNext()) {
                    val item = eventlistresult.next()
                    eventList.add(item)
                }
                cont.resume(eventList)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return eventList
    }

    override suspend fun getEvent(id: Int): EventList {
        var event = EventList()

        val cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(EventList::class.java).equalTo("id", id),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )

        suspendCoroutine<EventList> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {

                val eventlistresult = cloudDBZoneQueryTask.result.snapshotObjects
                event = eventlistresult.next()
                cont.resume(event)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return event
    }

    override suspend fun getEventCount(userId: String): Long {
        var eventCount: Long = 0

        val now = Calendar.getInstance().timeInMillis

        val cloudDBZoneQueryTask = mCloudDBZone!!.executeCountQuery(
            CloudDBZoneQuery.where(EventList::class.java).contains("participants", userId), "id",
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )

        suspendCoroutine<Long> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {
                eventCount = it
                cont.resume(eventCount)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return eventCount
    }

    override suspend fun queryAllEventDetailWithParameter(value: Int): ArrayList<EventDetail> {
        var allEventsDetail: ArrayList<EventDetail> = arrayListOf()

        val cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(EventDetail::class.java).equalTo("eventDetailId", value),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        suspendCoroutine<ArrayList<EventDetail>> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {

                val eventdetaillistresult = cloudDBZoneQueryTask.result.snapshotObjects

                while (eventdetaillistresult.hasNext()) {
                    var item = eventdetaillistresult.next()
                    allEventsDetail.add(item)
                }
                cont.resume(allEventsDetail)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return allEventsDetail
    }

    override suspend fun getEventCategoryList(): ArrayList<EventDetail> {
        val allEventsDetail: ArrayList<EventDetail> = arrayListOf()

        val cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(EventDetail::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        suspendCoroutine<ArrayList<EventDetail>> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {

                val eventdetaillistresult = cloudDBZoneQueryTask.result.snapshotObjects

                while (eventdetaillistresult.hasNext()) {
                    var item = eventdetaillistresult.next()
                    allEventsDetail.add(item)
                }
                cont.resume(allEventsDetail)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return allEventsDetail
    }

    override suspend fun getEventList(value1: Long, value2: Long): ArrayList<EventList> {
        var eventList: ArrayList<EventList> = arrayListOf()

        val cloudDBZoneQueryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(EventList::class.java).greaterThan("date", value1)
                .lessThan("date", value2).orderByAsc("date"),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        suspendCoroutine<ArrayList<EventList>> { cont ->
            cloudDBZoneQueryTask.addOnSuccessListener {

                val eventlistresult = cloudDBZoneQueryTask.result.snapshotObjects

                while (eventlistresult.hasNext()) {
                    var item = eventlistresult.next()
                    eventList.add(item)
                }
                cont.resume(eventList)
            }
            cloudDBZoneQueryTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return eventList
    }
    /*  fun queryAll(value:String) {
          if (mCloudDBZone == null) {
              Log.w(TAG, "CloudDBZone is null, try re-open it")
              return
          }
          if(value.equals("1")) {
              val queryTask = mCloudDBZone!!.executeQuery(
                  CloudDBZoneQuery.where(WorkoutList::class.java),
                  CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
              )
              queryTask.addOnSuccessListener { snapshot -> processQueryResult(snapshot,null,null,null,"1") }
                  .addOnFailureListener {
                 //     mUiCallBack.updateUiOnError("Query workout list from cloud failed")
                  }
          }
          if(value.equals("2")){
              val queryTask = mCloudDBZone!!.executeQuery(
                  CloudDBZoneQuery.where(EventList::class.java),
                  CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
              )
              queryTask.addOnSuccessListener { snapshot -> processQueryResult(null,snapshot,null,null,"2") }
                  .addOnFailureListener {
                 //     mUiCallBack.updateUiOnError("Query workout list from cloud failed")
                  }
          }
          if(value.equals("3")){
              val queryTask = mCloudDBZone!!.executeQuery(
                  CloudDBZoneQuery.where(EventDetail::class.java),
                  CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
              )
              queryTask.addOnSuccessListener { snapshot -> processQueryResult(null,null,snapshot,null,"3") }
                  .addOnFailureListener {
                   //  mUiCallBack.updateUiOnError("Query workout list from cloud failed")
                  }
          }
          if(value.equals("4")){
              val queryTask = mCloudDBZone!!.executeQuery(
                  CloudDBZoneQuery.where(WorkoutDetail::class.java),
                  CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
              )
              queryTask.addOnSuccessListener { snapshot -> processQueryResult(null,null,null,snapshot,"4") }
                  .addOnFailureListener {
                     // mUiCallBack.updateUiOnError("Query workout list from cloud failed")
                  }
          }
      }*/


    /* fun queryWorkouts(query: CloudDBZoneQuery<WorkoutList>,query1:CloudDBZoneQuery<EventList>,value: String) {
         if (mCloudDBZone == null) {
             Log.w(TAG, "CloudDBZone is null, try re-open it")
             return
         }
         if (value.equals("1")) {
             val queryTask = mCloudDBZone!!.executeQuery(
                 query,
                 CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
             )
             queryTask.addOnSuccessListener { snapshot ->
                 processQueryResult(
                     snapshot,
                     null,
                     null,
                     null,
                     "1"
                 )
             }
                 .addOnFailureListener { //
                     // mUiCallBack.updateUiOnError("Query failed") }
                 }
             if (value.equals("2")) {
                 val queryTask = mCloudDBZone!!.executeQuery(
                     query1,
                     CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
                 )
                 queryTask.addOnSuccessListener { snapshot ->
                     processQueryResult(
                         null,
                         snapshot,
                         null,
                         null,
                         "2"
                     )
                 }
                     .addOnFailureListener {// mUiCallBack.updateUiOnError("Query failed") }
                     }
             }
         }
     }*/

    /*  fun processQueryResult(
          snapshot: CloudDBZoneSnapshot<WorkoutList?>?,
          snapshot1: CloudDBZoneSnapshot<EventList?>?,
          snapshot2: CloudDBZoneSnapshot<EventDetail?>?,
          snapshot3: CloudDBZoneSnapshot<WorkoutDetail?>?,
          value: String
      ) {
          val workoutInfoCursor = snapshot?.snapshotObjects
          val eventInfoCursor = snapshot1?.snapshotObjects
          val eventListInfoCursor = snapshot2?.snapshotObjects
          val workoutListInfoCursor = snapshot3?.snapshotObjects

          val workoutInfoList: MutableList<WorkoutList> = ArrayList()
          val eventInfoList: MutableList<EventList> = ArrayList()
          val workoutListInfoList: MutableList<WorkoutDetail> = ArrayList()
          val eventListInfoList: MutableList<EventDetail> = ArrayList()


          try {
              if (workoutInfoCursor != null && value.equals("1")) {
                  while (workoutInfoCursor.hasNext()) {
                      val workoutInfo = workoutInfoCursor.next()
                      if (workoutInfo != null) {
                          workoutInfoList.add(workoutInfo)
                      }
                      if (workoutInfo != null) {
                          updateWorkoutIndex(workoutInfo)
                      }
                  }
              }
              if (eventInfoCursor != null && value.equals("2")) {
                  while (eventInfoCursor.hasNext()) {
                      val eventInfo = eventInfoCursor.next()
                      if (eventInfo != null) {
                          eventInfoList.add(eventInfo)
                      }
                      if (eventInfo != null) {
                          updateEventIndex(eventInfo)
                      }
                  }
              }
              if (eventListInfoCursor != null && value.equals("3")) {
                  while (eventListInfoCursor.hasNext()) {
                      val eventListInfo = eventListInfoCursor.next()
                      if (eventListInfo != null) {
                          eventListInfoList.add(eventListInfo)
                      }
                      if (eventListInfo != null) {
                          updateEventListIndex(eventListInfo)
                      }
                  }
              }
              if (workoutListInfoCursor != null && value.equals("4")) {
                  while (workoutListInfoCursor.hasNext()) {
                      val workoutListInfo = workoutListInfoCursor.next()
                      if (workoutListInfo != null) {
                          workoutListInfoList.add(workoutListInfo)
                      }
                      if (workoutListInfo != null) {
                          updateWorkoutListIndex(workoutListInfo)
                      }
                  }
              }

          } catch (e: AGConnectCloudDBException) {
              Log.w("TAG", "processQueryResult: " + e.message)
          } finally {
              if (snapshot != null) {
                  snapshot.release()
              }
          }
          if (value == "1") {
              //  mUiCallBack.onAddOrQuery(workoutInfoList,null,null,null)
          } else if (value == "2") {
              //mUiCallBack.onAddOrQuery(null,eventInfoList,null,null)

          } else if (value == "3") {
              //    mUiCallBack.onAddOrQuery(null,null,eventListInfoList,null)

          } else if (value == "4") {
              //  mUiCallBack.onAddOrQuery(null,null,null,workoutListInfoList)

          }
      }
*/

    override suspend fun upsertEventInfos(eventInfo: EventList): String {
        var result: String = ""
        if (mCloudDBZone == null) {
            Log.w("TAG", "CloudDBZone is null, try re-open it")
        }
        val upsertTask = mCloudDBZone!!.executeUpsert(eventInfo!!)
        suspendCoroutine<String> { cont ->
            upsertTask.addOnSuccessListener {

                result = upsertTask.result.toString()

                cont.resume(result)
            }
            upsertTask.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
        return result
    }


    override fun deleteEventInfos(eventInfoList: List<EventList>) {


        if (mCloudDBZone == null) {
            Log.w("TAG", "CloudDBZone is null, try re-open it")
            return
        }

        val deleteTask = mCloudDBZone!!.executeDelete(eventInfoList!!)

        if (deleteTask.exception != null) {
            // mUiCallBack.updateUiOnError("Delete workout info failed")
            return
        }
        // mUiCallBack.onDelete(eventInfoList)


    }

    /*      fun updateWorkoutIndex(workoutInfo: WorkoutList) {
               try {
                   mReadWriteLock.writeLock().lock()
                   if (mWorkoutIndex < workoutInfo.id) {
                       mWorkoutIndex = workoutInfo.id
                   }
               } finally {
                   mReadWriteLock.writeLock().unlock()
               }
           }
*/
    fun updateEventIndex(eventInfo: EventList) {
        try {
            mReadWriteLock.writeLock().lock()
            if (mEventIndex < eventInfo.id) {
                mEventIndex = eventInfo.id
            }
        } finally {
            mReadWriteLock.writeLock().unlock()
        }
    }

    val eventIndex: Int
        get() = try {
            mReadWriteLock.readLock().lock()
            mEventIndex
        } finally {
            mReadWriteLock.readLock().unlock()
        }
    /* fun updateEventListIndex(eventListInfo: EventDetail) {
         try {
             mReadWriteLock.writeLock().lock()
             if (mEventListIndex < eventListInfo.id) {
                 mEventListIndex = eventListInfo.id
             }
         } finally {
             mReadWriteLock.writeLock().unlock()
         }
     }


     val eventListIndex: Int
     get() = try {
         mReadWriteLock.readLock().lock()
         mEventListIndex
     } finally {
         mReadWriteLock.readLock().unlock()
     }
     fun updateWorkoutListIndex(workoutListInfo: WorkoutDetail) {
         try {
             mReadWriteLock.writeLock().lock()
             if (mWorkoutListIndex < workoutListInfo.id) {
                 mWorkoutListIndex = workoutListInfo.id
             }
         } finally {
             mReadWriteLock.writeLock().unlock()
         }
     }


     val workoutListIndex: Int
     get() = try {
         mReadWriteLock.readLock().lock()
         mWorkoutListIndex
     } finally {
         mReadWriteLock.readLock().unlock()
     }


     val workoutIndex: Int
     get() = try {
         mReadWriteLock.readLock().lock()
         mWorkoutIndex
     } finally {
         mReadWriteLock.readLock().unlock()
     }
*/

    /* interface UiCallBack {
fun onAddOrQuery(workoutInfoList: List<WorkoutList?>?,eventInfoList: List<EventList?>?,eventListInfoList: List<EventDetail?>?,workoutListInfoList: List<WorkoutDetail?>?)
fun onSubscribe(workoutInfoList: List<WorkoutDetail>?)
fun onDelete(eventInfoList: List<EventList?>?)
fun updateUiOnError(errorMessage: String?)
companion object {
    val DEFAULT: UiCallBack = object :
        UiCallBack {
        override fun onAddOrQuery(workoutInfoList: List<WorkoutList?>?,eventInfoList: List<EventList?>?,eventListInfoList: List<EventDetail?>?,workoutListInfoList: List<WorkoutDetail?>?) {
            Log.w(TAG, "Using default onAddOrQuery")
        }

        override fun onSubscribe(workoutInfoList: List<WorkoutDetail>?) {
            Log.w(TAG, "Using default onSubscribe")
        }

        override fun onDelete(eventInfoList: List<EventList?>?) {
            Log.w(TAG, "Using default onDelete")
        }

        override fun updateUiOnError(errorMessage: String?) {
            Log.w(TAG, "Using default updateUiOnError")
        }
    }*/


    companion object {
        private const val TAG = "CloudDBZoneWrapper"

        fun initAGConnectCloudDB(context: Context?) {
            AGConnectCloudDB.initialize(context!!)
        }


    }
}


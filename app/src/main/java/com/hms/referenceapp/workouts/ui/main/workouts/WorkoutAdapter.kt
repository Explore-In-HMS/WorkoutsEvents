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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.Workout
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutDetail
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutList
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.workout_item.view.*


class WorkoutAdapter(val workoutList: ArrayList<WorkoutList>, private val listener: ItemClickListener ) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {


    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    var mClickListener: ItemClickListener? = null

   lateinit var context: Context

    interface ItemClickListener
    {
        fun clickRow(position: Int)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_item, parent, false)
      context=view.context
        return WorkoutViewHolder(view)

    }

    override fun getItemCount(): Int {
        return workoutList.size

    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {

        Picasso.get().load(workoutList[position].imageUrl).into(holder.itemView.image_view_work)

        holder.itemView.main_title.text = workoutList[position].workoutName
        holder.itemView.text_view_calorie.text = workoutList[position].calorie.toString()
        holder.itemView.text_view_equipment.text=workoutList[position].equipment
        holder.itemView.text_view_duration.text= context.getString(R.string.text_mins_duration,(workoutList[position].workoutDuration/60).toString())



        holder.itemView.card_view.setOnClickListener {
            mClickListener = listener
            mClickListener?.clickRow(position)

        }
    }

    fun updateWorkoutList(newWorkoutList: List<WorkoutList>) {
        workoutList.clear()
        workoutList.addAll(newWorkoutList)
        notifyDataSetChanged()
    }


}


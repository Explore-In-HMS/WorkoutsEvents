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
import com.hms.referenceapp.workouts.model.dbmodels.WorkoutDetail
import kotlinx.android.synthetic.main.item_template_workout_detail.view.*

class WorkoutDetailAdapter(val workoutDetailList: ArrayList<WorkoutDetail>,private val listener: WorkoutDetailAdapter.ItemClickListener): RecyclerView.Adapter<WorkoutDetailAdapter.WorkoutDetailViewHolder>() {
    private var context: Context? = null



    class WorkoutDetailViewHolder(var view: View) : RecyclerView.ViewHolder(view) {


    }

    var mClickListener: ItemClickListener? = null


    interface ItemClickListener
    {
        fun clickRow(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutDetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_template_workout_detail,parent,false)
        context = parent.getContext();
        return WorkoutDetailViewHolder(view)

    }

    override fun getItemCount(): Int {
        return workoutDetailList.size
    }

    override fun onBindViewHolder(holder: WorkoutDetailViewHolder, position: Int) {
        if(workoutDetailList[position].duration!=null) {
           holder.view.item_title.text = workoutDetailList[position].name
            holder.view.item_detail.text ="Duration:" +workoutDetailList[position].duration.toString()+" sec"
        }
            else {
           holder.view.item_title.text = workoutDetailList[position].name
            holder.view.item_detail.text = "Repeat Count :" +workoutDetailList[position].repeatCount.toString()
        }
        val imageResourceId: Int = context!!.resources.getIdentifier(
            "workout_${position}",
            "drawable",
            context!!.getPackageName())
        holder.view.item_image.setImageResource(imageResourceId)

        holder.view.setOnClickListener {
            //val action = FeedFragmentDirections.actionFeedFragmentToCountryFragment()
           // Navigation.findNavController(it).navigate(action)
            mClickListener = listener
            mClickListener?.clickRow(position)
        }

    }


    fun updateWorkoutDetailList(newWorkoutDetailList: List<WorkoutDetail>) {
        workoutDetailList.clear()
        workoutDetailList.addAll(newWorkoutDetailList)
        notifyDataSetChanged()
    }
}
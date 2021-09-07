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
package com.hms.referenceapp.workouts.ui.main.events

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.utils.IOnItemSelectListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DateListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //Class Constants
    private val dayNameFormatter = SimpleDateFormat("EEE", Locale.getDefault())
    private val dayNumberFormatter = SimpleDateFormat("dd", Locale.getDefault())

    //Class Variables
    private var selectedPosition = 0
    private var dateList: ArrayList<Date>
    private var itemSelectListener: IOnItemSelectListener

    constructor(
        dateList: ArrayList<Date>,
        itemSelectListener: IOnItemSelectListener
    ) {
        this.dateList = dateList
        this.itemSelectListener = itemSelectListener
    }

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var parentCarView: CardView = itemView.findViewById(R.id.card_view_parent)
        var dayNameTextView: TextView = itemView.findViewById(R.id.text_view_day_name)
        var dayNumberTextView: TextView = itemView.findViewById(R.id.text_view_day_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_template_date, parent, false)
        return DateViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dateList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dateViewHolder = holder as DateViewHolder
        val context = dateViewHolder.itemView.context

        val date = dateList[position]

        dateViewHolder.dayNameTextView.text = dayNameFormatter.format(date)
        dateViewHolder.dayNumberTextView.text = dayNumberFormatter.format(date)

        if (selectedPosition == position) {
            dateViewHolder.parentCarView.setCardBackgroundColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.colorAccent)
                )
            )
            dateViewHolder.dayNameTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorGray
                )
            )
            dateViewHolder.dayNumberTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorAccent
                )
            )
        } else {
            dateViewHolder.parentCarView.setCardBackgroundColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.colorGray)
                )
            )
            dateViewHolder.dayNameTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimary
                )
            )
            dateViewHolder.dayNumberTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimaryDark
                )
            )
        }

        dateViewHolder.itemView.setOnClickListener {
            if (selectedPosition != position) {
                val tempPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(tempPosition)
                notifyItemChanged(selectedPosition)
                itemSelectListener.onItemSelect(date, position)
            }
        }
    }
}
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
package com.hms.referenceapp.workouts.ui.main.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.dbmodels.EventDetail
import com.hms.referenceapp.workouts.model.dbmodels.EventList
import com.hms.referenceapp.workouts.ui.main.events.EEventType
import com.hms.referenceapp.workouts.utils.IOnItemButtonClickListener
import com.hms.referenceapp.workouts.utils.IOnItemSelectListener
import com.hms.referenceapp.workouts.utils.getDirectionsIntent
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProfileEventListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //Class Constants
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    //Class Variables
    private var currentUserId: String
    private var eventList: ArrayList<EventList>
    private var eventCategoryList: ArrayList<EventDetail>
    private var eventType: EEventType
    private var itemSelectListener: IOnItemSelectListener
    private var itemButtonClickListener: IOnItemButtonClickListener
    private var noEventView: View

    constructor(
        currentUserId: String,
        eventList: ArrayList<EventList>,
        eventCategoryList: ArrayList<EventDetail>,
        eventType: EEventType,
        itemSelectListener: IOnItemSelectListener,
        itemButtonClickListener: IOnItemButtonClickListener,
        noEventView: View
    ) {
        this.currentUserId = currentUserId
        this.eventList = ArrayList()
        this.eventList.addAll(eventList)
        this.eventCategoryList = eventCategoryList
        this.eventType = eventType
        this.itemSelectListener = itemSelectListener
        this.itemButtonClickListener = itemButtonClickListener
        this.noEventView = noEventView
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pictureImageView: ImageView = itemView.findViewById(R.id.image_view_picture)
        var titleTextView: TextView = itemView.findViewById(R.id.text_view_title)
        var descriptionTextView: TextView = itemView.findViewById(R.id.text_view_description)
        var timeTextView: TextView = itemView.findViewById(R.id.text_view_time)
        var dateTextView: TextView = itemView.findViewById(R.id.text_view_date)
        var participant1ImageView: ImageView = itemView.findViewById(R.id.image_view_participant_1)
        var participant2ImageView: ImageView = itemView.findViewById(R.id.image_view_participant_2)
        var participantCountTextView: TextView =
            itemView.findViewById(R.id.text_view_participant_count)
        var joinButton: Button = itemView.findViewById(R.id.button_join)
        var directionsImageButton: ImageButton = itemView.findViewById(R.id.image_button_directions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_template_event, parent, false)
        return EventViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val eventViewHolder = holder as EventViewHolder
        val context = eventViewHolder.itemView.context

        val event = eventList[position]
        val eventCategory = getEventCategory(event.eventDetailId.toInt())

        Picasso.get().load(eventCategory.imageUrl).into(eventViewHolder.pictureImageView)
        eventViewHolder.titleTextView.text = event.eventName
        eventViewHolder.descriptionTextView.text = event.description
        eventViewHolder.timeTextView.text = timeFormatter.format(event.date)
        eventViewHolder.dateTextView.text = dateFormatter.format(event.date)
        eventViewHolder.dateTextView.visibility = View.VISIBLE

        eventViewHolder.participantCountTextView.text = event.participantsCount.toString()
        eventViewHolder.participant1ImageView.visibility = View.GONE
        eventViewHolder.participant2ImageView.visibility = View.GONE
        /*when (event.participantsCount) {
            0 -> {
                eventViewHolder.participant1ImageView.visibility = View.GONE
                eventViewHolder.participant2ImageView.visibility = View.GONE
            }
            1 -> {
                eventViewHolder.participant1ImageView.visibility = View.VISIBLE
                eventViewHolder.participant2ImageView.visibility = View.GONE

                eventViewHolder.participant1ImageView.setImageResource(R.drawable.participant_1)
            }
            else -> {
                eventViewHolder.participant1ImageView.visibility = View.VISIBLE
                eventViewHolder.participant2ImageView.visibility = View.VISIBLE

                eventViewHolder.participant1ImageView.setImageResource(R.drawable.participant_1)
                eventViewHolder.participant2ImageView.setImageResource(R.drawable.participant_2)
            }
        }*/

        if (eventType == EEventType.UPCOMING) {
            if (currentUserId == event.eventCreaterID.toString()) {
                eventViewHolder.joinButton.visibility = View.GONE
            } else {
                eventViewHolder.joinButton.visibility = View.VISIBLE
                if (event.participants.contains(currentUserId)) {//Leave
                    eventViewHolder.joinButton.text = context.getString(R.string.text_leave)
                } else {//Join
                    eventViewHolder.joinButton.text = context.getString(R.string.text_join)
                }
                eventViewHolder.joinButton.setOnClickListener {
                    itemButtonClickListener.onItemButtonClick(
                        eventViewHolder.joinButton.id,
                        event,
                        position
                    )
                }
            }

            eventViewHolder.directionsImageButton.setOnClickListener {
                getDirectionsIntent(context, event.lattitude, event.longtitude)
            }

            eventViewHolder.itemView.setOnClickListener {
                itemSelectListener.onItemSelect(event, position)
            }
        } else {
            eventViewHolder.joinButton.visibility = View.GONE
            eventViewHolder.directionsImageButton.visibility = View.GONE
        }
    }

    fun updateEventList(newList: ArrayList<EventList>) {
        eventList = ArrayList()
        eventList.addAll(newList)
        checkListSize()
        notifyDataSetChanged()
    }

    fun updateEventCategoryList(newList: ArrayList<EventDetail>) {
        this.eventCategoryList = newList
        notifyDataSetChanged()
    }

    private fun getEventCategory(eventCategoryId: Int): EventDetail {
        for (eventCategory in eventCategoryList) {
            if (eventCategory.id == eventCategoryId) {
                return eventCategory
            }
        }
        return eventCategoryList[0]
    }

    private fun checkListSize() {
        if (eventList.size <= 0) {
            noEventView.visibility = View.VISIBLE
        } else {
            noEventView.visibility = View.GONE
        }
    }
}
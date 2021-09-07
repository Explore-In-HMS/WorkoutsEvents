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
package com.hms.referenceapp.workouts.ui.eventdetail

import CircleTransform
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.Friends
import com.hms.referenceapp.workouts.utils.Constants
import com.hms.referenceapp.workouts.utils.IOnItemButtonClickListener
import com.hms.referenceapp.workouts.utils.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.participants_layout.view.*

class ParticipantRecyclerAdapter(
    private var participantList: ArrayList<Friends>,
    private var itemButtonClickListener: IOnItemButtonClickListener,
    private var currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.participants_layout, parent, false)
        return ParticipantViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val participantViewHolder = holder as ParticipantViewHolder

        val participant = participantList[position]
        var friendIdList: String = ""
        for (friend in User.friendsArray!!) {
            friendIdList += "(${friend.userId})"
        }
        participantViewHolder.participantName.text = participant.name
        if (currentUserId == participant.userId) {
            participantViewHolder.friendButton.visibility = View.INVISIBLE
        } else {
            participantViewHolder.friendButton.visibility = View.VISIBLE
            if (friendIdList.contains("(${participant.userId})")) {
                participantViewHolder.friendButton.text = "Remove"
            } else {
                participantViewHolder.friendButton.text = "Add Friend"
            }

        }

        var picUrl =
            "https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/User_icon_2.svg/1024px-User_icon_2.svg.png"
        if (participant.profileImage != null && participant.profileImage != "") {
            picUrl = participant.profileImage
        }
        Picasso.get().load(picUrl).transform(CircleTransform())
            .into(participantViewHolder.participantImage)

        participantViewHolder.friendButton.setOnClickListener {
            if (friendIdList.contains("(${participant.userId})")) {
                friendIdList = friendIdList.replace("(${participant.userId})", "")
                participantViewHolder.friendButton.text = "Add Friend"
            } else {
                friendIdList += "(${participant.userId})"
                participantViewHolder.friendButton.text = "Remove"
            }
            itemButtonClickListener.onItemButtonClick(
                participantViewHolder.friendButton.id,
                participant,
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return participantList.size
    }

    inner class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var participantName: TextView = itemView.findViewById(R.id.participant_name_text_view)
        var participantImage: ImageView = itemView.findViewById(R.id.participant_image_view)
        var friendButton: Button = itemView.findViewById(R.id.friend_action_button)
    }

}
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

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.dbmodels.EventList

import com.hms.referenceapp.workouts.ui.eventdetail.EventDetailActivity
import com.hms.referenceapp.workouts.ui.main.workouts.WorkoutDetailViewModel
import com.hms.referenceapp.workouts.utils.Constants.INTENT_PARAMETER_EVENT
import com.hms.referenceapp.workouts.utils.IOnItemButtonClickListener
import com.hms.referenceapp.workouts.utils.IOnItemSelectListener
import com.huawei.agconnect.auth.AGConnectAuth
import com.hms.referenceapp.workouts.utils.SharedPreferencesHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class EventListFragment : Fragment() {

    //Variables
    private val eventsViewModel: EventsViewModel by viewModel()
    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()
    private val currentUserId = sharedPreferencesHelper.getUserId().toString()
    private lateinit var eventListAdapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_list, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val eventSwipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_event)
        eventSwipeRefresh.setOnRefreshListener {
            eventsViewModel.refreshEventList()
        }

        val eventRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_event)
        eventRecyclerView.itemAnimator = DefaultItemAnimator()
        eventListAdapter =
            EventListAdapter(
                eventsViewModel,
                currentUserId,
                ArrayList(),
                ArrayList(),
                object : IOnItemSelectListener {
                    override fun onItemSelect(item: Any, position: Int) {
                        item as EventList
                        val intent = Intent(requireContext(), EventDetailActivity::class.java)
                        intent.putExtra(INTENT_PARAMETER_EVENT, item.toJSON())
                        startActivity(intent)
                    }
                },
                object : IOnItemButtonClickListener {
                    override fun onItemButtonClick(buttonId: Int, item: Any, position: Int) {
                        item as EventList
                        eventsViewModel.getEvent(item.id, object : IOnItemSelectListener {
                            override fun onItemSelect(item: Any, position: Int) {
                                item as EventList
                                when (buttonId) {
                                    R.id.button_join -> {
                                        if (item.participants.contains(currentUserId)) {//Leave
                                            item.participantsCount--
                                            item.participants =
                                                item.participants.replace(", $currentUserId", "")
                                        } else {//Join
                                            item.participantsCount++
                                            item.participants += ", $currentUserId"
                                        }
                                    }
                                }
                                eventsViewModel.updateEvent(item)
                                eventListAdapter.notifyItemChanged(position)
                            }
                        })
                    }
                },
                view.findViewById(R.id.view_no_event)
            )
        eventRecyclerView.adapter = eventListAdapter

        eventRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {// Scroll Down
                    eventsViewModel.setFabVisible(false)
                } else if (dy < 0) {// Scroll Up
                    eventsViewModel.setFabVisible(true)
                }
            }
        })

        eventsViewModel.eventList.observe(viewLifecycleOwner, {
            eventListAdapter.updateEventList(it)
            eventListAdapter.filterByEventCategory(eventsViewModel.selectedEventCategoryId.value as Int)
            eventSwipeRefresh.isRefreshing = false
        })
        eventsViewModel.eventCategoryList.observe(viewLifecycleOwner, {
            eventListAdapter.updateEventCategoryList(it)
        })
        eventsViewModel.getEventList(Calendar.getInstance().timeInMillis)

        eventsViewModel.selectedEventCategoryId.observe(viewLifecycleOwner, {
            eventListAdapter.filterByEventCategory(it)
        })
    }
}

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

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.ui.main.events.EEventType
import com.hms.referenceapp.workouts.utils.BUNDLE_KEY_EVENT_TYPE
import com.hms.referenceapp.workouts.utils.IOnItemButtonClickListener
import com.hms.referenceapp.workouts.utils.IOnItemSelectListener
import com.hms.referenceapp.workouts.utils.SharedPreferencesHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList

class ProfileEventListFragment : Fragment() {

    //Variables
    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()
    private val currentUserId = sharedPreferencesHelper.getUserId().toString()
    private val profileEventsViewModel: ProfileEventsViewModel by viewModel()
    private lateinit var eventListAdapter: ProfileEventListAdapter
    private lateinit var eventType: EEventType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_list, container, false)
        init(view)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventType = it.getSerializable(BUNDLE_KEY_EVENT_TYPE) as EEventType
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(eventType: EEventType) =
            ProfileEventListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(BUNDLE_KEY_EVENT_TYPE, eventType)
                }
            }
    }

    private fun init(view: View) {
        val eventSwipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_event)
        eventSwipeRefresh.setOnRefreshListener {
            eventSwipeRefresh.isRefreshing = false
        }

        val eventRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_event)
        eventRecyclerView.itemAnimator = DefaultItemAnimator()
        eventListAdapter =
            ProfileEventListAdapter(
                currentUserId,
                ArrayList(),
                ArrayList(),
                eventType,
                object : IOnItemSelectListener {
                    override fun onItemSelect(item: Any, position: Int) {
                        /*item as EventList
                        val intent = Intent(requireContext(), EventDetailActivity::class.java)
                        intent.putExtra(INTENT_PARAMETER_EVENT, item.toJSON())
                        startActivity(intent)*/
                    }
                },
                object : IOnItemButtonClickListener {
                    override fun onItemButtonClick(buttonId: Int, item: Any, position: Int) {
                        /*item as EventList
                        when (buttonId) {
                            R.id.button_join -> {
                                if (item.participants.contains(currentUserId)) {//Leave
                                    item.participantsCount--
                                    item.participants =
                                        item.participants.replace(", $currentUserId", "")
                                    unsubscribeTopic(requireContext(), item.id.toString())
                                } else {//Join
                                    item.participantsCount++
                                    item.participants += ", $currentUserId"
                                    subscribeTopic(requireContext(), item.id.toString())
                                }
                            }
                        }
                        profileEventsViewModel.updateEvent(item)
                        eventListAdapter.notifyItemChanged(position)*/
                    }
                },
                view.findViewById(R.id.view_no_event)
            )
        eventRecyclerView.adapter = eventListAdapter

        profileEventsViewModel.eventList.observe(viewLifecycleOwner, {
            eventListAdapter.updateEventList(it)
        })
        profileEventsViewModel.eventCategoryList.observe(viewLifecycleOwner, {
            eventListAdapter.updateEventCategoryList(it)
        })
        profileEventsViewModel.getEventList(currentUserId, eventType)
    }
}

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.hms.referenceapp.workouts.R
import com.hms.referenceapp.workouts.model.dbmodels.EventDetail
import com.hms.referenceapp.workouts.ui.eventcreation.EventCreationActivity
import com.hms.referenceapp.workouts.ui.main.workouts.WorkoutDetailViewModel
import com.hms.referenceapp.workouts.utils.EVENT_DATE_LIST_LIMIT
import com.hms.referenceapp.workouts.utils.IOnItemSelectListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList


class EventsFragment : Fragment() {

    //Variables
    private val eventsViewModel: EventsViewModel by viewModel()

    //Widgets
    private lateinit var progressView: View
    private lateinit var progressBarView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_events, container, false)
        init(root)
        return root
    }

    override fun onResume() {
        super.onResume()
        eventsViewModel.refreshEventList()
    }

    private fun init(view: View) {
        progressView = view.findViewById(R.id.view_progress)
        progressBarView = view.findViewById(R.id.view_progress_bar)

        eventsViewModel.onProgress.observe(viewLifecycleOwner, {
            onProgress(it)
        })

        view.findViewById<ImageButton>(R.id.image_button_filter).setOnClickListener {
            openFilterPopupMenu(it)
        }

        val addEventFab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_event)
        addEventFab.tag = "visible"
        addEventFab.shrink()
        addEventFab.setOnClickListener {
            startActivity(Intent(requireContext(), EventCreationActivity::class.java))
        }

        eventsViewModel.isFabVisible.observe(viewLifecycleOwner, {
            if (it) {
                if (addEventFab.tag.toString() != "visible") {
                    addEventFab.tag = "visible"
                    addEventFab.animate().scaleX(1f).scaleY(1f).start()
                }
            } else {
                if (addEventFab.tag.toString() != "hidden") {
                    addEventFab.tag = "hidden"
                    addEventFab.animate().scaleX(0f).scaleY(0f).start()
                }
            }
        })

        initDateList(view)
        initViewPager(view)
    }

    private fun initDateList(view: View) {
        val dateList = ArrayList<Date>()
        val calendar = Calendar.getInstance()
        for (i in 0..EVENT_DATE_LIST_LIMIT) {
            dateList.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        val dateRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_date)
        val dateListAdapter = DateListAdapter(dateList, object : IOnItemSelectListener {
            override fun onItemSelect(item: Any, position: Int) {
                val date = item as Date
                eventsViewModel.getEventList(date.time)
            }
        })
        dateRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        dateRecyclerView.itemAnimator = DefaultItemAnimator()
        dateRecyclerView.adapter = dateListAdapter
    }

    private fun initViewPager(view: View) {
        val eventListFragment = EventListFragment()
        val eventMapFragment = EventMapFragment()
        val fragmentPagerAdapter = object : FragmentPagerAdapter(
            childFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            val fragments = arrayOf(eventListFragment, eventMapFragment)

            val fragmentNames = arrayOf(
                getString(R.string.text_list),
                getString(R.string.text_map)
            )

            override fun getCount(): Int {
                return fragments.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return fragmentNames[position]
            }

            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }
        }

        val viewPager = view.findViewById<ViewPager>(R.id.view_pager_events)
        viewPager.adapter = fragmentPagerAdapter
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_events)
        tabLayout.setupWithViewPager(viewPager)
        for (i in 0 until tabLayout.tabCount) {
            val tabTextView = TextView(context)
            tabTextView.text = tabLayout.getTabAt(i)!!.text
            tabTextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.font_t7)

            tabTextView.textSize = resources.getDimension(R.dimen.text_size_xxxsmall)
            tabTextView.setTextColor(
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.color_style_tab_text
                )
            )
            tabTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            tabLayout.getTabAt(i)!!.customView = tabTextView
        }
    }

    private fun onProgress(isVisible: Boolean) {
        if (isVisible) {
            progressView.visibility = View.VISIBLE
            progressBarView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.GONE
            progressBarView.visibility = View.GONE
        }
    }

    private fun openFilterPopupMenu(view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menu.add(0, 0, 0, getString(R.string.text_all))
        for (eventDetail in eventsViewModel.eventCategoryList.value as ArrayList<EventDetail>) {
            popupMenu.menu.add(eventDetail.id, eventDetail.id, eventDetail.id, eventDetail.name)
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            eventsViewModel.setSelectedEventCategoryId(item.order)
            true
        }
    }
}
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
package com.hms.referenceapp.workouts.di

import com.hms.referenceapp.workouts.services.HuaweiAccountServiceImpl
import com.hms.referenceapp.workouts.ui.eventcreation.EventCreationViewModel
import com.hms.referenceapp.workouts.ui.eventdetail.EventDetailViewModel
import com.hms.referenceapp.workouts.ui.login.LoginViewModel
import com.hms.referenceapp.workouts.ui.main.profile.ProfileEventsViewModel
import com.hms.referenceapp.workouts.ui.main.profile.ProfileViewModel
import com.hms.referenceapp.workouts.ui.main.workouts.WorkoutDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mViewModelModules = module {
    viewModel {
        LoginViewModel(
            accountService = HuaweiAccountServiceImpl(
                androidContext()
            )
        )
    }

    /*viewModel {
        WorkoutDetailViewModel(
            usecase = get()
        )
    }*/

    viewModel {
        ProfileEventsViewModel(
            cloudDBUseCase = get()
        )
    }

    viewModel {
        ProfileViewModel(
            cloudDBUseCase = get()
        )
    }

    viewModel {
        EventCreationViewModel(get())
    }

    viewModel {
        EventDetailViewModel(get())
    }
}
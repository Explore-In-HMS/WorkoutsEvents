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
package com.hms.referenceapp.workouts.services

import android.content.Intent
import com.hms.referenceapp.workouts.model.Worker
import com.hms.referenceapp.workouts.model.login.SignInResult
import io.reactivex.Single

interface AccountService {
    fun silentSignIn(): Single<SignInResult>
    fun transmitTokenIntoAppGalleryConnect(accessToken: String): Single<SignInResult>
    fun getSignInIntent(intent: (Intent) -> Unit)
    fun onSignInActivityResult(intent: Intent): Single<SignInResult>
    fun signOut(): Worker<Unit>
}
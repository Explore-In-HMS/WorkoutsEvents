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

import android.content.Context
import android.content.Intent
import android.util.Log
import com.hms.referenceapp.workouts.model.Worker
import com.hms.referenceapp.workouts.model.login.HuaweiAccountMapper
import com.hms.referenceapp.workouts.model.login.SignInResult
import com.hms.referenceapp.workouts.utils.Constants
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hmf.tasks.Task
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import io.reactivex.Single
import io.reactivex.SingleEmitter
import java.util.*

class HuaweiAccountServiceImpl(
    private val context: Context
) : AccountService {
    private var mHuaweiIdAuthService: HuaweiIdAuthService
    private val mapper = HuaweiAccountMapper()

    init {
        val params = HuaweiIdAuthParamsHelper(
            HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM
        )
            .setAccessToken()
            .setScopeList(getScopes())
            .setIdToken()
            .setEmail()
            .createParams()
        mHuaweiIdAuthService = HuaweiIdAuthManager.getService(context, params)
    }

    private fun getScopes(): MutableList<Scope> {
        val scopeList: MutableList<Scope> = LinkedList()
        scopeList.add(HuaweiIdAuthAPIManager.HUAWEIID_BASE_SCOPE) // Basic account permissions
        return scopeList
    }

    companion object {
        var user: AuthHuaweiId? = null
    }

    override fun silentSignIn(): Single<SignInResult> {
        return Single.create { emitter: SingleEmitter<SignInResult> ->
            val task: Task<AuthHuaweiId> = mHuaweiIdAuthService.silentSignIn()
            task.addOnSuccessListener {
                emitter.onSuccess(mapper.map(it))
                user = it
            }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun transmitTokenIntoAppGalleryConnect(accessToken: String): Single<SignInResult> {
        return Single.create { emitter ->
            val credential = HwIdAuthProvider.credentialWithToken(accessToken)
            AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener {
                    emitter.onSuccess(SignInResult(it.user.uid, setOf()))
                }.addOnFailureListener {
                    Log.e(Constants.loginActivityTAG, "Transmission Error: ${it.message}")
                }
        }
    }

    override fun getSignInIntent(intent: (Intent) -> Unit) {
        intent.invoke(mHuaweiIdAuthService.signInIntent)
    }

    override fun onSignInActivityResult(intent: Intent): Single<SignInResult> {
        return Single.create { emittir ->
            val task: Task<AuthHuaweiId> = HuaweiIdAuthManager.parseAuthResultFromIntent(intent)
            task.addOnSuccessListener {
                Log.i("", "onSignInActivityResult: ${it.email}")
                emittir.onSuccess(mapper.map(it))
                user = it
            }
            task.addOnFailureListener { emittir.onError(it) }
        }
    }

    override fun signOut(): Worker<Unit> {
        val worker: Worker<Unit> = Worker()

        mHuaweiIdAuthService.signOut()
            .addOnSuccessListener { worker.onSuccess(Unit) }
            .addOnFailureListener { worker.onFailure(it) }
            .addOnCanceledListener { worker.onCancel() }

        return worker
    }
}
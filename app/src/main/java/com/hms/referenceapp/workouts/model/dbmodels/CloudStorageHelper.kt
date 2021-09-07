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
package com.hms.referenceapp.workouts.model.dbmodels

import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement
import com.huawei.agconnect.cloud.storage.core.StorageReference
import com.huawei.hmf.tasks.Tasks

object CloudStorageHelper {

    //All Cloud Storage functions can be located in this helper class
    private lateinit var storageManagement: AGCStorageManagement

    //initialize helper class with the instance of storage object
    init {
        initStorage()
    }

    private fun initStorage() {
        storageManagement = AGCStorageManagement.getInstance()
    }

    /*get all the file list from storage area
    Call your-storage-reference.listAll*/
    fun listAllFile(): List<StorageReference> {
        var ref = storageManagement.getStorageReference("images/")
        var fileStorageList = ref.listAll()
        return Tasks.await(fileStorageList).fileList
    } }
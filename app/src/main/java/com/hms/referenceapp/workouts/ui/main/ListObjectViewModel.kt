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

package com.hms.referenceapp.workouts.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.referenceapp.workouts.model.dbmodels.CloudStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListObjectsViewModel : ViewModel() {

    val fileList: LiveData<List<String>>
        get() = _listOfFiles



    private val _listOfFiles = MutableLiveData<List<String>>()



    init {
        listAllFiles()
    }

    fun listAllFiles() {

        CoroutineScope(Dispatchers.IO).launch {
            val list = CloudStorageHelper.listAllFile()
            var fileNames = arrayListOf<String>()
            for (x in 1 until list.size) {
                fileNames.add(list[x].name)
            }
            _listOfFiles.postValue(fileNames)

        }

    }}
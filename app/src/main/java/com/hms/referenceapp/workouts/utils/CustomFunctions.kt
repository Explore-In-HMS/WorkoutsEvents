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
package com.hms.referenceapp.workouts.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.hms.referenceapp.workouts.R

fun getDirectionsIntent(context: Context, latitude: Double, longitude: Double) {
    val uriString =
        "mapapp://navigation?daddr=$latitude, $longitude"
    val contentUrl: Uri = Uri.parse(uriString)
    val intent = Intent(Intent.ACTION_VIEW, contentUrl)
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(
            context,
            context.getString(R.string.text_toast_petal_map_not_installed),
            Toast.LENGTH_LONG
        ).show()
    }
}
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

import com.hms.referenceapp.workouts.model.Friends


const val AGC_API_KEY =
    "CgB6e3x93nA+atqUFALpXpv37MBVbUKLc9k2cLHgoHVAEK/dhKwGBaX2+8FaUMjaGHoUoP0Lj7BavCpUErFBxqho"
const val AGC_APP_SECRET = "3dd63dee56949062bebdc46ebf7d77d25e9dab3092131e258a262e32fe780173"
const val AGC_APP_ID = "103623781"
const val APP_NAME = "WorkoutsEvents"

const val GRANT_TYPE = "client_credentials"

const val SHARED_PREFERENCES_KEY_ACCESS_TOKEN = "access_token"
const val SHARED_PREFERENCES_KEY_ALLOWED_NOTIFICATIONS = "allowed_notifications"
const val SHARED_PREFERENCES_KEY_USER_CITY = "user_city"

const val DEFAULT_CITY = "Istanbul"

const val EVENT_DATE_LIST_LIMIT = 15

const val BUNDLE_KEY_EVENT_TYPE = "event_type"

const val JSON_OBJECT_KEY_ID = "id"
const val JSON_OBJECT_KEY_TOPIC = "topic"
const val JSON_OBJECT_KEY_TOPIC_TEXT = "topic_text"

//Map Constants
const val MAP_PARAMETER_ZOOM_LEVEL_FOCUS = 14f

object Constants {
    const val packageName = "com.hms.referenceapp.workouts"
    val durationArray = arrayListOf(0, 15, 30, 45, 60, 90, 120, 150, 180)
    const val noProperLocation = "Please select a proper location."
    const val bundleApiKey = "bundle_arg_key_map_view_api_key"
    const val eventDetailActivityTAG = "EventDetailActivityTAG"
    const val eventCreationActivityTAG = "EventCreationActivity"
    const val loginActivityTAG = "LoginActivity"
    const val apiConnectionTAG = "APIConnection"
    const val REQUEST_SIGN_IN_LOGIN = 1002
    const val userId = "userId"
    const val userName = "userName"
    const val userAvatar = "userAvatar"
    const val accessToken = "accessToken"
    const val tokenType = "tokenType"
    const val userIdFromDb = "userIdFromDb"
    const val nanStr = "NaN"
    const val INTENT_PARAMETER_EVENT = "event"
}

object User {
    var friendsArray: ArrayList<Friends>? =
        arrayListOf(
            Friends(
                id = "NaN",
                name = "NaN",
                profileImage = "NaN",
                userId = "NaN"
            )
        )
}

object EventParticipants {
    var participantsList: ArrayList<Friends>? =
        arrayListOf(
            Friends(
                id = "NaN",
                name = "NaN",
                profileImage = "NaN",
                userId = "NaN"
            )
        )
}

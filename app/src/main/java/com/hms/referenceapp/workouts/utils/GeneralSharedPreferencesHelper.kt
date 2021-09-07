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
import android.content.SharedPreferences
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

private val FILE_NAME = "app_data"

fun putToSharedPreferences(context: Context, key: String, obj: Any) {
    val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    val editor = sp.edit()

    if (obj is String) {
        editor.putString(key, obj)
    } else if (obj is Int) {
        editor.putInt(key, obj)
    } else if (obj is Boolean) {
        editor.putBoolean(key, obj)
    } else if (obj is Float) {
        editor.putFloat(key, obj)
    } else if (obj is Long) {
        editor.putLong(key, obj)
    } else {
        editor.putString(key, obj.toString())
    }

    SharedPreferencesCompat.apply(editor)
}

fun getFromSharedPreferences(context: Context, key: String, defaultObj: Any): Any? {
    val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    if (defaultObj is String) {
        return sp.getString(key, defaultObj)
    } else if (defaultObj is Int) {
        return sp.getInt(key, defaultObj)
    } else if (defaultObj is Boolean) {
        return sp.getBoolean(key, defaultObj)
    } else if (defaultObj is Float) {
        return sp.getFloat(key, defaultObj)
    } else if (defaultObj is Long) {
        return sp.getLong(key, defaultObj)
    }

    return null
}

fun removeFromSharedPreferences(context: Context, key: String) {
    val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    val editor = sp.edit()
    editor.remove(key)
    SharedPreferencesCompat.apply(editor)
}

fun clearFromSharedPreferences(context: Context) {
    val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    val editor = sp.edit()
    editor.clear()
    SharedPreferencesCompat.apply(editor)
}

fun contains(context: Context, key: String): Boolean {
    val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    return sp.contains(key)
}

fun getAllFromSharedPreferences(context: Context): Map<String?, *>? {
    val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    return sp.all
}

private object SharedPreferencesCompat {
    private val applyMethod = findApplyMethod()
    private fun findApplyMethod(): Method? {
        try {
            val clz: Class<*> = SharedPreferences.Editor::class.java
            return clz.getMethod("apply")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        return null
    }

    fun apply(editor: SharedPreferences.Editor) {
        try {
            if (applyMethod != null) {
                applyMethod.invoke(editor)
                return
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        editor.commit()
    }
}
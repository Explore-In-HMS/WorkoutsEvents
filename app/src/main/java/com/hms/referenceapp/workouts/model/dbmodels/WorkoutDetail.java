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
package com.hms.referenceapp.workouts.model.dbmodels;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.Text;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.EntireEncrypted;
import com.huawei.agconnect.cloud.database.annotations.NotNull;
import com.huawei.agconnect.cloud.database.annotations.Indexes;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

import java.util.Date;

/**
 * Definition of ObjectType WorkoutDetail.
 *
 * @since 2021-01-29
 */
@PrimaryKeys({"id"})
public final class WorkoutDetail extends CloudDBZoneObject {
    private Integer id;

    @NotNull
    @DefaultValue(stringValue = "pushup")
    private String name;

    private Integer duration;

    private Integer repeatCount;

    private Text workoutText;

    private Integer resTime;

    private String workoutUrl;

    @NotNull
    @DefaultValue(intValue = 1)
    private Integer workoutId;

    public WorkoutDetail() {
        super(WorkoutDetail.class);
        this.name = "pushup";
        this.workoutId = 1;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setWorkoutText(Text workoutText) {
        this.workoutText = workoutText;
    }

    public Text getWorkoutText() {
        return workoutText;
    }

    public void setResTime(Integer resTime) {
        this.resTime = resTime;
    }

    public Integer getResTime() {
        return resTime;
    }

    public void setWorkoutUrl(String workoutUrl) {
        this.workoutUrl = workoutUrl;
    }

    public String getWorkoutUrl() {
        return workoutUrl;
    }

    public void setWorkoutId(Integer workoutId) {
        this.workoutId = workoutId;
    }

    public Integer getWorkoutId() {
        return workoutId;
    }

}

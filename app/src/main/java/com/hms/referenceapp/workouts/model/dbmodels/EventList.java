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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Definition of ObjectType EventList.
 *
 * @since 2021-01-29
 */
@PrimaryKeys({"id"})
public final class EventList extends CloudDBZoneObject {
    private Integer id;

    @NotNull
    @DefaultValue(stringValue = "Basketball")
    private String eventName;

    @NotNull
    @DefaultValue(stringValue = "1")
    private String eventDetailId;

    @NotNull
    @DefaultValue(stringValue = "Istanbul")
    private String city;

    private Double lattitude;

    private Double longtitude;

    private Integer duration;

    private String participants;

    private Integer participantsCount;

    private String description;

    private Long date;

    @NotNull
    @DefaultValue(longValue = 542595611279557056L)
    private Long eventCreaterID;

    public EventList() {
        super(EventList.class);
        this.eventName = "Basketball";
        this.eventDetailId = "1";
        this.city = "Istanbul";
        this.eventCreaterID = 542595611279557056L;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventDetailId(String eventDetailId) {
        this.eventDetailId = eventDetailId;
    }

    public String getEventDetailId() {
        return eventDetailId;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setLattitude(Double lattitude) {
        this.lattitude = lattitude;
    }

    public Double getLattitude() {
        return lattitude;
    }

    public void setLongtitude(Double longtitude) {
        this.longtitude = longtitude;
    }

    public Double getLongtitude() {
        return longtitude;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipantsCount(Integer participantsCount) {
        this.participantsCount = participantsCount;
    }

    public Integer getParticipantsCount() {
        return participantsCount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getDate() {
        return date;
    }

    public void setEventCreaterID(Long eventCreaterID) {
        this.eventCreaterID = eventCreaterID;
    }

    public Long getEventCreaterID() {
        return eventCreaterID;
    }

    public EventList(String jsonString) throws JSONException {
        super(EventList.class);
        JSONObject object = new JSONObject(jsonString);
        id = object.getInt("id");
        eventName = object.getString("eventName");
        eventDetailId = object.getString("eventDetailId");
        city = object.getString("city");
        lattitude = object.getDouble("lattitude");
        longtitude = object.getDouble("longtitude");
        duration = object.getInt("duration");
        participants = object.getString("participants");
        participantsCount = object.getInt("participantsCount");
        description = object.getString("description");
        date = object.getLong("date");
        eventCreaterID = object.getLong("eventCreaterID");
    }

    public String toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("eventName", eventName);
            object.put("eventDetailId", eventDetailId);
            object.put("city", city);
            object.put("lattitude", lattitude);
            object.put("longtitude", longtitude);
            object.put("duration", duration);
            object.put("participants", participants);
            object.put("participantsCount", participantsCount);
            if (description == null) {
                object.put("description", "");
            } else {
                object.put("description", description);
            }
            object.put("date", date);
            object.put("eventCreaterID", eventCreaterID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
}

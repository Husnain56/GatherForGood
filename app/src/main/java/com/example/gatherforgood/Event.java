package com.example.gatherforgood;

import java.io.Serializable;

public class Event implements Serializable {

    private String eventId;
    private String hostUid;
    private String hostName;
    private String eventType;
    private String title;
    private int volunteersRequired;
    private int volunteersJoined;
    private String genderSetting;
    private String date;
    private String time;
    private long eventTimeMillis;
    private String endDate;
    private String endTime;
    private long eventEndTimeMillis;
    private String location;
    private double lat;
    private double lng;
    private String description;
    private String requirements;
    private String status;
    private long createdAt;

    public Event() {}

    public Event(String eventId, String hostUid, String hostName,
                 String eventType, String title,
                 int volunteersRequired, int volunteersJoined,
                 String genderSetting, String date, String time,
                 long eventTimeMillis, String endDate, String endTime,
                 long eventEndTimeMillis, String location,
                 double lat, double lng,
                 String description, String requirements,
                 String status, long createdAt) {
        this.eventId             = eventId;
        this.hostUid             = hostUid;
        this.hostName            = hostName;
        this.eventType           = eventType;
        this.title               = title;
        this.volunteersRequired  = volunteersRequired;
        this.volunteersJoined    = volunteersJoined;
        this.genderSetting       = genderSetting;
        this.date                = date;
        this.time                = time;
        this.eventTimeMillis     = eventTimeMillis;
        this.endDate             = endDate;
        this.endTime             = endTime;
        this.eventEndTimeMillis  = eventEndTimeMillis;
        this.location            = location;
        this.lat                 = lat;
        this.lng                 = lng;
        this.description         = description;
        this.requirements        = requirements;
        this.status              = status;
        this.createdAt           = createdAt;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getHostUid() { return hostUid; }
    public void setHostUid(String hostUid) { this.hostUid = hostUid; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getVolunteersRequired() { return volunteersRequired; }
    public void setVolunteersRequired(int volunteersRequired) { this.volunteersRequired = volunteersRequired; }

    public int getVolunteersJoined() { return volunteersJoined; }
    public void setVolunteersJoined(int volunteersJoined) { this.volunteersJoined = volunteersJoined; }

    public String getGenderSetting() { return genderSetting; }
    public void setGenderSetting(String genderSetting) { this.genderSetting = genderSetting; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public long getEventTimeMillis() { return eventTimeMillis; }
    public void setEventTimeMillis(long eventTimeMillis) { this.eventTimeMillis = eventTimeMillis; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public long getEventEndTimeMillis() { return eventEndTimeMillis; }
    public void setEventEndTimeMillis(long eventEndTimeMillis) { this.eventEndTimeMillis = eventEndTimeMillis; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
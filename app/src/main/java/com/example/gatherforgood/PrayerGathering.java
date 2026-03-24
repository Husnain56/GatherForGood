package com.example.gatherforgood;

public class PrayerGathering  implements java.io.Serializable{

    private String id;
    private String hostUid;
    private String description;

    private String hostName;
    private String prayerType;
    private String date;
    private String time;
    private String location;
    private double latitude;
    private double longitude;
    private String genderSetting;
    private String status;
    private int participantCount;
    private long createdAt;

    public PrayerGathering() {

    }

    public PrayerGathering(String id, String hostUid, String description, String hostName, String prayerType,
                           String date, String time, String location,
                           double latitude, double longitude,
                           String genderSetting, String status, int participantCount) {
        this.id = id;
        this.hostUid = hostUid;
        this.description = description;
        this.hostName = hostName;
        this.prayerType = prayerType;
        this.date = date;
        this.time = time;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.genderSetting = genderSetting;
        this.status = status;
        this.participantCount = participantCount;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHostUid() { return hostUid; }
    public void setHostUid(String hostUid) { this.hostUid = hostUid; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getPrayerType() { return prayerType; }
    public void setPrayerType(String prayerType) { this.prayerType = prayerType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getGenderSetting() { return genderSetting; }
    public void setGenderSetting(String genderSetting) { this.genderSetting = genderSetting; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}

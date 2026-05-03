package com.example.gatherforgood;

public class ParticipantItem {
    public String uid;
    public String name;
    public String gender;
    public String role;
    public long   joinedAt;

    public ParticipantItem(String uid, String name, String gender, String role, long joinedAt) {
        this.uid      = uid;
        this.name     = name;
        this.gender   = gender;
        this.role     = role;
        this.joinedAt = joinedAt;
    }
}
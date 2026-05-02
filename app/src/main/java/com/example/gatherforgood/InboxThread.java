package com.example.gatherforgood;

public class InboxThread {

    private String participantUid;
    private String participantName;
    private String lastMessage;
    private long   lastMessageTime;
    private String chatId;

    public InboxThread() {}

    public InboxThread(String participantUid, String participantName,
                       String lastMessage, long lastMessageTime, String chatId) {
        this.participantUid  = participantUid;
        this.participantName = participantName;
        this.lastMessage     = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.chatId          = chatId;
    }

    public String getParticipantUid()  { return participantUid; }
    public String getParticipantName() { return participantName; }
    public String getLastMessage()     { return lastMessage; }
    public long   getLastMessageTime() { return lastMessageTime; }
    public String getChatId()          { return chatId; }

    public void setLastMessage(String lastMessage)         { this.lastMessage     = lastMessage; }
    public void setLastMessageTime(long lastMessageTime)   { this.lastMessageTime = lastMessageTime; }
}
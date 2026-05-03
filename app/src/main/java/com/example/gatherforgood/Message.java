package com.example.gatherforgood;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String text;
    private long   timestamp;
    private String role;

    public Message() {}

    public Message(String messageId, String senderId, String senderName,
                   String text, long timestamp, String role) {
        this.messageId  = messageId;
        this.senderId   = senderId;
        this.senderName = senderName;
        this.text       = text;
        this.timestamp  = timestamp;
        this.role       = role;
    }

    public String getMessageId()  { return messageId; }
    public String getSenderId()   { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText()       { return text; }
    public long   getTimestamp()  { return timestamp; }
    public String getRole()       { return role; }

    public void setMessageId(String messageId)   { this.messageId = messageId; }
    public void setSenderId(String senderId)     { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setText(String text)             { this.text = text; }
    public void setTimestamp(long timestamp)     { this.timestamp = timestamp; }
    public void setRole(String role)             { this.role = role; }
}
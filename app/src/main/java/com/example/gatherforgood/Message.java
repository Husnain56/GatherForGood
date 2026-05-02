package com.example.gatherforgood;

public class Message {

    private String messageId;
    private String senderId;
    private String senderName;
    private String text;
    private long   timestamp;

    public Message() {}

    public Message(String messageId, String senderId, String senderName,
                   String text, long timestamp) {
        this.messageId  = messageId;
        this.senderId   = senderId;
        this.senderName = senderName;
        this.text       = text;
        this.timestamp  = timestamp;
    }

    public String getMessageId()  { return messageId; }
    public String getSenderId()   { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText()       { return text; }
    public long   getTimestamp()  { return timestamp; }

    public void setMessageId(String messageId)   { this.messageId  = messageId; }
    public void setSenderId(String senderId)     { this.senderId   = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setText(String text)             { this.text       = text; }
    public void setTimestamp(long timestamp)     { this.timestamp  = timestamp; }
}
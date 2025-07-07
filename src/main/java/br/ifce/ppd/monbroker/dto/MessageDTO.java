package br.ifce.ppd.monbroker.dto;

public class MessageDTO {
    private String sender;
    private String recipient;
    private String content;
    private MessageType type;

    public enum MessageType { DIRECT, TOPIC }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
}

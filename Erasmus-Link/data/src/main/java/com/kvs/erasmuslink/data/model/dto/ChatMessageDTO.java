package com.kvs.erasmuslink.data.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO class for Chat Message Entity
 *
 * @author Venislav Kirilov
 */
public class ChatMessageDTO {
    @NotNull(message = "SenderId cannot be null")
    private Integer senderId;

    @NotNull(message = "ReceiverId cannot be null")
    private Integer receiverId;

    @NotBlank(message = "Content cannot be empty")
    @Size(min = 1, max = 100, message = "content must be between 1 and 100 characters")
    private String content;

    private boolean isRead;

    public ChatMessageDTO() {}

    public ChatMessageDTO(Integer senderId, Integer receiverId, String content, boolean isRead) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.isRead = isRead;
    }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}


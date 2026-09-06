package com.kvs.erasmuslink.service.controller;

import com.kvs.erasmuslink.data.model.dto.ChatMessageDTO;
import com.kvs.erasmuslink.data.model.entity.ChatMessage;
import com.kvs.erasmuslink.service.service.ChatMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for chat message endpoints
 *
 * @author Venislav Kirilov
 */
@RestController
@RequestMapping("api/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping
    public ChatMessage sendMessage(@RequestBody @Valid ChatMessageDTO requestDTO) {
        return chatMessageService.sendMessage(requestDTO);
    }
}
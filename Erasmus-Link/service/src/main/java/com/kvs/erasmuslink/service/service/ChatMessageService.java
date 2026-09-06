package com.kvs.erasmuslink.service.service;

import com.kvs.erasmuslink.data.model.dto.ChatMessageDTO;
import com.kvs.erasmuslink.data.model.entity.ChatMessage;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.repository.ChatMessageRepository;
import com.kvs.erasmuslink.data.model.repository.UserRepository;
import com.kvs.erasmuslink.service.exception.ChatMessageException;
import org.springframework.stereotype.Service;

/**
 * Service class for chat message endpoints
 *
 * @author Venislav Kirilov
 */
@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                              UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new chat mesage entity
     *
     * @param chatMessageDTO contains the data for the entity
     * @return the entity
     */
    public ChatMessage sendMessage(ChatMessageDTO chatMessageDTO) {
        User sender = userRepository.findById(chatMessageDTO.getSenderId())
                .orElseThrow(() -> new ChatMessageException("Sender not found"));

        User receiver = userRepository.findById(chatMessageDTO.getReceiverId())
                .orElseThrow(() -> new ChatMessageException("Receiver not found"));

        ChatMessage message = new ChatMessage(
                sender,
                receiver,
                chatMessageDTO.getContent()
        );

        return chatMessageRepository.save(message);
    }
}

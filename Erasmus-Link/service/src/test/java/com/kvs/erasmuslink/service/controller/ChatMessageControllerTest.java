package com.kvs.erasmuslink.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.erasmuslink.data.model.dto.ChatMessageDTO;
import com.kvs.erasmuslink.data.model.entity.ChatMessage;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.UserType;
import com.kvs.erasmuslink.service.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatMessageController.class)
public class ChatMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatMessageService chatMessageService;

    private ObjectMapper objectMapper;
    private ChatMessage sampleChatMessage;
    private ChatMessageDTO sampleDTO;
    private User sampleUser1;
    private User sampleUser2;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        sampleDTO = new ChatMessageDTO(
                1,
                2,
                "Hi",
                false
        );

        sampleUser1 = new User(
                "John Doe",
                "johndoe",
                "john@example.com",
                UserType.PARTICIPANT,
                "hashedPassword"
        );

        sampleUser2 = new User(
                "Jane Doe",
                "janedoe",
                "jane@example.com",
                UserType.ORGANIZATION,
                "hashedPassword"
        );
        sampleChatMessage = new ChatMessage(
                sampleUser1,
                sampleUser2,
                "Hi"
        );
    }

    @Nested
    @DisplayName("POST /api/messages")
    class SendMessageTests {
        @Test
        @DisplayName("Should successfully send message with valid input")
        void sendMessage_validInput_returnsCreatedMessage() throws Exception {
            sampleUser1.setId(1);
            sampleUser2.setId(2);
            when(chatMessageService.sendMessage(any(ChatMessageDTO.class)))
                    .thenReturn(sampleChatMessage);

            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Hi"))
                    .andExpect(jsonPath("$.sender.id").value(1))
                    .andExpect(jsonPath("$.receiver.id").value(2));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when content is empty or null")
        void sendMessage_emptyOrNullContent_returnsBadRequest(String content) throws Exception {
            sampleDTO.setContent(content);

            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when content exceeds 100 characters")
        void sendMessage_contentTooLong_returnsBadRequest() throws Exception {
            String longMessage = "a".repeat(101);
            sampleDTO.setContent(longMessage);

            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when senderId is null")
        void sendMessage_nullSenderId_returnsBadRequest() throws Exception {
            sampleDTO.setSenderId(null);

            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when receiverId is null")
        void sendMessage_nullReceiverId_returnsBadRequest() throws Exception {
            sampleDTO.setReceiverId(null);

            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        @DisplayName("Should reject when content is blank")
        void sendMessage_blankContent_returnsBadRequest(String blankContent) throws Exception {
            sampleDTO.setContent(blankContent);

            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }
    }
}

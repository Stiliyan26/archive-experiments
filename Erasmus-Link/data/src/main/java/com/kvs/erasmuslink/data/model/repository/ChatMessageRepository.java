package com.kvs.erasmuslink.data.model.repository;

import com.kvs.erasmuslink.data.model.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository class for CRUD operations for Chat Message
 *
 * @author Venislav Kirilov
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {}
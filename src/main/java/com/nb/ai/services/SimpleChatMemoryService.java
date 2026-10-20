package com.nb.ai.services;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import com.nb.ai.entities.SimpleChatMemory;
import com.nb.ai.repository.SimpleChatMemoryRepository;

import jakarta.transaction.Transactional;

@Service
public class SimpleChatMemoryService {

	private final SimpleChatMemoryRepository simpleChatMemoryRepository;
	
	public SimpleChatMemoryService(SimpleChatMemoryRepository chatMemoryRepository) {
		this.simpleChatMemoryRepository = chatMemoryRepository;
	}
	
	public void addChatMessage(String conversationId, Message message) {
		simpleChatMemoryRepository.save(SimpleChatMemory.builder()
				.conversationId(conversationId)
				.messageType(message.getMessageType().getValue().toUpperCase())
				.messageContent(message.getText())
				.build());
	}
	
	public List<Message> getAllMessages(String conversationId) {
		
		List<SimpleChatMemory> chatMemoryList = simpleChatMemoryRepository.findAllByConversationIdOrderByIdAsc(conversationId);
		return chatMemoryList.stream()
			.map(cm -> createMessage(cm.getMessageType().toUpperCase(), cm.getMessageContent()))
			.toList();
	}
	
	
	private Message createMessage(String messageType, String messageContent) {
		
		MessageType mt = MessageType.valueOf(messageType);
		Message message = switch(mt) {
			case USER -> new UserMessage(messageContent);
			case SYSTEM -> new SystemMessage(messageContent);
			case ASSISTANT -> new AssistantMessage(messageContent);
			default -> null;
		};
		
		return message;
	}
	
    @Transactional
    public void deleteAllMessages(String conversationId) {
    	simpleChatMemoryRepository.deleteByConversationId(conversationId);
    }
}

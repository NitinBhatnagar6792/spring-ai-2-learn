package com.nb.ai.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nb.ai.services.SimpleChatMemoryService;

@RestController
@RequestMapping("/simple-memory")
public class SimpleChatMemoryController {

	private final ChatClient olamaChatClient;
	private final SimpleChatMemoryService chatMemoryService;

	public SimpleChatMemoryController(@Qualifier("ollamaChatClient") ChatClient olamaChatClient,
			SimpleChatMemoryService chatMemoryService) {
		this.olamaChatClient = olamaChatClient;
		this.chatMemoryService = chatMemoryService;
	}

    @GetMapping("/chat/start")
    public String startChat(@RequestParam String message) {
        
    	// Generate a new conversation id
        String conversationId = UUID.randomUUID().toString();

    	// Create and add a system message to memory
        Message systemMessage = new SystemMessage(
        		"""
        		You are a useful assistant for conversationId:%s
        		Always return conversationId in the start of the response
        		""".formatted(conversationId)
        		);
        chatMemoryService.addChatMessage(conversationId, systemMessage);

        // Create and add a user message to memory
        Message userMessage = new UserMessage(message);
        chatMemoryService.addChatMessage(conversationId, userMessage);

        String response =olamaChatClient.prompt()
            .messages(List.of(systemMessage, userMessage))
            .call()
            .content();

        // Add the assistant's response to memory
        chatMemoryService.addChatMessage(conversationId, new AssistantMessage(response));
        return response;
    }

    @GetMapping("/chat/follow-up/{conversationId}")
    public String followUpChat(@PathVariable String conversationId, @RequestParam String message) {

        chatMemoryService.addChatMessage(conversationId, new UserMessage(message));

        List<Message> messages = chatMemoryService.getAllMessages(conversationId);

        String response =olamaChatClient.prompt()
            .messages(messages)
            .call()
            .content();

        // Add the assistant's response to memory
        chatMemoryService.addChatMessage(conversationId, new AssistantMessage(response));
        return response;
    }

}

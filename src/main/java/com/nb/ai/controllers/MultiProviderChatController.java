package com.nb.ai.controllers;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nb.ai.enums.ChatProvider;

@RestController
@RequestMapping("/multi-modal-chat")

public class MultiProviderChatController {
	
	// Define the logger instance manually
	private static final Logger logger = LoggerFactory.getLogger(MultiProviderChatController.class);

	@Autowired
	@Qualifier("ollamaChatClient")
	private ChatClient ollamaChatClient;
	
	@Autowired
	@Qualifier("openAiChatClient")
	private ChatClient openAiChatClient;
	
	@GetMapping("/chat/{provider}")
	public ResponseEntity<String> basicChat(@PathVariable String provider, @RequestParam String prompt) {
		logger.info("provider {}, prompt received={}", provider, prompt);
		
		validatePrompt(prompt);

		ChatProvider chatProvider = getChatProvider(provider);
		
		String llmResponse = getLlmResponse(chatProvider, prompt);
		
		logger.info("response from {} llm = {}", chatProvider, llmResponse);
		
		return ResponseEntity.status(HttpStatus.OK).body(llmResponse);
	}

	private ChatProvider getChatProvider(String provider) {

	    if (provider == null || provider.isBlank()) {
	        throw new RuntimeException("chatProvider cannot be empty");
	    }

	    try {
	        return ChatProvider.valueOf(provider.trim().toUpperCase());
	    } catch (IllegalArgumentException e) {
	        throw new RuntimeException(
	                "Supported Chat providers are: " +
	                Arrays.toString(ChatProvider.values()));
	    }
	}	

	private String getLlmResponse(ChatProvider chatProvider, String prompt) {
		ChatClient chatClient = getChatClient(chatProvider);
		String llmResponse = chatClient
				.prompt()
				.system("You are a helpful Java programming assistant.")
				.user(prompt)
				.call()
				.content();
		return llmResponse;
	}
	
	private ChatClient getChatClient(ChatProvider chatProvider) {
		ChatClient chatClient = switch (chatProvider) {
			case OPENAI -> openAiChatClient;
			case OLLAMA -> ollamaChatClient;
			default -> ollamaChatClient;
		};
		return chatClient;
	}
	
	private void validatePrompt(String prompt) {
		if (prompt == null || prompt.isBlank()) {
			throw new RuntimeException("Prompt cannot be empty");
		}
	}
	
}

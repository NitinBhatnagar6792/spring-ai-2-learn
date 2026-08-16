package com.nb.ai.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai-chat")

public class AIChatController {
	
    // Define the logger instance manually
    private static final Logger logger = LoggerFactory.getLogger(AIChatController.class);

    private final ChatClient chatClient;
	
	public AIChatController(ChatClient.Builder chatClientBuilder) {
		chatClient = chatClientBuilder.build();
	}
	
	@GetMapping("/basic")
	ResponseEntity<?> basicChat(@RequestParam  String prompt) {
		logger.info("prompt received={}", prompt);
		String llmResponse = chatClient
				.prompt()
				.user(prompt)
				.call()
				.content();
		logger.info("response from llm = {}", llmResponse);
		return ResponseEntity.status(HttpStatus.OK).body(llmResponse);
	}
}

package com.nb.ai.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
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
	public ResponseEntity<String> basicChat(@RequestParam String prompt) {
		logger.info("prompt received={}", prompt);

		if (prompt == null || prompt.isBlank()) {
			return ResponseEntity.badRequest().body("Prompt cannot be empty");
		}

		String llmResponse = chatClient
				.prompt()
				.system("You are a helpful Java programming assistant.")
				.user(prompt)
				.call()
				.content();
		logger.info("response from llm = {}", llmResponse);
		return ResponseEntity.status(HttpStatus.OK).body(llmResponse);
	}

	@GetMapping("/basic-with-options")
	public ResponseEntity<String> basicChat2(@RequestParam String prompt) {
		logger.info("prompt received={}", prompt);

		if (prompt == null || prompt.isBlank()) {
			return ResponseEntity.badRequest().body("Prompt cannot be empty");
		}

		SystemMessage systemMessage = new SystemMessage(
				"You are a helpful assistant who is good in providing short answers with in 30 words");

		UserMessage userMessage = new UserMessage(prompt);

		ChatOptions options = ChatOptions.builder()
				.maxTokens(50)
				.temperature(.7)
				.build();

		Prompt thePrompt = new Prompt(List.of(systemMessage, userMessage), options);

		String llmResponse = chatClient
				.prompt(thePrompt)
				.call()
				.content();

		logger.info("response from llm = {}", llmResponse);
		return ResponseEntity.status(HttpStatus.OK).body(llmResponse);
	}

}

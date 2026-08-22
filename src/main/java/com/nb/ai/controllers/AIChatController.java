package com.nb.ai.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nb.ai.dto.CourseResponse;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/ai-chat")

public class AIChatController {

	@Value("${spring.ai.ollama.chat.model}")
	private String ollamaModel;
	
	// Define the logger instance manually
	private static final Logger logger = LoggerFactory.getLogger(AIChatController.class);

	private final ChatClient chatClient;

	public AIChatController(@Qualifier("ollamaChatModel") ChatModel ollamaChatModel) {
		
		this.chatClient = ChatClient.builder(ollamaChatModel).build();
	}

	@PostConstruct
	public void validateOllamaModel() {
		if (ollamaModel == null || ollamaModel.isBlank()) {
			throw new IllegalStateException("spring.ai.ollama.chat.model must be configured");
		}
		// Add your supported-model validation here
		try {
			OllamaModel theAvailableOllamaModel = OllamaModel.valueOf(ollamaModel.trim().toUpperCase().replaceAll("[.:-]", "_"));
			logger.info("theAvailableOllamaModel:{}", theAvailableOllamaModel);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Invalid Ollama model configured: " + ollamaModel, e);
		}
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
	public ResponseEntity<String> basicWithOptions(@RequestParam String prompt) {
		logger.info("prompt received={}", prompt);

		if (prompt == null || prompt.isBlank()) {
			return ResponseEntity.badRequest().body("Prompt cannot be empty");
		}

		SystemMessage systemMessage = new SystemMessage(
				"You are a helpful assistant who is good in providing short answers with in 30 words.");

		UserMessage userMessage = new UserMessage(prompt);

		//ChatOptions options = ChatOptions.builder()
		//		.maxTokens(50)
		//		.temperature(.7)
		//		.build();

		// Since we know have configured ollama model we can used options which are valid in ollama
		OllamaChatOptions options = OllamaChatOptions.builder()
				.model(ollamaModel)    // need or else it get error org.springframework.ai.retry.NonTransientAiException: HTTP 404 - {"error":"model 'mistral' not found"}
				.maxTokens(50) 		// How long can the answer be?
				.temperature(.7) 	// How random/creative should it be?
				.topK(3)			// the model considers only the top k candidates i.e. Give me the best K candidates
				.topP(.8)			// Give me enough candidates to cover P of the probability i.e. TopP controls the cumulative probability mass of candidate tokens.
				.build();

		/* How topP works if given as topP(.8)
			 Suppose the model produces:
				Token          Probability
				--------------------------
				A              0.40
				B              0.25
				C              0.15
				D              0.10
				E              0.05
				F              0.03
				G              0.02

			 We keep tokens until cumulative probability reaches approximately 0.80
				A = 0.40
				B = 0.25    → 0.65
				C = 0.15    → 0.80

			 So the candidate set becomes: A, B, C
		 */


		Prompt thePrompt = new Prompt(List.of(systemMessage, userMessage), options);

		String llmResponse = chatClient
				.prompt(thePrompt)
				.call()
				.content();

		logger.info("response from llm = {}", llmResponse);
		return ResponseEntity.status(HttpStatus.OK).body(llmResponse);
	}

	@GetMapping("/basic-using-template")
	public ResponseEntity<String> basicUsingTemplate(@RequestParam String javaTerm) {
		logger.info("java term received={}", javaTerm);

		if (javaTerm == null || javaTerm.isBlank()) {
			return ResponseEntity.badRequest().body("javaTerm cannot be empty");
		}

		SystemMessage systemMessage = new SystemMessage(
				"You are a expert java teacher who is good in providing short answers with in 30 words.");

		PromptTemplate userMessageTemplate = new PromptTemplate("Explain the Java {javaTerm} term in simple words.");

		Prompt userPrompt = userMessageTemplate.create(Map.of("javaTerm", javaTerm));
		UserMessage userMessage = userPrompt.getUserMessage();

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

	@GetMapping("/basic-with-response-converter")
	public ResponseEntity<CourseResponse> basicWithResponseConverter(@RequestParam String courseCategory) {
		logger.info("course category received={}", courseCategory);

		if (courseCategory == null || courseCategory.isBlank()) {
			throw new RuntimeException("courseCategory cannot be empty");
		}

		SystemMessage systemMessage = new SystemMessage(
				"You are an expert librarian who knows all the name of the books and courses by heart.");

		PromptTemplate userMessageTemplate = new PromptTemplate("""
		        List 5 courses related to {courseCategory}.
		        Return the response strictly in the following format:
		        {format}
		        """);
		
		BeanOutputConverter<CourseResponse> converter = new BeanOutputConverter<>(CourseResponse.class);
		String format = converter.getFormat();
		
		Prompt userPrompt = userMessageTemplate.create(Map.of("courseCategory", courseCategory, "format", format));
		UserMessage userMessage = userPrompt.getUserMessage();

		ChatOptions options = ChatOptions.builder()
				.maxTokens(1000)
				.temperature(0.3)
				.topK(5)
				.topP(.8)
				.build();

		Prompt thePrompt = new Prompt(List.of(systemMessage, userMessage), options);

		String llmResponse = chatClient
				.prompt(thePrompt)
				.call()
				.content();

		logger.info("response from llm = {}", llmResponse);
		return ResponseEntity.status(HttpStatus.OK).body(converter.convert(llmResponse));
	}
	
}

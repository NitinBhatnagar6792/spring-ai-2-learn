package com.nb.ai.controllers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai-streaming-chat")
public class AIStreamingChatController {

	// Define the logger instance manually
	private static final Logger logger = LoggerFactory.getLogger(AIStreamingChatController.class);

	private final ChatClient chatClient;
	
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	
	public AIStreamingChatController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}
	
	/**
	 * In the bash shell run below CURL command and see the output
	 *  curl -N 'http://localhost:9002/ai-streaming-chat/sse-emitter?prompt=Tell%20me%20a%20famous%20quote'
	 * @param prompt
	 * @return
	 */
	@GetMapping(value = "/sse-emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter chatWithStream(String prompt) {

		logger.info("***Start chatWithStream() prompt = {} ***", prompt);
		
		if (prompt == null || prompt.isBlank()) {
			throw new RuntimeException("Prompt cannot be empty");
		}
		
		SseEmitter emitter = new SseEmitter();
		executorService.submit(() -> {

		    // Start an LLM streaming request
		    Flux<String> responseStream = chatClient
		            .prompt()
		            .user(prompt)
		            .stream()
		            .content();
		    logger.info("Start an LLM streaming request for prompt: {}", prompt);
		    
		    // Subscribe to that stream
		    responseStream.subscribe(

		        // Every token/chunk
		        token -> {
		            try {
		    		    logger.info("received token:{}", token);
		                emitter.send(
		                    SseEmitter.event().data(token)
		                );
		            } catch (Exception e) {
		    		    logger.error("error occured while processing token:{}", token);
		                emitter.completeWithError(e);
		            }
		        },

		        // If something goes wrong
		        error -> {
	    		    logger.error("erorr occured {}", error);
		            emitter.completeWithError(error);
		        },

		        // When streaming finishes
		        () -> {
				    logger.info("LLM streaming request is completed for prompt: {}", prompt);
		            emitter.complete();
		        }
		    );
		});
		logger.info("***End chatWithStream() prompt = {} ***", prompt);
		return emitter;
	}
	
}

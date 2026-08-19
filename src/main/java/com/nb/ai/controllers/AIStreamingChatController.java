package com.nb.ai.controllers;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private static final long SSE_EMITTER_TIMEOUT = 60_000L;
	
	private final ChatClient chatClient;
	
	private ExecutorService executorService;
	
	public AIStreamingChatController(ChatClient.Builder chatClientBuilder,
			@Qualifier("aiStreamingExecutor") ExecutorService executorService) {
		this.chatClient = chatClientBuilder.build();
		this.executorService = executorService;
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
		
		SseEmitter emitter = createSseEmitter();
		logger.info("Before submit thread = {}", Thread.currentThread().getName());
		executorService.submit(() -> {
			logger.info("Inside executor thread = {}", Thread.currentThread().getName());
		    processStreaming(prompt, emitter);
		});
		logger.info("***End chatWithStream() prompt = {} ***", prompt);
		return emitter;
	}

	
	/**
	 * In the bash shell run below CURL command and see the output
	 *  curl -N 'http://localhost:9002/ai-streaming-chat/sse-emitter2?prompt=Tell%20me%20a%20famous%20quote'
	 * @param prompt
	 * @return
	 */
	@GetMapping(value = "/sse-emitter2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter chatWithStreamWithoutExecuterService(String prompt) {

		logger.info("***Start chatWithStreamWithoutExecuterService() prompt = {} ***", prompt);
		
		if (prompt == null || prompt.isBlank()) {
			throw new RuntimeException("Prompt cannot be empty");
		}
		
		SseEmitter emitter = createSseEmitter();
	    processStreaming(prompt, emitter);
		logger.info("***End chatWithStreamWithoutExecuterService() prompt = {} ***", prompt);
		return emitter;
	}

	private SseEmitter createSseEmitter() {
		
		SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
		emitter.onTimeout(() -> {
		    logger.warn("SSE connection timed out");
		    emitter.complete();
		});

		emitter.onCompletion(() -> {
		    logger.info("SSE connection completed");
		});		
		return emitter;
	}

	
	private void processStreaming(String prompt, SseEmitter emitter) {
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
				    logger.info("thread: [{}], received token: {}", Thread.currentThread().getName(), token);
		            emitter.send(
		                SseEmitter.event().data(token)
		            );
		        } catch (Exception e) {
				    logger.error("Error occurred while processing token: {}", token, e);
		            emitter.completeWithError(e);
		        }
		    },

		    // If something goes wrong
		    error -> {
			    logger.error("Error occurred while streaming response {}", error);
		        emitter.completeWithError(error);
		    },

		    // When streaming finishes
		    () -> {
			    logger.info("LLM streaming request is completed for prompt: {}", prompt);
		        emitter.complete();
		    }
		);
	}
	
}

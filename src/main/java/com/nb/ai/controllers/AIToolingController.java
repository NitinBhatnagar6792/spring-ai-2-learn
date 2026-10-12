package com.nb.ai.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nb.ai.dto.WeatherRequest;

@RestController
@RequestMapping("ai-tooling")
public class AIToolingController {
	
	@Value("${spring.ai.openai.chat.model}")
	private String openaiModel;
	
	// Define the logger instance manually
	private static final Logger logger = LoggerFactory.getLogger(AIToolingController.class);

	private final ChatClient chatClient;

	private final FunctionToolCallback<WeatherRequest, String> weatherTool;
	
	public AIToolingController(@Qualifier("openAiChatClient") ChatClient chatClient,
			FunctionToolCallback<WeatherRequest, String> weatherTool) {
		this.chatClient = chatClient;
		this.weatherTool = weatherTool;
	}
	
    @GetMapping("/weather")
    public String getWeather(@RequestParam String prompt) {
    	logger.info("getWeather called for prompt : {}", prompt);

    	OpenAiChatOptions options = OpenAiChatOptions.builder()
    			.model(openaiModel)
    			.reasoningEffort("none")
    			.build();

    	SystemMessage systemMessage = new SystemMessage(
				"""
				You are a helpful assistant who is good in providing weather forcast.
				If user does not provide the unit of temperature then assume it as C for Celsius
    			""");
		UserMessage userMessage = new UserMessage(prompt);
		Prompt thePrompt = new Prompt(List.of(systemMessage, userMessage), options);
    	logger.info(" sending prompt: {}, along with weatherTool: {}",prompt, weatherTool);
        String response =  chatClient.prompt(thePrompt)
                .tools(weatherTool)
                .call()
                .content();
        logger.info("got response from llm {}", response);
        return response;
    }

}

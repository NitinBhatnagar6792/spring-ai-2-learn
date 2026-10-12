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

import com.nb.ai.dto.ReverseStringPayload;
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
	
	private static final int MIN_RANDOM_NUMBER = 100;
	private static final int MAX_RANDOM_NUMBER = 1000;

	public AIToolingController(@Qualifier("openAiChatClient") ChatClient chatClient,
			FunctionToolCallback<WeatherRequest, String> weatherTool) {
		this.chatClient = chatClient;
		this.weatherTool = weatherTool;
	}
	
    @GetMapping("/static-tool-weather")
    public String getWeather(@RequestParam String prompt) {
    	logger.info("getWeather called for prompt : {}", prompt);

    	SystemMessage systemMessage = new SystemMessage(
				"""
				You are a helpful assistant who is good in providing weather forcast.
				If user does not provide the unit of temperature then assume it as C for Celsius
    			""");
		UserMessage userMessage = new UserMessage(prompt);
		Prompt thePrompt = new Prompt(List.of(systemMessage, userMessage), createOpenAiChatOptions());
    	logger.info(" sending prompt: {}, along with weatherTool: {}",prompt, weatherTool);
        String response =  chatClient.prompt(thePrompt)
                .tools(weatherTool)
                .call()
                .content();
        logger.info("got response from llm {}", response);
        return response;
    }
    
    @GetMapping("/dynamic-tool-creation")
    public String dynamicToolCreationExample(@RequestParam String prompt) {
    	logger.info("dynamicToolCreationExample called for prompt : {}", prompt);

    	// Here we are creating randomNumberGenerator & reverseStringTool on method call
        var randomNumberGenerator = FunctionToolCallback
                .builder("randomNumberGenerator",
                        () -> {
                        	// generate a random number between min to max including both end value.
                        	var num = (int) (Math.random() * (MAX_RANDOM_NUMBER - MIN_RANDOM_NUMBER + 1)) + MIN_RANDOM_NUMBER;
                        	logger.info("[randomNumberGenerator]: random Number Generated is {}", num);
        					return num;
                        })
                .description(String.format("Generates a random number between %s and %s", MIN_RANDOM_NUMBER, MAX_RANDOM_NUMBER ))
                .inputType(Void.class)
                .build();        
        
        
        var reverseStringTool = FunctionToolCallback
                .builder("reverseString", (ReverseStringPayload in) -> {
                	var output = new StringBuilder(in.text()).reverse().toString();
                	logger.info("[reverseString]:reversed string is {}", output);
                	return output;
                })
                .description("Reverses the input string")
                .inputType(ReverseStringPayload.class)
                .build();

    	SystemMessage systemMessage = new SystemMessage(
				"""
				You are a helpful assistant who is good in using tools below tools
				randomNumberGenerator: Generates a random number between %s and %s
				reverseString: Reverses the input string
    			""".formatted(MIN_RANDOM_NUMBER, MAX_RANDOM_NUMBER));
    	
		UserMessage userMessage = new UserMessage(prompt);
		Prompt thePrompt = new Prompt(List.of(systemMessage, userMessage), createOpenAiChatOptions());
    	logger.info(" sending prompt: {}, to LLM with tools",thePrompt);

        String response =  chatClient.prompt(thePrompt)
                .tools(randomNumberGenerator, reverseStringTool)
                .call()
                .content();
        logger.info("got response from llm {}", response);
        return response;
    	
    }

	private OpenAiChatOptions createOpenAiChatOptions() {
		return OpenAiChatOptions.builder()
    			.model(openaiModel)
    			.reasoningEffort("none")
    			.build();
	}
    
}

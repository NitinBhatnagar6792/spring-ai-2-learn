package com.nb.ai.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nb.ai.dto.WeatherRequest;
import com.nb.ai.dto.WeatherResponse;
import com.nb.ai.services.WeatherService;

@RestController
@RequestMapping("ai-tooling")
public class AIToolingController {
	
	@Value("${spring.ai.openai.chat.model}")
	private String openaiModel;

	
	// Define the logger instance manually
	private static final Logger logger = LoggerFactory.getLogger(AIToolingController.class);

	private final ChatClient chatClient;

	@Autowired
	private WeatherService weatherService;
	
	public AIToolingController(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
		
		this.chatClient = ChatClient.builder(openAiChatModel).build();
	}
	
    @GetMapping("/weather")
    public String getWeather(@RequestParam String prompt) {
    	logger.info("getWeather called for prompt : {}", prompt);
        var weatherTool = FunctionToolCallback
                .builder("getCurrentWeather", (WeatherRequest request) -> {
                    String city = request.city() != null && !request.city().isBlank()
                            ? request.city()
                            : prompt;
                    String unit = request.unit() != null && !request.unit().isBlank()
                            ? request.unit()
                            : "C";
                    try {
                    	WeatherResponse weatherResponse = weatherService.getCurrentWeather(city, unit);
                        logger.info("weatherResponse is {}", weatherResponse);
                        return "Current temperature in " + city + " is " + weatherResponse.temp() + "°" + weatherResponse.unit();
                    } catch (Exception ex) {
                    	logger.error("Error in getting weather for city {}", city, ex);
                        return "Unable to fetch weather for " + city;
                    }
                })
                .description("Get the current weather for a city. Unit can be C for Celsius or F for Fahrenheit.")
                .inputType(WeatherRequest.class)
                .build();
    	logger.info("getWeather weatherTool : {}", weatherTool);

    	OpenAiChatOptions options = OpenAiChatOptions.builder()
    			.model(openaiModel)
    			.reasoningEffort("none")
    			.build();

    	SystemMessage systemMessage = new SystemMessage(
				"You are a helpful assistant who is good in providing weather forcast.");
		UserMessage userMessage = new UserMessage(prompt);
    	
		Prompt thePrompt = new Prompt(List.of(systemMessage, userMessage), options);
    	
        return chatClient.prompt(thePrompt)
                .tools(weatherTool)
                .call()
                .content();

    }

}

package com.nb.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nb.ai.dto.WeatherRequest;
import com.nb.ai.dto.WeatherResponse;
import com.nb.ai.services.WeatherService;

@Configuration
public class ToolConfig {

	// Define the logger instance manually
	private static final Logger logger = LoggerFactory.getLogger(ToolConfig.class);

	@Bean
	FunctionToolCallback<WeatherRequest, String> weatherTool(WeatherService weatherService) {

		return FunctionToolCallback.builder("getCurrentWeather", (WeatherRequest request) -> {
			String city = request.city();
			String unit = request.unit() != null ? request.unit() : "C";
			try {
				WeatherResponse weatherResponse = weatherService.getCurrentWeather(city, unit);
				logger.info("weatherResponse is {}", weatherResponse);
				return "Current temperature in " + city + " is " + weatherResponse.temp() + "°" + weatherResponse.unit();
			} catch (Exception ex) {
            	logger.error("Error in getting weather for city {}", city, ex);
                return "Unable to fetch weather for " + city;
			}
		})
		.description("Get the current weather for a city. " + "Unit can be C for Celsius or F for Fahrenheit.")
		.inputType(WeatherRequest.class)
		.build();
	}
}


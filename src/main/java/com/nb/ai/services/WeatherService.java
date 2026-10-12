package com.nb.ai.services;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nb.ai.dto.WeatherResponse;

@Service
public class WeatherService {

	// Define the logger instance manually
	private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public WeatherResponse getCurrentWeather(String city, String unit) {
    	
    	logger.info("getCurrentWeather called for city : {}, unit : {}", city, unit);
        try {
            RestClient restClient = RestClient.create();

            // wttr.in returns JSON content but with a text/plain Content-Type header,
            // so we read as String first and parse manually with Jackson.
            String jsonBody = restClient.get()
                    .uri("https://wttr.in/{city}?format=j1", city)
                    .retrieve()
                    .body(String.class);

            logger.info("getCurrentWeather got response : {}", jsonBody);

            ObjectMapper mapper = new ObjectMapper();
            Map response = mapper.readValue(jsonBody, Map.class);

            // Parse the JSON map to get current temperature
            List<Map<String, Object>> currentConditions = (List<Map<String, Object>>) response
                    .get("current_condition");
            Map<String, Object> current = currentConditions.get(0);

            String temp = (unit != null && unit.toLowerCase().contains("f"))
                    ? (String) current.get("temp_F")
                    : (String) current.get("temp_C");

            String unitValue = (unit != null && unit.toLowerCase().contains("f")) ? "F" : "C";
            logger.info("getCurrentWeather temp : {}, unitValue: {}", temp, unitValue);
            return new WeatherResponse(temp, unitValue);

        } catch (Exception e) {
        	logger.error("Error while calling getCurrentWeather for city : {}", city, e);
            return new WeatherResponse("Unknown", unit);
        }
    
    }
    
}

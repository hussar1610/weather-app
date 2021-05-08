package pl.makselan.weatherapp.weatherclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestWeatherClient {

    @Value("${api.key}")
    private String API_KEY;

    private final String GET_WEATHER_URI =
            "http://api.weatherstack.com/current?access_key="
            + API_KEY + "&query=";

    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    public RestWeatherClient(RestTemplateBuilder restTemplateBuilder) {
         restTemplate = restTemplateBuilder.build();
         this.objectMapper = new ObjectMapper();
    }

    public String getWeather(String locationQuery)  {
        Weather weather = restTemplate
                .getForObject(GET_WEATHER_URI+locationQuery, Weather.class);

        String weatherJson = null;
        try {
            weatherJson = objectMapper.writeValueAsString(weather);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return  weatherJson;
    }

    public String getWeather(List<String> locations){
        List<Weather> locationsWeather = new ArrayList<>();
        locations
                .forEach( (location) ->
                        locationsWeather.add(restTemplate.getForObject(GET_WEATHER_URI + location, Weather.class))
                );
        String locationsWeatherJson = null;
        try{
            locationsWeatherJson = objectMapper.writeValueAsString(locationsWeather);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return locationsWeatherJson;
    }

}

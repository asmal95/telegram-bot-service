package ru.javazen.telegram.bot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import ru.javazen.telegram.bot.longpolling.LongPollingUpdateProvider;
import ru.javazen.telegram.bot.TelegramService;
import ru.javazen.telegram.bot.TelegramServiceImpl;
import ru.javazen.telegram.bot.UpdateProvider;

@Configuration
@PropertySource("classpath:telegram.properties")
public class ServiceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfig.class);

    @Bean
    public TelegramService telegramService() {
        return new TelegramServiceImpl();
    }

    @Bean
    public UpdateProvider updateProvider() {
        return new LongPollingUpdateProvider();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
        restTemplate.getMessageConverters().add(tgServiceHttpMessageConverter());

        return restTemplate;
    }

    @Bean
    public AsyncRestTemplate asyncRestTemplate() {
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        asyncRestTemplate.getMessageConverters().removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
        asyncRestTemplate.getMessageConverters().add(tgServiceHttpMessageConverter());

        return asyncRestTemplate;
    }

    @Bean
    public MappingJackson2HttpMessageConverter tgServiceHttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(tgServiceObjectMapper());

        return converter;
    }

    @Bean
    public ObjectMapper tgServiceObjectMapper() {
        ObjectMapper objectMapper;
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}

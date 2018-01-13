package ru.javazen.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.javazen.telegram.bot.method.ApiMethod;

public class TelegramServiceImpl implements TelegramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramServiceImpl.class);

    @Value("${telegram.url}")
    private String telegramUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public <T> T executeMethod(ApiMethod apiMethod, String token, Class<T> clazz) {
        LOGGER.debug("Start executing {} method. Send the following entity: {}", apiMethod.getMethod(), apiMethod);

        HttpEntity<? extends ApiMethod> entity = new HttpEntity<>(apiMethod);

        ResponseEntity<T> result = restTemplate.exchange(
                telegramUrl + "/" + "bot" + token + "/" + apiMethod.getMethod(),
                HttpMethod.POST,
                entity,
                clazz);

        LOGGER.debug("returned value: " + result.toString());
        return result.getBody();
    }

    @Override
    public void executeMethod(ApiMethod apiMethod, String token) {
        executeMethod(apiMethod, token, Void.class);
    }

}
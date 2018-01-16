package ru.javazen.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.javazen.telegram.bot.method.ApiMethod;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TelegramServiceImpl implements TelegramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramServiceImpl.class);

    @Value("${telegram.url}")
    private String telegramUrl;

    @Autowired
    private RestTemplate restTemplate;

    private <T> TelegramResponse<T> exchangeAsTelegramResponse(String url,  HttpEntity<? extends ApiMethod> entity, ParameterizedTypeReference<TelegramResponse<T>> responseType) {
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType).getBody();
    }

    @Override
    public <T> T executeMethod(ApiMethod apiMethod, String token, Class<T> clazz) {
        LOGGER.debug("Start executing {} method. Send the following entity: {}", apiMethod.getMethod(), apiMethod);

        HttpEntity<? extends ApiMethod> entity = new HttpEntity<>(apiMethod);

        ResponseEntity<TelegramResponse<T>> result = restTemplate.exchange(
                telegramUrl + "/" + "bot" + token + "/" + apiMethod.getMethod(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<TelegramResponse<T>>() {
                    @Override
                    public Type getType() {
                        Type [] responseWrapperActualTypes = {clazz};
                        ParameterizedType responseWrapperType = new ParameterizedType() {
                            @Override
                            public Type[] getActualTypeArguments() {
                                return responseWrapperActualTypes;
                            }

                            @Override
                            public Type getRawType() {
                                return TelegramResponse.class;
                            }

                            @Override
                            public Type getOwnerType() {
                                return null;
                            }
                        };
                        return responseWrapperType;
                    }
                });



        TelegramResponse<T> telegramResponse = result.getBody();

        LOGGER.debug("returned value: " + telegramResponse.toString());
        return telegramResponse.getResult();
    }

    @Override
    public void executeMethod(ApiMethod apiMethod, String token) {
        executeMethod(apiMethod, token, Void.class);
    }

}
package ru.javazen.telegram.bot.longpolling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import ru.javazen.telegram.bot.TelegramBot;
import ru.javazen.telegram.bot.UpdateProvider;
import ru.javazen.telegram.bot.entity.Update;
import ru.javazen.telegram.bot.method.ApiMethod;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class LongPollingUpdateProvider implements UpdateProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LongPollingUpdateProvider.class);

    @Value("${telegram.url}")
    private String telegramUrl;

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    private List<TelegramBot> telegramBots = new ArrayList<>();
    private volatile boolean isServiceRun;

    public void addBot(TelegramBot telegramBot) {
        telegramBots.add(telegramBot);
        startLongPollingForBot(telegramBot);
    }

    @PostConstruct
    public void start() {
        telegramBots.forEach(this::startLongPollingForBot);
        isServiceRun = true;
    }

    @PreDestroy
    public void stop() {
        isServiceRun = false;
    }

    private void startLongPollingForBot(TelegramBot bot) {
        System.out.println("Registered bot: " + bot.getName());
        GetUpdates getUpdates = new GetUpdates();
        getUpdates.setTimeout(30);
        ListenableFuture<ResponseEntity<LongPollingResponse>> future =
                executeMethodAsync(getUpdates, bot.getToken(), LongPollingResponse.class);

        ListenableFutureCallback<ResponseEntity<LongPollingResponse>> callback = new LongPollingUpdateHandler(bot);
        future.addCallback(callback);
    }

    private class LongPollingUpdateHandler implements ListenableFutureCallback<ResponseEntity<LongPollingResponse>> {

        public LongPollingUpdateHandler(TelegramBot telegramBot) {
            this.telegramBot = telegramBot;
        }

        private TelegramBot telegramBot;
        private int maxUpdate;

        private int failureCount;

        private static final int LONG_PULLING_TIMEOUT = 30;
        private static final int MAX_FAILURE_ATTEMPTS = 5;
        private static final int RETRY_INTERVAL_MS = 10 * 1000 * 60;

        @Override
        public void onSuccess(ResponseEntity<LongPollingResponse> responseEntity) {
            LOGGER.debug("LongPolling success executed.");
            failureCount = 0;

            LongPollingResponse response = responseEntity.getBody();
            LOGGER.debug("Is response ok: {}", response.getOk());

            if (response.getOk()) {
                List<Update> updates = response.getResult();

                LOGGER.debug("Updates count: {}", updates.size());
                for (Update update : updates) {
                    telegramBot.handleUpdate(update);
                    if (update.getUpdateId() > maxUpdate) {
                        maxUpdate = update.getUpdateId();
                    }
                }
            }

            getUpdates();
        }

        @Override
        public void onFailure(Throwable throwable) {
            LOGGER.error("Error during execute async method for get update", throwable);
            if (failureCount++ < MAX_FAILURE_ATTEMPTS) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    LOGGER.debug("Sleeping was interrupted", e);
                }
                getUpdates();
            }
            if (failureCount >= MAX_FAILURE_ATTEMPTS) {
                LOGGER.debug("Failure count: {}. Will create retry task", failureCount);
                TimerTask retryTask = new TimerTask() {
                    @Override
                    public void run() {
                        LOGGER.debug("Retry task was called");
                        getUpdates();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(retryTask, RETRY_INTERVAL_MS);
            }
        }

        private void getUpdates() {
            if (isServiceRun) {
                GetUpdates getUpdates = new GetUpdates();
                getUpdates.setOffset(maxUpdate + 1);
                getUpdates.setTimeout(LONG_PULLING_TIMEOUT);

                ListenableFuture<ResponseEntity<LongPollingResponse>> future =
                        executeMethodAsync(getUpdates, telegramBot.getToken(), LongPollingResponse.class);

                future.addCallback(this);
            }
        }
    }

    private <T> ListenableFuture<ResponseEntity<T>> executeMethodAsync(ApiMethod apiMethod, String token, Class<T> clazz) {

        HttpEntity<? extends ApiMethod> entity = new HttpEntity<>(apiMethod);

        return asyncRestTemplate
                .exchange(
                        telegramUrl + "/" + "bot" + token + "/" + apiMethod.getMethod(),
                        HttpMethod.POST,
                        entity,
                        clazz);
    }

}

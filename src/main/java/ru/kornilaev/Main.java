package ru.kornilaev;

import ru.kornilaev.service.MailingService;
import ru.kornilaev.service.QuotesBotService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi  = new TelegramBotsApi(DefaultBotSession.class);
        QuotesBotService bot = new QuotesBotService();
        botsApi.registerBot(bot);
        ScheduledExecutorService schduler = Executors.newSingleThreadScheduledExecutor();
        schduler.scheduleAtFixedRate(new MailingService(bot), 0, 24, TimeUnit.HOURS);
    }
}
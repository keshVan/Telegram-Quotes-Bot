package ru.kornilaev.service;

public class MailingService implements Runnable {
    private final QuotesBotService bot;

    public MailingService(QuotesBotService bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        bot.sendMessages();
    }
}

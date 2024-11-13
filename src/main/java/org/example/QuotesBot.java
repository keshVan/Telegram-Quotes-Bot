package org.example;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;

public class QuotesBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final Database database;
    private final InlineKeyboardMarkup addToFavoritesButton;
    private final InlineKeyboardMarkup removeFromFavoritesButton;

    public QuotesBot() {
        botConfig = new BotConfig();
        try {
            database = new Database();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        String str1 = ":star: В избранное";
        var addToFavorites = InlineKeyboardButton
                .builder()
                .text(EmojiParser.parseToUnicode(str1))
                .callbackData("add_to_favorites")
                .build();
        addToFavoritesButton = InlineKeyboardMarkup
                .builder()
                .keyboardRow(List.of(addToFavorites))
                .build();


        String str2 = ":x: Удалить из избранного";
        var removeFromFavorites = InlineKeyboardButton
                .builder()
                .text(EmojiParser.parseToUnicode(str2))
                .callbackData("remove_from_favorites")
                .build();
        removeFromFavoritesButton = InlineKeyboardMarkup
                .builder()
                .keyboardRow(List.of(removeFromFavorites))
                .build();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            try {
                sendMessage(chatId, QuoteService.getRandomQuote(), addToFavoritesButton);
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callData) {
                case "add_to_favorites":
                    editMessage(chatId, messageId, removeFromFavoritesButton);
                    break;
                case "remove_from_favorites":
                    editMessage(chatId, messageId, addToFavoritesButton);
                    break;
            }

        }
    }

    private void sendMessage(Long chatId, String msgToSend, InlineKeyboardMarkup button) {
        SendMessage sm = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(msgToSend)
                .replyMarkup(button)
                .build();

        try {
            execute(sm);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void editMessage(Long chatId, int messageId, InlineKeyboardMarkup button) {
        EditMessageReplyMarkup newKb = EditMessageReplyMarkup
                .builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .build();
        newKb.setReplyMarkup(button);

        try {
            execute(newKb);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

}

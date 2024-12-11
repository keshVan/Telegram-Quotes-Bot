package ru.kornilaev.service;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kornilaev.config.BotConfig;
import ru.kornilaev.database.Database;
import ru.kornilaev.enums.ButtonText;
import ru.kornilaev.enums.CallbackData;

import java.sql.SQLException;
import java.util.List;


public class QuotesBotService extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final Database database;

    private final InlineKeyboardMarkup addToFavoritesButton;
    private final InlineKeyboardMarkup removeFromFavoritesButton;
    private final InlineKeyboardMarkup favoritesQuoteRemove;
    private final InlineKeyboardMarkup firstFavoritesQuoteRemove;
    private final InlineKeyboardMarkup lastFavoritesQuoteRemove;
    private final InlineKeyboardMarkup favoritesQuoteAdd;
    private final InlineKeyboardMarkup firstFavoritesQuoteAdd;
    private final InlineKeyboardMarkup lastFavoritesQuoteAdd;

    public QuotesBotService() {
        botConfig = new BotConfig();
        try {
            database = new Database();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        var addToFavorites = InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(ButtonText.ADD_IN_FAVORITES.value()))
                .callbackData(CallbackData.ADD.name())
                .build();

        addToFavoritesButton = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(addToFavorites))
                .build();

        var removeFromFavorites = InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(ButtonText.REMOVE_FROM_FAVORITES.value()))
                .callbackData(CallbackData.REMOVE.name())
                .build();

        removeFromFavoritesButton = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(removeFromFavorites))
                .build();

        var prevButton = InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(ButtonText.PREVIOUS.value()))
                .callbackData(CallbackData.PREV_QUOTE.name())
                .build();

        var nextButton = InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(ButtonText.NEXT.value()))
                .callbackData(CallbackData.NEXT_QUOTE.name())
                .build();

        var starButton = InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(ButtonText.STAR.value()))
                .callbackData(CallbackData.ADD_FROM_MENU.name())
                .build();

        var xButton = InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(ButtonText.X_MARK.value()))
                .callbackData(CallbackData.REMOVE_FROM_MENU.name())
                .build();

        favoritesQuoteRemove = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(prevButton, xButton, nextButton))
                .build();

        firstFavoritesQuoteRemove = InlineKeyboardMarkup.builder().
                keyboardRow(List.of(xButton, nextButton))
                .build();

        lastFavoritesQuoteRemove = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(prevButton, xButton))
                .build();

        favoritesQuoteAdd = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(prevButton, starButton, nextButton))
                .build();

        firstFavoritesQuoteAdd = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(starButton, nextButton))
                .build();

        lastFavoritesQuoteAdd = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(prevButton, starButton))
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
            switch (update.getMessage().getText()) {
                case "/start":
                    database.addUser(update.getMessage().getFrom().getId());
                    break;
                case "/quote":
                    sendMessage(chatId, QuoteService.getRandomQuote(), addToFavoritesButton);
                    break;
                case "/favorites":
                    sendFavorites(chatId, update.getMessage().getFrom().getId());
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long userId = update.getCallbackQuery().getFrom().getId();
            String[] message = update.getCallbackQuery().getMessage().getText().split("\n");

            switch (callData) {
                case "add":
                    editKeyboard(chatId, messageId, removeFromFavoritesButton);
                    database.addFavoriteQuote(userId, message[0], message[2]);
                    break;
                case "remove":
                    editKeyboard(chatId, messageId, addToFavoritesButton);
                    database.removeFromFavorites(userId, message[0], message[2]);
                    break;
                case "next_quote":
                    editFavoriteMenu(chatId, messageId, userId, callData);
                    break;
                case "prev_quote":
                    editFavoriteMenu(chatId, messageId, userId, callData);
                    break;
                case "remove_in_menu":
                    editMenuKeyboard(chatId, messageId, userId, callData);
                    database.removeFromFavorites(userId, message[0], message[2]);
                    break;
                case "add_in_menu":
                    editMenuKeyboard(chatId, messageId, userId, callData);
                    database.addFavoriteQuote(userId, message[0], message[2]);
                    break;
            }

        }
    }

    public void sendMessages() {
        List<Long> usersId = database.getUsersId();
        for (Long id : usersId) {
            sendMessage(id, QuoteService.getRandomQuote(), removeFromFavoritesButton);
        }
    }

    private void sendMessage(Long chatId, String msgToSend, InlineKeyboardMarkup button) {
        SendMessage sm = SendMessage.builder().chatId(chatId.toString()).text(msgToSend).replyMarkup(button).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendFavorites(Long chatId, Long userId) {
        int currIndex = database.currentIndex(userId);
        int quotesCount = database.getFavoriteQuotesCount(userId);

        if (quotesCount == 0) {
            sendMessage(chatId, "У вас нет избранных цитат.", null);
            return;
        }

        if (quotesCount == 1) {
            sendMessage(chatId, database.getFavoriteQuote(userId, 0), removeFromFavoritesButton);
            return;
        }

        if (currIndex == 0) {
            sendMessage(chatId, database.getFavoriteQuote(userId, currIndex), firstFavoritesQuoteRemove);
        } else if (currIndex == quotesCount - 1) {
            sendMessage(chatId, database.getFavoriteQuote(userId, currIndex), lastFavoritesQuoteRemove);
        } else {
            sendMessage(chatId, database.getFavoriteQuote(userId, currIndex), favoritesQuoteRemove);
        }
    }

    private void editKeyboard(Long chatId, int messageId, InlineKeyboardMarkup button) {
        EditMessageReplyMarkup newKb = EditMessageReplyMarkup.builder().chatId(chatId.toString()).messageId(messageId).build();
        newKb.setReplyMarkup(button);

        try {
            execute(newKb);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void editMenuKeyboard(Long chatId, int messageId, long userId, String data) {
        int currIndex = database.currentIndex(userId);
        int quotesCount = database.getFavoriteQuotesCount(userId);

        if (quotesCount == 1) {
            editKeyboard(chatId, messageId, removeFromFavoritesButton);
            return;
        }

        if (currIndex == 0) {
            editKeyboard(chatId, messageId, data.equals("add_in_menu") ? firstFavoritesQuoteRemove : firstFavoritesQuoteAdd);
        } else if (currIndex == quotesCount - 1) {
            editKeyboard(chatId, messageId, data.equals("add_in_menu") ? lastFavoritesQuoteRemove : lastFavoritesQuoteAdd);
        } else {
            editKeyboard(chatId, messageId, data.equals("add_in_menu") ? favoritesQuoteRemove : favoritesQuoteAdd);
        }


    }

    private void editFavoriteMenu(Long chatId, int messageId, long userId, String data) {
        List<String> favorites = database.getFavoriteQuotes(userId);
        int currIndex = database.currentIndex(userId);
        int quotesCount = database.getFavoriteQuotesCount(userId);

        EditMessageText newTxt = EditMessageText.builder().chatId(chatId.toString()).messageId(messageId).text("").build();

        EditMessageReplyMarkup newKb = EditMessageReplyMarkup.builder().chatId(chatId.toString()).messageId(messageId).build();

        switch (data) {
            case "next_quote":
                database.updateIndex(userId, currIndex + 1);
                newTxt.setText(database.getFavoriteQuote(userId, currIndex + 1));

                if (currIndex + 1 < quotesCount - 1) {
                    newKb.setReplyMarkup(favoritesQuoteRemove);
                } else if (currIndex + 1 == quotesCount - 1) {
                    newKb.setReplyMarkup(lastFavoritesQuoteRemove);
                }
                break;

            case "prev_quote":
                database.updateIndex(userId, currIndex - 1);
                newTxt.setText(database.getFavoriteQuote(userId, currIndex - 1));
                if (currIndex - 1 == 0) {
                    newKb.setReplyMarkup(firstFavoritesQuoteRemove);
                } else {
                    newKb.setReplyMarkup(favoritesQuoteRemove);
                }
                break;
        }

        try {
            execute(newTxt);
            execute(newKb);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
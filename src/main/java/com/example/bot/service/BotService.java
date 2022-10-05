package com.example.bot.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public interface BotService {
    default Long getUserChatId(Update update) {
        if (update.hasMessage()) return update.getMessage().getChatId();
        return update.getCallbackQuery().getMessage().getChatId();
    }

    default String getUserResponse(Update update) {
        if (update.hasMessage()) if (update.getMessage().hasText()) return update.getMessage().getText();
        else return "Nothing not found!";
        return update.getCallbackQuery().getData();
    }

    default ReplyKeyboardMarkup mainMenu() {
        return createMarkupButtons(List.of("Main menu"));
    }

    default InlineKeyboardMarkup checkPermission() {
        return createInlineKeyboardButton(List.of("Using bot"), 1);
    }

    default InlineKeyboardMarkup superUser() {
        return createInlineKeyboardButton(List.of("Manage bot as \nadmin"), 1);
    }

    default InlineKeyboardMarkup chooseType() {
        return createInlineKeyboardButton(List.of("Find by phone", "Find by passport"), 2);
    }

    default InlineKeyboardMarkup forSuperUsers() {
        return createInlineKeyboardButton(List.of("Find by phone", "Find by passport", "Add user", "Delete user", "List of users"), 2);
    }

    public default InlineKeyboardMarkup createInlineKeyboardButton(List<String> buttons, int column) {
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        int rowCount = column;
        for (int i = 0; i < buttons.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttons.get(i));
            button.setCallbackData(buttons.get(i));
            buttonRow.add(button);
            rowCount--;
            if ((rowCount == 0 || i == buttons.size() - 1)) {
                rowList.add(buttonRow);
                buttonRow = new ArrayList<>();
                rowCount = column;
            }
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    default ReplyKeyboardMarkup createMarkupButtons(List<String> rows) {
        ReplyKeyboardMarkup replyKeyboardMarkup = makeReplyMarkup();
        KeyboardRow keyboardRow = new KeyboardRow();
        List<KeyboardRow> rowList = new ArrayList<>();
        int rowcount = 2;
        for (int i = 0; i < rows.size(); i++) {
            keyboardRow.add(rows.get(i));
            rowcount--;
            if (rowcount == 0 || i == rows.size() - 1) {
                rowList.add(keyboardRow);
                keyboardRow = new KeyboardRow();
                rowcount = 2;
            }
        }
        replyKeyboardMarkup.setKeyboard(rowList);
        return replyKeyboardMarkup;
    }

    default ReplyKeyboardMarkup makeReplyMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        return replyKeyboardMarkup;
    }
}


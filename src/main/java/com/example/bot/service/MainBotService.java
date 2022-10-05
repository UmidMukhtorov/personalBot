package com.example.bot.service;


import com.example.bot.domain.Phones;
import com.example.bot.domain.User;
import com.example.bot.repository.PhonesRepository;
import com.example.bot.repository.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Service
public class MainBotService extends TelegramLongPollingBot implements BotService {
    @Value("${telegram.bot.username}")
    private String username;
    @Value("${telegram.bot.token}")
    private String token;
    @Value("${telegram.group.chatId}")
    private String groupChatId;
    private Long userChatId;
    private String userMessage;
    private final UserService userService;
    private final static Map<Long, String> round = new HashMap<>();
    private final static Map<Long, String> id = new HashMap<>();
    private final static Map<Long, String> name = new HashMap<>();
    private final UserRepository userRepository;
    private final PhonesRepository repository;

    public MainBotService(UserService userService, UserRepository userRepository, PhonesRepository repository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.repository = repository;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        userChatId = getUserChatId(update);
        String inputText = getUserResponse(update);
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                if (inputText.equals("Main menu")) {
                    if (update.getMessage().getFrom().getId() == 595139811
                            || update.getMessage().getFrom().getId() == 77412239) {
                        userMessage = "Choose one";
                        execute(null, forSuperUsers());
                    } else {
                        if (userService.activeUser(update)) {
                            round.remove(userChatId);
                            userMessage = "Choose one";
                            execute(null, chooseType());
                        }
                    }
                }
                if (inputText.equals("/start")) {
                    userService.saveUser(update);
                    round.remove(userChatId);
                    if (update.getMessage().getFrom().getId() == 595139811
                            || update.getMessage().getFrom().getId() == 77412239) {
                        userMessage = "You are running the bot as an admin";
                        execute(null, superUser());
                    } else {
                        userMessage = "Do you have the right to use ?";
                        execute(null, checkPermission());
                    }

                }
                if (round.size() > 0) {
                    if (round.get(userChatId).equals("phone") && isNumeric(update.getMessage().getText())) {
                        Optional<Phones> phones = repository.findByPhone(Integer.valueOf(update.getMessage().getText()));
                        if (phones.isEmpty()) {
                            userMessage = "User not found";
                            round.remove(userChatId);
                            execute(mainMenu(), null);
                        } else {
                            int phone = phones.get().getPhone();
                            Date birthDate = phones.get().getBirth_date();
                            Date connected = phones.get().getConnected();
                            String passport = phones.get().getPassport();
                            String fullData = phones.get().getFull_data();
                            userMessage = "Phone: " + phone + "\nBirth date: " + birthDate + "\nConnected date: " + connected
                                    + "\nPassport data: " +"<code>"+ passport+"</code>"+ "\nFull data: " + fullData;
                            round.remove(userChatId);
                            execute(mainMenu(), null);
                            String userName = userService.getUserName(userChatId);
                            executeGroup(userName + "\n\n" + userMessage);
                        }
                    } else {
                        if (round.get(userChatId).equals("phone")) {
                            round.remove(userChatId);
                            userMessage = "Incorrect information entered";
                            execute(mainMenu(), null);
                        }
                    }

                }
                if (round.size() > 0) {
                    if (round.get(userChatId).equals("addUser")) {
                        if (id.isEmpty()) {
                            id.put(userChatId, update.getMessage().getText());
                            userMessage = "Enter name";
                            execute1();
                        } else {
                            name.put(userChatId, update.getMessage().getText());
                            userService.saveNewUser(Long.valueOf(id.get(userChatId)), name.get(userChatId));
                            userMessage = "User successfully added";
                            execute(null, forSuperUsers());
                            round.remove(userChatId);
                            id.remove(userChatId);
                            name.remove(userChatId);
                        }
                    }
                }
                if (round.size() > 0) {
                    if (round.get(userChatId).equals("deleteUser")) {
                        userService.deleteUser(Long.valueOf(update.getMessage().getText()));
                        userMessage = "User successfully deleted";
                        execute(null, forSuperUsers());
                        round.remove(userChatId);
                    }
                }
                if (round.size() > 0) {
                    if (round.get(userChatId).equals("passport")) {
                        List<Phones> byPassport = repository.findByPassport(update.getMessage().getText());
                        if (byPassport.isEmpty()) {
                            userMessage = "User not found";
                        } else {
                            for (Phones value : byPassport) {
                                int phone = value.getPhone();
                                Date birthDate = value.getBirth_date();
                                Date connected = value.getConnected();
                                String passport = value.getPassport();
                                String fullData = value.getFull_data();
                                userMessage = userMessage + "\n\nPhone: " + phone + "\nBirth date: " + birthDate + "\nConnected date: " + connected
                                        + "\nPassport data: " + passport + "\nFull data: " + fullData;
                            }
                        }
                        round.remove(userChatId);
                        String userName = userService.getUserName(userChatId);
                        executeGroup(userName + "\n\n" + userMessage);
                        execute(mainMenu(), null);
                    }
                }
            }
        }
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("Manage bot as \nadmin")) {
                userService.saveActiveUser(update);
                executeDelete(update);
                userMessage = "Welcome bro!";
                execute(null, forSuperUsers());
            }
            if (update.getCallbackQuery().getData().equals("Using bot")) {
                if (userService.activeUser(update)) {
                    executeDelete(update);
                    userMessage = "Welcome !";
                    execute(null, chooseType());
                } else {
                    userMessage = "You cannot use a bot";
                    execute1();
                }
            }
            if (update.getCallbackQuery().getData().equals("Add user")) {
                executeDelete(update);
                userMessage = "Enter the new user id number";
                execute(mainMenu(), null);
                round.put(userChatId, "addUser");
            }
            if (update.getCallbackQuery().getData().equals("Delete user")) {
                executeDelete(update);
                userMessage = "Enter the user id number";
                execute(mainMenu(), null);
                round.put(userChatId, "deleteUser");
            }
            if (update.getCallbackQuery().getData().equals("Find by phone")) {
                executeDelete(update);
                userMessage = "Enter phone number\nExample: 901234567";
                execute(mainMenu(), null);
                round.put(userChatId, "phone");
            }
            if (update.getCallbackQuery().getData().equals("Find by passport")) {
                executeDelete(update);
                userMessage = "Enter passport data\nExample: AA1234567";
                execute(mainMenu(), null);
                round.put(userChatId, "passport");
            }
            if (update.getCallbackQuery().getData().equals("List of users")) {
                List<User> users = userRepository.findAll();
                executeDelete(update);
                userMessage = "List of users";
                for (User user : users) {
                    Long chatId = user.getChatId();
                    String name1 = user.getName();
                    String username1 = user.getUsername();
                    boolean activate = user.isActive();
                    userMessage = userMessage + "\n\nFirst&Last name: " + name1 + "\nUsername: " +
                            username1 + "\nUser id: " + chatId + "\nActivate: " + activate;
                }
                execute(mainMenu(), null);
            }
        }

    }

    private void executeGroup(String text) {
        SendMessage sendMessage;
        sendMessage = new SendMessage(groupChatId, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void execute1() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(this.userChatId));
        sendMessage.setText(this.userMessage);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void executeDelete(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(update.getCallbackQuery().getFrom().getId()));
        deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void execute(ReplyKeyboardMarkup replyKeyboardMarkup, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(this.userChatId));
        sendMessage.setText(this.userMessage);
        sendMessage.enableHtml(true);
        sendMessage.setParseMode(ParseMode.HTML);
        if (replyKeyboardMarkup != null)
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        else if (inlineKeyboardMarkup != null)
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return this.username;
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

}

package com.example.bot.service;

import com.example.bot.domain.User;
import com.example.bot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveActiveUser(Update update) {
        Long chatId = update.getCallbackQuery().getFrom().getId();
        Optional<User> users = userRepository.findByChatId(chatId);
        if (users.isPresent()) {
            User user = users.get();
            user.setActive(true);
            userRepository.save(user);
        }
    }

    public boolean activeUser(Update update) {
        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getFrom().getId();
            Optional<User> users = userRepository.findByChatId(chatId);
            User user = users.get();
            return user.isActive();
        } else {
            Long chatId = update.getMessage().getFrom().getId();
            Optional<User> users = userRepository.findByChatId(chatId);
            User user = users.get();
            return user.isActive();
        }
    }


    public void saveUser(Update update) {
        Long chatId = update.getMessage().getFrom().getId();
        Optional<User> users = userRepository.findByChatId(chatId);
        User user = new User();
        if (users.isEmpty()) {
            user.setChatId(chatId);
            user.setUsername(update.getMessage().getFrom().getUserName());
            user.setName(update.getMessage().getFrom().getFirstName());
            userRepository.save(user);
        }

    }

    public void saveNewUser(Long id, String name) {
        Optional<User> users = userRepository.findByChatId(id);
        if (users.isPresent()) {
            User user = users.get();
            user.setChatId(id);
            user.setName(name);
            user.setActive(true);
            userRepository.save(user);
        }
        if (users.isEmpty()) {
            User user = new User();
            user.setName(name);
            user.setChatId(id);
            user.setActive(true);
            userRepository.save(user);

        }
    }

    public void deleteUser(Long id) {
        Optional<User> users = userRepository.findByChatId(id);
        if (users.isPresent()) {
            User user = users.get();
            user.setActive(false);
            userRepository.save(user);
        }
    }

    public String getUserName(Long id) {
        Optional<User> user = userRepository.findByChatId(id);
        return user.get().getName();
    }

    public Optional<User> findUserByChatId(Update update) {
        return userRepository.findByChatId(update.getMessage().getChatId());
    }

}

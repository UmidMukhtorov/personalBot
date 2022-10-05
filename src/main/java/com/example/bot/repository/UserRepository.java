package com.example.bot.repository;

import com.example.bot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByChatId(Long chatId);

    Optional<User> deleteByChatId(Long chatId);

    List<User> findAll();


}

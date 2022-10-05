package com.example.bot.repository;

import com.example.bot.domain.Phones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PhonesRepository extends JpaRepository<Phones, Integer> {
    Optional<Phones> findByPhone(Integer phone);

    List<Phones> findByPassport(String phone);

}

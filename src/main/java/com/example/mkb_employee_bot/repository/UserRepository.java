package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserChatId(Long userChatId);

    boolean existsByUserChatId(Long userChatId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET phone_number = :newPhoneNumber, updated_At = CURRENT_TIMESTAMP WHERE users.user_chat_id = :userId", nativeQuery = true)
    void updatePhoneNumberByUserId(Long userId, String newPhoneNumber);


    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET language = :language, updated_at = CURRENT_TIMESTAMP WHERE user_chat_id = :userId", nativeQuery = true)
    void updateLanguageByUserId(Long userId, String language);

}
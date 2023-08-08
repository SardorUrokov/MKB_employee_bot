package com.example.mkb_employee_bot.repository;

import java.util.Optional;

import com.example.mkb_employee_bot.entiry.User;
import com.example.mkb_employee_bot.entiry.enums.Language;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

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

    @Query(value = "select language from users where user_chat_id = :userChatId", nativeQuery = true)
    String getUserLanguageByUserChatId(Long userChatId);

    @Query(value = "select role from users where user_chat_id = :userChatId", nativeQuery = true)
    String getUserRoleByUserChatId (Long userChatId);
}
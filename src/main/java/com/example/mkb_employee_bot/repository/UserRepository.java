package com.example.mkb_employee_bot.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.example.mkb_employee_bot.entity.User;
import com.example.mkb_employee_bot.entity.enums.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserChatId(Long userChatId);

    @Query(value = "SELECT u FROM users u WHERE u.role = 'ADMIN' OR u.role = 'SUPER_ADMIN'"
//            , nativeQuery = true
    )
    List<User> getAdminList();

    boolean existsByUserChatId(Long userChatId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET phone_number = :newPhoneNumber, updated_At = CURRENT_TIMESTAMP WHERE users.user_chat_id = :userId", nativeQuery = true)
    void updatePhoneNumberByUserId(Long userId, String newPhoneNumber);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET language = :language, updated_at = CURRENT_TIMESTAMP WHERE user_chat_id = :userId", nativeQuery = true)
    void updateLanguageByUserId(Long userId, String language);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET role = :role, updated_at = CURRENT_TIMESTAMP WHERE phone_number = :phoneNumber", nativeQuery = true)
    void updateRoleToUSERByPhoneNumber(String role, String phoneNumber);

    @Query(value = "select language from users where user_chat_id = :userChatId", nativeQuery = true)
    String getUserLanguageByUserChatId(Long userChatId);

    @Query(value = "select role from users where user_chat_id = :userChatId", nativeQuery = true)
    String getUserRoleByUserChatId (Long userChatId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET stage = :newStage, updated_At = CURRENT_TIMESTAMP WHERE users.user_chat_id = :userChatId", nativeQuery = true)
    void updateUserStageByUserChatId (Long userChatId, String newStage);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET step = :step, updated_At = CURRENT_TIMESTAMP WHERE users.user_chat_id = :chatId", nativeQuery = true)
    void updateUserStepByUserChatId(Long chatId, String step);

    @Query(value = "select stage from users where user_chat_id = :userChatId", nativeQuery = true)
    String getUserStageByUserChatId (Long userChatId);

    @Query(value = "select step from users where user_chat_id = :userChatId", nativeQuery = true)
    String getUserStepByUserChatId (Long userChatId);

    Optional<User> findByPhoneNumber(String phoneNumber);

    List<User> findAllByRole(Role role);

    List<User> findByPhoneNumberIn(List<String> phoneNumber);
}
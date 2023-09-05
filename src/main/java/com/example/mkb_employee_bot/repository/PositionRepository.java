package com.example.mkb_employee_bot.repository;

import java.util.List;
import java.util.Optional;

import com.example.mkb_employee_bot.entity.Management;
import com.example.mkb_employee_bot.entity.Position;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    @Query(value = "select name from Position where is_deleted = false", nativeQuery = true)
    List<String> getPositionNames();

    Optional<Position> findByName(String name);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Position SET is_deleted = true, updated_at = CURRENT_TIMESTAMP where Position.name = :name", nativeQuery = true)
    void updatePositionIsDeleted(String name);

    @Query(value = "select p.name from Position p where p.management.id = :management_id")
    List<String> getPositionNamesByManagementId(@Param("management_id") Long id);

    @Query(value = "select p from Position p where  p.name = :name and p.management.id = :management_id")
    Optional<Position> findByNameAndManagement(String name, @Param("management_id") Long id);
}
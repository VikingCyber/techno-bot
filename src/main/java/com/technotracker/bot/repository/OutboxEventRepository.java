package com.technotracker.bot.repository;

import com.technotracker.bot.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.sentAt IS NULL ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnsentOrderByCreatedAt();
}

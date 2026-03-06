package com.technotracker.bot.repository;

import com.technotracker.bot.model.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRequestRepository extends JpaRepository<UserRequest, UUID> {
}

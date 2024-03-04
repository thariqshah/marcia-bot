package com.bot.marcia.moviedb;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, Integer> {
}

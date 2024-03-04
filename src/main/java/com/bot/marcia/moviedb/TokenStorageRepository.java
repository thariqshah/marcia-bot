package com.bot.marcia.moviedb;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenStorageRepository extends JpaRepository<TokenStorage, String> {
}

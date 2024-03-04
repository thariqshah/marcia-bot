package com.bot.marcia.moviedb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "token_storage")
public class TokenStorage {
    @Id
    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "request_token",length = 1000)
    private String requestToken;
}

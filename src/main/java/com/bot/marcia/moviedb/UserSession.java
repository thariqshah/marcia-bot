package com.bot.marcia.moviedb;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_session")
public class UserSession {

    @Id
    @Column(name = "chat_id")
    private Integer chatId;

    @Column(name= "username")
    private String username;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "request_token")
    private String requestToken;

    @Column(name = "account_id")
    private Integer accountId;
}

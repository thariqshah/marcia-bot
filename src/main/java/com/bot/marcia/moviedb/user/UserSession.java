package com.bot.marcia.moviedb.user;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_session")
public class UserSession {

    @Id
    @Column(name = "chat_id")
    private String chatId;

    @Column(name= "username")
    private String username;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "request_token",length = 1000)
    private String requestToken;

    @Column(name = "access_token",length = 1000)
    private String accessToken;

    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "account_object_id")
    private String accountObjectId;
}

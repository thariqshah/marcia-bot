package com.bot.marcia.moviedb.dto;

public record RequestTokenDTO(Boolean success, String expires_at, String request_token,String session_id){
}
package com.bot.marcia.moviedb.dto;

public record UpdateListRequestDTO(String media_type, String media_id, boolean watchlist, boolean favorite){}

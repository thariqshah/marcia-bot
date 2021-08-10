package com.bot.marcia.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;


@Builder
@Data
public class MovieInfo {

    private String name;

    private String longName;

    private String ytsUrl;

    private String desc;

    private int year;

    private String coverImageUrl;

    private Map<String,String> torrentUrl;
}

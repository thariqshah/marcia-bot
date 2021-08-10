package com.bot.marcia.common;

public class Resources {

    public static final String YTS_BASE_URL = "https://yts.mx/api/v2/list_movies.json";

    public static final String discordBotToken = "ODc0NTc4MzEwOTU1NDIxNzE2.YRJAhg.SAv1cSd6lwzyz-Ec3mFh60LuWfg";


    public static String buildTelegramIntroMessage(String name){
       return  "Hey " +name+ "!\n\n"+
                "I Search ytx.mx for movies. Type in the exact movie title" +
                "\n\n<i>made with ♥ and java </i>";
    }
}

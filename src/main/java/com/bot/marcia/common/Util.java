package com.bot.marcia.common;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
public class Util {

    public static String buildTelegramIntroMessage(String name) {
        return "Hey " + name + "!\n\n" +
                "I Search ytx.mx for movies. Type in the exact movie title" +
                "\n\n<i>made with ♥ and java </i>";
    }
}

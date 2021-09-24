package com.bot.marcia.common;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
public class Util {

    public static String buildTelegramIntroMessage(String name) {
        return "Hello " + name + "!\n\n" +
                "I Search ytx.mx for movies and send you back torrent files I find." +
                "\nYou can start by replying with a movie name" +
                "\n\n<i></i>";
    }
}

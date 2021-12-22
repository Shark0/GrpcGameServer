package com.shark.game.util;

public class TokenUtil {

    public static String playerIdToToken(long playerId) {
        //FIXME Use JWT
        return String.valueOf(playerId);
    }

    public static Long tokenToUserId(String token) {
        //FIXME Use JWT
        return Long.valueOf(token);
    }
}

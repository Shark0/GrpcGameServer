package com.shark.game.manager;

import com.shark.game.entity.room.BaseRoom;
import com.shark.game.entity.room.RedBlackGameRoom;

import java.util.HashMap;
import java.util.Map;

public class RoomManager {

    public static final int ROCK_PAPER_SCISSORS_ROOM_ID = 1;
    public static final int RED_BLACK_ROOM_ID = 2;
    public static final int CARD_ROOM_ID = 3;

    private static RoomManager instance;

    private Map<String, BaseRoom> tokenRoomMap = new HashMap<>();

    private Map<Integer, BaseRoom> roomIdRoomMap = new HashMap<>();

    private RoomManager() {}

    public void init() {
        RedBlackGameRoom redBlackGameRoom = new RedBlackGameRoom();
        redBlackGameRoom.init();
        roomIdRoomMap.put(RED_BLACK_ROOM_ID, redBlackGameRoom);
    }

    public BaseRoom findRoomById(Integer roomId) {
        return roomIdRoomMap.get(roomId);
    }

    public void putRoomByToken(String token, BaseRoom room) {
        tokenRoomMap.put(token, room);
    }

    public BaseRoom getRoomByToken(String token) {
        return tokenRoomMap.get(token);
    }

    public static RoomManager getInstance() {
        if(instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }


}

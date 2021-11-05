package com.shark.game.manager;

import com.shark.game.entity.room.BaseRoomDO;
import com.shark.game.entity.room.BaseSeatRoomDO;
import com.shark.game.entity.room.CardSeatRoomDO;
import com.shark.game.entity.room.RedBlackGameRoomDO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomManager {

    public static final int ROCK_PAPER_SCISSORS_ROOM_TYPE = 1;
    public static final int RED_BLACK_ROOM_TYPE = 2;
    public static final int CARD_SEAT_ROOM_TYPE = 3;

    private static RoomManager instance;

    private Map<String, BaseRoomDO> tokenRoomMap = new HashMap<>();

    private Map<Integer, BaseRoomDO> roomIdRoomMap = new HashMap<>();

    private Map<Integer, List<BaseSeatRoomDO>> roomTypeQueueListMap = new HashMap<>();

    private RoomManager() {}

    public void init() {
        RedBlackGameRoomDO redBlackGameRoom = new RedBlackGameRoomDO();
        redBlackGameRoom.init();
        roomIdRoomMap.put(RED_BLACK_ROOM_TYPE, redBlackGameRoom);
    }

    public BaseRoomDO findRoomByType(Integer roomType) {
        switch (roomType) {
            case RED_BLACK_ROOM_TYPE:
                return roomIdRoomMap.get(roomType);
            case CARD_SEAT_ROOM_TYPE:
                List<BaseSeatRoomDO> baseSeatRoomDoList = roomTypeQueueListMap.get(roomType);
                if(baseSeatRoomDoList == null) {
                    baseSeatRoomDoList = new ArrayList<>();
                    roomTypeQueueListMap.put(roomType, baseSeatRoomDoList);
                }
                for(BaseSeatRoomDO baseSeatRoomDo: baseSeatRoomDoList) {
                    if(baseSeatRoomDo.isQueuing()) {
                        return baseSeatRoomDo;
                    }
                }
                CardSeatRoomDO cardSeatRoomDo = new CardSeatRoomDO(CARD_SEAT_ROOM_TYPE);
                cardSeatRoomDo.init();
                baseSeatRoomDoList.add(cardSeatRoomDo);
                return cardSeatRoomDo;
        }
        return null;
    }

    public void putRoomByToken(String token, BaseRoomDO room) {
        tokenRoomMap.put(token, room);
    }

    public BaseRoomDO getRoomByToken(String token) {
        return tokenRoomMap.get(token);
    }

    public static RoomManager getInstance() {
        if(instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public void removeRoomFromQueue(Integer gameType, BaseSeatRoomDO baseSeatRoomDo) {
        roomTypeQueueListMap.get(gameType).remove(baseSeatRoomDo);
    }
}

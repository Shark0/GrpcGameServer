package com.shark.game.manager;

import com.shark.game.entity.room.BaseRoomDO;
import com.shark.game.entity.room.RedBlackGameRoomDO;
import com.shark.game.entity.room.RockPaperScissorsGameRoomDO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomManager {

    public static final int ROCK_PAPER_SCISSORS_ROOM_TYPE = 1;
    public static final int RED_BLACK_ROOM_TYPE = 2;
    public static final int CARD_SEAT_ROOM_TYPE = 3;

    private static RoomManager instance;

    private final Map<String, BaseRoomDO> tokenRoomMap = new HashMap<>();

    private final Map<Integer, Map<Integer, BaseRoomDO>> agentIdRoomTypeRoomMap = new HashMap<>();



    private RoomManager() {}

    public void init() {
        List<Integer> agentIdList = findAgent();
        for(Integer agentId: agentIdList) {
            RedBlackGameRoomDO redBlackGameRoom = new RedBlackGameRoomDO(agentId, RED_BLACK_ROOM_TYPE, 10);
            redBlackGameRoom.init();
            Map<Integer, BaseRoomDO> roomIdRoomMap = new HashMap<>();
            roomIdRoomMap.put(RED_BLACK_ROOM_TYPE, redBlackGameRoom);
            agentIdRoomTypeRoomMap.put(agentId, roomIdRoomMap);
        }
    }

    private List<Integer> findAgent() {
        //FIXME load from agent id list
        return List.of(1);
    }

    public synchronized BaseRoomDO findRoomByAgentIdAndRoomType(Integer agentId, Integer roomType) {
        switch (roomType) {
            case ROCK_PAPER_SCISSORS_ROOM_TYPE:
                return new RockPaperScissorsGameRoomDO(agentId, roomType, 10);

            case RED_BLACK_ROOM_TYPE:
                return agentIdRoomTypeRoomMap.get(agentId).get(roomType);

            case CARD_SEAT_ROOM_TYPE:
                //TODO
                return null;
        }
        return null;
    }

    public synchronized void putRoomByToken(String token, BaseRoomDO room) {
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

}

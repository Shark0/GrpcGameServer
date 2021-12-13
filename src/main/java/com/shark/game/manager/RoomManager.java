package com.shark.game.manager;

import com.shark.game.entity.room.BaseRoomDO;
import com.shark.game.entity.room.RedBlackGameRoomDO;
import com.shark.game.entity.room.RockPaperScissorsGameRoomDO;
import com.shark.game.entity.room.texasHoldEm.TexasHoldEmGameRoomDO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomManager {

    public static final int ROCK_PAPER_SCISSORS_ROOM_TYPE = 1;
    public static final int RED_BLACK_ROOM_TYPE = 2;
    public static final int TEXAS_HOLDEM_ROOM_TYPE = 3;

    private static RoomManager instance;

    private final Map<Long, BaseRoomDO> playerIdRoomMap = new HashMap<>();

    private final Map<Integer, Map<Integer, BaseRoomDO>> agentIdRoomTypeRoomMap = new HashMap<>();



    private RoomManager() {}

    public void init() {
        List<Integer> agentIdList = findAgent();
        for(Integer agentId: agentIdList) {
            Map<Integer, BaseRoomDO> roomIdRoomMap = new HashMap<>();
            agentIdRoomTypeRoomMap.put(agentId, roomIdRoomMap);

            RedBlackGameRoomDO redBlackGameRoom = new RedBlackGameRoomDO(agentId, RED_BLACK_ROOM_TYPE, 10);
            roomIdRoomMap.put(RED_BLACK_ROOM_TYPE, redBlackGameRoom);


            TexasHoldEmGameRoomDO texasHoldemGameRoomDO =
                    new TexasHoldEmGameRoomDO(agentId, TEXAS_HOLDEM_ROOM_TYPE, 20000, 50, 12, 2);
            roomIdRoomMap.put(TEXAS_HOLDEM_ROOM_TYPE, texasHoldemGameRoomDO);
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
            case TEXAS_HOLDEM_ROOM_TYPE:
                return agentIdRoomTypeRoomMap.get(agentId).get(roomType);
        }
        return null;
    }

    public synchronized void putRoom(Long playerId, BaseRoomDO room) {
        playerIdRoomMap.put(playerId, room);
    }

    public BaseRoomDO getRoom(Long playerId) {
        return playerIdRoomMap.get(playerId);
    }

    public static RoomManager getInstance() {
        if(instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

}

package de.hs_rm.de.milefiz.messaging.events;

public enum EventType {
    MOVE, 
    JUMP, 
    ROLL_DICE, 
    MOVE_ERROR, 
    ROLL_DICE_ERROR, 
    COOLDOWN_READY, LOBBY_UPDATE, 
    DUEL, 
    TRIGGER_BARRIER_MOVE,
    MOVE_BARRIER,
    BARRIER_MOVE_ERROR,
    REJECTED_BY_BARRIER,
    MOVE_WITH_LOSS,
    MEEPLE_REACHED_END,
    WIN

}

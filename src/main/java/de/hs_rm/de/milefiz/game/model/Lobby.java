package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Lobby {

    private UUID id;
    private List<Player> players;
    private Field field;

    public Lobby() {
        id = UUID.randomUUID();
        players = new ArrayList<>();
    }

    public boolean isJoinable() {
        return players.size() < 4; // maximum 4 Players
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean addPlayer(Player player) {
        return players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

}

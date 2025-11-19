package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.EnumSet;
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

    /**
     * Sucht nach einer Farbe die noch nicht vergeben wurde.
     * @return Eine in der Lobby noch nicht verwendete Farbe
     */
    public Color getAvailableColor() {
        return EnumSet.allOf(Color.class).stream()
                //filter: Players->UsedColors dann Abfrage von Color die noch nicht existiert
                .filter(c -> players.stream().map(e -> e.getColor()).noneMatch(e -> c.equals(e)))
                .findFirst()
                .orElse(null);
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

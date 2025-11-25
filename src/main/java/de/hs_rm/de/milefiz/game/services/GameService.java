package de.hs_rm.de.milefiz.game.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Player;

/**
 * Zentrale Serviceklasse zur Verwaltung von Spielern und Spielfeld-Testdaten.
 *
 * Der {@code GameService} dient aktuell als einfache Platzhalter-Implementierung
 * für spätere Spiellogik- und Lobby-Funktionen. Er verwaltet temporär Spielerobjekte
 * anhand ihrer WebSocket-Session-ID und bietet rudimentäre Methoden zum Zugriff
 * auf Testfelder (z. B. für Entwicklungszwecke oder Simulationen)
 *
 * In einer späteren Ausbaustufe soll diese Klasse um Matchmaking-, Lobby- und
 * Persistenzfunktionen erweitert werden
 *
 * @author Maximilian Ressel
 * @version 0.1
 */
@Service
public class GameService {
    private Map<String, Player> playersBySession = new HashMap<>();

    private List<Field> testFields = new ArrayList<>();  //nur field -> testBoard

    public Player getPlayerBySession(String sessionId) {
        return playersBySession.get(sessionId);
    }

    public void registerPlayer(String sessionId, Player player) {
        playersBySession.put(sessionId, player);
    }

    public Player createDummyPlayer(String sessionId) {
        Player dummy = new Player(Color.BLUE, 1);
        registerPlayer(sessionId, dummy);
        return dummy;
    }

    public void saveTestField(Field field){
        testFields.add(field);
    }

    public Field getTestStartField(){
        return testFields.get(0);
    }

}
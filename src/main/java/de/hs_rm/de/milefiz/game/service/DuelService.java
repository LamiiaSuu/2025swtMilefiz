package de.hs_rm.de.milefiz.game.service;

import java.util.List;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public interface DuelService {

    /**
     * Liefert ein zufälliges Mini-Spiel zurück.
     */
    MiniGame randomGame();

    /**
     * Aktuelle Liste der registrierten Mini-Spiele.
     */
    List<MiniGame> getRegisteredGames();
}


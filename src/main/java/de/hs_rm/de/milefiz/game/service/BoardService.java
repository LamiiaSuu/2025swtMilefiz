package de.hs_rm.de.milefiz.game.service;

import de.hs_rm.de.milefiz.game.model.Board;

public interface BoardService {

    public boolean validateBoard(Board board) throws BoardValidateException;

}
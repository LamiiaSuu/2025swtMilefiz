package de.hs_rm.de.milefiz.game.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.BoardDTO;
import de.hs_rm.de.milefiz.game.model.FieldType;
import de.hs_rm.de.milefiz.game.model.Position;

@RestController
@RequestMapping("/api/game")
public class GameController {

    //zum testen 
    @GetMapping(path = "/getBoard")
    public BoardDTO getBoard() {
        BoardDTO board = new BoardDTO();
        Position pos0 = new Position(0,0);
        Position pos1 = new Position(0,1);
        Position pos2 = new Position(1, 1);
        Position pos3 = new Position(1, 2);

        board.addField(0, FieldType.START_BLUE, pos0, false, 1, -1, -1, -1);
        board.addField(1, FieldType.NORMAL, pos1, false, -1, 2, 0, -1);
        board.addField(2, FieldType.NORMAL, pos2, false, 3, -1, -1, 2);
        board.addField(3, FieldType.END, pos3, false, -1, -1, 2, -1);
        return board;
    }
}

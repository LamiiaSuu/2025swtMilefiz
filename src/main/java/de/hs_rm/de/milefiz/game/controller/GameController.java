package de.hs_rm.de.milefiz.game.controller;

import java.util.UUID;

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

        UUID id0 = UUID.randomUUID();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        board.addField(id0, FieldType.START_BLUE, pos0, false, id1, null, null, null);
        board.addField(id1, FieldType.NORMAL, pos1, false, null, id2, id0, null);
        board.addField(id2, FieldType.NORMAL, pos2, false, id3, null, null, id2);
        board.addField(id3, FieldType.END, pos3, false, null, null, id2, null);
        return board;
    }
}

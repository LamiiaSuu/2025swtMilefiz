package de.hs_rm.de.milefiz.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.BoardDTO;
import de.hs_rm.de.milefiz.game.model.BoardMapper;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.FieldType;
import de.hs_rm.de.milefiz.game.model.Position;
import de.hs_rm.de.milefiz.game.service.GameService;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private GameService gameService;

    public GameController(GameService gameService){
        this.gameService = gameService;
    }

    @GetMapping(path = "/getBoard")
    public BoardDTO getBoard() {
        Position pos0 = new Position(0, 0);
        Position pos1 = new Position(0, 2);
        Position pos2 = new Position(2, 2);
        Position pos3 = new Position(2, 0);
        Position pos4 = new Position(2, 4);
        Position pos5 = new Position(0, 4);

        Field zeroZero = new Field(FieldType.NORMAL, pos0);
        Field zeroTwo = new Field(FieldType.NORMAL, pos1);
        Field twoTwo = new Field(FieldType.NORMAL, pos2);
        Field twoZero = new Field(FieldType.NORMAL, pos3);
        Field twoFour = new Field(FieldType.NORMAL, pos4);
        Field zeroFour = new Field(FieldType.NORMAL, pos5);

        zeroZero.addNeighbour(zeroTwo, Direction.NORTH);
        zeroTwo.addNeighbour(twoTwo, Direction.WEST);
        twoTwo.addNeighbour(twoZero, Direction.SOUTH);
        twoZero.addNeighbour(zeroZero, Direction.EAST);
        twoTwo.addNeighbour(twoFour, Direction.NORTH);
        zeroTwo.addNeighbour(zeroFour, Direction.NORTH);
        zeroFour.addNeighbour(twoFour, Direction.WEST);
        
        
        Board board = new Board("test", zeroZero); 
        gameService.setTestBoard(board);
        return BoardMapper.mapToDTO(board);
    }
}

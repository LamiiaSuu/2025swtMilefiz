package de.hs_rm.de.milefiz.game.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.BoardDTO;
import de.hs_rm.de.milefiz.game.model.BoardMapper;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.FieldType;
import de.hs_rm.de.milefiz.game.model.Position;
import de.hs_rm.de.milefiz.game.services.GameService;

@RestController
@RequestMapping("/api/game")
public class GameController {

    // zum testen
    @Autowired
    private GameService gameService;

    @GetMapping(path = "/getBoard")
    public BoardDTO getBoard() {
        Position pos0 = new Position(0, 0);
        Position pos1 = new Position(0, 2);
        Position pos2 = new Position(2, 2);
        Position pos3 = new Position(2, 0);
        Position pos4 = new Position(2, 4);
        Position pos5 = new Position(0, 4);

        UUID id0 = UUID.randomUUID();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        UUID id4 = UUID.randomUUID();
        UUID id5 = UUID.randomUUID();

        Field zeroZero = new Field(FieldType.NORMAL, pos0);
        Field zeroTwo = new Field(FieldType.NORMAL, pos1);
        Field twoTwo = new Field(FieldType.NORMAL, pos2);
        Field twoZero = new Field(FieldType.NORMAL, pos3);
        Field twoFour = new Field(FieldType.NORMAL, pos4);
        Field zeroFour = new Field(FieldType.NORMAL, pos5);

        zeroZero.setId(id0);
        zeroTwo.setId(id1);
        twoTwo.setId(id2);
        twoZero.setId(id3);
        twoFour.setId(id4);
        zeroFour.setId(id5);

        zeroZero.addNeighbour(zeroTwo, Direction.NORTH);
        zeroTwo.addNeighbour(twoTwo, Direction.WEST);
        twoTwo.addNeighbour(twoZero, Direction.SOUTH);
        twoZero.addNeighbour(zeroZero, Direction.EAST);
        twoTwo.addNeighbour(twoFour, Direction.NORTH);
        zeroTwo.addNeighbour(zeroFour, Direction.NORTH);
        zeroFour.addNeighbour(twoFour, Direction.WEST);
        

        gameService.saveTestField(zeroZero);
        gameService.saveTestField(zeroTwo);
        gameService.saveTestField(twoTwo);
        gameService.saveTestField(twoZero);
        gameService.saveTestField(twoFour);
        gameService.saveTestField(zeroFour);

        return BoardMapper.mapToDTO(zeroZero);
    }
}

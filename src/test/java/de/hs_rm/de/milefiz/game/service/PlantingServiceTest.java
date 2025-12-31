package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hs_rm.de.milefiz.game.model.Position;
import de.hs_rm.de.milefiz.game.model.PositionFloat;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO.FieldDTO;

public class PlantingServiceTest {
    PlantingService plantingService;
    BoardDTO boardDTO;

    @BeforeEach
    void setUp() throws IOException {
        plantingService = new PlantingServiceImpl();
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("boards/dummyBoard.json");
        if (inputStream == null) {
            throw new IOException("BoardFile not found");
        }

        try {
            boardDTO = objectMapper.readValue(inputStream,
                    BoardDTO.class);
        } finally {
            inputStream.close();
        }
    }

    /**
     * testet, ob Bäume auf dem Weg gepflanzt werden
     * 
     * @author Thilo Wittmer
     */
    @Test
    void testForNoTreesOnPath() {
        boardDTO = plantingService.plantTrees(boardDTO, 1f);
        List<Position> fieldPositions = new ArrayList<>();

        for (FieldDTO field : boardDTO.getFields()) {
            Position pos = field.getPosition();
            fieldPositions.add(pos);

            // wenn es nachbarn gibt positionen zwischen den feldern auffüllen
            if (field.getNorth() != null) {
                fieldPositions.add(new Position(pos.getX(), pos.getY() + 1));
            }

            if (field.getEast() != null) {
                fieldPositions.add(new Position(pos.getX() + 1, pos.getY()));
            }

            if (field.getSouth() != null) {
                fieldPositions.add(new Position(pos.getX(), pos.getY() - 1));
            }

            if (field.getWest() != null) {
                fieldPositions.add(new Position(pos.getX() - 1, pos.getY()));
            }
        }

        List<PositionFloat> treePositions = boardDTO.getTrees().stream()
                .map(t -> t.getTreePosition())
                .toList();

        for (PositionFloat treePos : treePositions) {

            for (Position fPos : fieldPositions) {
                float deltaX = Math.abs(fPos.getX() - treePos.getX());
                float deltaY = Math.abs(fPos.getY() - treePos.getY());
                if (deltaX <= 0.5 && deltaY <= 0.5) {
                    System.out.println("false");
                }
                assertFalse(deltaX <= 0.5 && deltaY <= 0.5);
            }
        }
    }
}

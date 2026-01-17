package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class PlantingServiceTest {
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

    @Test
    void plantTrees_densityZero_createsNoTrees() {
        boardDTO = plantingService.plantTrees(boardDTO, 0f);

        assertNotNull(boardDTO.getTrees());
        assertTrue(boardDTO.getTrees().isEmpty(),
            "Bei Density 0 dürfen keine Bäume gepflanzt werden");
    }

    @Test
    void plantTrees_densityPositive_createsTrees() {
        boardDTO = plantingService.plantTrees(boardDTO, 0.2f);

        assertNotNull(boardDTO.getTrees());
        assertFalse(boardDTO.getTrees().isEmpty(),
            "Bei positiver Density sollten Bäume gepflanzt werden");
    }

    @Test
    void plantedTrees_areWithinExtendedBoardBounds() {
        boardDTO = plantingService.plantTrees(boardDTO, 0.2f);

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (FieldDTO field : boardDTO.getFields()) {
            minX = Math.min(minX, field.getPosition().getX());
            minY = Math.min(minY, field.getPosition().getY());
            maxX = Math.max(maxX, field.getPosition().getX());
            maxY = Math.max(maxY, field.getPosition().getY());
        }

        for (var tree : boardDTO.getTrees()) {
            PositionFloat pos = tree.getTreePosition();

            assertTrue(
                pos.getX() >= minX - 100 && pos.getX() <= maxX + 100,
                "Baum-X liegt außerhalb des erwarteten Bereichs"
            );
            assertTrue(
                pos.getY() >= minY - 100 && pos.getY() <= maxY + 100,
                "Baum-Y liegt außerhalb des erwarteten Bereichs"
            );
        }
    }


}

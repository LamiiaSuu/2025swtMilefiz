package de.hs_rm.de.milefiz.game.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.hs_rm.de.milefiz.game.model.Color;

class PlayerDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        PlayerDTO dto = new PlayerDTO();

        assertNull(dto.getId());
        assertNull(dto.getMeeples());
        assertNull(dto.getPlayerName());
        assertNull(dto.getColor());
        assertNull(dto.getActiveMeeple());
        assertEquals(0, dto.getRemainingMoves());
        assertEquals(0, dto.getMaxEnergy());
        assertFalse(dto.isLeader());
        assertFalse(dto.isMoved());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        PlayerDTO dto = new PlayerDTO();

        UUID id = UUID.randomUUID();
        MeepleDTO meeple1 = new MeepleDTO();
        MeepleDTO meeple2 = new MeepleDTO();
        MeepleDTO[] meeples = new MeepleDTO[] { meeple1, meeple2 };
        MeepleDTO activeMeeple = meeple1;

        dto.setId(id);
        dto.setMeeples(meeples);
        dto.setPlayerName("TestPlayer");
        dto.setLeader(true);
        dto.setColor(Color.RED);
        dto.setActiveMeeple(activeMeeple);
        dto.setRemainingMoves(3);
        dto.setMoved(true);
        dto.setMaxEnergy(10);

        assertEquals(id, dto.getId());
        assertArrayEquals(meeples, dto.getMeeples());
        assertEquals("TestPlayer", dto.getPlayerName());
        assertTrue(dto.isLeader());
        assertEquals(Color.RED, dto.getColor());
        assertEquals(activeMeeple, dto.getActiveMeeple());
        assertEquals(3, dto.getRemainingMoves());
        assertTrue(dto.isMoved());
        assertEquals(10, dto.getMaxEnergy());

        dto.setLeader(false);
        dto.setMoved(false);

        assertFalse(dto.isLeader());
        assertFalse(dto.isMoved());
    }
}

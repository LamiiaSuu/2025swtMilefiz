package de.hs_rm.de.milefiz.game.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MeepleDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        MeepleDTO dto = new MeepleDTO();

        assertNull(dto.getId());
        assertNull(dto.getCurrentFieldId());
        assertNull(dto.getLastFieldId());
        assertFalse(dto.isBarrier());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        UUID currentFieldId = UUID.randomUUID();
        UUID lastFieldId = UUID.randomUUID();
        boolean barrier = true;

        MeepleDTO dto = new MeepleDTO(id, currentFieldId, lastFieldId, barrier);

        assertEquals(id, dto.getId());
        assertEquals(currentFieldId, dto.getCurrentFieldId());
        assertEquals(lastFieldId, dto.getLastFieldId());
        assertTrue(dto.isBarrier());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        MeepleDTO dto = new MeepleDTO();

        UUID id = UUID.randomUUID();
        UUID currentFieldId = UUID.randomUUID();
        UUID lastFieldId = UUID.randomUUID();

        dto.setId(id);
        dto.setCurrentFieldId(currentFieldId);
        dto.setLastFieldId(lastFieldId);
        dto.setBarrier(true);

        assertEquals(id, dto.getId());
        assertEquals(currentFieldId, dto.getCurrentFieldId());
        assertEquals(lastFieldId, dto.getLastFieldId());
        assertTrue(dto.isBarrier());

        // explizit auch false testen (Branch für boolean)
        dto.setBarrier(false);
        assertFalse(dto.isBarrier());
    }
}

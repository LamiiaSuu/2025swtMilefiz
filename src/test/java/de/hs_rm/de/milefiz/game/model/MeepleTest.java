package de.hs_rm.de.milefiz.game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MeepleTest {

    @Test
    void constructor_setsBarrierFlagAndId() {
        Meeple barrierMeeple = new Meeple(true);
        Meeple normalMeeple = new Meeple(false);

        assertTrue(barrierMeeple.isBarrier());
        assertFalse(normalMeeple.isBarrier());

        assertNotNull(barrierMeeple.getId());
        assertNotNull(normalMeeple.getId());
    }

    @Test
    void copyConstructor_copiesIdBarrierAndCurrentField_only() {
        Meeple original = new Meeple(false);
        Field field = new Field(FieldType.NORMAL, new Position(0, 0));
        original.setCurrentField(field);

        Meeple copy = new Meeple(original);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.isBarrier(), copy.isBarrier());
        assertEquals(field, copy.getCurrentField());

        assertNull(copy.getLastField());
    }

    @Test
    void setCurrentField_setsCurrentAndLastFieldCorrectly() {
        Meeple meeple = new Meeple(false);

        Field field1 = new Field(FieldType.NORMAL, new Position(0, 0));
        Field field2 = new Field(FieldType.NORMAL, new Position(1, 0));

        meeple.setCurrentField(field1);

        assertEquals(field1, meeple.getCurrentField());
        assertNull(meeple.getLastField());

        meeple.setCurrentField(field2);

        assertEquals(field2, meeple.getCurrentField());
        assertEquals(field1, meeple.getLastField());
    }

    @Test
    void clearLastField_setsLastFieldToNull() {
        Meeple meeple = new Meeple(false);

        Field field1 = new Field(FieldType.NORMAL, new Position(0, 0));
        Field field2 = new Field(FieldType.NORMAL, new Position(1, 0));

        meeple.setCurrentField(field1);
        meeple.setCurrentField(field2);

        assertNotNull(meeple.getLastField());

        meeple.clearLastField();

        assertNull(meeple.getLastField());
    }

    @Test
    void equals_sameId_returnsTrue() {
        Meeple m1 = new Meeple(false);
        Meeple m2 = new Meeple(true);

        UUID sharedId = UUID.randomUUID();
        m1.setId(sharedId);
        m2.setId(sharedId);

        assertEquals(m1, m2);
        assertEquals(m2, m1);
    }

    @Test
    void equals_differentId_returnsFalse() {
        Meeple m1 = new Meeple(false);
        Meeple m2 = new Meeple(false);

        assertNotEquals(m1, m2);
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        Meeple meeple = new Meeple(false);

        assertEquals(meeple, meeple);
    }

    @Test
    void equals_otherTypeOrNull_returnsFalse() {
        Meeple meeple = new Meeple(false);

        assertNotEquals(meeple, "not a meeple");
        assertNotEquals(meeple, null);
    }

    @Test
    void setId_updatesId() {
        Meeple meeple = new Meeple(false);
        UUID newId = UUID.randomUUID();

        meeple.setId(newId);

        assertEquals(newId, meeple.getId());
    }
}

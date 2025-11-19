package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO des Spielbretts zur Übertragung an das Frontend
 */
public class BoardDTO {
    private List<FieldDTO> fields = new ArrayList<>();

    public List<FieldDTO> getFields() {
        return fields;
    }

    public void addField(UUID id, FieldType type, Position position, boolean isBarrier, UUID north, UUID east, UUID south, UUID west) {
        this.fields.add(new FieldDTO(id, type, position, isBarrier, north, east, south, west));
    }
    
    protected class FieldDTO {
        private UUID id;
        private UUID north;
        private UUID east; 
        private UUID south;
        private UUID west; 
        private FieldType type;
        private boolean isBarrier;
        private Position position;

        private FieldDTO(UUID id, FieldType type, Position position, boolean isBarrier, UUID north, UUID east, UUID south, UUID west) {
            this.id = id;
            this.type = type;
            this.position = position;
            this.isBarrier = isBarrier;
            this.north = north;
            this.east = east;
            this.south = south;
            this.west = west;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public UUID getNorth() {
            return north;
        }

        public void setNorth(UUID north) {
            this.north = north;
        }

        public UUID getEast() {
            return east;
        }

        public void setEast(UUID east) {
            this.east = east;
        }

        public UUID getSouth() {
            return south;
        }

        public void setSouth(UUID south) {
            this.south = south;
        }

        public UUID getWest() {
            return west;
        }

        public void setWest(UUID west) {
            this.west = west;
        }

        public FieldType getType() {
            return type;
        }

        public void setType(FieldType type) {
            this.type = type;
        }

        public boolean isBarrier() {
            return isBarrier;
        }

        public void setBarrier(boolean isBarrier) {
            this.isBarrier = isBarrier;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }
        
        
    }
}

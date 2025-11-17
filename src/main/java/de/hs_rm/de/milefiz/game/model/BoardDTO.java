package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.List;


public class BoardDTO {
    private List<FieldDTO> fields = new ArrayList<>();

    public List<FieldDTO> getFields() {
        return fields;
    }

    public void addField(long id, FieldType type, Position position, boolean isBarrier, long north, long east, long south, long west) {
        this.fields.add(new FieldDTO(id, type, position, isBarrier, north, east, south, west));
    }
    
    public class FieldDTO {
        private long id;
        private long north;
        private long east; 
        private long south;
        private long west; 
        private FieldType type;
        private boolean isBarrier;
        private Position position;

        private FieldDTO(long id, FieldType type, Position position, boolean isBarrier, long north, long east, long south, long west) {
            this.id = id;
            this.type = type;
            this.position = position;
            this.isBarrier = isBarrier;
            this.north = north;
            this.east = east;
            this.south = south;
            this.west = west;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getNorth() {
            return north;
        }

        public void setNorth(long north) {
            this.north = north;
        }

        public long getEast() {
            return east;
        }

        public void setEast(long east) {
            this.east = east;
        }

        public long getSouth() {
            return south;
        }

        public void setSouth(long south) {
            this.south = south;
        }

        public long getWest() {
            return west;
        }

        public void setWest(long west) {
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

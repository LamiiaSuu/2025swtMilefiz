package de.hs_rm.de.milefiz.game.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.FieldType;
import de.hs_rm.de.milefiz.game.model.Position;
import de.hs_rm.de.milefiz.game.model.PositionFloat;

/**
 * DTO des Spielbretts zur Übertragung an das Frontend
 */
public class BoardDTO {
    private UUID id;
    private String name;
    private List<FieldDTO> fields = new ArrayList<>();
    private List<TreeDTO> trees = new ArrayList<>();

    public BoardDTO() {
    }

    public BoardDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public List<FieldDTO> getFields() {
        return fields;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addField(UUID id, FieldType type, Position position, boolean barrier, UUID north, UUID east, UUID south,
            UUID west) {
        this.fields.add(new FieldDTO(id, type, position, barrier, north, east, south, west));
    }

    public List<TreeDTO> getTrees() {
        return trees;
    }

    public void addTree(PositionFloat treePosition) {
        trees.add(new TreeDTO(treePosition));
    }

    public static class TreeDTO {
        private PositionFloat treePosition;

        public TreeDTO(PositionFloat treePosition) {
            this.treePosition = treePosition;
        }

        public PositionFloat getTreePosition() {
            return treePosition;
        }

        public void setTreePosition(PositionFloat positionF) {
            this.treePosition = positionF;
        }

    }

    public static class FieldDTO {
        private UUID id;
        private UUID north;
        private UUID east;
        private UUID south;
        private UUID west;
        private FieldType type;
        private boolean barrier;
        private Position position;

        public FieldDTO() {
        }

        public FieldDTO(UUID id, FieldType type, Position position, boolean barrier, UUID north, UUID east, UUID south,
                UUID west) {
            this.id = id;
            this.type = type;
            this.position = position;
            this.barrier = barrier;
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
            return barrier;
        }

        public void setBarrier(boolean isBarrier) {
            this.barrier = isBarrier;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }

    }

}

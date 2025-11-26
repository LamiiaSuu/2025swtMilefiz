package de.hs_rm.de.milefiz.game.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Klasse, um ein Feld des Spielbrettes zu repräsentieren. 
 * Jedes Feld beinhaltet seine Nachbarn.
 */
public class Field {
    
    private UUID id;
    private Map<Direction, Field> neighbours = new HashMap<>();
    private FieldType type;
    private Position position;

    public Field(FieldType type, Position position) {
        id = UUID.randomUUID();
        this.type = type;
        this.position = position;
    }
    
    public Field getNorth() {
        return neighbours.keySet().contains(Direction.NORTH) ? neighbours.get(Direction.NORTH) : null;
    }

    public Field getEast() {
        return neighbours.keySet().contains(Direction.EAST) ? neighbours.get(Direction.EAST) : null;
    }

    public Field getSouth() {
        return neighbours.keySet().contains(Direction.SOUTH) ? neighbours.get(Direction.SOUTH) : null;
    }

    public Field getWest() {
        return neighbours.keySet().contains(Direction.WEST) ? neighbours.get(Direction.WEST) : null;
    }

    public UUID getId() {
        return id;
    }

    public Map<Direction, Field> getNeighbours() {
        return neighbours;
    }


    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    /**
     * Fügt diesem Feld einen Nachbarn hinzu
     * @param field das Nachbarfeld was hinzugefügt werden soll
     * @param direction in welche Richtung von diesem Feld das Nachbarfeld sein soll
     */
    public void addNeighbour(Field field, Direction direction) {
        
        if (neighbours.keySet().contains(direction)) {
            throw new IllegalArgumentException("Richtung " + direction + " ist schon besetzt");
        }

        neighbours.put(direction, field);
        field.getNeighbours().put(direction.getOpposite(), this);
    }

    public Position getPosition() {
        return position;
    }
    

    @Override
    public String toString() {
        Set<Direction> availableDirections = neighbours.keySet();
        return """
           %s {
             id: %s
             FieldType: %s
             availableDirections: %s
           }""".formatted(
             this.getClass().getSimpleName(),
             id,
             type,
             availableDirections
           );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Field)) {
            return false;
        }

        Field field = (Field) obj;

        return this.id.equals(field.id);
    }

    
}

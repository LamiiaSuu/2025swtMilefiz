package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.BoardDTO.FieldDTO;

public class BoardMapper {
    
    /**
     * mappt ein Board von {@link Board} zu {@link BoardDTO}
     * @param board Board 
     * @return Board als DTO
     */
    public static BoardDTO mapToDTO(Board board) {
        Field startField = board.getStartField();
        Stack<Field> remaining = new Stack<>(); 
        Map<Direction, Field> currentNeighbours;
        List<Field> visited = new ArrayList<>();
        BoardDTO out = new BoardDTO();

        remaining.add(startField);
        
        while (!remaining.isEmpty()) {
            Field node = remaining.pop();

            if (visited.contains(node)) {
                continue;
            }

            visited.add(node);
            currentNeighbours = node.getNeighbours();
            Set<Direction> availableDirections = currentNeighbours.keySet();
            UUID north = getNeighbourID(Direction.NORTH, currentNeighbours);
            UUID east = getNeighbourID(Direction.EAST, currentNeighbours); 
            UUID south= getNeighbourID(Direction.SOUTH, currentNeighbours);
            UUID west = getNeighbourID(Direction.WEST, currentNeighbours);
            boolean isBarrier = false;

            for (Meeple barrier : board.getBarriers()) {
                if (barrier.getCurrentField().equals(node)) {
                    isBarrier = true;
                }
            }

            out.addField(node.getId(), node.getType(), node.getPosition(), isBarrier, north, east, south, west);

            for (Direction dir : availableDirections) {
               Field neighbour = currentNeighbours.get(dir);
               if (!visited.contains(neighbour)) {
                remaining.add(neighbour);
               } 
            }
        }

        return out;
    }

    private static UUID getNeighbourID(Direction direction, Map<Direction, Field> currentNeighbours) {
        Set<Direction> availableDirections = currentNeighbours.keySet();
        return availableDirections.contains(direction) ? currentNeighbours.get(direction).getId() : null;
    }

    public static Board mapToBoard(BoardDTO boardDTO) {
 
        List<FieldDTO> fieldDTOs = boardDTO.getFields();
        List<Field> fields = new ArrayList<>();
        FieldDTO startDTO = fieldDTOs.removeFirst();
        Field startField = new Field(startDTO.getId(), startDTO.getType(), startDTO.getPosition());
        Board board = new Board("dummy-board", startField);
        fields.add(startField);
        if (startDTO.isBarrier()) {
            Meeple barrier = new Meeple(true);
            barrier.setCurrentField(startField);
            board.addBarrier(barrier);
        }
        //geht bestimmt irgendwie besser ¯\_(ツ)_/¯
        for (FieldDTO tempDTO : fieldDTOs) {
            Field field = new Field(tempDTO.getId(), tempDTO.getType(), tempDTO.getPosition());

            for (Field tempField : fields) {
                UUID dirID = tempDTO.getNorth();
                if (dirID != null && dirID.equals(tempField.getId())) {
                    tempField.addNeighbour(field, Direction.SOUTH);
                }

                dirID = tempDTO.getEast();
                if (dirID != null && dirID.equals(tempField.getId())) {
                    tempField.addNeighbour(field, Direction.WEST);
                }

                dirID = tempDTO.getSouth();
                if (dirID != null && dirID.equals(tempField.getId())) {
                    tempField.addNeighbour(field, Direction.NORTH);
                }

                dirID = tempDTO.getWest();
                if (dirID != null && dirID.equals(tempField.getId())) {
                    tempField.addNeighbour(field, Direction.EAST);
                }
            }

            if (tempDTO.isBarrier()) {
                Meeple barrier = new Meeple(true);
                barrier.setCurrentField(field);
                board.addBarrier(barrier);
            }

            fields.add(field);
        }

        return board;
    }

}

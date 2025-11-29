package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

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
            UUID north = availableDirections.contains(Direction.NORTH) ? currentNeighbours.get(Direction.NORTH).getId() : null;
            UUID east = availableDirections.contains(Direction.EAST) ? currentNeighbours.get(Direction.EAST).getId() : null; 
            UUID south= availableDirections.contains(Direction.SOUTH) ? currentNeighbours.get(Direction.SOUTH).getId() : null;
            UUID west = availableDirections.contains(Direction.WEST) ? currentNeighbours.get(Direction.WEST).getId() : null;
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
}

package de.hs_rm.de.milefiz.game.model.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Tree;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO.FieldDTO;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO.TreeDTO;

/**
 * Mapper Klasse, die zwischen {@link Board} und {@link BoardDTO} mappt.
 * 
 * @author Thilo Wittmer
 */
public class BoardMapper {

    /**
     * mappt ein Board von {@link Board} zu {@link BoardDTO}
     * 
     * @param board Board
     * @return Board als DTO
     */
    public static BoardDTO mapToDTO(Board board) {
        Field startField = board.getStartBlue();
        Stack<Field> remaining = new Stack<>();
        Map<Direction, Field> currentNeighbours;
        List<Field> visited = new ArrayList<>();

        List<Tree> trees = board.getTrees();
        List<TreeDTO> treeDTOs;

        BoardDTO out = new BoardDTO(board.getId(), board.getName());

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
            UUID south = getNeighbourID(Direction.SOUTH, currentNeighbours);
            UUID west = getNeighbourID(Direction.WEST, currentNeighbours);
            boolean isBarrier = false;

            for (Meeple barrier : board.getBarriers()) {
                if (node.equals(barrier.getCurrentField())) {
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

        treeDTOs = trees.stream().map(tree -> new TreeDTO(tree.getPosition(), tree.getType())).toList();
        out.addTrees(treeDTOs);

        return out;
    }

    private static UUID getNeighbourID(Direction direction, Map<Direction, Field> currentNeighbours) {
        Set<Direction> availableDirections = currentNeighbours.keySet();
        return availableDirections.contains(direction) ? currentNeighbours.get(direction).getId() : null;
    }

    /**
     * mappt ein {@link BoardDTO} zu {@link Board}
     * 
     * @param boardDTO
     * @return die Struktur des boardDTO als Board
     */
    public static Board mapToBoard(BoardDTO boardDTO) {

        List<FieldDTO> fieldDTOs = boardDTO.getFields();
        List<Field> fields = new ArrayList<>();
        FieldDTO startDTO = fieldDTOs.removeFirst();
        Field startField = new Field(startDTO.getId(), startDTO.getType(), startDTO.getPosition());

        List<TreeDTO> treeDTOs = boardDTO.getTrees();
        List<Tree> trees;

        Board board = new Board(boardDTO.getId(), boardDTO.getName(), null, null, null, null);

        fields.add(startField);

        if (startDTO.isBarrier()) {
            Meeple barrier = new Meeple(true);
            barrier.setCurrentField(startField);
            board.addBarrier(barrier);
        }

        checkForStartType(startField, board);

        // geht bestimmt irgendwie besser ¯\_(ツ)_/¯
        for (FieldDTO tempDTO : fieldDTOs) {
            Field field = new Field(tempDTO.getId(), tempDTO.getType(), tempDTO.getPosition());

            for (Field tempField : fields) {
                UUID dirID = tempDTO.getNorth();
                if (dirID != null && dirID.equals(tempField.getId())) {
                    tempField.addNeighbour(field, Direction.NORTH);
                }

                dirID = tempDTO.getEast();
                if (dirID != null && dirID.equals(tempField.getId())) {
                    tempField.addNeighbour(field, Direction.EAST);
                }

                dirID = tempDTO.getSouth();
                if (dirID != null && dirID.equals(tempField.getId())) {
                    tempField.addNeighbour(field, Direction.SOUTH);
                }

                dirID = tempDTO.getWest();
                if (dirID != null && dirID.equals(tempField.getId())) {
                    tempField.addNeighbour(field, Direction.WEST);
                }
            }

            checkForStartType(field, board);

            if (tempDTO.isBarrier()) {
                Meeple barrier = new Meeple(true);
                barrier.setCurrentField(field);
                board.addBarrier(barrier);
            }

            fields.add(field);
        }

        trees = treeDTOs.stream().map(treeDTO -> new Tree(treeDTO.getTreeType(), treeDTO.getTreePosition())).toList();
        board.addTrees(trees);

        return board;
    }

    /**
     * Ueberprueft, ob das Feld ein Startfeld ist und wenn ja, wird es dem Board
     * hinzugefuegt
     * 
     * @param field das Feld, wo ueberprueft wird, ob es ein Startfeld ist
     * @param board das Board, dem das potenzielle Startfeld hinzugefügt werden soll
     */
    public static void checkForStartType(Field field, Board board) {
        switch (field.getType()) {
            case START_GREEN -> board.setStartGreen(field);
            case START_YELLOW -> board.setStartYellow(field);
            case START_BLUE -> board.setStartBlue(field);
            case START_RED -> board.setStartRed(field);
            default -> {
            }
        }
    }
}

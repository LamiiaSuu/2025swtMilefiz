package de.hs_rm.de.milefiz.game.service;

import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;

public interface PlantingService {

    public BoardDTO plantTrees(BoardDTO boardDTO, float density);

    public int[][] generateBlueNoiseVoidCluster(float density, int width, int height);
}

package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO.TreeDTO;

public class PlantingServiceTest {

    private PlantingService plantingService;

    @BeforeEach
    void setup() {
        plantingService = new PlantingServiceImpl();
    }

    @Test
    void plantTreesOnBoardTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("boards/dummyBoard.json");

        if (inputStream == null) {
            throw new IOException("Test board file not found");
        }

        try {
            BoardDTO boardDTO = objectMapper.readValue(inputStream, BoardDTO.class);
            plantingService.plantTrees(boardDTO, 0.35f);
            for (TreeDTO tree : boardDTO.getTrees()) {
                System.out.println(tree.getTreePosition());
            }
            assertTrue(boardDTO.getTrees().size() > 0);
        } finally {
            inputStream.close();
        }
    }

    @Test
    void blueNoiseGeneratorTest() {
        int[][] res = plantingService.generateBlueNoiseVoidCluster(0.35f, 64, 64);
        printBlueNoisePreview(res);

    }

    public void printBlueNoisePreview(int[][] mask) {
        printBlueNoisePreview(mask, '#', '.', 1, 1, 1, 1, false);
    }

    public void printBlueNoisePreview(
            int[][] mask,
            char onChar,
            char offChar,
            int scaleX,
            int scaleY,
            int tileX,
            int tileY,
            boolean drawBorder) {

        // Basic validation
        if (mask == null || mask.length == 0 || mask[0] == null || mask[0].length == 0) {
            System.out.println("Empty or null mask.");
            return;
        }
        final int H = mask.length;
        final int W = mask[0].length;
        for (int y = 1; y < H; y++) {
            if (mask[y] == null || mask[y].length != W) {
                throw new IllegalArgumentException("Mask must be rectangular and non-null.");
            }
        }

        // Normalize parameters
        scaleX = Math.max(1, scaleX);
        scaleY = Math.max(1, scaleY);
        tileX = Math.max(1, tileX);
        tileY = Math.max(1, tileY);

        // Stats
        long onCount = 0;
        int[] rowSum = new int[H];
        int[] colSum = new int[W];

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int v = mask[y][x];
                if (v != 0) {
                    onCount++;
                    rowSum[y]++;
                    colSum[x]++;
                }
            }
        }
        long total = (long) W * H;
        double density = onCount / (double) total;

        int rowMin = W, rowMax = 0, colMin = H, colMax = 0;
        for (int v : rowSum) {
            rowMin = Math.min(rowMin, v);
            rowMax = Math.max(rowMax, v);
        }
        for (int v : colSum) {
            colMin = Math.min(colMin, v);
            colMax = Math.max(colMax, v);
        }

        double rowAvgCount = onCount / (double) H;
        double colAvgCount = onCount / (double) W;

        // Header
        System.out.printf("Blue-noise preview: %dx%d | on=%d off=%d | density=%.4f%n",
                W, H, onCount, (total - onCount), density);
        System.out.printf("Row fill (counts): min=%d avg=%.2f max=%d  | fractions: min=%.4f avg=%.4f max=%.4f%n",
                rowMin, rowAvgCount, rowMax,
                rowMin / (double) W, density, rowMax / (double) W);
        System.out.printf("Col fill (counts): min=%d avg=%.2f max=%d  | fractions: min=%.4f avg=%.4f max=%.4f%n",
                colMin, colAvgCount, colMax,
                colMin / (double) H, density, colMax / (double) H);

        // Visualization
        final int drawWidth = W * scaleX * tileX;

        if (drawBorder) {
            System.out.println("+" + repeat('-', drawWidth) + "+");
        }

        for (int ty = 0; ty < tileY; ty++) {
            for (int y = 0; y < H; y++) {
                for (int sy = 0; sy < scaleY; sy++) {
                    if (drawBorder)
                        System.out.print("|");
                    StringBuilder line = new StringBuilder(drawWidth);
                    for (int tx = 0; tx < tileX; tx++) {
                        for (int x = 0; x < W; x++) {
                            char ch = (mask[y][x] != 0) ? onChar : offChar;
                            for (int sx = 0; sx < scaleX; sx++) {
                                line.append(ch);
                            }
                        }
                    }
                    System.out.print(line.toString());
                    if (drawBorder)
                        System.out.print("|");
                    System.out.println();
                }
            }
        }

        if (drawBorder) {
            System.out.println("+" + repeat('-', drawWidth) + "+");
        }
    }

    private String repeat(char c, int count) {
        if (count <= 0)
            return "";
        char[] arr = new char[count];
        Arrays.fill(arr, c);
        return new String(arr);
    }

}

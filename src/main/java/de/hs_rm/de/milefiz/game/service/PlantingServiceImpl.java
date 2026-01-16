package de.hs_rm.de.milefiz.game.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Position;
import de.hs_rm.de.milefiz.game.model.PositionFloat;
import de.hs_rm.de.milefiz.game.model.TreeType;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO.FieldDTO;

/**
 * Implementation des {@link PlantingService}
 * 
 * Diese Klasse stellt in erster Linie die Methode
 * {@link #plantTrees(BoardDTO, float)} bereit, die zum Pflanzen von Bäumen
 * genutzt werden kann.
 * 
 * Außerdem enthält sie alle dafür notwendigen Methoden.
 * 
 * <p>
 * Zum Pflanzen der Bäume wird ein blaues Rauschen verwendet, welches mit dem
 * void und cluster Algorithmus erstellt wird.
 * 
 * @author Thilo Wittmer
 */

@Service
public class PlantingServiceImpl implements PlantingService {

    private final Logger logger = LoggerFactory.getLogger(PlantingService.class);

    /**
     * pflanzt bäume auf das BoardDTO mit der angegebene density.
     * 
     * Es gibt in der Methode noch ein paar parameter, die das Verhalten
     * beeinflussen
     * 
     * @param boardDTO das BoardDTO, wo Bäume gepflanzt werden sollen
     * @param density  die Dichte mit der Bäume gepflanzt werden sollen
     * 
     * @return das BoardDTO mit den gepflanzten Bäumen
     */
    @Override
    public BoardDTO plantTrees(BoardDTO boardDTO, float density) {

        logger.info("start planting trees");

        // Parameter die noch angepasst werden können

        // Rand um die Felder herum, wo noch Bäume gepflanzt werden sollen
        final int TREE_BORDER = 65;

        // wieviele Bäume können innerhalb einer koordinaten einheit stehen. bestimmt,
        // wie nah Bäume beieinander stehen
        // dadurch werden auch die menge der Bäume geändert: je näher die Bäume
        // beieinander stehen können, desto mehr wird es geben
        final int TREES_PER_COORD = 2;

        // wert zwischen 0 und 1. wieviel weniger Bäume sollen am rand stehen?
        // 0.5 == 50% weniger
        final double LESS_TREES_ON_BORDER = 0.99;

        final int NO_OF_TYPES = TreeType.values().length;
        boardDTO.deleteAllTrees();

        int[] minPos = getMinPos(boardDTO);
        int[] maxPos = getMaxPos(boardDTO);

        // // ursprung des koordinatensystems auf 0 und lässt einen rand um die
        // // felder
        // for (FieldDTO field : boardDTO.getFields()) {
        // Position p = field.getPosition();
        // field.setPosition(new Position(p.getX() + TREE_BORDER - minPos[0], p.getY() +
        // TREE_BORDER - minPos[1]));

        // }

        // minPos = getMinPos(boardDTO);
        // maxPos = getMaxPos(boardDTO);

        //

        int boardWidth = maxPos[0] - minPos[0];
        int boardHeight = maxPos[1] - minPos[1];

        int offsetX = TREE_BORDER - minPos[0];
        int offsetY = TREE_BORDER - minPos[1];

        int[][] blockedByPath = getBlockedPositions(boardDTO, minPos, offsetX, offsetY);

        int totalWidth = (boardWidth + 2 * TREE_BORDER) * TREES_PER_COORD;
        int totalHeight = (boardHeight + 2 * TREE_BORDER) * TREES_PER_COORD;

        int[][] blueNoise = generateBlueNoiseVoidCluster(density, totalWidth, totalHeight);

        for (int i = 0; i < blueNoise.length; i++) {
            for (int j = 0; j < blueNoise[0].length; j++) {
                if (blueNoise[i][j] == 1) {
                    // Original-Koordinaten - KEINE VERSCHIEBUNG
                    float x = (j / (float) TREES_PER_COORD) - TREE_BORDER + minPos[0];
                    float y = (i / (float) TREES_PER_COORD) - TREE_BORDER + minPos[1];

                    // Für Blockierungs-Check
                    float xShifted = x + offsetX;
                    float yShifted = y + offsetY;

                    int xFloor = (int) Math.floor(xShifted);
                    int xCeil = (int) Math.ceil(xShifted);
                    int yFloor = (int) Math.floor(yShifted);
                    int yCeil = (int) Math.ceil(yShifted);

                    // Blockierung prüfen
                    if (xFloor >= 0 && xCeil < blockedByPath.length &&
                            yFloor >= 0 && yCeil < blockedByPath[0].length) {
                        int isBlocked = blockedByPath[xFloor][yFloor] +
                                blockedByPath[xFloor][yCeil] +
                                blockedByPath[xCeil][yFloor] +
                                blockedByPath[xCeil][yCeil];
                        if (isBlocked > 0) {
                            continue;
                        }
                    }

                    // Innerhalb oder außerhalb?
                    boolean insideBoard = (x >= minPos[0] && x <= maxPos[0] &&
                            y >= minPos[1] && y <= maxPos[1]);

                    if (insideBoard) {
                        // Innerhalb: nur am direkten Rand filtern
                        int distToEdge = Math.min(
                                Math.min((int) (x - minPos[0]), (int) (maxPos[0] - x)),
                                Math.min((int) (y - minPos[1]), (int) (maxPos[1] - y)));

                        if (distToEdge < 4 && Math.random() < 0.5) { // 50% am Rand
                            continue;
                        }
                    } else {
                        // Außerhalb: stark filtern mit LESS_TREES_ON_BORDER
                        if (Math.random() < LESS_TREES_ON_BORDER) {
                            continue;
                        }
                    }

                    // Baum pflanzen - ORIGINAL-KOORDINATEN
                    double rand = Math.random();
                    int typeInd = (int) (rand * NO_OF_TYPES);
                    TreeType treeType = TreeType.values()[typeInd];
                    boardDTO.addTree(new PositionFloat(x, y), treeType);
                }
            }
        }

        return boardDTO;
    }

    /**
     * ermittelt die Positionen, die durch den Weg für das Bäumepflanzen blockiert
     * sein sollen
     * 
     * @param boardDTO    das board
     * @param boundingBox die boundingbox der felder
     *                    <p>
     *                    siehe {@link #getMaxPos(BoardDTO)}
     * @return array, wo die die indizes der blockierten positionen auf 1 gesetzt
     *         sind
     */
    private int[][] getBlockedPositions(BoardDTO boardDTO, int[] minPos, int offsetX, int offsetY) {
        int[] maxPos = getMaxPos(boardDTO);
        int width = maxPos[0] - minPos[0] + 2 * 200; // +Border
        int height = maxPos[1] - minPos[1] + 2 * 200;

        int[][] res = new int[width + 4][height + 4];

        for (FieldDTO field : boardDTO.getFields()) {
            Position p = field.getPosition();
            // Offset anwenden für Blockierungs-Array
            int x = p.getX() + offsetX;
            int y = p.getY() + offsetY;

            if (x >= 0 && x < res.length && y >= 0 && y < res[0].length) {
                res[x][y] = 1;

                if (field.getType().isStart() || field.getType().isEnd()) {
                    res = blockSourrounding(2, x, y, res);
                    continue;
                }

                if (field.getNorth() != null && y + 1 < res[0].length) {
                    res[x][y + 1] = 1;
                }
                if (field.getEast() != null && x + 1 < res.length) {
                    res[x + 1][y] = 1;
                }
                if (field.getSouth() != null && y > 0) {
                    res[x][y - 1] = 1;
                }
                if (field.getWest() != null && x > 0) {
                    res[x - 1][y] = 1;
                }
            }
        }

        return res;
    }

    private int[][] blockSourrounding(int dist, int x, int y, int[][] res) {
        if (res == null || res.length == 0 || res[0] == null)
            return res;

        final int width = res.length;
        final int height = res[0].length;

        int xStart = Math.max(0, x - dist);
        int xEnd = Math.min(width - 1, x + dist);
        int yStart = Math.max(0, y - dist);
        int yEnd = Math.min(height - 1, y + dist);

        for (int xi = xStart; xi <= xEnd; xi++) {
            for (int yi = yStart; yi <= yEnd; yi++) {
                res[xi][yi] = 1;
            }
        }
        return res;
    }

    /**
     * ermittelt oberste rechte ecke der bounding box für die Felder des boards
     * 
     * @param boardDTO board für das die bounding box ermittelt werden soll
     * @return int[] wo int[0] x wert und int[1] der y wert der ecke ist
     */
    private int[] getMaxPos(BoardDTO boardDTO) {
        int x = 0;
        int y = 0;
        for (FieldDTO field : boardDTO.getFields()) {
            x = field.getPosition().getX() > x ? field.getPosition().getX() : x;
            y = field.getPosition().getY() > y ? field.getPosition().getY() : y;
        }
        int[] res = new int[2];
        res[0] = x;
        res[1] = y;
        return res;
    }

    /**
     * ermittelt unterste linke ecke der bounding box für die Felder des boards
     * 
     * @param boardDTO board für das die bounding box ermittelt werden soll
     * @return int[] wo int[0] x wert und int[1] der y wert der ecke ist
     */
    private int[] getMinPos(BoardDTO boardDTO) {
        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;
        for (FieldDTO field : boardDTO.getFields()) {
            x = field.getPosition().getX() < x ? field.getPosition().getX() : x;
            y = field.getPosition().getY() < y ? field.getPosition().getY() : y;
        }
        int[] res = new int[2];
        res[0] = x;
        res[1] = y;
        return res;
    }

    /**
     * wenn man nur densitiy, breite und höhe setzen möchte
     * 
     * @param density gewünschter Anteil an gesetzten Pixeln in [0,1]
     * @param width   Bildbreite > 0
     * @param height  Bildhöhe > 0
     * @return int[height][width] mit 0/1 Blue-Noise-Muster
     * 
     */
    public int[][] generateBlueNoiseVoidCluster(float density, int width, int height) {
        return generateBlueNoiseVoidCluster(density, width, height, 0, System.nanoTime());
    }

    /**
     * full control version
     *
     * @param density      gewünschter Anteil an gesetzten Pixeln in [0,1]
     * @param width        Bildbreite > 0
     * @param height       Bildhöhe > 0
     * @param kernelRadius Nachbarschaftsradius; falls kleiner gleich 0 wird eine
     *                     Heuristik
     *                     genutzt
     * @param seed         RNG-Seed (für reproduzierbare Ergebnisse)
     * @return int[height][width] mit 0/1 Blue-Noise-Muster
     */
    public int[][] generateBlueNoiseVoidCluster(float density, int width, int height, int kernelRadius,
            long seed) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid dimensions");
        }
        if (Float.isNaN(density) || Float.isInfinite(density)) {
            throw new IllegalArgumentException("Invalid density");
        }
        density = Math.max(0f, Math.min(1f, density));
        final int n = width * height;

        // Triviale Fälle
        if (density == 0f) {
            return new int[height][width];
        }
        if (density == 1f) {
            int[][] ones = new int[height][width];
            for (int y = 0; y < height; y++) {
                Arrays.fill(ones[y], 1);
            }
            return ones;
        }

        // Zufallszahlengenerator
        final Random rng = new Random(seed);

        // Kernel
        final int radius = (kernelRadius > 0) ? kernelRadius : Math.max(2, Math.min(width, height) / 8);

        // kann man auch noch anpassen. Bestimmt, wie stark "gruppiert" das Rauschen ist
        final double sigma = Math.max(0.75, radius / 2.0);
        final List<Offset> kernel = buildGaussianKernel(radius, sigma);

        // Status-Arrays
        final boolean[][] occ = new boolean[height][width]; // belegt (true) vs frei (false)
        final int[][] order = new int[height][width]; // Rangordnung
        final double[][] energyMap = new double[height][width];

        // Initialisierung
        for (int y = 0; y < height; y++) {
            Arrays.fill(order[y], -1);
        }
        seedHalf(occ, rng);

        // Initiale energy map erstellen
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (occ[y][x]) {
                    addKernelAt(energyMap, x, y, kernel, +1.0, width, height);
                }
            }
        }

        // Rangordnung durch abwechselndes Entfernen von Clustern und Einfügen von voids
        int low = 0, high = n - 1;
        while (low <= high) {
            boolean progressed = false;

            // Cluster-Schritt: Entferne das am stärksten gebündelte belegte, nicht
            // zugewiesene Pixel
            int[] pCluster = argmaxOccupiedUnassigned(energyMap, occ, order);
            if (pCluster != null) {
                int cx = pCluster[0], cy = pCluster[1];
                occ[cy][cx] = false;
                order[cy][cx] = high--;
                addKernelAt(energyMap, cx, cy, kernel, -1.0, width, height);
                progressed = true;
            }

            // void-Schritt: Füge am leersten, nicht zugewiesenen Pixel hinzu
            if (low <= high) {
                int[] pVoid = argminEmptyUnassigned(energyMap, occ, order);
                if (pVoid != null) {
                    int vx = pVoid[0], vy = pVoid[1];
                    occ[vy][vx] = true;
                    order[vy][vx] = low++;
                    addKernelAt(energyMap, vx, vy, kernel, +1.0, width, height);
                    progressed = true;
                }
            }

            // Falls kein Schritt Fortschritt, abbrechen
            if (!progressed)
                break;
        }

        // Fallback: Falls noch etwas nicht zugewiesen ist (sollte nicht vorkommen), in
        // Scan-Reihenfolge zuweisen
        AtomicBoolean foundUnassigned = new AtomicBoolean(false);
        forEachPixel(width, height, (x, y) -> {
            if (order[y][x] == -1)
                foundUnassigned.set(true);
        });
        if (foundUnassigned.get()) {
            int i = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (order[y][x] == -1) {
                        order[y][x] = i++;
                    }
                }
            }
        }

        // Rangordnung nach angeforderter Dichte threshholden
        final int thresholdCount = Math.round(density * n);
        final int[][] out = new int[height][width];
        forEachPixel(width, height, (x, y) -> {
            out[y][x] = (order[y][x] < thresholdCount) ? 1 : 0;
        });
        return out;
    }

    // Erzeuge Gaußschen Kernel mit summe 1 und normierten Gewichten
    private List<Offset> buildGaussianKernel(int r, double sigma) {
        List<Offset> list = new ArrayList<>();
        double twoSigma2 = 2.0 * sigma * sigma;
        double sum = 0.0;
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                if (dx == 0 && dy == 0)
                    continue; // exclude self
                double w = Math.exp(-(dx * dx + dy * dy) / twoSigma2);
                if (w > 1e-12) {
                    list.add(new Offset(dx, dy, w));
                    sum += w;
                }
            }
        }
        // normalisieren
        if (sum > 0) {
            for (Offset o : list)
                o.w /= sum;
        }
        return list;
    }

    // Aktualisiert die energy map, indem der Kernel (sign=+1) addiert oder
    // (sign=-1) subtrahiert wird, zentriert bei (x,y)
    private void addKernelAt(double[][] E, int x, int y, List<Offset> kernel, double sign, int W, int H) {
        for (Offset o : kernel) {
            int nx = wrap(x + o.dx, W);
            int ny = wrap(y + o.dy, H);
            E[ny][nx] += sign * o.w;
        }
    }

    // Finde das belegte, nicht zugewiesene Pixel mit maximaler Energie
    private int[] argmaxOccupiedUnassigned(double[][] E, boolean[][] occ, int[][] order) {
        double best = -Double.MAX_VALUE;
        int bx = -1, by = -1;
        int H = E.length, W = E[0].length;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (occ[y][x] && order[y][x] == -1) {
                    double v = E[y][x];
                    if (v > best) {
                        best = v;
                        bx = x;
                        by = y;
                    }
                }
            }
        }
        return (bx < 0) ? null : new int[] { bx, by };
    }

    // Finde das freie, nicht zugewiesene Pixel mit minimaler Energie
    private int[] argminEmptyUnassigned(double[][] E, boolean[][] occ, int[][] order) {
        double best = Double.MAX_VALUE;
        int bx = -1, by = -1;
        int H = E.length, W = E[0].length;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (!occ[y][x] && order[y][x] == -1) {
                    double v = E[y][x];
                    if (v < best) {
                        best = v;
                        bx = x;
                        by = y;
                    }
                }
            }
        }
        return (bx < 0) ? null : new int[] { bx, by };
    }

    private int wrap(int v, int n) {
        int m = v % n;
        return (m < 0) ? m + n : m;
    }

    private void seedHalf(boolean[][] occ, Random rng) {
        int H = occ.length, W = occ[0].length;
        int target = (W * H) / 2;
        int[] idx = new int[W * H];
        for (int i = 0; i < idx.length; i++)
            idx[i] = i;
        shuffle(idx, rng);
        for (int k = 0; k < target; k++) {
            int i = idx[k];
            int y = i / W;
            int x = i % W;
            occ[y][x] = true;
        }
    }

    private void shuffle(int[] a, Random rng) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }

    private void forEachPixel(int W, int H, PixelConsumer pc) {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                pc.accept(x, y);
            }
        }
    }

    private interface PixelConsumer {
        void accept(int x, int y);
    }

    private final class Offset {
        final int dx, dy;
        double w;

        Offset(int dx, int dy, double w) {
            this.dx = dx;
            this.dy = dy;
            this.w = w;
        }
    }

}

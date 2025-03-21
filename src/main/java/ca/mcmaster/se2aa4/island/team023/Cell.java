package ca.mcmaster.se2aa4.island.team023;

import java.util.List;
import java.util.ArrayList;

public class Cell {

    public Point<Integer> location;

    public String id;
    public Biomes biome;

    List<String> creeks = new ArrayList<>();
    List<String> sites = new ArrayList<>();

    public Cell(int x, int y, String id, String biome) {
        location = new Point<>(x, y);
        this.id = id;
        // this.biome = Biomes.valueOf(biome);
    }

}

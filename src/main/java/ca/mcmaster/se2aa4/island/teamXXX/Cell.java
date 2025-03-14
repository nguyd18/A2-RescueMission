package ca.mcmaster.se2aa4.island.teamXXX;

public class Cell {

    public Point<Integer> location;
    public Point<Double> poiLocation;

    public Cell north;
    public Cell east;
    public Cell south;
    public Cell west;

    public String id;
    public Biomes biome;

    public Cell(int x, int y, String id, String biome) {
        location = new Point<>(x, y);
        this.id = id;
        this.biome = Biomes.valueOf(biome);
    }

}

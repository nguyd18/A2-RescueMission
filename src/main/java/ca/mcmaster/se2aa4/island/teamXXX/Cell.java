package ca.mcmaster.se2aa4.island.teamXXX;

public class Cell {
    
    private int x;
    private int y;

    public Cell north;
    public Cell east;
    public Cell south;
    public Cell west;

    public String id;
    public Biomes biome;

    public Cell(int x, int y, String id, String biome) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.biome = Biomes.valueOf(biome);
    }

}

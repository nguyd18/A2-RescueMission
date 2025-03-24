package ca.mcmaster.se2aa4.island.team023;

/*
 * Represents a cell that only contains ocean
 */
public class OceanCell extends Cell {

    public OceanCell(int x, int y) {
        super(x, y);
    }
    
    @Override
    public boolean isGround() {
        return false;
    }
}

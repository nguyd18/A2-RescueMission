package ca.mcmaster.se2aa4.island.team023;

/*
 * Represents a cell of the map that is known to be ground but the detailed information is still unknown
 */
public class GroundCell extends Cell{

    public GroundCell(int x, int y, String id) {
        super(x, y, id);
    }
    
    @Override
    public boolean isGround() {
        return true;
    }    
}

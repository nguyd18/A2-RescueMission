package ca.mcmaster.se2aa4.island.team023;

import java.util.Collections;

public class DetailedGroundCell extends GroundCell {

    public DetailedGroundCell(int x, int y, String[] creeks, String[] sites) {
        super(x, y);
        Collections.addAll(this.creeks, creeks);
        Collections.addAll(this.sites, sites);
    }

}

package ca.mcmaster.se2aa4.island.team023;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailedGroundCell extends GroundCell implements DetailedCell {
    
    private List<String> creeks = new ArrayList<>();
    private List<String> sites = new ArrayList<>();

    public DetailedGroundCell(int x, int y, String[] creeks, String[] sites) {
        super(x, y);
        Collections.addAll(this.creeks, creeks);
        Collections.addAll(this.sites, sites);
    }
    
    public List<String> getCreeks() {
        return creeks;
    }

    public List<String> getSites() {
        return sites;
    }
}

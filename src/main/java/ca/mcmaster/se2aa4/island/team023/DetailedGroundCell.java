package ca.mcmaster.se2aa4.island.team023;

import java.util.ArrayList;
import java.util.List;

public class DetailedGroundCell extends GroundCell implements DetailedCell {
    
    private List<String> creeks = new ArrayList<>();
    private List<String> sites = new ArrayList<>();

    public DetailedGroundCell(int x, int y, String id) {
        super(x, y, id);
    }
    
    public List<String> getCreeks() {
        return creeks;
    }

    public List<String> getSites() {
        return sites;
    }
}

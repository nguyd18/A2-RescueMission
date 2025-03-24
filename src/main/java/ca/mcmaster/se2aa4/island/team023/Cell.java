package ca.mcmaster.se2aa4.island.team023;

import java.util.ArrayList;
import java.util.List;

public abstract class Cell {

    public Point<Integer> location;

    protected List<String> creeks = new ArrayList<>();
    protected List<String> sites = new ArrayList<>();

    public Cell(int x, int y) {
        location = new Point<>(x, y);
    }

    public abstract boolean isGround();

    public List<String> getCreeks() {
        return creeks;
    }

    public List<String> getSites() {
        return sites;
    }
}

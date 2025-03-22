package ca.mcmaster.se2aa4.island.team023;

public abstract class Cell {

    public Point<Integer> location;

    public String id;

    public Cell(int x, int y, String id) {
        location = new Point<>(x, y);
        this.id = id;
    }

    public abstract boolean isGround();


}

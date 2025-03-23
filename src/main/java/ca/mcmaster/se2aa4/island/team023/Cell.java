package ca.mcmaster.se2aa4.island.team023;

public abstract class Cell {

    public Point<Integer> location;

    public Cell(int x, int y) {
        location = new Point<>(x, y);
    }

    public abstract boolean isGround();


}

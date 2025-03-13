package ca.mcmaster.se2aa4.island.teamXXX;

/*
 * A class for respresenting 2D integer cartesian coordinates
 * Objects are immutable and can be compared with equals()
 */
public record Point(int x, int y) {

    public static Point add(Point a, Point b) {
        return new Point(a.x() + b.x(), a.y() + b.y());
    }

    public double getMagnitude() {
        return Math.sqrt(x*x + y*y);
    }
}

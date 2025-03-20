package ca.mcmaster.se2aa4.island.team023;

/*
 * A class for respresenting 2D integer cartesian coordinates
 * Objects are immutable and can be compared with equals()
 */
public record Point<T extends Number>(T x, T y) {

    public double getMagnitude() {
        return Math.sqrt(Math.pow((double)x, 2) + Math.pow((double)y, 2));
    }
    
    public static Point<Integer> addInts(Point<Integer> a, Point<Integer> b) {
        return new Point<>(a.x() + b.x(), a.y() + b.y());
    }
    
    public static Point<Double> addDoubles(Point<Double> a, Point<Double> b) {
        return new Point<>(a.x() + b.x(), a.y() + b.y());
    }
}

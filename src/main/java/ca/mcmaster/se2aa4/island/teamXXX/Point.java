package ca.mcmaster.se2aa4.island.teamXXX;

/*
 * A class for respresenting 2D integer cartesian coordinates
 * Objects are immutable and can be compared with equals()
 */
public record Point<T extends Number>(T x, T y) {

    public double getMagnitude() {
        return Math.sqrt(Math.pow((double)x, 2) + Math.pow((double)y, 2));
    }
}

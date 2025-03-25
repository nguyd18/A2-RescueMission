package ca.mcmaster.se2aa4.island.team023;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ExampleTest {

    @Test
    public void checkHeadingTurnLeft() {
        Heading heading = new Heading("E");
        heading.turnCounterClockwise();
        assertTrue(heading.getHeadingState() == Heading.HeadingStates.N);
    }

    @Test
    public void checkHeadingTurnRight() {
        Heading heading = new Heading("E");
        heading.turnClockwise();
        assertTrue(heading.getHeadingState() == Heading.HeadingStates.S);
    }

    @Test
    public void checkHeadingNextPoint() {
        Heading heading = new Heading("E");
        Point<Integer> nextP = new Point<Integer>(1, 0);
        assertTrue(heading.getHeadingState().getNextPoint().equals(nextP));
    }

    @Test
    public void checkPointEquality() {
        Point<Integer> p1 = new Point<Integer>(1, 0);
        Point<Integer> p2 = new Point<Integer>(1, 0);
        assertTrue(p1.equals(p2));
    }

    @Test
    public void checkPointAddition() {
        Point<Integer> p1 = new Point<Integer>(1, 0);
        Point<Integer> p2 = new Point<Integer>(5, -2);
        Point<Integer> res = new Point<Integer>(6, -2);
        assertTrue(res.equals(Point.addInts(p1, p2)));
    }

}

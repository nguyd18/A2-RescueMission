package ca.mcmaster.se2aa4.island.team023;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.json.JSONObject;


public class ExampleTest {

    @Test
    public void sampleTest() {
        System.out.println("sampleTest ran!");
        assertTrue(1 == 1);
    }

    @Test
    public void testTurnLeftProducesCorrectAction() {
        Drone drone = new Drone("E", 1000);
        JSONObject action = drone.turnLeft();

        assertEquals("heading", action.getString("action"));
        assertEquals("N", action.getJSONObject("parameters").getString("direction"));  // E turned left = N
    }

    @Test
    public void testForwardChangesPosition() {
        Drone drone = new Drone("E", 1000);
        JSONObject action = drone.forward();

        assertEquals("fly", action.getString("action")); // you can’t directly get position unless you expose it for testing (e.g., via a getter)
    }




}

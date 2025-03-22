package ca.mcmaster.se2aa4.island.team023;

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.team023.Heading.HeadingStates;

public class Drone extends Aircraft {
    private IMap map = new Map();
    private Point<Integer> relativePos;
    private Logger logger = LogManager.getLogger();
    private boolean groundDetected;
    private boolean firstRun;
    private boolean isIsland;

    private Queue<JSONObject> actions = new ArrayDeque<>();

    public Drone(String heading, int fuelCap) {
        super(heading, fuelCap);
        relativePos = new Point<>(0, 0);
        firstRun = true;
        groundDetected = false;
        isIsland = false;
    }

    /*
     * Called every game loop, this method returns a JSONObject representing the drone's chosen action
     */
    public JSONObject makeDecision() {
        // nothing here yet
        if (firstRun) {
            firstRun = false;
        }

        // if action remaining in queue execute
        if (!actions.isEmpty()) return actions.remove();

        // actions done per cell
        if (groundDetected){ // if ground was detected by echo, scan below
            actions.add(scan());
        } else { // if ground was not detected by echo, keep echoing until ground is detected
            actions.add(radar(heading.getHeadingState().next()));
        }

        if (isIsland) { // if the drone is flying over the island, scan below, and echo forward
            actions.add(radar(heading.getHeadingState()));
        }

        // move forward by default
        actions.add(forward());

        return actions.remove();
    }
   
    /*
     * Called after every action, this method updates values in response to the return JSONObject
     */
    public void update(JSONObject response) {
        map.placeCell(relativePos.x(), relativePos.y(), response);
		// update battery
		updateBattery(response);
        
        try {
            // Step 1: If the drone detects the island, turn right to face the island
            if (!groundDetected && response.getJSONObject("extras").getString("found").equals("GROUND")) {
                groundDetected = true;
                actions.clear();
                actions.add(turnRight());
                return;
            }
            // Step 2: If the drone is flying over the island, update state
            if (groundDetected && !isIsland && !response.getJSONObject("extras").getJSONArray("biomes").get(0).equals("OCEAN")) {
                isIsland = true;
            }
            // Step 3: Check radar response to decide whether to stop
            if (isIsland && response.has("extras") && response.getJSONObject("extras").has("found")) {
                String terrainAhead = response.getJSONObject("extras").getString("found");
    
                if (!terrainAhead.equals("GROUND")) {
                    actions.clear();
                    actions.add(stop());
                }
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        // stop if not on ocean
        // try {
        //     if (!response.getJSONObject("extras").getJSONArray("biomes").get(0).equals("OCEAN")){
        //         actions.clear();
        //         actions.add(stop());
        //     }
        // } catch (Exception e) {
        //     logger.error(e.getMessage());
        // }
    }
   
    // Action to fly forward
    protected JSONObject forward() {
        JSONObject action = new JSONObject();
        action.put("action", "fly");
        relativePos = Point.addInts(relativePos, heading.getHeadingState().getNextPoint());
        return action;
    }

    protected JSONObject turnLeft() {
        // TODO encapsulate this
        relativePos = Point.addInts(relativePos, heading.getHeadingState().getNextPoint());
        heading.turnCounterClockwise();
        relativePos = Point.addInts(relativePos, heading.getHeadingState().getNextPoint());

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "heading");
        direction.put("direction", heading.getHeadingState().toString());
        action.put("parameters", direction);

        return action;
    }

    protected JSONObject turnRight() {

        // TODO encapsulate this
        relativePos = Point.addInts(relativePos, heading.getHeadingState().getNextPoint());
        heading.turnClockwise();
        relativePos = Point.addInts(relativePos, heading.getHeadingState().getNextPoint());

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "heading");
        direction.put("direction", heading.getHeadingState().toString());
        action.put("parameters", direction);

        return action;
    }

    protected JSONObject radar(HeadingStates dir) {

        // assert dir != heading.getHeadingState().next().next() : "Invalid echo. Drone tried to send radar backwards";

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "echo");
        direction.put("direction", dir.toString());
        action.put("parameters", direction);
        return action;
    }

    protected JSONObject scan() {
        JSONObject action = new JSONObject();
        action.put("action", "scan");
        return action;
    }

    protected JSONObject stop() {
        JSONObject action = new JSONObject();
        action.put("action", "stop");
        return action;
    }

	protected void updateBattery(JSONObject response) {
		try {
			int cost = response.getInt("cost");
			fuel -= cost;
			logger.info("Current battery level: " + fuel);
		} catch (JSONException e) {
			logger.error(e.getMessage());
		}
	}
}

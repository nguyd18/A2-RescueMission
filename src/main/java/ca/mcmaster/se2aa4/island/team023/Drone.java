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
  
    protected String creekID;
    protected String siteID;
    // flags
    private boolean groundDetected;
    private boolean firstRun;
    private boolean isFlyingOverIsland;
    private boolean isFlyingDownwards;
    private boolean droneHasTurnedAround;
    private boolean firstphase;
    private boolean startOfSecondPhase;
  
    private boolean foundCreek;
    private boolean foundEmergencySite;

    private Queue<JSONObject> actions = new ArrayDeque<>();

    public Drone(String heading, int fuelCap) {
        super(heading, fuelCap);
        relativePos = new Point<>(0, 0);
        firstRun = true;
        groundDetected = false;
        isFlyingOverIsland = false;
        isFlyingDownwards = false;
        droneHasTurnedAround = false;
        firstphase = true;
        startOfSecondPhase = false;
      
        foundCreek = false;
        foundEmergencySite = false;
    
    }

    /*
     * Called every game loop, this method returns a JSONObject representing the drone's chosen action
     */
    public JSONObject makeDecision() {
        logger.info("**** Making a decision...");
        // nothing here yet
        if (firstRun) {
            firstRun = false;
        }

        // if action remaining in queue execute
        if (!actions.isEmpty()) return actions.remove();

        // actions done per cell
        if (groundDetected){ // if ground was detected by echo, scan below
            logger.info("**** Ground detected. Scanning below");
            actions.add(scan());
        } else if (!groundDetected && firstphase){ // if ground was not detected by echo, keep echoing until ground is detected
            logger.info("**** Ground not detected. Echo to find the island");
            actions.add(radar(heading.getHeadingState().next()));
        } else {
            // this is the starting of the second phase
            if (isFlyingDownwards) {
                logger.info("**** Facing " + heading.getHeadingState() + ". Echoing " + heading.getHeadingState().next());
                actions.add(radar(heading.getHeadingState().next()));
            } else {
                logger.info("**** Facing " + heading.getHeadingState() + ". Echoing " + heading.getHeadingState().prev());
                actions.add(radar(heading.getHeadingState().prev()));
            }
        }
      
        if (isFlyingOverIsland) { // if the drone is flying over the island, scan below, and echo forward
            // TODO wasting battery by calling radar everytime
            // TODO we should call radar when the drone is over the ocean
            logger.info("**** Flying over island. Echoing forward");
            actions.add(radar(heading.getHeadingState()));
        }

        if (droneHasTurnedAround) { // if the drone has turned around, echo forward
            logger.info("**** Drone has made a 180. Echoing forward");
            actions.add(radar(heading.getHeadingState()));
        }

        // move forward by default
        logger.info("**** Flying forward");
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
      
        if (fuel < 60) {
            logger.info(creekID);
            logger.info(siteID);
            actions.clear();
            actions.add(stop());
            return;
        }

        try {
            // Find the creek
            if (!foundCreek && response.getJSONObject("extras").has("creeks")){
                var creeks = response.getJSONObject("extras").getJSONArray("creeks");
                if (creeks.length() > 0){
                    creekID = creeks.getString(0);
                    foundCreek = true;
                }
            }
            // Find emergency site
            if (!foundEmergencySite && response.getJSONObject("extras").has("sites")){
                var sites = response.getJSONObject("extras").getJSONArray("sites");
                if (sites.length() > 0) {
                    siteID = sites.getString(0);
                    foundEmergencySite = true;
                }
            }

            // First phase of the algorithm
            if (firstphase) {
                // Step 1: If the drone detects the island, turn right to face the island
                if (!groundDetected && response.getJSONObject("extras").has("found")) {
                    if (response.getJSONObject("extras").getString("found").equals("GROUND")) {
                        logger.info("**** Ground detected for the first time. Turning right to face the island");
                        groundDetected = true;
                        actions.clear();
                        actions.add(turnRight());
                        isFlyingDownwards = true;
                        return;
                    }
                }
            
                // Step 2: If the drone is flying over the island, update state
                if (groundDetected && !isFlyingOverIsland && response.getJSONObject("extras").has("biomes")) {
                    if (!response.getJSONObject("extras").getJSONArray("biomes").get(0).equals("OCEAN")) {
                        logger.info("**** Drone is now flying over the island");
                        isFlyingOverIsland = true;
                    }
                }
            
                // Step 3: Check radar response to decide whether to turn
                // TODO we should also send a radar to the side of the drone to see if there is ground
                // TODO continue flying until the radar doesn't detect ground anymore
                if (isFlyingOverIsland && response.has("extras") && response.getJSONObject("extras").has("found") && !droneHasTurnedAround) {
                    String terrainAhead = response.getJSONObject("extras").getString("found");
        
                    if (!terrainAhead.equals("GROUND")) {
                        logger.info("**** No ground detected ahead. Performing U-turn");
                        actions.clear();
                        if (isFlyingDownwards) {
                            actions.add(turnLeft());
                            actions.add(turnLeft());
                            isFlyingDownwards = false;
                        } else {
                            actions.add(turnRight());
                            actions.add(turnRight());
                            isFlyingDownwards = true;
                        }
                
                        droneHasTurnedAround = true;
                        return;
                    }
                }
            
                // Step 4: If the drone has turned around, echoes forward but doesn't detect ground, stop the drone
                if (droneHasTurnedAround && response.getJSONObject("extras").has("found")) {
                    if (!response.getJSONObject("extras").getString("found").equals("GROUND")) {
                        logger.info("**** No ground detected after U-turn");
                        logger.info(creekID);
                        logger.info(siteID);
                        actions.clear();
                        groundDetected = false;
                        isFlyingOverIsland = false;
                        firstphase = false;
                        startOfSecondPhase = true;
                        droneHasTurnedAround = false;
                        return;
                    } else {
                        droneHasTurnedAround = false;
                        return;
                    }

                }
            } else { // Second phase of the algorithm
                // Step 1: Keep flying forward until the drone doesn't detect the island
                // Turn around and start interlace scanning
                if (startOfSecondPhase && response.getJSONObject("extras").has("found")) {
                    if (!response.getJSONObject("extras").getString("found").equals("GROUND")) {
                        logger.info("**** Moved out of the 1st phase");
                        actions.clear();
                        if (isFlyingDownwards) {
                            actions.add(turnRight());
                            actions.add(forward());
                            actions.add(turnRight());
                            isFlyingDownwards = false;
                        } else {
                            actions.add(turnLeft());
                            actions.add(forward());
                            actions.add(turnLeft());
                            isFlyingDownwards = true;
                        }
                        groundDetected = true;
                        isFlyingOverIsland = true;
                        startOfSecondPhase = false;
                        return;
                    }
                }

                // Step 2: continue interlace scanning
                if (isFlyingOverIsland && response.has("extras") && response.getJSONObject("extras").has("found") && !droneHasTurnedAround) {
                    String terrainAhead = response.getJSONObject("extras").getString("found");
        
                    if (!terrainAhead.equals("GROUND")) {
                        logger.info("**** No ground detected ahead. Performing U-turn");
                        actions.clear();
                        if (isFlyingDownwards) {
                            actions.add(turnRight());
                            actions.add(turnRight());
                            isFlyingDownwards = false;
                        } else {
                            actions.add(turnLeft());
                            actions.add(turnLeft());
                            isFlyingDownwards = true;
                        }
                
                        droneHasTurnedAround = true;
                        return;
                    }
                }

                // Step 3: If the drone has turned around, echoes forward but doesn't detect ground, stop the drone
                if (droneHasTurnedAround && response.getJSONObject("extras").has("found")) {
                    if (!response.getJSONObject("extras").getString("found").equals("GROUND")) {
                        logger.info("**** No ground detected after U-turn");
                        logger.info(creekID);
                        logger.info(siteID);
                        actions.clear();
                        actions.add(stop());
                        return;
                    } else {
                        droneHasTurnedAround = false;
                        return;
                    }
                }
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
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

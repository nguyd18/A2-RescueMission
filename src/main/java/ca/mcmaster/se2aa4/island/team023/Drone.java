package ca.mcmaster.se2aa4.island.team023;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Drone extends Aircraft {
    private IMap map = new Map();
    private Point<Integer> relativePos;
    private Logger logger = LogManager.getLogger();
  
    protected String creekID;
    protected String siteID;
    // flags
    private boolean groundDetected;
    private boolean isFlyingOverIsland;
    private boolean isFlyingDownwards;
    private boolean droneHasTurnedAround;
    private boolean firstphase;
    private boolean startOfSecondPhase;
  
    private boolean foundCreek;
    private boolean foundEmergencySite;

    private DroneActions actions;

    public Drone(String heading, int fuelCap) {
        super(heading, fuelCap);
        relativePos = new Point<>(0, 0);
        groundDetected = false;
        isFlyingOverIsland = false;
        isFlyingDownwards = false;
        droneHasTurnedAround = false;
        firstphase = true;
        startOfSecondPhase = false;
      
        foundCreek = false;
        foundEmergencySite = false;

        actions = new DroneActions(this);
        actions.setDefaultEcho(false, false, true);
        actions.setDefaultMovement(false, true, false);
    
    }

    /*
     * Called every game loop, this method returns a JSONObject representing the drone's chosen action
     */
    public JSONObject makeDecision() {
        logger.info("**** Making a decision...");
      
        // if (isFlyingOverIsland) { // if the drone is flying over the island, scan below, and echo forward
        //     // TODO wasting battery by calling radar everytime
        //     // TODO we should call radar when the drone is over the ocean
        //     logger.info("**** Flying over island. Echoing forward");
        //     actions.add(radar(heading.getHeadingState()));
        // }

        // if (droneHasTurnedAround) { // if the drone has turned around, echo forward
        //     logger.info("**** Drone has made a 180. Echoing forward");
        //     actions.add(radar(heading.getHeadingState()));
        // }

        return actions.nextAction();
    }
   
    /*
     * Called after every action, this method updates values in response to the return JSONObject
     */
    public void update(JSONObject response) {
        map.placeCell(relativePos.x(), relativePos.y(), heading.getHeadingState().getNextPoint().x(), heading.getHeadingState().getNextPoint().y(), response);
		// update battery
		updateBattery(response);
      
        if (fuel < 60) {
            logger.info(creekID);
            logger.info(siteID);
            actions.queueStop();
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
                        actions.addRightTurn();
                        actions.setDefaultEcho(false, false, false);
                        actions.setDefaultScan(true);
                        isFlyingDownwards = true;
                        return;
                    }
                }
            
                // Step 2: If the drone is flying over the island, update state
                if (groundDetected && !isFlyingOverIsland && response.getJSONObject("extras").has("biomes")) {
                    if (!response.getJSONObject("extras").getJSONArray("biomes").get(0).equals("OCEAN")) {
                        logger.info("**** Drone is now flying over the island");
                        isFlyingOverIsland = true;
                        actions.setDefaultEcho(false, true, false);
                    }
                }
            
                // Step 3: Check radar response to decide whether to turn
                // TODO we should also send a radar to the side of the drone to see if there is ground
                // TODO continue flying until the radar doesn't detect ground anymore
                if (isFlyingOverIsland && response.has("extras") && response.getJSONObject("extras").has("found") && !droneHasTurnedAround) {
                    String terrainAhead = response.getJSONObject("extras").getString("found");
        
                    if (!terrainAhead.equals("GROUND")) {
                        logger.info("**** No ground detected ahead. Performing U-turn");
                        if (isFlyingDownwards) {
                            actions.addDoubleLeft();
                            isFlyingDownwards = false;
                        } else {
                            actions.addDoubleRight();
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
                        actions.clearQueue();
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
                        if (isFlyingDownwards) {
                            actions.addLongDoubleRight();
                            actions.setDefaultEcho(false, false, true);
                            isFlyingDownwards = false;
                        } else {
                            actions.addLongDoubleLeft();
                            actions.setDefaultEcho(true, false, false);
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
                        if (isFlyingDownwards) {
                            actions.addDoubleRight();
                            isFlyingDownwards = false;
                        } else {
                            actions.addDoubleLeft();
                            isFlyingDownwards = true;
                        }

                        actions.setDefaultEcho(false, true, false);
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
                        actions.queueStop();
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

    public Heading getHeading() {
        return heading;
    }

    // Point is immutable, so there is no concern of it being modified
    public Point<Integer> getRelativePos() {
        return relativePos;
    }

    public void setRelativePos(Point<Integer> p) {
        relativePos = p;
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

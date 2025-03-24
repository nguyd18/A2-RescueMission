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
    private boolean groundNoLongerDetected;
    private boolean isInBottomPass;
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
        groundNoLongerDetected = false;
        isInBottomPass = false;
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

        return actions.nextAction();
    }
   
    /*
     * Called after every action, this method updates values in response to the return JSONObject
     */
    public void update(JSONObject response) {
        map.placeCell(relativePos.x(), relativePos.y(), heading.getHeadingState().next().getNextPoint().x(), heading.getHeadingState().next().getNextPoint().y(), response);
        logger.info("****** drone x: {}", relativePos.x());
		// update battery
		updateBattery(response);
      
        if (fuel < 50) {
            logger.info(creekID);
            logger.info(siteID);
            logger.info(map.getString());
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

                // Step 1: Fly forward until drone detects ground
                if (!groundDetected && response.getJSONObject("extras").has("found")) {
                    if (response.getJSONObject("extras").getString("found").equals("GROUND")) {
                        logger.info("**** Ground detected for the first time");
                        groundDetected = true;
                        return;
                    }
                }

                // Step 2: Keep flying until there is no ground detected anymore. Drone maps top edge of map with echo
                if (groundDetected && !groundNoLongerDetected && response.getJSONObject("extras").has("found")) {
                    if (!response.getJSONObject("extras").getString("found").equals("GROUND")) {
                        logger.info("**** Ground no longer detected");
                        groundNoLongerDetected = true;
                        actions.clearQueue();
                        actions.addRightTurn();
                        actions.setDefaultEcho(false, false, false);
                        return;
                    }
                }

                // Step 3: fly to bottom of map and turn right
                if (groundNoLongerDetected && !isInBottomPass) {
                    logger.info("**** drone y: {}", relativePos.y());
                    logger.info("**** bottom y: {}", map.getHeight());
                    if (relativePos.y() == map.getHeight() - 3) {
                        actions.clearQueue();
                        actions.addRightTurn();
                        actions.setDefaultEcho(false, false, true);
                        isInBottomPass = true;
                        return;
                    }
                }

                // Step 4: Pass under the island and map bottom edge with echo. Turn right at left edge of island
                logger.info("****left edge: {}", map.getLeftEdge());
                if (isInBottomPass && !isFlyingOverIsland && relativePos.x() < map.getLeftEdge() + 2) {
                    actions.clearQueue();
                    actions.addRightTurn();
                    actions.setDefaultEcho(false, false, false);
                    actions.setDefaultScan(true);
                    isFlyingOverIsland = true;
                    isFlyingDownwards = false;
                    return;
                }

                // enable scan in moments where the drone is actually over land
                if (isFlyingOverIsland) {
                    actions.setDefaultScan(map.highestGroundCellOfColumn(relativePos.x()) <= relativePos.y() && relativePos.y() <= map.lowestGroundCellOfColumn(relativePos.x()));
                }
            
                // Step 5: Turn if the drone is no longer over the island
                if (isFlyingOverIsland && !droneHasTurnedAround ) {

                    if (isFlyingDownwards) {
                        // runs when drone has passed the lowest point of the island in that column or the next column
                        if (relativePos.y() + 1 > Math.max(map.lowestGroundCellOfColumn(relativePos.x()), map.lowestGroundCellOfColumn(relativePos.x() + 2))) {
                            actions.clearQueue();
                            actions.addDoubleLeft();
                            // actions.setDefaultScan(false);  // disable scan on turning, it could overwrite the data from echo
                            isFlyingDownwards = !isFlyingDownwards;
                            droneHasTurnedAround = true;
                            return;
                        }
                    } else {

                        // the case where col2 doesn't have ground must be addressed
                        int col1 = map.highestGroundCellOfColumn(relativePos.x());
                        int col2 = map.highestGroundCellOfColumn(relativePos.x() + 2);
                        if (col2 == -1) col2 = col1;

                        // runs when drone has passed the highest point of the island in that column or the next
                        if (relativePos.y() - 1 < Math.min(col1, col2)) {
                            actions.clearQueue();
                            actions.addDoubleRight();
                            // actions.setDefaultScan(false);  // disable scan on turning, it could overwrite the data from echo
                            isFlyingDownwards = !isFlyingDownwards;
                            droneHasTurnedAround = true;
                            return;
                        }
                    }
                }
            
                // Step 6: If the drone has passed the right edge of the island, start phase two
                if (isFlyingOverIsland && relativePos.x() > map.getRightEdge()) {
                    
                    groundDetected = false;
                    isFlyingOverIsland = false;
                    firstphase = false;
                    startOfSecondPhase = true;
                    droneHasTurnedAround = false;
                    return;

                } else {
                    droneHasTurnedAround = false;
                }
            } else { // Second phase of the algorithm
                // Step 1: Fly until reaching the next turnaround point determined by height of the island
                // Turn around and start interlace scanning
                if (startOfSecondPhase) {
                    if (isFlyingDownwards) {
                        if (relativePos.y() + 1 > map.lowestGroundCellOfColumn(relativePos.x() - 3)) {
                            actions.clearQueue();
                            actions.addLongDoubleRight();
                            isFlyingDownwards = !isFlyingDownwards;
                            groundDetected = true;
                            isFlyingOverIsland = false;
                            startOfSecondPhase = false;
                            return;
                        }
                    } else {
                        if (relativePos.y() - 1 < map.highestGroundCellOfColumn(relativePos.x() - 3)) {
                            actions.clearQueue();
                            actions.addLongDoubleLeft();
                            isFlyingDownwards = !isFlyingDownwards;
                            groundDetected = true;
                            isFlyingOverIsland = true;
                            startOfSecondPhase = false;
                            return;
                        }
                    }
                }

                // enable scan in moments where the drone is actually over land
                // actions.setDefaultScan(true);
                if (isFlyingOverIsland) {
                    actions.setDefaultScan(map.highestGroundCellOfColumn(relativePos.x()) <= relativePos.y() && relativePos.y() <= map.lowestGroundCellOfColumn(relativePos.x()));
                }

                // Step 2: continue interlace scanning
                if (isFlyingOverIsland && !droneHasTurnedAround ) {

                    if (isFlyingDownwards) {
                        // runs when drone has passed the lowest point of the island in that column or the next column
                        if (relativePos.y() + 1 > Math.max(map.lowestGroundCellOfColumn(relativePos.x()), map.lowestGroundCellOfColumn(relativePos.x() - 2))) {
                            actions.addDoubleRight();
                            // actions.setDefaultScan(false);  // disable scan on turning, it could overwrite the data from echo
                            isFlyingDownwards = !isFlyingDownwards;
                            droneHasTurnedAround = true;
                            return;
                        }
                    } else {

                        // the case where col2 doesn't have ground must be addressed
                        int col1 = map.highestGroundCellOfColumn(relativePos.x());
                        int col2 = map.highestGroundCellOfColumn(relativePos.x() - 2);
                        if (col2 == -1) col2 = col1;

                        // runs when drone has passed the highest point of the island in that column or the next
                        if (relativePos.y() - 1 < Math.min(col1, col2)) {
                            actions.addDoubleLeft();
                            // actions.setDefaultScan(false);  // disable scan on turning, it could overwrite the data from echo
                            isFlyingDownwards = !isFlyingDownwards;
                            droneHasTurnedAround = true;
                            return;
                        }
                    }
                }

                // Step 3: If the drone has passed the left edge of the island, end
                if (relativePos.x() < map.getLeftEdge()) {
                    logger.info(map.getString());
                    actions.queueStop();
                    return;
                } else {
                    droneHasTurnedAround = false;
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

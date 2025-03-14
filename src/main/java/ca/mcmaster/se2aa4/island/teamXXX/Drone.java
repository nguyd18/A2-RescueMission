package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Drone extends Aircraft {
	
	private POI[] pOI;
	private IMap iMap;
	private Point<Integer> relativePos;
	private final Logger logger = LogManager.getLogger();

	public Drone(String heading, int fuelCap) {
		super(heading, fuelCap);
		relativePos = new Point<>(0, 0);
	}

	public JSONObject makeDecision() {
		return null;
	}
	
	public void update(JSONObject response) {
		//Figure out what to update, map, fuel, 
		if (response == null){
			System.out.println("Warning: System received a null response.");
			return;
		}

		//Extract cost and update battery level
		int cost = response.optInt("cost", 0);
		this.fuelCap -= cost;
		logger.info("Battery reduced by "+ cost + ". Remaining: "+ this.fuelCap);

		//Extract action status
		String status = response.optString("status", "UNKNOWN");
		logger.info("Action status: "+ status);

		//Check for extra data in the response
		JSOnObject extras = response.optJSONObject("extras");
		if (extra != null){
			//If an echo response, update map details
			if (extra.has("found")){
				String found = extras.getString("found");
				int range = extras.getInt("range");
				logger.info("Radar detected: " + found + " at range: " + range );
				//Update internal map logic if needed
			}	

			// If scan response, extract biome, creeks and emergency sites
			if (extra.has("biomes")){
				logger.info("Scanned Biomes: " + extras,getJSONArrya("biomes"));
			}
			if (extras.has("creeks")){
				logger.info("Creeks found: " + extras.getJSONArray("creeks"));
			}
			if (extras.has("sites")){
				logger.info("Emergency site found: " + extras.getJSONArray("sites"));
			}

		}

	}

	protected void forward() {
		JSONObject command = new JSONObject();
		command.put("action", "fly");
		logger.info("Drone moving forward.");
		return command;
	}

	protected void turnLeft() {
		String newDdirection = getNewDirection("LEFT");

		JSONObject command = new JSONObject();
		command.put("action", "heading");

		JSONObject parameters = new JSONObject();
		parameters.put("direction", newDirection);

		command.put("parameters", parameters);
		logger.info("Drone turning left to face "+ newDirection);

		return command;
	}

	protected void turnRight() {
		String newDirection = getNewDirection("RIGHT");

    	JSONObject command = new JSONObject();
    	command.put("action", "heading");

    	JSONObject parameters = new JSONObject();
    	parameters.put("direction", newDirection);
	
    	command.put("parameters", parameters);
	
    	logger.info("Drone turning right to face " + newDirection);
    
    return command;
	}

	protected void radar() {

	}

	protected void scan() {

	}

}

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

	private boolean firstRun;
	private int count = 0;

	private Queue<JSONObject> actions = new ArrayDeque<>();

	public Drone(String heading, int fuelCap) {
		super(heading, fuelCap);
		relativePos = new Point<>(0, 0);
		firstRun = true;
	}

	/*
	 * Called every game loop, this method returns a JSONObject representing the drone's chosen action
	 */
	public JSONObject makeDecision() {
		if (firstRun) {
			// nothing here yet
			firstRun = false;
		}

		// abort after 50 iterations to stop test drone from flying off the map
		// otherwise take the next action in the queue is there is one
		if (count > 50) {
			actions.clear();
			return stop();
		}

		// if action remaining in queue execute
		if (!actions.isEmpty()) return actions.remove();

		// actions done per cell
		actions.add(scan());
		actions.add(radar(heading.getHeadingState().next()));
		actions.add(forward());

		count++;
		return actions.remove();
	}
	
	/*
	 * Called after every action, this method updates values in response to the return JSONObject
	 */
	public void update(JSONObject response) {
		map.placeCell(relativePos.x(), relativePos.y(), response);
		try {
			if (response.getJSONObject("extras").get("found").equals("GROUND")) {
				actions.clear();
				actions.add(turnRight());
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

}

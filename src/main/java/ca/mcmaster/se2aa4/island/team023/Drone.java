package ca.mcmaster.se2aa4.island.team023;

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Drone extends Aircraft {
	
	private POI[] pOI;
	private IMap map = new Map();
	private Point<Integer> relativePos;
	private Logger logger = LogManager.getLogger();

	private int count = 0;
	private boolean scanFlag = true;
	private boolean radarFlag = false;

	private Queue<JSONObject> actions = new ArrayDeque<>();

	public Drone(String heading, int fuelCap) {
		super(heading, fuelCap);
		relativePos = new Point<>(0, 0);
	}

	public JSONObject makeDecision() {
		if (count > 50) {
			actions.clear();
			return stop();
		}
		if (!actions.isEmpty()) return actions.remove();

		actions.add(scan());
		actions.add(radar());
		actions.add(forward());

		count++;
		return actions.remove();
	}
	
	public void update(JSONObject response) {
		map.placeCell(relativePos.x(), relativePos.y(), response);
		scanFlag = !scanFlag;
	}

	protected JSONObject forward() {
		JSONObject action = new JSONObject();
		action.put("action", "fly");
		relativePos = new Point<>(relativePos.x() + heading.getHeadingState().getNextPoint().x(), relativePos.y() + heading.getHeadingState().getNextPoint().y());
		return action;
	}

	protected void turnLeft() {

	}

	protected void turnRight() {

	}

	protected JSONObject radar() {
		JSONObject action = new JSONObject();
		JSONObject direction = new JSONObject();
		action.put("action", "echo");
		direction.put("direction", heading.getHeadingState().next().toString());
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

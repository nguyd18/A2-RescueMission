package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Drone extends Aircraft {
	
	private POI[] pOI;
	private IMap iMap;
	private Point relativePos;

	public Drone(String heading, int fuelCap) {
		super(heading, fuelCap);
		relativePos = new Point(0, 0);
	}

	public JSONObject makeDecision() {
		return null;
	}
	
	public void update(JSONObject response) {
		
	}

	protected void forward() {

	}

	protected void turnLeft() {

	}

	protected void turnRight() {

	}

	protected void radar() {

	}

	protected void scan() {

	}

}

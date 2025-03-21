package ca.mcmaster.se2aa4.island.team023;

import org.json.JSONObject;

public abstract class Aircraft {

	protected int fuel;
	protected Heading heading;

	public Aircraft(String heading, int fuelCap) {
		this.heading = new Heading(heading);
		this.fuel = fuelCap;

	}

	public abstract JSONObject makeDecision();
	
	public abstract void update(JSONObject response);

	protected abstract JSONObject forward();

	protected abstract JSONObject turnLeft();

	protected abstract JSONObject turnRight();

}

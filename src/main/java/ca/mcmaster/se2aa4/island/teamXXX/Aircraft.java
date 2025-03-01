package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public abstract class Aircraft {

	protected int fuel;
	protected Headings heading;

	public Aircraft(String heading, int fuelCap) {
		this.heading = Headings.valueOf(heading);
		this.fuel = fuelCap;
	}

	public abstract JSONObject makeDecision();
	
	public abstract void update(JSONObject response);

	protected abstract void forward();

	protected abstract void turnLeft();

	protected abstract void turnRight();

}

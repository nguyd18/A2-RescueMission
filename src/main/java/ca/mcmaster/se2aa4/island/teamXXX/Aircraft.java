package ca.mcmaster.se2aa4.island.teamXXX;

public abstract class Aircraft {

	protected int fuel;
	protected Headings heading;

	public Aircraft(String heading, int fuelCap) {
		this.heading = Headings.valueOf(heading);
		this.fuel = fuelCap;
	}

	public abstract void makeDecision();

	protected abstract void forward();

	protected abstract void turnLeft();

	protected abstract void turnRight();

	public abstract void getResults();

}

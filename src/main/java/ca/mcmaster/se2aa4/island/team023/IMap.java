package ca.mcmaster.se2aa4.island.team023;

import org.json.JSONObject;

public interface IMap {

	public abstract void placeCell(int x, int y, JSONObject results);

	public abstract Cell getCell(int x, int y);

}

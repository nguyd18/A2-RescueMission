package ca.mcmaster.se2aa4.island.team023;

import org.json.JSONObject;

public interface IMap {

	public void placeCell(int x, int y, JSONObject results);

	public Cell getCell(int x, int y);

	public int getWidth();
	
	public int getHeight();

}

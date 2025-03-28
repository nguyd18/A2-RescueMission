package ca.mcmaster.se2aa4.island.team023;

import org.json.JSONObject;

public interface IMap {

	public void placeCell(int x, int y, int nextX, int nextY, JSONObject results);
	public Cell getCell(int x, int y);

	public String getClosestCreek();

	public int getWidth();
	public int getHeight();

	public int highestGroundCellOfColumn(int x);
	public int lowestGroundCellOfColumn(int x);

	public int getLeftEdge();
	public int getRightEdge();

}

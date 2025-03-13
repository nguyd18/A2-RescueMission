package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Map implements IMap {

	private Cell rootCell;

	public Map() {
		rootCell = new Cell(0, 0, null, null);
	}


	/**
	 * @see IMap#placeCell()
	 */
	public void placeCell(JSONObject results) {
		if (results == null) return;
		
	}


	/**
	 * @see IMap#getCell()
	 */
	public void getCell() {

	}

}

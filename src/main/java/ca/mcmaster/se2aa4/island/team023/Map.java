package ca.mcmaster.se2aa4.island.team023;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class Map implements IMap {

	private List<List<Cell>> grid = new ArrayList<>();

	private int h = 0;
	private int w = 0;

	private boolean isOcean;
	private boolean fromScan;
	private int distance;

	/**
	 * @see IMap#placeCell()
	 */
	public void placeCell(int x, int y, JSONObject results) {

		// expand map if too narrow
		while (x >= w) {
			for (List<Cell> row : grid) {
				row.add(null);
			}
			w++;
		}
		
		// expand map if too short
		while (y >= h) {
			ArrayList<Cell> row = new ArrayList<>();
			while (row.size() < w) row.add(null);
			grid.add(row);
			h++;
		}

		parseJSON(results);
		grid.get(y).set(x, new OceanCell(x, y, null));
		
	}

	private void parseJSON(JSONObject input) throws JSONException {

		isOcean = true;
		fromScan = false;

		JSONObject extras = input.getJSONObject("extras");



	}


	/**
	 * @see IMap#getCell()
	 */
	public Cell getCell(int x, int y) {

		// consider raising error instead
		if (y >= h || x >= w) return null;
		return grid.get(y).get(x);
	}

	public int getWidth() {
		return w;
	}
	
	public int getHeight() {
		return w;
	}

}

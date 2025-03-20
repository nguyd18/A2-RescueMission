package ca.mcmaster.se2aa4.island.team023;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

public class Map implements IMap {

	private List<List<Cell>> grid = new ArrayList<>();

	private int h = 0;
	private int w = 0;

	public Map() {
		
	}


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

		grid.get(y).set(x, new Cell(x, y, null, null));
		
	}


	/**
	 * @see IMap#getCell()
	 */
	public Cell getCell(int x, int y) {

		// consider raising error instead
		if (y >= h || x >= w) return null;
		return grid.get(y).get(x);
	}

}

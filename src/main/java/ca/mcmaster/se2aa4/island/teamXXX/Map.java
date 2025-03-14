package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

public class Map implements IMap {

	List<Cell> grid = new ArrayList<>();

	private Cell rootCell;

	public Map() {
		rootCell = new Cell(0, 0, null, null);
	}


	/**
	 * @see IMap#placeCell()
	 */
	public void placeCell(int x, int y, JSONObject results) {
		if (results == null) return;
		
	}


	/**
	 * @see IMap#getCell()
	 */
	public Cell getCell(int x, int y) {
		Cell curr = rootCell;

		while (curr != null) {
			if (curr.east != null && curr.location.x() < x) {
				curr = curr.east;
			} else if (curr.west != null && curr.location.x() > x) {
				curr = curr.west;
			}

			if (curr.south != null && curr.location.y() < y) {
				curr = curr.south;
			} else if (curr.north != null && curr.location.y() > y) {
				curr = curr.north;
			}
		}

		return curr;
	}

}

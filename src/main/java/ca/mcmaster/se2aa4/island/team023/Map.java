package ca.mcmaster.se2aa4.island.team023;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;

public class Map implements IMap {

	private List<List<Cell>> grid = new ArrayList<>();

	private int h = 0;
	private int w = 0;

	private boolean isOcean;
	private boolean fromEcho;
	private boolean radarGroundFound;
	private int distance = 0;

	private List<Point<Integer>> pois = new ArrayList<>();

	private String[] creeks;
	private String[] sites;

	/**
	 * @see IMap#placeCell()
	 */
	public void placeCell(int x, int y, int nextX, int nextY, JSONObject results) {
		
		// no need to place cell if no scan or echo used
		if (results.getJSONObject("extras").isEmpty()) return;

		parseJSON(results);
		if (fromEcho) {

			expandMap(x + distance * nextX, y + distance * nextY);

			// loop through all points from (x, y) to the end of the radar ping and mark as ocean
			int i = 1;
			while (i < Math.abs(distance * nextX) || i < Math.abs(distance * nextY)) {
				grid.get(y + i*nextY).set(x + i*nextX, new OceanCell(x + i*nextX, y + i*nextY));
				i++;
			}

			// if the pinged point is ground, mark it, otherwise ignore OOB
			if (radarGroundFound) grid.get(y + i*nextY).set(x + i*nextX, new GroundCell(x + i*nextX, y + i*nextY));
			
		} else {
			expandMap(x, y);
			if (isOcean && grid.get(y).get(x) == null) grid.get(y).set(x, new OceanCell(x, y));
			else {
				if (creeks.length > 0 || sites.length > 0) pois.add(new Point<Integer>(x, y));
				grid.get(y).set(x, new DetailedGroundCell(x, y, creeks, sites));
			}
		}
		
		
	}

	private void expandMap(int x, int y) {
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
	}

	private void parseJSON(JSONObject input) {

		isOcean = true;
		fromEcho = false;
		radarGroundFound = false;
		distance = 0;

		JSONObject extras;
		extras = input.getJSONObject("extras");

		// echo response case
		if (extras.has("range") && extras.has("found")) {
			fromEcho = true;
			distance = extras.getInt("range");

			radarGroundFound = extras.get("found").equals("GROUND");
			return;
		}

		// scan response case
		if (extras.has("biomes") && extras.has("creeks") && extras.has("sites")) {

			JSONArray contents = extras.getJSONArray("biomes");

			// check if ocean is the only listed biome => prioritize ground if it exists
			for (int i = 0; i < contents.length();i++) {
				isOcean = isOcean && contents.getString(i).equals("OCEAN");
			}

			// collect all creeks
			contents = extras.getJSONArray("creeks");
			creeks = new String[contents.length()];
			for (int i=0;i < creeks.length;i++) {
				creeks[i] = contents.getString(i);
			}

			// collect all sites
			contents = extras.getJSONArray("sites");
			sites = new String[contents.length()];
			for (int i=0;i < sites.length;i++) {
				sites[i] = contents.getString(i);
			}
			
		}

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
		return h;
	}

	/**
	 * Returns the y value of the ground cell found first in the column
	 * Note that highest in this case means northernmost; it is actually the lowest y-value of the column
	 * Returns -1 if no ground cell found
	 * 
	 * @param x the column to search
	 * @return the y value of the highest ground cell in column x
	 */
	public int highestGroundCellOfColumn(int x) {
		if (x >= w) return -1;

		for (int i=0;i < h;i++) {
			if (grid.get(i).get(x) != null && grid.get(i).get(x).isGround()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the y value of the ground cell found last in the column
	 * Note that lowest in this case means southernmost; it is actually the greatest y-value of the column
	 * Returns -1 if no ground cell found
	 * 
	 * @param x the column to search
	 * @return the y value of the lowest ground cell in column x
	 */
	public int lowestGroundCellOfColumn(int x) {
		if (x >= w) return -1;

		for (int i=h - 1;i >= 0;i--) {
			if (grid.get(i).get(x) != null && grid.get(i).get(x).isGround()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @return the lowest x-value of a ground cell in the map. returns -1 if no ground cells
	 */
	public int getLeftEdge() {
		for (int x=0;x < w;x++) {
			for (int y=0;y < h;y++) {
				if (grid.get(y).get(x) != null && grid.get(y).get(x).isGround()) {
					return x;
				}
			}
		}
		return -1;
	}

	/**
	 * 
	 * @return the greatest x-value of a ground cell in the map. returns -1 if no ground cells
	 */
	public int getRightEdge() {
		for (int x=w-1;x >= 0;x--) {
			for (int y=0;y < h;y++) {
				if (grid.get(y).get(x) != null && grid.get(y).get(x).isGround()) {
					return x;
				}
			}
		}
		return -1;
	}

	public String getClosestCreek() {
		String id = null;
		boolean siteFound = false;
		Point<Integer> siteLocation = null;

		for (Point<Integer> p : pois) {
			if (grid.get(p.y()).get(p.x()).getSites() != null && grid.get(p.y()).get(p.x()).getSites().size() > 0) {
				siteFound = true;
				siteLocation = p;
				break;
			}
		}

		// approximate to map centre if not found
		if (!siteFound) {
			siteLocation = new Point<Integer>(Math.round(getRightEdge() - getLeftEdge() / 2), Math.round(h/2));
		}

		double minDist = w*h;  // intialize to max possible distance
		for (Point<Integer> p : pois) {
			if (!p.equals(siteLocation) && Point.distBetweenPoints(p, siteLocation) < minDist) {
				minDist = Point.distBetweenPoints(p, siteLocation);
				if (grid.get(p.y()).get(p.x()).getCreeks().size() > 0) id = grid.get(p.y()).get(p.x()).getCreeks().get(0);
				
			}
		}

		return id;
	}

}
